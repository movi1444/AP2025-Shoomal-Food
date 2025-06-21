package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserTypes;
import com.aut.shoomal.dto.request.UserRegisterRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.UserRegisterResponse;
import com.aut.shoomal.exceptions.*;
import com.aut.shoomal.auth.SignupManager;
import com.aut.shoomal.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;

public class RegisterHandler extends AbstractHttpHandler
{
    private final SignupManager signupManager;
    public RegisterHandler(SignupManager signupManager)
    {
        this.signupManager = signupManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        if (!checkHttpMethod(exchange, "POST"))
            return;
        if (!checkContentType(exchange))
            return;

        UserRegisterRequest requestBody;
        try {
            requestBody = parseRequestBody(exchange, UserRegisterRequest.class);
            if (requestBody == null) {
                sendResponse(
                        exchange,
                        HttpURLConnection.HTTP_BAD_REQUEST,
                        new ApiResponse(false, "400 Invalid Request: Request body is empty.")
                );
                return;
            }
        } catch (IOException e) {
            System.err.println("Error parsing request body for /auth/register: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            return;
        }

        try {
            UserTypes types = UserTypes.fromName(requestBody.getRole());
            User user = signupManager.handleSignup(
                    types,
                    requestBody.getFullName(),
                    requestBody.getPhone(),
                    requestBody.getPassword(),
                    requestBody.getEmail(),
                    (requestBody.getBankInfo() != null) ? requestBody.getBankInfo().getBankName() : null,
                    (requestBody.getBankInfo() != null) ? requestBody.getBankInfo().getAccountNumber() : null,
                    requestBody.getAddress(),
                    requestBody.getProfileImageBase64()
            );
            System.out.println("User registered successfully.");

            String token = JwtUtil.generateToken(user);
            UserRegisterResponse response = new UserRegisterResponse(
                    "200 User registered successfully",
                    token,
                    user.getId()
            );
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid role provided: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Invalid 'role' type. Must be BUYER, SELLER or COURIER."));
        } catch (InvalidInputException e) {
            System.err.println("Invalid input during register: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, e.getMessage()));
        } catch (DuplicateUserException e) {
            System.err.println("Duplicate user: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_CONFLICT, new ApiResponse(false, e.getMessage()));
        } catch (NotFoundException e) {
            System.err.println("Server configuration error: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: Server configuration issue with user roles."));
        } catch (ServiceUnavailableException e) {
            System.err.println("Critical service error during registration: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: A core service failed during registration."));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during /auth/register: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        }

        //TODO: catch 404 and 429 errors.
    }
}