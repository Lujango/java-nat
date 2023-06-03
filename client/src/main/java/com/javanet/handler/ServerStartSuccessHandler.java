package com.javanet.handler;

import com.javanet.cmd.message.serverToClient.ServerStartSuccessCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerStartSuccessHandler extends SimpleChannelInboundHandler<ServerStartSuccessCmd> {

    private Logger log = LoggerFactory.getLogger(ServerStartSuccessHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerStartSuccessCmd msg) {
        log.info(msg.toString());
    }

}