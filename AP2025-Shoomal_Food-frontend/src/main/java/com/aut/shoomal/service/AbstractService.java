package com.aut.shoomal.service;

import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.exceptions.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractService
{
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;
    public AbstractService()
    {
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
    }

    protected HttpRequest.Builder createRequestBuilder(String endpoint)
    {
        String BASE_URL = "http://localhost:8080/";
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
                    System.out.println("HTTP Response Status: " + code);

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
                        handleHttpErrorResponse(code, responseBody);
                        return null;
                    }
                        })
                .exceptionally(e -> {
                    handleCompletableFutureException(e);
                    return null;
                });
    }

    protected <T> CompletableFuture<List<T>> sendListRequest(HttpRequest request, TypeReference<List<T>> typeReference)
    {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> {
                    String responseBody = httpResponse.body();
                    int statusCode = httpResponse.statusCode();
                    System.out.println("HTTP Response Status: " + statusCode);

                    if (statusCode >= 200 && statusCode < 300)
                    {
                        try {
                            return objectMapper.readValue(responseBody, typeReference);
                        } catch (IOException e) {
                            System.err.println("Error deserializing successful list response to " + typeReference.getType() + ": " + e.getMessage());
                            throw new FrontendServiceException(statusCode, "Failed to parse successful server list response.", "Failed to process server data for list.");
                        }
                    }
                    else
                    {
                        handleHttpErrorResponse(statusCode, responseBody);
                        return null;
                    }
                })
                .exceptionally(e -> {
                    handleCompletableFutureException(e);
                    return null;
                });
    }

    protected String buildQueryString(Map<String, String> params)
    {
        if (params == null || params.isEmpty())
            return "";

        StringBuilder queryString = new StringBuilder();
        boolean firstParam = true;

        for (Map.Entry<String, String> entry : params.entrySet())
        {
            String paramName = entry.getKey();
            String paramValue = entry.getValue();

            if (paramName != null && !paramName.isEmpty() && paramValue != null && !paramValue.isEmpty())
            {
                if (firstParam)
                {
                    queryString.append("?");
                    firstParam = false;
                }
                else
                    queryString.append("&");
                queryString.append(URLEncoder.encode(paramName, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(paramValue, StandardCharsets.UTF_8));
            }
        }
        return queryString.toString();
    }

    protected void handleHttpErrorResponse(int code, String responseBody) throws FrontendServiceException
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
                throw new InvalidInputException(backendErrorMessage, "ورودی وارد شده اشتباه است:" + "\n" + backendErrorMessage);
            case 401:
                throw new UnauthorizedException(backendErrorMessage, "کاربر احراز هویت نشده است.");
            case 403:
                throw new ForbiddenException(backendErrorMessage, "دسترسی غیر مجاز: " + "\n" + backendErrorMessage);
            case 404:
                throw new NotFoundException(backendErrorMessage, "منابع مورد نیاز پیدا نشدند: " + "\n" + backendErrorMessage);
            case 409:
                throw new ConflictException(backendErrorMessage, "تداخل در وضعیت منابع موجود: " + "\n" + backendErrorMessage);
            case 500:
                throw new ServiceUnavailableException(backendErrorMessage, "خطای داخلی در سرور رخ داد.");
            default:
                throw new FrontendServiceException(code, backendErrorMessage, "An unexpected server error occurred. Status: " + code);
        }
    }

    protected void handleCompletableFutureException(Throwable e)
    {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        switch (cause) {
            case ConnectException ignored -> {
                System.err.println("Network connection refused: " + cause.getMessage());
                throw new FrontendServiceException(-1, "Failed to connect to the server. Please ensure the backend is running.", "Failed to connect to the server. Please check your network connection.");
            }
            case UnknownHostException ignored -> {
                System.err.println("Unknown host: " + cause.getMessage());
                throw new FrontendServiceException(-3, "Server address could not be resolved.", "Cannot reach the server. Please check the application configuration or your internet connection.");
            }
            case IOException ignored -> {
                System.err.println("I/O error during HTTP request: " + cause.getMessage());
                throw new FrontendServiceException(-4, "An I/O error occurred during the request.", "A problem occurred while sending or receiving data. Please try again.");
            }
            case InterruptedException ignored -> {
                Thread.currentThread().interrupt();
                System.err.println("Request interrupted: " + cause.getMessage());
                throw new FrontendServiceException(-2, "Request was interrupted.", "Operation interrupted. Please try again.");
            }
            case FrontendServiceException frontendServiceException -> throw frontendServiceException;
            case null, default -> {
                System.err.println("Unhandled client-side exception: " + cause.getMessage());
                throw new FrontendServiceException(0, "An unexpected client-side error occurred: " + cause.getMessage(), "An unexpected error occurred. Please try again later.");
            }
        }
    }
}