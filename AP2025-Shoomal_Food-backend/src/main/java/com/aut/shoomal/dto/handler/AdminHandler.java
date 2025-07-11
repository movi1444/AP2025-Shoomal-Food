package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.UpdateApprovalRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.BankInfoResponse;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.dto.response.UserResponse;
import com.aut.shoomal.dto.response.TransactionResponse;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.exceptions.ServiceUnavailableException;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderManager;
import com.aut.shoomal.payment.transaction.PaymentTransaction;
import com.aut.shoomal.payment.transaction.PaymentTransactionManager;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class AdminHandler extends AbstractHttpHandler {
    private final UserManager userManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    private final OrderManager orderManager;
    private final RestaurantManager restaurantManager;
    private final PaymentTransactionManager transactionManager;

    private static final Pattern USER_STATUS_PATH = Pattern.compile("^/admin/users/(\\d+)/status$");

    public AdminHandler(UserManager userManager, RestaurantManager restaurantManager,
                        BlacklistedTokenDao blacklistedTokenDao, OrderManager orderManager,
                        PaymentTransactionManager transactionManager) {
        this.userManager = userManager;
        this.restaurantManager = restaurantManager;
        this.blacklistedTokenDao = blacklistedTokenDao;
        this.orderManager = orderManager;
        this.transactionManager = transactionManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        User authenticatedUser = authenticate(exchange, userManager, blacklistedTokenDao);
        if (authenticatedUser == null) return;

        try {
            if (!isAdmin(authenticatedUser)) {
                sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN,
                        new ApiResponse(false, "Admin access required"));
                return;
            }

            if (path.equals("/admin/users") && method.equalsIgnoreCase("GET")) {
                handleListAllUsers(exchange);
            }
            else if (USER_STATUS_PATH.matcher(path).matches() && method.equalsIgnoreCase("PATCH")) {
                handleUserStatusUpdate(exchange, path);
            }
            else if (path.equals("/admin/orders") && method.equalsIgnoreCase("GET")) {
                handleListAllOrders(exchange);
            }
            else if (path.equals("/admin/transactions") && method.equalsIgnoreCase("GET")) {
                handleListAllTransactions(exchange);
            }
            else {
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND,
                        new ApiResponse(false, "Admin resource not found"));
            }
        } catch (NotFoundException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND,
                    new ApiResponse(false, e.getMessage()));
        } catch (InvalidInputException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST,
                    new ApiResponse(false, e.getMessage()));
        } catch (ConflictException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_CONFLICT,
                    new ApiResponse(false, e.getMessage()));
        } catch (ServiceUnavailableException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR,
                    new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred in AdminHandler: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        } finally {
            exchange.close();
        }
    }

    private void handleUserStatusUpdate(HttpExchange exchange, String path) throws IOException {
        Optional<Integer> userId = extractIdFromPath(path, USER_STATUS_PATH);
        if (userId.isEmpty()) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST,
                    new ApiResponse(false, "Invalid user ID"));
            return;
        }

        UpdateApprovalRequest request = parseRequestBody(exchange, UpdateApprovalRequest.class);
        if (request == null || request.getStatus() == null) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST,
                    new ApiResponse(false, "Status is required"));
            return;
        }

        try {
            User user = userManager.getUserById(userId.get().longValue());
            if (user == null) throw new NotFoundException("User not found");

            String role = user.getRole().getName().toLowerCase();
            String message = switch (role) {
                case "courier" -> {
                    userManager.setUserApprovalStatus(userId.get().toString(), request.getStatus());
                    yield "Courier status updated";
                }
                case "seller" -> {
                    restaurantManager.setApprovalStatus(userId.get().toString(), request.getStatus());
                    yield "Seller and restaurant status updated";
                }
                default -> throw new InvalidInputException("Only sellers/couriers can be approved");
            };

            sendResponse(exchange, HttpURLConnection.HTTP_OK,
                    new ApiResponse(true, message));
        } catch (NotFoundException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND,
                    new ApiResponse(false, e.getMessage()));
        }
    }

    private void handleListAllUsers(HttpExchange exchange) throws IOException {
        List<User> users = userManager.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responses);
    }

    private void handleListAllOrders(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange);

        String search = queryParams.get("search");
        String vendorId = queryParams.get("vendor");
        String customerId = queryParams.get("customer");
        String courierId = queryParams.get("courier");
        String statusString = queryParams.get("status");

        try {
            List<Order> orders = orderManager.getAllOrders(search, vendorId, customerId, courierId, statusString);

            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, orderResponses);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Invalid 'status' value or ids are not number."));
        }
    }

    public void handleListAllTransactions(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);

            String search = queryParams.get("search");
            String userIdStr = queryParams.get("user");
            String methodStr = queryParams.get("method");
            String statusStr = queryParams.get("status");

            Long userId = null;
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    userId = Long.parseLong(userIdStr);
                } catch (NumberFormatException e) {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: 'user' ID must be a number."));
                    return;
                }
            }

            List<PaymentTransaction> transactions = transactionManager.getAllTransactions(search, userId, methodStr, statusStr);
            List<TransactionResponse> transactionResponses = transactions.stream()
                    .map(transaction -> {
                        Long transactionUserId = (transaction.getUser() != null) ? transaction.getUser().getId() : null;
                        Integer orderId = (transaction.getOrder() != null) ? transaction.getOrder().getId() : null;

                        return new TransactionResponse(
                                Math.toIntExact(transaction.getId()),
                                transaction.getStatus().getStatus(),
                                transaction.getMethod().getName(),
                                orderId,
                                (transactionUserId != null) ? Math.toIntExact(transactionUserId) : null
                        );
                    })
                    .collect(Collectors.toList());
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, transactionResponses);
        } catch (InvalidInputException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        }
        catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /admin/transactions: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        }
    }


    private UserResponse convertToUserResponse(User user) {
        if (user == null) return null;
        BankInfoResponse bankInfo = (user.getBank() != null) ? new BankInfoResponse(user.getBank().getName(), user.getBank().getAccountNumber()) : null;
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getRole().getName(),
                user.getAddress(),
                user.getProfileImageBase64(),
                bankInfo
        );
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
                order.getAdditionalFee(),
                order.getTaxFee(),
                order.getCourierFee(),
                order.getPayPrice(),
                order.getOrderStatus().getName(),
                order.getCreatedAt().toString(),
                order.getUpdatedAt().toString()
        );
    }

    private boolean isAdmin(User user) {
        return user.getRole().getName().equalsIgnoreCase("Admin");
    }
}