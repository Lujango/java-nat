package com.javanet.cmd.message;

import io.netty.buffer.ByteBuf;

public class Ping implements Cmd {

    private static Ping instance = new Ping();

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(Cmd.ping);
    }

    public static Ping decoderFrom(ByteBuf in) {
        return instance;
    }

}
