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
package com.coder.lb.local.proxy.socks;

import com.coder.lb.local.proxy.properties.ServerConfigProperties;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@ChannelHandler.Sharable
public final class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage> {

    public static final SocksServerHandler INSTANCE = new SocksServerHandler();
    private final AtomicBoolean enablePwdAuth = new AtomicBoolean(false);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, SocksMessage socksRequest) throws Exception {
        // just support socks5
        if (socksRequest.version() == SocksVersion.SOCKS5) {
            if (socksRequest instanceof Socks5InitialRequest) {
                ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
                ServerConfigProperties instance = ServerConfigProperties.init();
                enablePwdAuth.set(instance.getUsername() != null || instance.getPassword() != null);
                ctx.write(new DefaultSocks5InitialResponse(
                        enablePwdAuth.get() ? Socks5AuthMethod.PASSWORD : Socks5AuthMethod.NO_AUTH
                ));
            } else if (socksRequest instanceof Socks5PasswordAuthRequest) {
                ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
                ServerConfigProperties instance = ServerConfigProperties.init();

                if (enablePwdAuth.get()) {
                    boolean authResult = Objects.equals(instance.getUsername(), ((Socks5PasswordAuthRequest) socksRequest).username())
                            && Objects.equals(instance.getPassword(), ((Socks5PasswordAuthRequest) socksRequest).password());
                    ctx.write(new DefaultSocks5PasswordAuthResponse(
                            authResult ? Socks5PasswordAuthStatus.SUCCESS : Socks5PasswordAuthStatus.FAILURE
                    ));
                } else {
                    ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
                }
            } else if (socksRequest instanceof Socks5CommandRequest) {
                Socks5CommandRequest socks5CmdRequest = (Socks5CommandRequest) socksRequest;
                if (socks5CmdRequest.type() == Socks5CommandType.CONNECT) {
                    ctx.pipeline().addLast(new ProxyConnectHandler());
                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(socksRequest);
                } else {
                    ctx.close();
                }
            } else {
                ctx.close();
            }
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        throwable.printStackTrace();
        SocksServerUtils.closeOnFlush(ctx.channel());
    }
}
