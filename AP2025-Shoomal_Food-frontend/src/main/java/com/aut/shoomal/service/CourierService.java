package com.aut.shoomal.service;

import com.aut.shoomal.dto.request.UpdateDeliveryStatusRequest;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.dto.response.UpdateDeliveryStatusResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CourierService extends AbstractService
{
    public CompletableFuture<List<OrderResponse>> getAvailableOrders(String token)
    {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("/deliveries/available", token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating get available orders request: " + e.getMessage());
            throw new RuntimeException("Error creating get available orders request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<UpdateDeliveryStatusResponse> changeDeliveryStatus(String token, Integer orderId, UpdateDeliveryStatusRequest request)
    {
        String endpoint = "/deliveries/" + orderId;
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, UpdateDeliveryStatusResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating update delivery status request: " + e.getMessage());
            throw new RuntimeException("Error creating update delivery status request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<OrderResponse>> getDeliveryHistory(
        String token,
        String search,
        String vendor,
        String user
    )
    {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("search", search);
            params.put("vendor", vendor);
            params.put("user", user);
            String query = buildQueryString(params);

            String baseEndpoint = "/deliveries/history";
            String endpoint = baseEndpoint + query;
            HttpRequest request = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating get delivery history request: " + e.getMessage());
            throw new RuntimeException("Error creating get delivery history request: " + e.getMessage(), e);
        }
    }
}