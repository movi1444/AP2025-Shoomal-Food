package com.aut.shoomal.exceptions;

public class ForbiddenException extends FrontendServiceException
{
    public ForbiddenException(String backendMessage, String clientMessage)
    {
        super(401, backendMessage, clientMessage);
    }
}
