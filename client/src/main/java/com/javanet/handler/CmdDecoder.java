package com.javanet.handler;

import com.javanet.cmd.message.Cmd;
import com.javanet.cmd.message.DataConnectionCmd;
import com.javanet.cmd.message.Ping;
import com.javanet.cmd.message.Pong;
import com.javanet.cmd.message.serverToClient.LoginFailCmd;
import com.javanet.cmd.message.serverToClient.ServerStartFailCmd;
import com.javanet.cmd.message.serverToClient.ServerStartSuccessCmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CmdDecoder extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(CmdDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        byte flag = in.readByte();
        if (flag == Cmd.ping) {
            out.add(Ping.decoderFrom(in));
            return;
        }
        if (flag == Cmd.pong) {
            out.add(Pong.decoderFrom(in));
            return;
        }
        if (flag == Cmd.dataConnectionCmd) {
            out.add(DataConnectionCmd.decoderFrom(in));
            return;
        }
        if (flag == Cmd.ServerToClient.serverStartSuccessCmd) {
            out.add(ServerStartSuccessCmd.decoderFrom(in));
            return;
        }
        if (flag == Cmd.ServerToClient.serverStartFailCmd) {
            out.add(ServerStartFailCmd.decoderFrom(in));
            return;
        }
        if (flag == Cmd.ServerToClient.loginFail) {
            out.add(LoginFailCmd.decoderFrom(in));
            return;
        }
        log.error("Unable to recognize the cmd sent by the server, cmd :{}", flag);
        in.skipBytes(actualReadableBytes());
        ctx.close();
    }


}