package com.javanet.cmd.handler;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The handler used for converting data, passing the read data into the out
 */
public class TransactionHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(TransactionHandler.class);
    private boolean autoRead;
    private Channel out;

    //out channel， is auto read
    public TransactionHandler(Channel out, boolean autoRead) {
        this.out = out;
        this.autoRead = autoRead;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        boolean autoRelease = true;
        try {
            if (out.isActive()) {
                ChannelFuture f = out.writeAndFlush(msg);
                f.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                //hand read
                if (!autoRead) {
                    f.addListener(future -> {
                        if (future.isSuccess()) {
                            ctx.read();
                        } else {
                            log.error("handler write fail in:" + ctx + ",to:" + out, future.cause());
                            ctx.close();
                        }
                    });
                }
                autoRelease = false;
            }
        } finally {
            if (autoRelease)
                ReferenceCountUtil.release(msg);
        }
    }

    //如果输入的channel被关闭了，则手动关闭输出的管道
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (out.isActive()) {
            log.debug("[{}]closed， close other", ctx.name());
            out.flush().close();
        } else {
            log.debug("[{}]all closed，no handle", ctx.name());
        }
        super.channelInactive(ctx);
    }
}
