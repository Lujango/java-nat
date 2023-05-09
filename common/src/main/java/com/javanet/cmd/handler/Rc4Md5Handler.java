package com.javanet.cmd.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.ReferenceCountUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.List;


import static com.javanet.cmd.util.CodeUtil.md5;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Rc4Md5Handler extends ByteToMessageCodec<ByteBuf> {

    private final static SecureRandom random = new SecureRandom();
    private boolean firstDecode = true;
    private boolean firstEncode = true;
    private Cipher decoderCipher;
    private Cipher encoderCipher;

    private String password;

    public Rc4Md5Handler(String password) {
        this.password = password;
    }

    /*
     * When writing data for the first time, generate a random IV to construct a cipher
     *Using Cipher to encrypt data
     * */
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        if (firstEncode) {
            byte[] iv = randomIv();
            encoderCipher = Cipher.getInstance("RC4");
            encoderCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(md5(md5(password.getBytes(UTF_8)), iv), "RC4"));
            firstEncode = false;
            out.writeBytes(iv);
        }
        if (!msg.isReadable()) {
            return;
        }

        //Clear text length that requires encryption
        int dataLength = msg.readableBytes();
        int outputSize = encoderCipher.getOutputSize(dataLength);

        //Allocate and store the buff of ciphertext
        ByteBuf outPut = ctx.alloc().ioBuffer(outputSize);
        try {
            int updateLength = encoderCipher.update(msg.nioBuffer(), outPut.nioBuffer(0, outputSize));
            //Ignore processed data
            msg.skipBytes(updateLength);
            outPut.writerIndex(updateLength);
            out.writeBytes(outPut);
        } finally {
            ReferenceCountUtil.release(outPut);
        }
    }


    /*
     * When reading for the first time, the first 16 bits are used as iv to build a cipher, and there is no need to build it for future use
     * */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (firstDecode) {
            if (in.readableBytes() < 16) {
                return;
            }
            byte[] iv = readByte(in, 16);
            decoderCipher = Cipher.getInstance("RC4");
            byte[] realPassWord = md5(md5(password.getBytes(UTF_8)), iv);
            decoderCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(realPassWord, "RC4"));
            firstDecode = false;
        }
        if (!in.isReadable()) {
            return;
        }
        //Ciphertext length and plaintext length
        int dataLength = in.readableBytes();
        int outputSize = decoderCipher.getOutputSize(dataLength);
        //Create a buf and parse plaintext to this buf
        ByteBuf outPut = in.alloc().ioBuffer(outputSize);
        try {
            int updateLength = decoderCipher.update(in.nioBuffer(), outPut.nioBuffer(0, outputSize));
            in.skipBytes(updateLength);
            outPut.writerIndex(updateLength);
            //Add an index here because it will be released once in finally
            outPut.retain();
            out.add(outPut);
        } finally {
            ReferenceCountUtil.release(outPut);
        }
    }


    private byte[] readByte(ByteBuf in, int length) {
        byte[] bytes = ByteBufUtil.getBytes(in, in.readerIndex(), length);
        in.skipBytes(bytes.length);
        return bytes;
    }

    private byte[] randomIv() {
        return random.generateSeed(16);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (encoderCipher != null) {
            encoderCipher.doFinal();
        }
        if (decoderCipher != null) {
            decoderCipher.doFinal();
        }
        super.channelInactive(ctx);
    }
}
