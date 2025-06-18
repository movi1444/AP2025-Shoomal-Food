package com.aut.shoomal.dto.handler;

import com.aut.shoomal.auth.LogoutManager;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;

public class LogoutHandler extends AbstractHttpHandler
{
    private final LogoutManager logoutManager;
    public LogoutHandler(LogoutManager logoutManager)
    {
        this.logoutManager = logoutManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        if (!checkHttpMethod(exchange, "POST"))
            return;
        if (!checkContentType(exchange))
            return;

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer "))
        {
            sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, new ApiResponse(false, "401 Unauthorized: No token provided."));
            return;
        }
        String token = auth.substring(7);

        try {
            logoutManager.blacklistToken(token);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 User logged out successfully"));
        } catch (InvalidInputException e) {
            System.err.println("Logout failed: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Logout failed: " + e.getMessage()));
        } catch (ConflictException e) {
            System.err.println("Logout conflict: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_CONFLICT, new ApiResponse(false, e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("500 Internal server error during logout: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal server error during logout."));
        }
    }
}