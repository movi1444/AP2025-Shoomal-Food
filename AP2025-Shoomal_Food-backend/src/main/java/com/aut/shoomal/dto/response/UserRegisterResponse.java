package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRegisterResponse
{
    private String message, token;
    @JsonProperty("user_id")
    private String userId;
    public UserRegisterResponse() {}
    public UserRegisterResponse(String message, String token, Long userId)
    {
        this.message = message;
        this.token = token;
        this.userId = (userId == null) ? null : String.valueOf(userId);
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

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }
}