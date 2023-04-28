package com.javanet.cmd.handler;

import com.javanet.cmd.message.Ping;
import com.javanet.cmd.message.Pong;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingPongHandler extends IdleStateHandler {

    private static Logger log = LoggerFactory.getLogger(PingPongHandler.class);
    private static int writeTimeOut = 10;
    private static int readTimeout = writeTimeOut * 4 + 2;

    public PingPongHandler() {
        super(readTimeout, writeTimeOut, 0);
    }

    /**
     * Add multiple handlers here to handle ping and pong messages
     *
     * *For pong messages, discard them directly
     *
     * *For ping messages, a response to pong is required
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        ctx.pipeline().addAfter(ctx.name(), null, new SimpleChannelInboundHandler<Pong>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Pong msg) {
                log.debug("receive Pong");
            }
        });
        ctx.pipeline().addAfter(ctx.name(), null, new SimpleChannelInboundHandler<Ping>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Ping msg) {
                log.debug("receive Ping");
                ctx.writeAndFlush(new Pong()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        });
    }

    /*
     * If there is no write for a long time (write timeout), send Ping
        Disconnect if not read for a long time (read timeout)
     * */
    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        if (evt.state() == IdleState.WRITER_IDLE) {
            log.debug("send Ping");
            ctx.writeAndFlush(new Ping()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            log.info("Read timeout disconnect ：{}", ctx.channel().remoteAddress());
            ctx.flush().close();
        }
    }

}