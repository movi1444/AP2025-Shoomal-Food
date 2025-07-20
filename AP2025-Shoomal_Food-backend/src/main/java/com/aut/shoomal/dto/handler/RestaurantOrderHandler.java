package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.restaurant.RestaurantOrderStatus;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.entity.user.Seller;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.UpdateOrderStatusRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.exceptions.ForbiddenException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderManager;
import com.aut.shoomal.payment.order.OrderStatus;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RestaurantOrderHandler extends AbstractHttpHandler {

    private final RestaurantManager restaurantManager;
    private final OrderManager orderManager;
    protected final UserManager userManager;
    protected final BlacklistedTokenDao blacklistedTokenDao;

    private static final Pattern RESTAURANT_ID_ORDERS_PATH = Pattern.compile("/restaurants/(\\d+)/orders/?$");
    private static final Pattern RESTAURANT_ORDER_ID_PATH = Pattern.compile("/restaurants/orders/(\\d+)$");

    public RestaurantOrderHandler(RestaurantManager restaurantManager, OrderManager orderManager,
                                  UserManager userManager, BlacklistedTokenDao blacklistedTokenDao) {
        this.restaurantManager = restaurantManager;
        this.orderManager = orderManager;
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
            if (RESTAURANT_ID_ORDERS_PATH.matcher(requestPath).matches()) {
                Optional<Integer> restaurantIdOptional = extractIdFromPath(requestPath, RESTAURANT_ID_ORDERS_PATH);
                int restaurantId = restaurantIdOptional.orElse(-1);

                if (restaurantId == -1) {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Invalid restaurant ID in path."));
                    return;
                }

                if (!restaurantManager.isOwner(restaurantId, String.valueOf(authenticatedUser.getId()))) {
                    sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Not the owner of this restaurant."));
                    return;
                }
                if (!(authenticatedUser instanceof Seller) || !((Seller) authenticatedUser).isApproved()) {
                    sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Seller is not yet approved to manage restaurant orders."));
                    return;
                }

                if (method.equalsIgnoreCase("GET")) {
                    handleListOrdersForRestaurant(exchange, restaurantId);
                } else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "Method Not Allowed. Expected GET."));
                }
            }
            else if (RESTAURANT_ORDER_ID_PATH.matcher(requestPath).matches()) {
                Optional<Integer> orderIdOptional = extractIdFromPath(requestPath, RESTAURANT_ORDER_ID_PATH);
                int orderId = orderIdOptional.orElse(-1);

                if (orderId == -1) {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Invalid order ID in path."));
                    return;
                }

                if (method.equalsIgnoreCase("PATCH")) {
                    handleChangeOrderStatus(exchange, authenticatedUser, orderId);
                } else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "Method Not Allowed. Expected PATCH."));
                }
            }
            else {
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "Resource not found"));
            }
        } catch (NotFoundException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "Resource not found: " + e.getMessage()));
        } catch (InvalidInputException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Invalid input: " + e.getMessage()));
        } catch (ForbiddenException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "Internal server error: " + e.getMessage()));
        } finally {
            exchange.close();
        }
    }

    private void handleListOrdersForRestaurant(HttpExchange exchange, int restaurantId) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange);

        String search = queryParams.get("search");
        String customerIdStr = queryParams.get("user");
        String courierIdStr = queryParams.get("courier");
        String statusString = queryParams.get("status");

        List<Order> orders = orderManager.getAllOrders(search, String.valueOf(restaurantId), customerIdStr, courierIdStr, statusString);

        List<OrderResponse> orderResponses = orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());

        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, orderResponses);
    }

    private void handleChangeOrderStatus(HttpExchange exchange, User authenticatedUser, int orderId) throws IOException {
        if (!checkContentType(exchange)) return;

        if (!restaurantManager.isOwner(orderManager.findOrderById(orderId).getRestaurant().getId().intValue(), String.valueOf(authenticatedUser.getId()))) {
            throw new ForbiddenException("403 Forbidden: You are not authorized to change the status of this order.");
        }
        if (!(authenticatedUser instanceof Seller) || !((Seller) authenticatedUser).isApproved()) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Seller is not yet approved to manage restaurant orders."));
            return;
        }

        UpdateOrderStatusRequest requestBody;
        try {
            requestBody = parseRequestBody(exchange, UpdateOrderStatusRequest.class);
            if (requestBody == null || requestBody.getStatus() == null || requestBody.getStatus().trim().isEmpty()) {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: 'status' is required."));
                return;
            }
        } catch (IOException e) {
            System.err.println("Error parsing request body for PATCH /restaurants/orders/{order_id}: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            return;
        }

        try {
            Order order = orderManager.findOrderById(orderId);
            if (order == null) {
                throw new NotFoundException("404 Not Found: Order with ID " + orderId + " not found.");
            }

            OrderStatus internalOrderStatus = switch (RestaurantOrderStatus.fromValue(requestBody.getStatus())) {
                case ACCEPTED -> OrderStatus.WAITING_VENDOR;
                case REJECTED -> OrderStatus.CANCELLED;
                case SERVED -> OrderStatus.FINDING_COURIER;
            };

            order.setOrderStatus(internalOrderStatus);
            orderManager.updateOrder(order);

            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Order status changed successfully."));

        } catch (NotFoundException e) {
            System.err.println(e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, e.getMessage()));
        } catch (ForbiddenException e) {
            System.err.println(e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, e.getMessage()));
        } catch (InvalidInputException e) {
            System.err.println(e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during PATCH /restaurants/orders/" + orderId + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        }
    }


    private OrderResponse convertToOrderResponse(Order order) {
        if (order == null) return null;
        return new OrderResponse(
                order.getId(),
                order.getDeliveryAddress(),
                Math.toIntExact(order.getCustomer().getId()),
                Math.toIntExact(order.getRestaurant().getId()),
                (order.getCourier() != null) ? Math.toIntExact(order.getCourier().getId()) : null,
                (order.getCoupon() != null) ? order.getCoupon().getId() : null,
                order.getOrderItems().stream().map(item -> item.getFood().getId().intValue()).toList(),
                order.getRawPrice(),
                order.getAdditionalFee(),
                order.getTaxFee(),
                order.getCourierFee(),
                order.getPayPrice(),
                order.getOrderStatus().getName(),
                order.getCreatedAt().toString(),
                order.getUpdatedAt().toString()
        );
    }
}