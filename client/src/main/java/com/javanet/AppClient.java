package com.javanet;

import com.javanet.cmd.handler.CmdEncoder;
import com.javanet.cmd.handler.ExceptionHandler;
import com.javanet.cmd.handler.PingPongHandler;
import com.javanet.cmd.handler.Rc4Md5Handler;
import com.javanet.handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * client start
 */
public class AppClient {

    private static Logger log = LoggerFactory.getLogger(AppClient.class);
    public static EventLoopGroup work = new NioEventLoopGroup(1);
    public static AttributeKey<Config> key = AttributeKey.newInstance("config");

    private Config config = Config.INSTANCE;

    public static void main(String[] args) {
        AppClient appClient = new AppClient();
        appClient.start();
    }

    private void start() {
        createManagerConnection()
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("Successfully connected {}:{} Waiting for server validation ", config.getServerIp(), config.getServerPort());
                        //断开连接10s后会重试
                        future.channel().closeFuture().addListener(f -> {
                            log.info("Detected disconnection for 10 and reconnected ", f.cause());
                            work.schedule(this::start, 10, TimeUnit.SECONDS);
                        });
                    } else {
                        log.error("Connection failed, try again in 5 seconds :{}", future.cause().toString());
                        work.schedule(this::start, 5, TimeUnit.SECONDS);
                    }
                });
    }


    private ChannelFuture createManagerConnection() {
        Bootstrap b = new Bootstrap();
        return b.group(work)
                .channel(NioSocketChannel.class)
                .remoteAddress(config.getServerIp(), config.getServerPort())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000)
                .option(ChannelOption.TCP_NODELAY, true)
                // config save to channel
                .attr(key, config)
                .handler(new ChannelInitializer<Channel>() {
                             @Override
                             protected void initChannel(Channel ch) {
                                 ch.pipeline().addLast(new Rc4Md5Handler(config.getToken()));
                                 ch.pipeline().addLast(new CmdEncoder());

                                 ch.pipeline().addLast(new CmdDecoder());
                                 ch.pipeline().addLast(new PingPongHandler());
                                 ch.pipeline().addLast(new LoginHandler());
                                 ch.pipeline().addLast(new DataConnectionHandler());
                                 ch.pipeline().addLast(new ServerStartSuccessHandler());
                                 ch.pipeline().addLast(new ServerStartFailHandler());
                                 ch.pipeline().addLast(new LoginFailHandler());
                                 ch.pipeline().addLast(ExceptionHandler.INSTANCE);
                             }
                         }
                )
                .connect();
    }

}
