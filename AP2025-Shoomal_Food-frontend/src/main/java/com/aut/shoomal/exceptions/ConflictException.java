package com.aut.shoomal.exceptions;

public class ConflictException extends FrontendServiceException
{
    public ConflictException(String backendMessage, String clientMessage)
    {
        super(409, backendMessage, clientMessage);
    }
}