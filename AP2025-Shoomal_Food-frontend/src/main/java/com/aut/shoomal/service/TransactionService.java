package com.aut.shoomal.service;

import com.aut.shoomal.dto.response.TransactionResponse;
import com.fasterxml.jackson.core.type.TypeReference;

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
}