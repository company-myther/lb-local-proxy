/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.example.demo.socks;

import com.example.demo.enums.HostMatcherEnum;
import com.example.demo.matcher.HostMatcher;
import com.example.demo.pojo.RemoteServerInfo;
import com.example.demo.properties.ServerConfigProperties;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

/**
 * @author coder
 */
@ChannelHandler.Sharable
public final class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksMessage> {
    private static ServerConfigProperties serverConfigProperties;
    private static final Logger     logger = LoggerFactory.getLogger(SocksServerConnectHandler.class);

    private final Bootstrap b = new Bootstrap();

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final SocksMessage message) {
        if (message instanceof Socks4CommandRequest) {
            final Socks4CommandRequest request = (Socks4CommandRequest) message;
            Promise<Channel> promise = ctx.executor().newPromise();
            promise.addListener(
                    (FutureListener<Channel>) future -> {
                        final Channel outboundChannel = future.getNow();
                        if (future.isSuccess()) {
                            ChannelFuture responseFuture = ctx.channel().writeAndFlush(
                                    new DefaultSocks4CommandResponse(Socks4CommandStatus.SUCCESS));

                            responseFuture.addListener((ChannelFutureListener) channelFuture -> {
                                ctx.pipeline().remove(SocksServerConnectHandler.this);
                                outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));
                                ctx.pipeline().addLast(new RelayHandler(outboundChannel));
                            });
                        } else {
                            ctx.channel().writeAndFlush(
                                    new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED));
                            SocksServerUtils.closeOnFlush(ctx.channel());
                        }
                    });

            final Channel inboundChannel = ctx.channel();
            b.group(inboundChannel.eventLoop())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new DirectClientHandler(promise));


            b.connect(request.dstAddr(), request.dstPort()).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    // Close the connection if the connection attempt has failed.
                    ctx.channel().writeAndFlush(
                            new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED)
                    );
                    SocksServerUtils.closeOnFlush(ctx.channel());
                }
                // succeed: Connection established use handler provided results
            });
        } else if (message instanceof Socks5CommandRequest) {
            processSock5((Socks5CommandRequest) message, ctx);
        } else {
            ctx.close();
        }
    }

    private void processSock5(Socks5CommandRequest request, ChannelHandlerContext ctx) {
        Promise<Channel> promise = ctx.executor().newPromise();
        promise.addListener(
                (FutureListener<Channel>) future -> {
                    final Channel outboundChannel = future.getNow();
                    if (future.isSuccess()) {
                        ChannelFuture responseFuture =
                                ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                                        Socks5CommandStatus.SUCCESS,
                                        request.dstAddrType(),
                                        request.dstAddr(),
                                        request.dstPort()));

                        responseFuture.addListener((ChannelFutureListener) channelFuture -> {
                            ctx.pipeline().remove(SocksServerConnectHandler.this);
                            outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));
                            ctx.pipeline().addLast(new RelayHandler(outboundChannel));
                        });
                    } else {
                        ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                                Socks5CommandStatus.FAILURE, request.dstAddrType()));
                        SocksServerUtils.closeOnFlush(ctx.channel());
                    }
                });

        RemoteServerInfo matchServer = getMatchServer(request.dstAddr());
        logger.info("请求{}:{} 代理到 {}：{}", request.dstAddr(), request.dstPort(), matchServer.getHost(), matchServer.getPort());
        final Channel inboundChannel = ctx.channel();
        b.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addFirst(new Socks5ProxyHandler(new InetSocketAddress(matchServer.getHost(), matchServer.getPort())))
                                .addLast(new DirectClientHandler(promise));
                    }
                });
        b.connect(request.dstAddr(), request.dstPort()).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                // Close the connection if the connection attempt has failed.
                ctx.channel().writeAndFlush(
                        new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType()));
                SocksServerUtils.closeOnFlush(ctx.channel());
            }
            // succeed: Connection established use handler provided results
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        SocksServerUtils.closeOnFlush(ctx.channel());
    }

    private RemoteServerInfo getMatchServer(String targetHost) {
            for (RemoteServerInfo remoteServerInfo : serverConfigProperties.getRemoteServerInfoList()) {
                for (HostMatcherEnum hostMatcherEnum : remoteServerInfo.getHostMatcher()) {
                    HostMatcher hostMatcher = HostMatcher.map.get(hostMatcherEnum);
                    boolean match = hostMatcher != null &&
                            hostMatcher.match(targetHost, remoteServerInfo.getMatchData());
                    if (match) {
                        return remoteServerInfo;
                    }
                }
            }
        return serverConfigProperties.getDefaultRemoteServer();
    }

    public static void setServerConfigProperties(ServerConfigProperties properties) {
        serverConfigProperties = properties;
    }
}
