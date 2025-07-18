package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.entity.user.Seller;
import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.food.FoodManager;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.AddFoodItemRequest;
import com.aut.shoomal.dto.request.UpdateFoodItemRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.exceptions.*;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.regex.Pattern;

public class FoodItemHandler extends AbstractHttpHandler {

    private final RestaurantManager restaurantManager;
    private final FoodManager foodManager;
    protected final UserManager userManager;
    protected final BlacklistedTokenDao blacklistedTokenDao;

    private static final Pattern FOOD_ITEM_ID_PATH_PATTERN = Pattern.compile("/restaurants/\\d+/item/(\\d+).*");
    private static final Pattern RESTAURANT_ID_FROM_ITEM_PATH_PATTERN = Pattern.compile("/restaurants/(\\d+)/item.*");

    public FoodItemHandler(RestaurantManager restaurantManager, FoodManager foodManager, UserManager userManager, BlacklistedTokenDao blacklistedTokenDao) {
        this.restaurantManager = restaurantManager;
        this.foodManager = foodManager;
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
            Optional<Integer> restaurantIdOptional = extractIdFromPath(requestPath, RESTAURANT_ID_FROM_ITEM_PATH_PATTERN);
            int restaurantId = restaurantIdOptional.orElse(-1);

            Optional<Integer> foodItemIdOptional = extractIdFromPath(requestPath, FOOD_ITEM_ID_PATH_PATTERN);
            int foodItemId = foodItemIdOptional.orElse(-1);

            if (requestPath.equals("/restaurants/" + restaurantId + "/item") && method.equalsIgnoreCase("POST") && restaurantId != -1) {
                handleAddFoodItem(exchange, authenticatedUser, restaurantId);
            } else if (requestPath.equals("/restaurants/" + restaurantId + "/item/" + foodItemId) && foodItemId != -1 && restaurantId != -1) {
                if (method.equalsIgnoreCase("PUT")) {
                    handleUpdateFoodItem(exchange, authenticatedUser, restaurantId, foodItemId);
                } else if (method.equalsIgnoreCase("DELETE")) {
                    handleDeleteFoodItem(exchange, authenticatedUser, restaurantId, foodItemId);
                } else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "Method Not Allowed. Expected PUT or DELETE"));
                }
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

    private void handleAddFoodItem(HttpExchange exchange, User authenticatedUser, Integer restaurantIdFromPath) throws IOException {
        if (!checkHttpMethod(exchange, "POST")) return;
        if (!restaurantManager.isOwner(restaurantIdFromPath, String.valueOf(authenticatedUser.getId()))) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Not the owner of this restaurant."));
            return;
        }
        if (!(authenticatedUser instanceof Seller) || !((Seller) authenticatedUser).isApproved()) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Seller is not yet approved to manage food items."));
            return;
        }
        AddFoodItemRequest request = parseRequestBody(exchange, AddFoodItemRequest.class);
        if (request == null || request.getName() == null || request.getName().trim().isEmpty() ||
                request.getDescription() == null || request.getDescription().trim().isEmpty() ||
                request.getPrice() == null || request.getSupply() == null ||
                request.getKeywords() == null || request.getKeywords().isEmpty()) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Invalid input: name, description, price, supply, and categories are required."));
            return;
        }

        Food newFoodItem = foodManager.addFoodItem(restaurantIdFromPath, request, String.valueOf(authenticatedUser.getId()));
        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, convertToFoodItemResponse(newFoodItem));
    }

    private void handleUpdateFoodItem(HttpExchange exchange, User authenticatedUser, Integer restaurantIdFromPath, Integer itemId) throws IOException {
        if (!checkHttpMethod(exchange, "PUT")) return;
        if (!restaurantManager.isOwner(restaurantIdFromPath, String.valueOf(authenticatedUser.getId()))) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Not the owner of this restaurant."));
            return;
        }
        if (!(authenticatedUser instanceof Seller) || !((Seller) authenticatedUser).isApproved()) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Seller is not yet approved to manage food items."));
            return;
        }
        UpdateFoodItemRequest request = parseRequestBody(exchange, UpdateFoodItemRequest.class);
        if (request == null) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Invalid input: Request body is empty."));
            return;
        }
        if (request.getVendor_id() != null && !request.getVendor_id().equals(restaurantIdFromPath)) {
            throw new InvalidInputException("Vendor ID in request body must match restaurant ID in path.");
        }
        Food updatedFoodItem = foodManager.updateFoodItem(restaurantIdFromPath, itemId, request, String.valueOf(authenticatedUser.getId()));
        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, convertToFoodItemResponse(updatedFoodItem));
    }

    private void handleDeleteFoodItem(HttpExchange exchange, User authenticatedUser, Integer restaurantId, Integer itemId) throws IOException {
        if (!checkHttpMethod(exchange, "DELETE")) return;
        if (!restaurantManager.isOwner(restaurantId, String.valueOf(authenticatedUser.getId()))) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Not the owner of this restaurant."));
            return;
        }
        if (!(authenticatedUser instanceof Seller) || !((Seller) authenticatedUser).isApproved()) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Seller is not yet approved to manage food items."));
            return;
        }
        foodManager.deleteFoodItem(restaurantId, itemId, String.valueOf(authenticatedUser.getId()));
        sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "Food item removed successfully"));
    }

    private com.aut.shoomal.dto.response.FoodItemResponse convertToFoodItemResponse(Food food) {
        if (food == null) return null;
        return new com.aut.shoomal.dto.response.FoodItemResponse(
                food.getId(),
                food.getName(),
                food.getImageBase64(),
                food.getDescription(),
                (int) food.getPrice(),
                food.getSupply(),
                food.getKeywords()
        );
    }
}