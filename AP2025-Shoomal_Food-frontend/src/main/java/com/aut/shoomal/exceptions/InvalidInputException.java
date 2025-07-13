package com.aut.shoomal.exceptions;

public class InvalidInputException extends FrontendServiceException
{
    public InvalidInputException(String backendMessage, String clientMessage)
    {
        super(400, backendMessage, clientMessage);
    }
}