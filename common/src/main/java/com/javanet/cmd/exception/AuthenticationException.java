package com.javanet.cmd.exception;


/**
 * auth exception
 */
public class AuthenticationException extends RuntimeException {

    public static AuthenticationException INSTANCE = new AuthenticationException();

    public AuthenticationException() {
        super("password no ok");
    }
}
