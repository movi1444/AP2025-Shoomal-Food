package com.aut.shoomal.exceptions;

public class UnauthorizedException extends RuntimeException
{
    public UnauthorizedException(String message) {
        super(message);
    }
}
