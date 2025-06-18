package com.aut.shoomal.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse
{
    private boolean success;
    private String message, error;
    private Object data;
    public ApiResponse(boolean success, String message)
    {
        this.success = success;
        if (success)
            this.message = message;
        else
            this.error = message;
    }

    public ApiResponse(boolean success, String message, Object data)
    {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }
}