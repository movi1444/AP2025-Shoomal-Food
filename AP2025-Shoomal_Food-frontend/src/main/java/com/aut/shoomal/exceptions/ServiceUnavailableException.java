package com.aut.shoomal.exceptions;

public class ServiceUnavailableException extends FrontendServiceException
{
    public ServiceUnavailableException(String backendMessage, String clientMessage)
    {
        super(500, backendMessage, clientMessage);
    }
}