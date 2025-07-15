package com.aut.shoomal.service;

import com.aut.shoomal.dto.request.UpdateProfileRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.UserResponse;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class ProfileService extends AbstractService
{
    public CompletableFuture<UserResponse> getProfile(String token)
    {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("auth/profile", token)
                    .GET()
                    .build();
            return sendRequest(request, UserResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating get profile request: " + e.getMessage());
            throw new RuntimeException("Client error creating get profile request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> updateProfile(UpdateProfileRequest request, String token)
    {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("auth/profile", token)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating update profile request: " + e.getMessage());
            throw new RuntimeException("Client error creating update profile request: " + e.getMessage(), e);
        }
    }
}