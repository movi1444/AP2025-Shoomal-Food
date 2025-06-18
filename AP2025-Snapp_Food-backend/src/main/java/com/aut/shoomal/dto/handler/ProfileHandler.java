package com.aut.shoomal.dto.handler;

import com.aut.shoomal.Erfan.BankInfo;
import com.aut.shoomal.Erfan.User;
import com.aut.shoomal.Erfan.UserManager;
import com.aut.shoomal.auth.SignupManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.UpdateProfileRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.BankInfoResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.exceptions.DuplicateUserException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;

public class ProfileHandler extends AbstractHttpHandler
{
    private final UserManager userManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    public ProfileHandler(UserManager userManager, BlacklistedTokenDao blacklistedTokenDao)
    {
        this.userManager = userManager;
        this.blacklistedTokenDao = blacklistedTokenDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        if (!checkContentType(exchange))
            return;
        User user = authenticate(exchange, userManager, blacklistedTokenDao);
        if (user == null)
            return;

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equalsIgnoreCase("GET"))
        {
            if (path.equals("/auth/profile"))
                getProfile(exchange, user);
            else
            {
                System.err.println("404 Resource not found for path: " + path);
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for GET /auth/profile."));
            }
        }
        else if (method.equalsIgnoreCase("PUT"))
        {
            if (path.equals("/auth/profile"))
                updateProfile(exchange, user);
            else
            {
                System.err.println("404 Resource not found for path: " + path);
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for PUT /auth/profile."));
            }
        }
        else
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "Method Not Allowed."));
    }

    private void getProfile(HttpExchange exchange, User user) throws IOException
    {
        try {
            UserResponse response = new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getPhoneNumber(),
                    user.getEmail(),
                    user.getRole().getName(),
                    user.getAddress(),
                    user.getProfileImageBase64(),
                    (user.getBank() != null) ? new BankInfoResponse(user.getBank().getName(), user.getBank().getAccountNumber()) : null
            );
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (Exception e) {
            System.err.println("Error getting user profile: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal server error: An unexpected error occurred."));
        }
    }

    private void updateProfile(HttpExchange exchange, User user) throws IOException
    {
        try {
            UpdateProfileRequest request = parseRequestBody(exchange, UpdateProfileRequest.class);
            if (request == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            if (request.getBankInfo() != null)
            {
                String bankName = request.getBankInfo().getBankName();
                String accountNumber = request.getBankInfo().getAccountNumber();
                checkBankInfoData(bankName, accountNumber);

                if (bankName != null && accountNumber != null && !bankName.trim().isEmpty() && !accountNumber.trim().isEmpty())
                {
                    BankInfo bankInfo = new BankInfo(bankName, accountNumber);
                    user.setBank(bankInfo);
                }
            }

            if (request.getFullName() != null)
            {
                if (request.getFullName().trim().isEmpty())
                    throw new InvalidInputException("'full_name' cannot be empty.");
                try {
                    User newUser = userManager.getUserByName(request.getFullName());
                    if (newUser != null && !newUser.getId().equals(user.getId()))
                        throw new DuplicateUserException("409 Conflict: User with this name already exists.");
                } catch (NotFoundException ignored) {}
                user.setName(request.getFullName());
            }
            if (request.getPhone() != null)
            {
                if (request.getPhone().trim().isEmpty())
                    throw new InvalidInputException("'phone' cannot be empty.");
                else if (!SignupManager.isValidPhoneNumber(request.getPhone()))
                    throw new InvalidInputException("'phone' must be a valid phone number.");
                try {
                    User newUser = userManager.getUserByPhoneNumber(request.getPhone());
                    if (newUser != null && !newUser.getId().equals(user.getId()))
                        throw new DuplicateUserException("409 Conflict: User with the same phone number already exists.");
                } catch (NotFoundException ignored) {}
                user.setPhoneNumber(request.getPhone());
            }

            if (request.getEmail() != null && !request.getEmail().trim().isEmpty())
            {
                if (!SignupManager.checkEmailValidation(request.getEmail()))
                    throw new InvalidInputException("'email' must be a valid email address.");
                try {
                    User newUser = userManager.getUserByEmail(request.getEmail());
                    if (newUser != null && !newUser.getId().equals(user.getId()))
                        throw new DuplicateUserException("409 Conflict: User with the same email already exists.");
                } catch (NotFoundException ignored) {}
                user.setEmail(request.getEmail());
            }
            if (request.getAddress() != null)
                user.setAddress(request.getAddress());
            if (request.getProfileImageBase64() != null)
                user.setProfileImageBase64(request.getProfileImageBase64());

            userManager.updateUser(user);
            System.out.println("Profile updated successfully.");
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Profile updated successfully"));
        } catch (IOException e) {
            System.err.println("Error parsing update profile request body: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Malformed JSON in request body."));
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid input: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        } catch (DuplicateUserException e) {
            System.err.println(e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_CONFLICT, new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error updating user profile: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal server: An unexpected error occurred."));
        }
    }

    private void checkBankInfoData(String bankName, String accountNumber) throws InvalidInputException
    {
        if (bankName != null && !bankName.trim().isEmpty())
        {
            if (accountNumber == null || accountNumber.trim().isEmpty())
                throw new InvalidInputException("Bank account number required if bank name is provided.");
        }
        else if (accountNumber != null && !accountNumber.trim().isEmpty())
            throw new InvalidInputException("Bank name required if account number is provided.");
    }
}