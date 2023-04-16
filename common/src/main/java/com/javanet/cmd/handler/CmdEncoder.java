package com.javanet.cmd.handler;

import com.javanet.cmd.message.Cmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * cmd encoder
 */
public class CmdEncoder extends MessageToByteEncoder<Cmd> {

    //输出
    @Override
    protected void encode(ChannelHandlerContext ctx, Cmd msg, ByteBuf out) {
        msg.encoderTo(out);
    }


}
