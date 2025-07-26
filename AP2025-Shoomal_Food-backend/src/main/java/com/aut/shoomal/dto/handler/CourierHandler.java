package com.aut.shoomal.dto.handler;

import com.aut.shoomal.dto.response.UpdateDeliveryStatusResponse;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.entity.user.CourierDeliveryStatus;
import com.aut.shoomal.entity.user.Courier;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.UpdateDeliveryStatusRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.ForbiddenException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderManager;
import com.aut.shoomal.payment.order.OrderStatus;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
    private final RestaurantManager restaurantManager;
    private final BlacklistedTokenDao blacklistedTokenDao;

    private static final Pattern DELIVERIES_AVAILABLE_PATH = Pattern.compile("/deliveries/available/?$");
    private static final Pattern DELIVERIES_ORDER_ID_PATH = Pattern.compile("/deliveries/(\\d+)$");
    private static final Pattern DELIVERIES_HISTORY_PATH = Pattern.compile("/deliveries/history/?$");
    private static final Pattern VENDOR_DELIVERY_PATTERN = Pattern.compile("/deliveries/vendor/?$");

    public CourierHandler(UserManager userManager, OrderManager orderManager, BlacklistedTokenDao blacklistedTokenDao, RestaurantManager restaurantManager) {
        this.userManager = userManager;
        this.orderManager = orderManager;
        this.blacklistedTokenDao = blacklistedTokenDao;
        this.restaurantManager = restaurantManager;
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

        if (!(authenticatedUser instanceof Courier) || !((Courier) authenticatedUser).isApproved()) {
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, "Forbidden request: Courier is not yet approved to perform delivery actions."));
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
            } else if (VENDOR_DELIVERY_PATTERN.matcher(requestPath).matches() && method.equalsIgnoreCase("GET")) {
                getVendorNames(exchange, authenticatedUser.getId());
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

    private void getVendorNames(HttpExchange exchange, Long id) throws IOException
    {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Restaurant> restaurants = restaurantManager.findByCourier(session, id);
        if (restaurants == null)
        {
            session.close();
            throw new NotFoundException("No restaurant found.");
        }
        List<String> names = restaurants.stream().map(Restaurant::getName).toList();
        session.close();
        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, names);
    }

    private void handleGetAvailableDeliveries(HttpExchange exchange) throws IOException {
        List<Order> availableOrders = orderManager.getAllOrders(null, null, null, null, OrderStatus.FINDING_COURIER.getName());
        List<Order> orders2 = orderManager.getAllOrders(null, null, null, null, OrderStatus.ON_THE_WAY.getName());
        availableOrders.addAll(orders2);
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
            if (requestBody == null || requestBody.getStatus() == null || requestBody.getStatus().isEmpty()) {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: 'status' is required in request body."));
                return;
            }
        } catch (IOException e) {
            System.err.println("Error parsing request body for PATCH /deliveries/{order_id}: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            return;
        }

        CourierDeliveryStatus courierStatus = CourierDeliveryStatus.fromValue(requestBody.getStatus());
        OrderStatus newOrderStatus;
        try {
            newOrderStatus = switch (courierStatus) {
                case ACCEPTED, RECEIVED -> OrderStatus.ON_THE_WAY;
                case DELIVERED -> OrderStatus.COMPLETED;
                default -> throw new InvalidInputException("Invalid courier delivery status provided.");
            };
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid status value: " + courierStatus.getValue()));
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Order order = session.get(Order.class, orderId);
        if (order == null) {
            if (transaction != null)
                transaction.rollback();
            session.close();
            throw new NotFoundException("Order with ID " + orderId + " not found.");
        }

        if (order.getOrderStatus() == OrderStatus.FINDING_COURIER && courierStatus == CourierDeliveryStatus.ACCEPTED) {
            if (order.getCourier() != null) {
                if (transaction != null)
                    transaction.rollback();
                session.close();
                throw new ConflictException("Delivery already assigned to another courier.");
            }
            order.setCourier(authenticatedUser);
        }
        else if (order.getCourier() != null && !order.getCourier().getId().equals(authenticatedUser.getId())) {
            if (transaction != null)
                transaction.rollback();
            session.close();
            throw new ForbiddenException("You are not authorized to update this order's status.");
        }

        if (!isValidStatusTransition(order.getOrderStatus(), newOrderStatus)) {
            if (transaction != null)
                transaction.rollback();
            session.close();
            throw new InvalidInputException("Invalid status transition from " + order.getOrderStatus().getName() + " to " + newOrderStatus.getName());
        }

        order.setOrderStatus(newOrderStatus);
        session.merge(order);
        transaction.commit();

        UpdateDeliveryStatusResponse response = new UpdateDeliveryStatusResponse("Order status updated successfully.", this.convertToOrderResponse(order));
        session.close();
        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case FINDING_COURIER -> newStatus == OrderStatus.ON_THE_WAY;
            case ON_THE_WAY -> newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.ON_THE_WAY;
            default -> false;
        };
    }

    private void handleGetDeliveryHistory(HttpExchange exchange, User authenticatedUser) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange);

        String search = queryParams.get("search");
        String vendorId = queryParams.get("vendor");
        String customerId = queryParams.get("customer");
        String statusString = queryParams.get("status");

        List<Order> orders = orderManager.getAllOrders(search, vendorId, customerId, authenticatedUser.getName(), statusString);
        if (orders == null)
            throw new NotFoundException("No orders found.");

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
                order.getCustomer().getName(),
                order.getRestaurant().getName(),
                (order.getCourier() != null) ? order.getCourier().getName() : null,
                (order.getCoupon() != null) ? order.getCoupon().getId() : null,
                order.getOrderItems().stream().map(item -> item.getFood().getName()).toList(),
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