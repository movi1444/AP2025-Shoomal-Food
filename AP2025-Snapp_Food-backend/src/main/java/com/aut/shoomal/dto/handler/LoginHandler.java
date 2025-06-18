package com.aut.shoomal.dto.handler;

import com.aut.shoomal.Erfan.User;
import com.aut.shoomal.auth.LoginManager;
import com.aut.shoomal.dto.request.UserLoginRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.BankInfoResponse;
import com.aut.shoomal.dto.response.UserLoginResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;

public class LoginHandler extends AbstractHttpHandler
{
    private final LoginManager loginManager;
    public LoginHandler(LoginManager loginManager)
    {
        this.loginManager = loginManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        if (!checkHttpMethod(exchange, "POST"))
            return;
        if (!checkContentType(exchange))
            return;

        UserLoginRequest requestBody;
        try {
            requestBody = parseRequestBody(exchange, UserLoginRequest.class);
            if (requestBody == null)
            {
                sendResponse(
                        exchange,
                        HttpURLConnection.HTTP_BAD_REQUEST,
                        new ApiResponse(false, "400 Invalid request: Request body is empty.")
                );
                return;
            }
        } catch (IOException e) {
            System.err.println("Error parsing request body for /auth/login: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            return;
        }

        try {
            User user = loginManager.handleLogin(
                    requestBody.getPassword(),
                    requestBody.getPhone()
            );
            System.out.println("User logged in successfully.");

            String token = JwtUtil.generateToken(user);
            UserLoginResponse loginResponse = getLoginResponse(user, token);
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, loginResponse);
        } catch (InvalidInputException e) {
            System.err.println("Invalid input during login: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during auth/login: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        }

        //TODO: catch 404 and 429 errors.
    }

    private static UserLoginResponse getLoginResponse(User user, String token)
    {
        BankInfoResponse bankInfo = null;
        if (user.getBank() != null)
            bankInfo = new BankInfoResponse(
                user.getBank().getName(),
                user.getBank().getAccountNumber()
            );
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getRole().getName(),
                user.getAddress(),
                user.getProfileImageBase64(),
                bankInfo
        );
        return new UserLoginResponse(
                "200 User logged in successfully",
                token,
                userResponse
        );
    }
}