package com.github.longkerdandy.evo.adapter.evo.tcp;

import com.github.longkerdandy.evo.adapter.evo.storage.EvoRedisStorage;
import com.github.longkerdandy.evo.api.mq.Publisher;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * TCP Client
 * Connect to the Evolution platform
 */
public class TcpClient implements Runnable {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);

    private final String host;
    private final int port;
    private final EvoRedisStorage storage;
    private final Publisher publisher;
    private volatile TcpClientHandler handler; // make it thread safe

    public TcpClient(String host, int port, EvoRedisStorage storage, Publisher publisher) {
        this.host = host;
        this.port = port;
        this.storage = storage;
        this.publisher = publisher;
    }

    public TcpClientHandler getHandler() {
        return handler;
    }

    @Override
    public void run() {
        // re-used event loop
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);

        // enter the connect loop
        connect(workerGroup);
    }

    /**
     * Client connect loop
     * Reconnect if connect failed
     *
     * @param eventLoop EventLoopGroup
     */
    public void connect(EventLoopGroup eventLoop) {
        // create new handler
        TcpClientHandler h = new TcpClientHandler(this.storage, publisher, this);
        this.handler = h;
        // create new bootstrap
        Bootstrap b = new Bootstrap();
        b.group(eventLoop);
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
                p.addLast(h);
            }
        });

        // start the client
        b.connect(this.host, this.port).addListener((ChannelFuture f) -> {
            // reconnect if failed
            if (f.isSuccess()) {
                logger.debug("Connect to Evolution platform succeeded");
            } else {
                logger.debug("Connect to Evolution platform failed, reconnect after 15 seconds");
                f.channel().eventLoop().schedule(() -> connect(f.channel().eventLoop()), 15, TimeUnit.SECONDS);
            }
        });
    }
}
