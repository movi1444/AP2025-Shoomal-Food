package com.aut.shoomal.service;

import com.aut.shoomal.dto.response.ApiResponse;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class LogoutService extends AbstractService
{
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