package com.github.longkerdandy.evo.gateway;

import com.github.longkerdandy.evo.gateway.handler.BusinessHandler;
import com.github.longkerdandy.evo.gateway.repo.ChannelRepository;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * TCP Server
 */
public class TCPServer {

    private static final String HOST = "0.0.0.0";
    private static final int PORT = 1883;
    private static final int THREADS = Runtime.getRuntime().availableProcessors() * 2;

    public static void main(String[] args) throws Exception {
        // channel(connection) repository
        ChannelRepository channelRepository = new ChannelRepository();
        // configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(THREADS);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            // mqtt encoder & decoder
                            p.addLast(new MqttEncoder());
                            p.addLast(new MqttDecoder());
                            // business handler, in separate ExecutorGroup
                            p.addLast(new DefaultEventExecutorGroup(THREADS),
                                    new BusinessHandler(channelRepository));
                        }
                    });

            // start the server.
            ChannelFuture f = b.bind(HOST, PORT).sync();

            // wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
