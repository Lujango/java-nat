package com.javanet.handler;

import com.javanet.cmd.message.serverToClient.ServerStartFailCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerStartFailHandler  extends SimpleChannelInboundHandler<ServerStartFailCmd> {

    private Logger log = LoggerFactory.getLogger(ServerStartFailHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerStartFailCmd msg) {
        log.error(msg.toString());
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }

}