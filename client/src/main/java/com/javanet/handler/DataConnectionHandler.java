package com.javanet.handler;

import com.javanet.AppClient;
import com.javanet.Config;
import com.javanet.DataConnectionClient;
import com.javanet.cmd.message.DataConnectionCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataConnectionHandler extends SimpleChannelInboundHandler<DataConnectionCmd> {

    private Logger log = LoggerFactory.getLogger(DataConnectionHandler.class);

    /*
     * It should be noted that this ctx belongs to the managerChannel and cannot be closed even if the activation fails
     * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataConnectionCmd msg) {
        Attribute<Config> conf = ctx.channel().attr(AppClient.key);
        Config config = conf.get();
        log.debug("Received from the server DataConnection,{} -> {}:{}", msg.getServerPort(), msg.getLocalHost(), msg.getLocalPort());
        new DataConnectionClient(config, msg).start();
    }

}
