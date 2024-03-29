package com.javanet.cmd.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorLogListener implements ChannelFutureListener {

    private static Logger log = LoggerFactory.getLogger(ErrorLogListener.class);

    public static ErrorLogListener INSTANCE = new ErrorLogListener();

    @Override
    public void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
            log.error("", future.cause());
        }
    }
}
