package com.javanet.handler;

import com.javanet.ServerManager;
import com.javanet.cmd.handler.ExceptionHandler;
import com.javanet.cmd.handler.PingPongHandler;
import com.javanet.cmd.message.Cmd;
import com.javanet.cmd.message.DataConnectionCmd;
import com.javanet.cmd.message.Ping;
import com.javanet.cmd.message.Pong;
import com.javanet.cmd.message.clientToServer.LoginCmd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServerCmdDecoder extends ReplayingDecoder<Void> {

    private static Logger log = LoggerFactory.getLogger(ServerCmdDecoder.class);

    //防止多次登录
    private boolean addHandler = false;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        byte flag = in.readByte();

        //新建数据连接
        if (flag == Cmd.dataConnectionCmd) {
            DataConnectionCmd cmd = DataConnectionCmd.decoderFrom(in);
            log.debug("获取客户端连接:{}", cmd);
            ctx.pipeline().remove(this);
            ServerManager.addConnection(cmd, ctx.channel());
            return;
        }

        //收到客户端pong,只有管理连接才能收到pong，数据连接不会发送Ping也就不会收到Pong
        if (flag == Cmd.pong) {
            out.add(Pong.decoderFrom(in));
            return;
        }
        //收到客户端的ping
        if (flag == Cmd.ping) {
            out.add(Ping.decoderFrom(in));
            return;
        }

        //登录
        if (flag == Cmd.ClientToServer.login) {
            LoginCmd login = LoginCmd.decoderFrom(in);
            if (!addHandler) {
                ctx.pipeline().addLast(new PingPongHandler());
                ctx.pipeline().addLast(new LoginCmdHandler());
                ctx.pipeline().addLast(ExceptionHandler.INSTANCE);
                addHandler = !addHandler;
            }
            out.add(login);
            return;
        }

        //无法识别的指令
        log.error("无法识别客户端:{} 发送的指令,指令:{}", ctx.channel().remoteAddress(), flag);
        in.skipBytes(actualReadableBytes());
        ctx.close();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        if ("Connection reset by peer".equals(cause.getMessage())) {
            log.error("Connection reset by peer local:{},remote:{}", channel.localAddress(), channel.remoteAddress());
            return;
        }
        log.error("ServerCmdDecoder:" + channel.localAddress() + " remote" + channel.remoteAddress(), cause);
    }
}
