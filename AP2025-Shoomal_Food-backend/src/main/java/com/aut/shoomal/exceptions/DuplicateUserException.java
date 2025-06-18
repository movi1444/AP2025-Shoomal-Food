package com.aut.shoomal.exceptions;

public class DuplicateUserException extends RuntimeException
{
    public DuplicateUserException(String message)
    {
        super(message);
    }
}