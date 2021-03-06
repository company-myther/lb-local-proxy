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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public final class SocksServer implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(SocksServer.class);

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        new ThreadPoolExecutor(
                1,
                1,
                10,
                TimeUnit.HOURS,
                new LinkedBlockingQueue<>(1),
                new DefaultThreadFactory("socks-server")
        ).submit(() -> {
            try {
                startServer();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private void startServer() throws JsonProcessingException {
        ServerConfigProperties serverConfigProperties = ServerConfigProperties.getInstance();
        logger.info("Start the server, the configuration information is: \n{}\n---", new YAMLMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(serverConfigProperties));

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SocksServerInitializer());
            if (serverConfigProperties.getBindHost() != null) {
                b.bind(serverConfigProperties.getBindHost(), serverConfigProperties.getPort()).sync().channel().closeFuture().sync();
            } else {
                b.bind(serverConfigProperties.getPort()).sync().channel().closeFuture().sync();
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
