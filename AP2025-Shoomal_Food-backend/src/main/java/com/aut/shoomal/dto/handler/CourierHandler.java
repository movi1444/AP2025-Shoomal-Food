package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.enums.CourierDeliveryStatus;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.UpdateDeliveryStatusRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.ForbiddenException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.Order;
import com.aut.shoomal.payment.OrderManager;
import com.aut.shoomal.payment.OrderStatus;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CourierHandler extends AbstractHttpHandler {

    private final UserManager userManager;
    private final OrderManager orderManager;
    private final BlacklistedTokenDao blacklistedTokenDao;

    private static final Pattern DELIVERIES_AVAILABLE_PATH = Pattern.compile("/deliveries/available/?$");
    private static final Pattern DELIVERIES_ORDER_ID_PATH = Pattern.compile("/deliveries/(\\d+)$");
    private static final Pattern DELIVERIES_HISTORY_PATH = Pattern.compile("/deliveries/history/?$");

    public CourierHandler(UserManager userManager, OrderManager orderManager, BlacklistedTokenDao blacklistedTokenDao) {
        this.userManager = userManager;
        this.orderManager = orderManager;
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

        if (!checkUserRole(exchange, authenticatedUser, "courier")) {
            return;
        }

        try {
            if (DELIVERIES_AVAILABLE_PATH.matcher(requestPath).matches() && method.equalsIgnoreCase("GET")) {
                handleGetAvailableDeliveries(exchange);
            } else if (DELIVERIES_ORDER_ID_PATH.matcher(requestPath).matches() && method.equalsIgnoreCase("PATCH")) {
                Optional<Integer> orderIdOptional = extractIdFromPath(requestPath, DELIVERIES_ORDER_ID_PATH);
                if (orderIdOptional.isPresent()) {
                    handleUpdateDeliveryStatus(exchange, authenticatedUser, orderIdOptional.get());
                } else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Invalid order ID."));
                }
            } else if (DELIVERIES_HISTORY_PATH.matcher(requestPath).matches() && method.equalsIgnoreCase("GET")) {
                handleGetDeliveryHistory(exchange, authenticatedUser);
            } else {
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "Resource not found."));
            }
        } catch (NotFoundException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "Resource not found: " + e.getMessage()));
        }  catch (InvalidInputException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "Invalid input: " + e.getMessage()));
        } catch (ForbiddenException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: " + e.getMessage()));
        } catch (ConflictException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_CONFLICT, new ApiResponse(false, "Conflict: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "Internal server error: " + e.getMessage()));
        } finally {
            exchange.close();
        }
    }

    private void handleGetAvailableDeliveries(HttpExchange exchange) throws IOException {
        List<Order> availableOrders = orderManager.getAllOrders(null, null, null, null, OrderStatus.FINDING_COURIER.getName());
        List<OrderResponse> responseList = availableOrders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responseList);
    }

    private void handleUpdateDeliveryStatus(HttpExchange exchange, User authenticatedUser, int orderId) throws IOException {
        if (!checkContentType(exchange)) return;

        UpdateDeliveryStatusRequest requestBody;
        try {
            requestBody = parseRequestBody(exchange, UpdateDeliveryStatusRequest.class);
            if (requestBody == null || requestBody.getStatus() == null) {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: 'status' is required in request body."));
                return;
            }
        } catch (IOException e) {
            System.err.println("Error parsing request body for PATCH /deliveries/{order_id}: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            return;
        }

        CourierDeliveryStatus courierStatus = requestBody.getStatus();
        OrderStatus newOrderStatus;
        try {
            switch (courierStatus) {
                case ACCEPTED:
                    newOrderStatus = OrderStatus.ON_THE_WAY;
                    break;
                case RECEIVED:
                    newOrderStatus = OrderStatus.ON_THE_WAY;
                    break;
                case DELIVERED:
                    newOrderStatus = OrderStatus.COMPLETED;
                    break;
                default:
                    throw new InvalidInputException("Invalid courier delivery status provided.");
            }
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid status value: " + courierStatus.getValue()));
            return;
        }

        Order order = orderManager.findOrderById(orderId);
        if (order == null) {
            throw new NotFoundException("Order with ID " + orderId + " not found.");
        }

        if (order.getOrderStatus() == OrderStatus.FINDING_COURIER && courierStatus == CourierDeliveryStatus.ACCEPTED) {
            if (order.getCourier() != null) {
                throw new ConflictException("Delivery already assigned to another courier.");
            }
            order.setCourier(authenticatedUser);
        }
        else if (order.getCourier() == null || !order.getCourier().getId().equals(authenticatedUser.getId())) {
            throw new ForbiddenException("You are not authorized to update this order's status.");
        }

        if (!isValidStatusTransition(order.getOrderStatus(), newOrderStatus)) {
            throw new InvalidInputException("Invalid status transition from " + order.getOrderStatus().getName() + " to " + newOrderStatus.getName());
        }

        order.setOrderStatus(newOrderStatus);
        orderManager.updateOrder(order);

        sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "Order status updated successfully."));
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case FINDING_COURIER:
                return newStatus == OrderStatus.ON_THE_WAY;
            case ON_THE_WAY:
                return newStatus == OrderStatus.COMPLETED;
            default:
                return false;
        }
    }

    private void handleGetDeliveryHistory(HttpExchange exchange, User authenticatedUser) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange);

        String search = queryParams.get("search");
        String vendorId = queryParams.get("vendor");
        String customerId = queryParams.get("customer");
        String statusString = queryParams.get("status");

        List<Order> orders = orderManager.getAllOrders(search, vendorId, customerId, String.valueOf(authenticatedUser.getId()), statusString);

        List<OrderResponse> responseList = orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responseList);
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
                order.getOrderItems().stream().map(item -> Math.toIntExact(item.getFood().getId())).toList(),
                order.getRawPrice(),
                order.getTaxFee(),
                order.getCourierFee(),
                order.getPayPrice(),
                order.getOrderStatus().getName(),
                order.getCreatedAt().toString(),
                order.getUpdatedAt().toString()
        );
    }
}