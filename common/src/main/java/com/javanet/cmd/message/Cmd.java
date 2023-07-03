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
     * Serialize the current object into Bytebuf
     * */
    void encoderTo(ByteBuf buf);

}
