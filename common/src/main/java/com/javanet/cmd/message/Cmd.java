package com.javanet.cmd.message;

import io.netty.buffer.ByteBuf;

public  interface Cmd {

    byte dataConnectionCmd = 0;
    byte ping = 4;
    byte pong = 6;


    interface ServerToClient {
        byte serverStartSuccessCmd = 2;
        byte serverStartFailCmd = 3;
        byte loginFail = 7;
    }

    interface ClientToServer {
        byte login = 5;
    }


    /*
     * 将当前对象序列化到Bytebuf中
     * */
    void encoderTo(ByteBuf buf);

}
