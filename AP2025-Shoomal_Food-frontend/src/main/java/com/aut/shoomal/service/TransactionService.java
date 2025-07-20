package com.aut.shoomal.service;

import com.aut.shoomal.dto.request.PaymentRequest;
import com.aut.shoomal.dto.request.WalletRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.TransactionResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransactionService extends AbstractService
{
    public CompletableFuture<List<TransactionResponse>> getTransactions(String token)
    {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("transactions", token)
                    .GET()
                    .build();
            return sendListRequest(httpRequest, new TypeReference<>() {});
        } catch (Exception e) {
            System.err.println("Client error creating get transaction request: " + e.getMessage());
            throw new RuntimeException("Client error creating get transaction request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<ApiResponse> chargeWallet(String token, WalletRequest request)
    {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("/wallet/top-up", token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, ApiResponse.class);
        } catch (Exception e) {
            System.err.println("Client error creating charge wallet request: " + e.getMessage());
            throw new RuntimeException("Client error creating charge wallet request: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<TransactionResponse> onlinePayment(String token, PaymentRequest request)
    {
        try {
            HttpRequest httpRequest = createAuthenticatedRequestBuilder("/payment/online", token)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
            return sendRequest(httpRequest, TransactionResponse.class);
        } catch (Exception e) {
            System.err.println("Client error creating online payment request: " + e.getMessage());
            throw new RuntimeException("Client error creating online payment request: " + e.getMessage(), e);
        }
    }
}