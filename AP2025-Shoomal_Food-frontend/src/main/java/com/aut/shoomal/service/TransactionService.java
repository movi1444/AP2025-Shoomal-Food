package com.aut.shoomal.service;

import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.TransactionResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .thenApply(httpResponse -> {
                        String responseBody = httpResponse.body();
                        int statusCode = httpResponse.statusCode();

                        if (statusCode >= 200 && statusCode < 300)
                        {
                            try {
                                return objectMapper.readValue(responseBody, new TypeReference<List<TransactionResponse>>() {});
                            } catch (Exception e) {
                                System.err.println("Error deserializing successful transaction history response: " + e.getMessage());
                                throw new RuntimeException("Failed to process server data.", e);
                            }
                        }
                        else
                        {
                            sendRequest(httpRequest, ApiResponse.class).join();
                            return null;
                        }
                    })
                    .exceptionally(e -> {
                        sendRequest(httpRequest, ApiResponse.class).join();
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Client error creating get transaction request: " + e.getMessage());
            throw new RuntimeException("Client error creating get transaction request: " + e.getMessage(), e);
        }
    }
}