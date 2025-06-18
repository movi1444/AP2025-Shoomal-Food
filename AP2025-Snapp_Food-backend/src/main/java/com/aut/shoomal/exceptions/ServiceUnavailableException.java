package com.aut.shoomal.exceptions;

public class ServiceUnavailableException extends RuntimeException
{
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
