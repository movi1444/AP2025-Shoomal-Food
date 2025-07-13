package com.aut.shoomal.exceptions;

public class FrontendServiceException extends RuntimeException
{
    private final int code;
    private final String backendMessage;
    public FrontendServiceException(int statusCode, String backendMessage, String clientMessage)
    {
        super(clientMessage);
        this.code = statusCode;
        this.backendMessage = backendMessage;
    }

    public int getCode()
    {
        return code;
    }

    public String getBackendMessage()
    {
        return backendMessage;
    }
}