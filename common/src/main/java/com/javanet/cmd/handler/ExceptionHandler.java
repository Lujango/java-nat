package com.javanet.cmd.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExceptionHandler
 */
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(ExceptionHandler.class);
    public static ExceptionHandler INSTANCE = new ExceptionHandler();

    public static final String NAME = "ExceptionHandler";

    private ExceptionHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        if ("Connection reset by peer".equals(cause.getMessage())) {
            log.error("end:Connection reset by peer local:{},remote:{}", channel.localAddress(), channel.remoteAddress());
            return;
        }
        log.error("end local:" + channel.localAddress() + " remote" + channel.remoteAddress(), cause);
        ctx.close();
    }
}
