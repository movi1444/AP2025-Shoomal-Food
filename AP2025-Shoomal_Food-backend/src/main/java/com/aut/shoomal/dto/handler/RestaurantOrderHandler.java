package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.restaurant.Restaurant;
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
import com.aut.shoomal.payment.transaction.PaymentTransaction;
import com.aut.shoomal.payment.transaction.PaymentTransactionManager;
import com.aut.shoomal.payment.transaction.PaymentTransactionStatus;
import com.aut.shoomal.payment.wallet.Wallet;
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
import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.food.FoodManager;
import com.aut.shoomal.payment.order.OrderItem;

public class RestaurantOrderHandler extends AbstractHttpHandler {

    private final RestaurantManager restaurantManager;
    private final OrderManager orderManager;
    protected final UserManager userManager;
    private final PaymentTransactionManager paymentTransactionManager;
    protected final BlacklistedTokenDao blacklistedTokenDao;
    private final FoodManager foodManager;

    private static final Pattern RESTAURANT_ID_ORDERS_PATH = Pattern.compile("/restaurants/(\\d+)/orders/?$");
    private static final Pattern RESTAURANT_ORDER_ID_PATH = Pattern.compile("/restaurants/orders/(\\d+)$");

    public RestaurantOrderHandler(RestaurantManager restaurantManager, OrderManager orderManager,
                                  UserManager userManager, BlacklistedTokenDao blacklistedTokenDao,
                                  PaymentTransactionManager paymentTransactionManager,
                                  FoodManager foodManager) {
        this.restaurantManager = restaurantManager;
        this.orderManager = orderManager;
        this.userManager = userManager;
        this.blacklistedTokenDao = blacklistedTokenDao;
        this.paymentTransactionManager = paymentTransactionManager;
        this.foodManager = foodManager;
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

        Restaurant restaurant = restaurantManager.findById((long) restaurantId);
        if (restaurant == null)
            throw new NotFoundException("Restaurant not found");

        List<Order> orders = orderManager.getAllOrders(search, restaurant.getName(), customerIdStr, courierIdStr, statusString);

        List<OrderResponse> orderResponses = orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());

        sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, orderResponses);
    }

    private void handleChangeOrderStatus(HttpExchange exchange, User authenticatedUser, int orderId) throws IOException {
        if (!checkContentType(exchange)) return;

        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Order order = session.get(Order.class, (long) orderId);
            if (order == null) {
                throw new NotFoundException("404 Not Found: Order with ID " + orderId + " not found.");
            }

            if (!restaurantManager.isOwner(order.getRestaurant().getId().intValue(), String.valueOf(authenticatedUser.getId()))) {
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

            OrderStatus internalOrderStatus = switch (RestaurantOrderStatus.fromValue(requestBody.getStatus())) {
                case ACCEPTED -> OrderStatus.WAITING_VENDOR;
                case REJECTED -> OrderStatus.CANCELLED;
                case SERVED -> OrderStatus.FINDING_COURIER;
            };

            boolean success = true;
            if (internalOrderStatus == OrderStatus.CANCELLED) {
                success = refundTransaction(exchange, session, order);
                if (success) {
                    for (OrderItem item : order.getOrderItems()) {
                        Food food = item.getFood();
                        food.setSupply(food.getSupply() + item.getQuantity());
                        foodManager.updateFood(food, session);
                    }
                }
            }

            if (success)
            {
                order.setOrderStatus(internalOrderStatus);
                session.merge(order);
                tx.commit();
            }
            else if (tx != null)
                tx.rollback();
            session.close();

            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Order status changed successfully."));

        } catch (NotFoundException e) {
            if (tx != null) tx.rollback();
            System.err.println(e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, e.getMessage()));
        } catch (ForbiddenException e) {
            if (tx != null) tx.rollback();
            System.err.println(e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_FORBIDDEN, new ApiResponse(false, e.getMessage()));
        } catch (InvalidInputException e) {
            if (tx != null) tx.rollback();
            System.err.println(e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("An unexpected error occurred during PATCH /restaurants/orders/" + orderId + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        } finally {
            if (session != null) session.close();
        }
    }

    private boolean refundTransaction(HttpExchange exchange, Session session, Order order) throws IOException
    {
        try {
            PaymentTransaction transaction = paymentTransactionManager.getByOrderId(session, order.getId());
            Wallet wallet = getWallet(order, transaction);
            transaction.setStatus(PaymentTransactionStatus.REFUNDED);
            session.merge(transaction);
            session.merge(wallet);
            return true;
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
            return false;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during refunding transaction: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, e.getMessage()));
            return false;
        }
    }

    private static Wallet getWallet(Order order, PaymentTransaction transaction)
    {
        if (transaction == null)
            throw new NotFoundException("404 Not Found: Transaction with order id " + order.getId() + " not found.");
        User user = transaction.getUser();
        if (user == null)
            throw new NotFoundException("404 Not Found: User with order id " + order.getId() + " and Transaction id " + transaction.getId() + " not found.");
        Wallet wallet = user.getWallet();
        if (wallet == null)
            throw new NotFoundException("Wallet not found.");

        wallet.deposit(transaction.getAmount());
        return wallet;
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

