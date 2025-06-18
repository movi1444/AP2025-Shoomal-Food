package com.aut.shoomal.dto.response;

public class UserLoginResponse
{
    private String message, token;
    private UserResponse user;
    public UserLoginResponse() {}
    public UserLoginResponse(String message, String token, UserResponse user)
    {
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public UserResponse getUser()
    {
        return user;
    }

    public void setUser(UserResponse user)
    {
        this.user = user;
    }
}