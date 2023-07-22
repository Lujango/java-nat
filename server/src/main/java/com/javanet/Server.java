package com.javanet;

import com.javanet.cmd.handler.ExceptionHandler;
import com.javanet.cmd.handler.TimeOutHandler;
import com.javanet.cmd.handler.TransactionHandler;
import com.javanet.conf.ServerWorker;
import com.javanet.handler.FlowManagerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * listen port start server
 * no autoRead
 */
public class Server {

    private static Logger log = LoggerFactory.getLogger(Server.class);
    private EventLoopGroup boss = AppServer.boss;
    private EventLoopGroup work = AppServer.work;

    private ChannelFuture bind;

    private ServerWorker serverWorker;
    private Channel managerChannel;
    private FlowManagerHandler flowManagerHandler;

    public Server(ServerWorker serverWorker, Channel managerChannel) {
        this.serverWorker = serverWorker;
        this.managerChannel = managerChannel;
        flowManagerHandler =
                new FlowManagerHandler("server on " + serverWorker.getServerPort(), "发送到使用者的", "从使用者接收的");
    }


    public void start(Promise<Server> promise) {
        ServerBootstrap sb = new ServerBootstrap();
        bind = sb.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024)
                .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new TimeOutHandler(0, 0, 180));
                        ch.pipeline().addLast(flowManagerHandler);
                        ch.pipeline().addLast(new ServerHandler(Server.this));
                        ch.pipeline().addLast(ExceptionHandler.INSTANCE);
                    }
                }).bind(Config.bindHost, serverWorker.getServerPort());

        //添加服务开启成功失败事件
        bind.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                promise.setSuccess(this);
            } else {
                promise.setFailure(future.cause());
            }
        });
    }

    public void stop() {
        if (managerChannel != null && managerChannel.isActive()) {
            managerChannel.close();
        }
        if (bind != null && bind.channel().isActive()) {
            bind.channel().close().addListener(f -> log.info("Server on port:{} closed successful", serverWorker.getServerPort()));
        }
    }

    public ServerWorker getServerWorker() {
        return serverWorker;
    }

    public Channel getManagerChannel() {
        return managerChannel;
    }

    //When an external network user connects to the listening port,
    // they will open a connection with the client and transmit data
    private static class ServerHandler extends ChannelInboundHandlerAdapter {

        private Server server;

        ServerHandler(Server server) {
            this.server = server;
        }

        @Override
        public void channelActive(ChannelHandlerContext userConnection) {
            Promise<Channel> promise = ServerManager.getConnection(server);
            promise.addListener((GenericFutureListener<Future<Channel>>) future -> {
                if (future.isSuccess()) {
                    Channel clientChannel = future.getNow();
                    clientChannel.pipeline().addLast("linkClient", new TransactionHandler(userConnection.channel(), true));
                    clientChannel.pipeline().addLast(ExceptionHandler.INSTANCE);
                    userConnection.pipeline().replace(this, "linkUser", new TransactionHandler(clientChannel, true));
                    userConnection.channel().config().setAutoRead(true);
                } else {
                    //promise失败可能是：超时没结果被定时任务取消的
                    log.error("The server failed to obtain a connection to the client, closing the userConnection" , future.cause());
                    userConnection.close();
                }
            });
        }
    }
}
