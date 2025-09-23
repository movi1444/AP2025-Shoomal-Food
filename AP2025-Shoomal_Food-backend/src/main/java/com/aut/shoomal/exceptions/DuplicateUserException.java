package com.aut.shoomal.exceptions;

public class DuplicateUserException extends ConflictException
{
    public DuplicateUserException(String message)
    {
        super(message);
    }
}