package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.entity.user.Seller;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.CreateRestaurantRequest;
import com.aut.shoomal.dto.request.UpdateRestaurantRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.exceptions.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RestaurantCoreHandler extends AbstractHttpHandler {

    private final RestaurantManager restaurantManager;
    protected final UserManager userManager;
    protected final BlacklistedTokenDao blacklistedTokenDao;

    private static final Pattern RESTAURANT_ID_PATH_PATTERN = Pattern.compile("/restaurants/(\\d+).*");

    public RestaurantCoreHandler(RestaurantManager restaurantManager, UserManager userManager, BlacklistedTokenDao blacklistedTokenDao) {
        this.restaurantManager = restaurantManager;
        this.userManager = userManager;
        this.blacklistedTokenDao = blacklistedTokenDao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        User authenticatedUser = authenticate(exchange, userManager, blacklistedTokenDao);
        if (authenticatedUser == null) {
            return;
        }

        try {
            Optional<Integer> restaurantIdOptional = extractIdFromPath(requestPath, RESTAURANT_ID_PATH_PATTERN);
            int restaurantId = restaurantIdOptional.orElse(-1);

            if (requestPath.equals("/restaurants") && method.equalsIgnoreCase("POST")) {
                handleCreateRestaurant(exchange, authenticatedUser);
            } else if (requestPath.equals("/restaurants/mine") && method.equalsIgnoreCase("GET")) {
                handleGetMyRestaurants(exchange, authenticatedUser);
            } else if (requestPath.equals("/restaurants/" + restaurantId) && method.equalsIgnoreCase("PUT") && restaurantId != -1) {
                handleUpdateRestaurant(exchange, authenticatedUser, restaurantId);
            } else {
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "Resource not found"));
            }
        } catch (NotFoundException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "Resource not found: " + e.getMessage()));
        } catch (InvalidInputException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Invalid input: " + e.getMessage()));
        } catch (ForbiddenException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: " + e.getMessage()));
        } catch (ConflictException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_CONFLICT, new ApiResponse(false, "Conflict: " + e.getMessage()));
        } catch (ServiceUnavailableException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_UNAVAILABLE, new ApiResponse(false, "Service temporarily unavailable: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "Internal server error: " + e.getMessage()));
        } finally {
            exchange.close();
        }
    }

    private void handleCreateRestaurant(HttpExchange exchange, User authenticatedUser) throws IOException {
        if (!checkHttpMethod(exchange, "POST")) return;
        if (!isUserAllowed(authenticatedUser, "seller")) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Only sellers can create restaurants."));
            return;
        }

        if (!(authenticatedUser instanceof Seller) || !((Seller) authenticatedUser).isApproved()) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Seller is not yet approved to create restaurants."));
            return;
        }

        CreateRestaurantRequest request = parseRequestBody(exchange, CreateRestaurantRequest.class);
        if (request == null || request.getName() == null || request.getName().trim().isEmpty() ||
                request.getAddress() == null || request.getAddress().trim().isEmpty() ||
                request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Invalid input: name, address, and phone are required."));
            return;
        }
        if (request.getPhone().length() != 8 || !request.getPhone().matches("\\d+")) {
            throw new InvalidInputException("Phone number must be exactly 8 digits.");
        }
        Restaurant newRestaurant = restaurantManager.createRestaurant(request, String.valueOf(authenticatedUser.getId()));
        sendResponse(exchange, HttpURLConnection.HTTP_CREATED, new ApiResponse(true, "Restaurant created successfully", convertToRestaurantResponse(newRestaurant)));
    }

    private void handleGetMyRestaurants(HttpExchange exchange, User authenticatedUser) throws IOException {
        if (!checkHttpMethod(exchange, "GET")) return;
        if (!isUserAllowed(authenticatedUser, "seller")) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Only sellers can view their restaurants."));
            return;
        }
        if (!(authenticatedUser instanceof Seller) || !((Seller) authenticatedUser).isApproved()) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Seller is not yet approved to view restaurants."));
            return;
        }
        List<Restaurant> myRestaurants = restaurantManager.getRestaurantsBySeller(String.valueOf(authenticatedUser.getId()));
        List<RestaurantResponse> responseList = myRestaurants.stream()
                .map(this::convertToRestaurantResponse)
                .collect(Collectors.toList());
        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responseList);
    }

    private void handleUpdateRestaurant(HttpExchange exchange, User authenticatedUser, Integer restaurantId) throws IOException {
        if (!checkHttpMethod(exchange, "PUT")) return;
        if (!restaurantManager.isOwner(restaurantId, String.valueOf(authenticatedUser.getId()))) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Not the owner of this restaurant."));
            return;
        }
        if (!(authenticatedUser instanceof Seller) || !((Seller) authenticatedUser).isApproved()) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Seller is not yet approved to update restaurants."));
            return;
        }
        Restaurant restaurant = restaurantManager.findById((long) restaurantId);
        if (restaurant == null) {
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "Restaurant not found."));
            return;
        }

        UpdateRestaurantRequest request = parseRequestBody(exchange, UpdateRestaurantRequest.class);
        Restaurant updatedRestaurant = restaurantManager.updateRestaurant(restaurantId, request, String.valueOf(authenticatedUser.getId()));
        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, convertToRestaurantResponse(updatedRestaurant));
    }

    private boolean isUserAllowed(User user, String requiredRole) {
        return user.getRole().getName().equalsIgnoreCase(requiredRole);
    }

    private RestaurantResponse convertToRestaurantResponse(Restaurant restaurant) {
        if (restaurant == null) return null;
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getLogoBase64(),
                restaurant.getTaxFee(),
                restaurant.getAdditionalFee()
        );
    }
}