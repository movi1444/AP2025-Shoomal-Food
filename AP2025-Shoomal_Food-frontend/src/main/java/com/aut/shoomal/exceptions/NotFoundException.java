package com.aut.shoomal.exceptions;

public class NotFoundException extends FrontendServiceException
{
    public NotFoundException(String backendMessage, String clientMessage)
    {
        super(404, backendMessage, clientMessage);
    }
}