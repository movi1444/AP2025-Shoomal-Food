package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.UnauthorizedException;
import com.aut.shoomal.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractHttpHandler implements HttpHandler
{
    protected final ObjectMapper mapper = new ObjectMapper();

    protected <T> T parseRequestBody(HttpExchange exchange, Class<T> clazz) throws IOException
    {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                data.append(line);
            return (!data.isEmpty()) ? mapper.readValue(data.toString(), clazz) : null;
        }
    }

    protected void sendResponse(HttpExchange exchange, int statusCode, ApiResponse response) throws IOException
    {
        String jsonResponse = mapper.writeValueAsString(response);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (exchange; OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected void sendRawJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException
    {
        String jsonResponse = mapper.writeValueAsString(response);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (exchange; OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected boolean checkHttpMethod(HttpExchange exchange, String method) throws IOException
    {
        if (!exchange.getRequestMethod().equalsIgnoreCase(method))
        {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "Method Not Allowed. Expected " + method));
            return false;
        }
        return true;
    }

    protected boolean checkContentType(HttpExchange exchange) throws IOException
    {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().startsWith("application/json"))
        {
            System.err.println("415 Unsupported Media Type. Expected application/json.");
            sendResponse(
                    exchange,
                    HttpURLConnection.HTTP_UNSUPPORTED_TYPE,
                    new ApiResponse(false, "Unsupported Media Type. Expected application/json.")
            );
            return false;
        }
        return true;
    }

    protected User authenticate(HttpExchange exchange, UserManager userManager, BlacklistedTokenDao blacklistedTokenDao) throws IOException
    {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer "))
        {
            System.err.println("401 Unauthorized: No token provided.");
            sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, new ApiResponse(false, "401 Unauthorized: No token provided."));
            return null;
        }

        String token = auth.substring(7);
        try {
            if (blacklistedTokenDao.isTokenBlacklisted(token))
            {
                System.err.println("401 Unauthorized: Token has been blacklisted.");
                sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, new ApiResponse(false, "Unauthorized: Your session has expired. Please log in again."));
                return null;
            }
        } catch (RuntimeException e) {
            System.err.println("Error checking blacklist status: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "Internal server error during authentication."));
            return null;
        }

        User user;
        try {
            user = JwtUtil.validateToken(token, userManager);
        } catch (UnauthorizedException e) {
            System.err.println(e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, new ApiResponse(false, e.getMessage()));
            return null;
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid Input: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
            return null;
        }
        return user;
    }

    protected boolean checkUserRole(HttpExchange exchange, User user, String requiredRole) throws IOException
    {
        if (user == null || user.getRole() == null || !user.getRole().getName().equalsIgnoreCase(requiredRole))
        {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "403 Forbidden: Insufficient privileges. Required role: " + requiredRole));
            return false;
        }
        return true;
    }

    protected Optional<Integer> extractIdFromPath(String path, Pattern pattern)
    {
        Matcher matcher = pattern.matcher(path);
        if (matcher.matches())
        {
            try {
                return Optional.of(Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException e) {
                System.err.println("Error parsing ID from path: " + path);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    protected Map<String, String> parseQueryParams(HttpExchange exchange)
    {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || query.trim().isEmpty())
            return Collections.emptyMap();
        return Arrays.stream(query.split("&"))
                .filter(param -> !param.trim().isEmpty())
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        pair -> pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "",
                        (existing, replacement) -> existing,
                        java.util.LinkedHashMap::new
                ));
    }
}