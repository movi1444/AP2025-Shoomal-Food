package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.entity.food.FoodManager;
import com.aut.shoomal.entity.menu.MenuManager;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.payment.order.OrderManager;
import com.aut.shoomal.payment.transaction.PaymentTransactionManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Pattern;

public class RestaurantHandler extends AbstractHttpHandler {

    private final RestaurantCoreHandler restaurantCoreHandler;
    private final FoodItemHandler foodItemHandler;
    private final MenuHandler menuHandler;
    private final RestaurantOrderHandler restaurantOrderHandler;
    private PaymentTransactionManager paymentTransactionManager;

    private static final Pattern RESTAURANT_CORE_PATH_PATTERN = Pattern.compile("/restaurants(?:/\\d+)?/?$|/restaurants/mine/?$");
    private static final Pattern FOOD_ITEM_PATH_PATTERN = Pattern.compile("/restaurants/(\\d+)/item");
    private static final Pattern MENU_PATH_PATTERN = Pattern.compile("/restaurants/\\d+/menu(?:/[^/]+(?:/\\d+)?)?/?$");
    private static final Pattern RESTAURANT_ORDERS_PATH_PATTERN = Pattern.compile("/restaurants/(\\d+)/orders/?$|/restaurants/orders/(\\d+)$");
    private static final Pattern GET_FOODS_PATH_PATTERN = Pattern.compile("/restaurants/(\\d+)/items");
    private static final Pattern GET_FOODS_TITLE_PATH_PATTERN = Pattern.compile("/restaurants/(\\d+)/items(?:/[^/]+(?:/\\d+)?)?/?$");
    private static final Pattern GET_MENUS_FROM_ID_PATTERN = Pattern.compile("/restaurants/\\d+/menus");
    private static final Pattern EDIT_MENU_PATH_PATTERN = Pattern.compile("/restaurants/\\d+/menu/edit(?:/[^/]+(?:/\\d+)?)?/?$");


    public RestaurantHandler(RestaurantManager restaurantManager, FoodManager foodManager, MenuManager menuManager,
                             UserManager userManager, BlacklistedTokenDao blacklistedTokenDao,
                             OrderManager orderManager, PaymentTransactionManager paymentTransactionManager) {
        this.restaurantCoreHandler = new RestaurantCoreHandler(restaurantManager, userManager, blacklistedTokenDao);
        this.foodItemHandler = new FoodItemHandler(restaurantManager, foodManager, userManager, blacklistedTokenDao);
        this.menuHandler = new MenuHandler(restaurantManager, menuManager, foodManager, userManager, blacklistedTokenDao);
        this.restaurantOrderHandler = new RestaurantOrderHandler(restaurantManager, orderManager, userManager, blacklistedTokenDao, paymentTransactionManager,foodManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        User authenticatedUser = authenticate(exchange, restaurantCoreHandler.userManager, restaurantCoreHandler.blacklistedTokenDao);
        if (authenticatedUser == null) {
            return;
        }

        try {
            if (RESTAURANT_CORE_PATH_PATTERN.matcher(requestPath).matches()) {
                restaurantCoreHandler.handle(exchange);
            } else if (FOOD_ITEM_PATH_PATTERN.matcher(requestPath).matches() || GET_FOODS_PATH_PATTERN.matcher(requestPath).matches() || GET_FOODS_TITLE_PATH_PATTERN.matcher(requestPath).matches()) {
                foodItemHandler.handle(exchange);
            } else if (MENU_PATH_PATTERN.matcher(requestPath).matches() || GET_MENUS_FROM_ID_PATTERN.matcher(requestPath).matches() || EDIT_MENU_PATH_PATTERN.matcher(requestPath).matches()) {
                menuHandler.handle(exchange);
            } else if (RESTAURANT_ORDERS_PATH_PATTERN.matcher(requestPath).matches()) {
                restaurantOrderHandler.handle(exchange);
            }
            else {
                sendResponse(
                        exchange,
                        HttpURLConnection.HTTP_NOT_FOUND,
                        new ApiResponse(false, "Resource not found")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(
                    exchange,
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    new ApiResponse(false, "Internal server error: " + e.getMessage())
            );
        } finally {
            exchange.close();
        }
    }
}