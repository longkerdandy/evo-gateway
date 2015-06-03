package com.github.longkerdandy.evo.adapter.evo.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP Client
 * Connect to the Evolution platform
 */
public class TcpClient implements Runnable {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);

    private final String host;
    private final int port;
    private final TcpClientHandler handler;

    public TcpClient(String host, int port, TcpClientHandler handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void run() {
        // configure the client
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    // encoder/decoder
                    p.addLast(new Encoder());
                    p.addLast(new Decoder());
                    // handler
                    p.addLast(handler);
                }
            });

            // start the client
            ChannelFuture f = b.connect(this.host, this.port).sync();

            // wait until the connection is closed
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(ExceptionUtils.getMessage(e));
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
