package com.aut.shoomal.service;

import com.aut.shoomal.dto.request.UserLoginRequest;
import com.aut.shoomal.dto.request.UserRegisterRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.UserLoginResponse;
import com.aut.shoomal.dto.response.UserRegisterResponse;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class AuthService extends AbstractService
{
    public CompletableFuture<UserRegisterResponse> register(UserRegisterRequest request)
    {
        try {
            HttpRequest httpRequest = createRequestBuilder("auth/register")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, UserRegisterResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating register request: " + e.getMessage());
            throw new RuntimeException("Error creating register request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<UserLoginResponse> login(UserLoginRequest request)
    {
        try {
            HttpRequest httpRequest = createRequestBuilder("auth/login")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, UserLoginResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating login request: " + e.getMessage());
            throw new RuntimeException("Error creating login request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> logout(String token)
    {
        try {
            String emptyRequest = "{}";
            HttpRequest request = createAuthenticatedRequestBuilder("auth/logout", token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(emptyRequest)))
                    .build();
            return sendRequest(request, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating logout request: " + e.getMessage());
            throw new RuntimeException("Error creating logout request: " + e.getMessage(), e);
        }
    }
}