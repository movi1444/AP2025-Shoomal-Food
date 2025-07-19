package com.aut.shoomal.service;

import com.aut.shoomal.dto.request.CreateCouponRequest;
import com.aut.shoomal.dto.request.UpdateCouponRequest;
import com.aut.shoomal.dto.response.CouponResponse;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.dto.response.TransactionResponse;
import com.aut.shoomal.dto.response.AdminUserResponse;
import com.aut.shoomal.dto.request.UpdateApprovalRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AdminDataService extends AbstractService {

    public CompletableFuture<List<AdminUserResponse>> getAllUsers(String token) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("admin/users", token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<List<AdminUserResponse>>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllUsers request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllUsers request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<OrderResponse>> getAllOrders(String token) {
        return getAllOrders(token, null, null, null, null, null);
    }

    public CompletableFuture<List<OrderResponse>> getAllOrders(String token, String search, String restaurant, String customer, String courier, String status) {
        try {
            Map<String, String> params = new HashMap<>();
            if (search != null) params.put("search", search);
            if (restaurant != null) params.put("vendor", restaurant);
            if (customer != null) params.put("customer", customer);
            if (courier != null) params.put("courier", courier);
            if (status != null) params.put("status", status);

            String query = buildQueryString(params);
            String endpoint = "admin/orders" + query;

            HttpRequest request = createAuthenticatedRequestBuilder(endpoint, token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllOrders request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllOrders request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<TransactionResponse>> getAllTransactions(String token) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("admin/transactions", token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllTransactions request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllTransactions request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> updateUserApprovalStatus(String userId, UpdateApprovalRequest requestBody, String token) {
        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = createAuthenticatedRequestBuilder("admin/users/" + userId + "/status", token)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
            return sendRequest(request, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating updateUserApprovalStatus request: " + e.getMessage());
            throw new RuntimeException("Client error creating updateUserApprovalStatus request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<RestaurantResponse>> getAllRestaurants(String token) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("admin/restaurants", token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<List<RestaurantResponse>>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllRestaurants request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllRestaurants request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<List<CouponResponse>> getAllCoupons(String token) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("admin/coupons", token)
                    .GET()
                    .build();
            return sendListRequest(request, new TypeReference<List<CouponResponse>>() {});
        } catch (Exception e) {
            System.err.println("Error creating getAllCoupons request: " + e.getMessage());
            throw new RuntimeException("Client error creating getAllCoupons request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<CouponResponse> getCouponById(String token, Integer couponId) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("admin/coupons/" + couponId, token)
                    .GET()
                    .build();
            return sendRequest(request, CouponResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating getCouponById request: " + e.getMessage());
            throw new RuntimeException("Client error creating getCouponById request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<CouponResponse> createCoupon(String token, CreateCouponRequest requestBody) {
        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = createAuthenticatedRequestBuilder("admin/coupons", token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
            return sendRequest(request, CouponResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating createCoupon request: " + e.getMessage());
            throw new RuntimeException("Client error creating createCoupon request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<CouponResponse> updateCoupon(String token, Integer couponId, UpdateCouponRequest requestBody) {
        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = createAuthenticatedRequestBuilder("admin/coupons/" + couponId, token)
                    .PUT(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
            return sendRequest(request, CouponResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating updateCoupon request: " + e.getMessage());
            throw new RuntimeException("Client error creating updateCoupon request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> deleteCoupon(String token, Integer couponId) {
        try {
            HttpRequest request = createAuthenticatedRequestBuilder("admin/coupons/" + couponId, token)
                    .DELETE()
                    .build();
            return sendRequest(request, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Error creating deleteCoupon request: " + e.getMessage());
            throw new RuntimeException("Client error creating deleteCoupon request: " + e.getMessage(), e);
        }
    }
}