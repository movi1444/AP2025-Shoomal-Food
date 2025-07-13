package com.aut.shoomal.exceptions;

public class UnauthorizedException extends FrontendServiceException
{
    public UnauthorizedException(String backendMessage, String clientMessage)
    {
        super(401, backendMessage, clientMessage);
    }
}