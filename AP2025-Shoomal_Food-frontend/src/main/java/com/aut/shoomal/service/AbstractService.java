package com.aut.shoomal.service;

import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractService
{
    protected final String BASE_URL = "http://localhost:8080/";
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;
    public AbstractService()
    {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
    }

    protected HttpRequest.Builder createRequestBuilder(String endpoint)
    {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");
    }

    protected HttpRequest.Builder createAuthenticatedRequestBuilder(String endpoint, String token)
    {
        return createRequestBuilder(endpoint)
                .header("Authorization", "Bearer " + token);
    }

    protected <T> CompletableFuture<T> sendRequest(HttpRequest request, Class<T> response)
    {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> {
                    String responseBody = httpResponse.body();
                    int code = httpResponse.statusCode();
                    System.out.println("HTTP Response Status: " + code + ", Body: " + responseBody);

                    if (code >= 200 && code < 300)
                    {
                        try {
                            return objectMapper.readValue(responseBody, response);
                        } catch (Exception e) {
                            System.err.println("Error deserializing successful response to " + response.getSimpleName() + ": " + e.getMessage());
                            throw new FrontendServiceException(code, "Failed to parse successful server response.", "Failed to process server data.");
                        }
                    }
                    else
                    {
                        String backendErrorMessage = "An unknown error occurred on the server.";
                        try {
                            ApiResponse apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
                            if (apiResponse != null) {
                                if (apiResponse.getError() != null)
                                    backendErrorMessage = apiResponse.getError();
                                else if (apiResponse.getMessage() != null)
                                    backendErrorMessage = apiResponse.getMessage();
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing error response body into ApiResponse: " + e.getMessage());
                            backendErrorMessage = "Server returned unreadable error: " + responseBody;
                        }

                        switch (code)
                        {
                            case 400:
                                throw new InvalidInputException(backendErrorMessage, "Invalid input provided. Please check your data.");
                            case 401:
                                throw new UnauthorizedException(backendErrorMessage, "Authentication failed. Please log in again.");
                            case 403:
                                throw new ForbiddenException(backendErrorMessage, "Permission denied.");
                            case 404:
                                throw new NotFoundException(backendErrorMessage, "Resource not found.");
                            case 409:
                                throw new ConflictException(backendErrorMessage, "Conflict occurred. The request could not be completed due to a conflict with the current state of the resource.");
                            case 500:
                                throw new ServiceUnavailableException(backendErrorMessage, "Service is temporarily unavailable. Please try again later.");
                            default:
                                throw new FrontendServiceException(code, backendErrorMessage, "An unexpected server error occurred. Status: " + code);
                        }
                    }
                        })
                .exceptionally(e -> {
                    System.err.println("Network or request error: " + e.getMessage());
                    if (response.equals(ApiResponse.class))
                        return response.cast(new ApiResponse(false, "Network error or server unavailable."));
                    return null;
                });
    }
}