package com.javanet.handler;

import com.javanet.AppClient;
import com.javanet.Config;
import com.javanet.cmd.message.clientToServer.LoginCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LoginHandler  extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Config config = ctx.channel().attr(AppClient.key).get();
        ctx.writeAndFlush(new LoginCmd(config.getClientName()));
        super.channelActive(ctx);
    }
}
