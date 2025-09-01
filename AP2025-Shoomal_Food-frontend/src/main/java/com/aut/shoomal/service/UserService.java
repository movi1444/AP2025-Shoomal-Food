package com.aut.shoomal.service;

import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserService extends AbstractService
{
    public CompletableFuture<List<String>> getCustomersWithOrder(String token, Integer restaurantId)
    {
        String endpoint = "order/customer/" + restaurantId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Client error creating get customer list request: " + e.getMessage());
            throw new RuntimeException("Client error creating get customer list request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<String>> getCouriersWithOrder(String token, Integer restaurantId)
    {
        String endpoint = "order/courier/" + restaurantId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Client error creating get courier list request: " + e.getMessage());
            throw new RuntimeException("Client error creating get courier list request: " + e.getMessage(), e);
        }
    }
}