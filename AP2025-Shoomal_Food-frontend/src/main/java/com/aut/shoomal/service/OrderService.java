package com.aut.shoomal.service;

import com.aut.shoomal.dto.response.OrderResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.aut.shoomal.dto.request.SubmitOrderRequest;
import com.aut.shoomal.dto.response.CouponResponse;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OrderService extends AbstractService
{
    public CompletableFuture<List<OrderResponse>> getBuyerOrderHistory(
            String token,
            String search,
            String vendor)
    {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("search", search);
            params.put("vendor", vendor);
            String query = buildQueryString(params);

            String endpoint = "orders/history" + query;
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error getting buyer order history: " + e.getMessage());
            throw new RuntimeException("Error getting buyer order history: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<OrderResponse> submitOrder(String token, SubmitOrderRequest request) {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("orders", token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, OrderResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating submit order request: " + e.getMessage());
            throw new RuntimeException("Error creating submit order request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<CouponResponse> checkCouponValidity(String token, String couponCode) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("coupon_code", couponCode);
            String query = buildQueryString(params);
            String endpoint = "coupons" + query;
            HttpRequest httpRequest = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendRequest(httpRequest, CouponResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating check coupon validity request: " + e.getMessage());
            throw new RuntimeException("Error creating check coupon validity request: " + e.getMessage(), e);
        }
    }
}