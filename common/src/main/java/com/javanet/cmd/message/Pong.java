package com.javanet.cmd.message;

import io.netty.buffer.ByteBuf;

public class Pong implements Cmd {

    private static Pong instance = new Pong();

    @Override
    public void encoderTo(ByteBuf buf) {
        buf.writeByte(Cmd.pong);
    }

    public static Pong decoderFrom(ByteBuf in) {
        return instance;
    }

}