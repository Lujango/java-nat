package com.javanet;

import com.javanet.cmd.handler.*;
import com.javanet.cmd.listener.ErrorLogListener;
import com.javanet.cmd.message.DataConnectionCmd;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataConnectionClient {
    private static Logger log = LoggerFactory.getLogger(DataConnectionClient.class);
    private static EventLoopGroup work = AppClient.work;
    private Config config;
    private DataConnectionCmd msg;

    public DataConnectionClient(Config config, DataConnectionCmd msg) {
        this.config = config;
        this.msg = msg;
    }

    /*
     * When connecting to the server through data connection,
     * if it is not indicated that it is a data connection,
     * the server will not actively send data,
     * so it is changed to automatic reading to improve performance
     * */
    public void start() {
        //After successfully connecting to the Local port, try connecting to the server again
        createConnectionToLocal(msg.getLocalHost(), msg.getLocalPort()).addListener((ChannelFutureListener) localFuture -> {
            if (!localFuture.isSuccess()) {
                log.error("Client connect Local fail", localFuture.cause());
                return;
            }
            final Channel channelToLocal = localFuture.channel();

            createConnectionToServer(config.getServerIp(), config.getServerPort()).addListener((ChannelFutureListener) serverFuture -> {
                if (serverFuture.isSuccess()) {
                    Channel channelToServer = serverFuture.channel();
                    channelToServer.pipeline().addBefore(ExceptionHandler.NAME, "linkServer", new TransactionHandler(channelToLocal, true));
                    channelToLocal.pipeline().addBefore(ExceptionHandler.NAME, "linkLocal", new TransactionHandler(channelToServer, true));
                    //Send authentication message and remove Rc4Md5handler as encryption is not required during the data transmission phase
                    channelToServer.writeAndFlush(msg)
                            .addListener(ErrorLogListener.INSTANCE)
                            .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    log.error("Client connect Remote fail", serverFuture.cause());
                    channelToLocal.close();
                }
            });
        });
    }

    //Create a connection to a local port
    private ChannelFuture createConnectionToLocal(String host, int port) {
        return newClientBootStrap(work)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 180));
                        ch.pipeline().addLast(ExceptionHandler.NAME, ExceptionHandler.INSTANCE);
                    }
                })
                .connect(host, port);
    }

    //Create a connection to the server port
    //Client connections to the server do not require timeout management,
    // as the server will proactively disconnect based on timeout judgment
    private ChannelFuture createConnectionToServer(String host, int port) {
        return newClientBootStrap(work)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new Rc4Md5Handler(config.getToken()));
                        ch.pipeline().addLast(new CmdEncoder());

                        ch.pipeline().addLast(ExceptionHandler.NAME, ExceptionHandler.INSTANCE);
                    }
                })
                .connect(host, port);
    }

    private static Bootstrap newClientBootStrap(EventLoopGroup group) {
        return new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.TCP_NODELAY, true);
    }

}
