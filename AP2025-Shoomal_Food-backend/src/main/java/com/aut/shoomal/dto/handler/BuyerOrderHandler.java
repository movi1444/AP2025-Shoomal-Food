package com.aut.shoomal.dto.handler;

import com.aut.shoomal.dto.request.PaymentRequest;
import com.aut.shoomal.dto.request.WalletRequest;
import com.aut.shoomal.dto.response.TransactionResponse;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.OrderItemRequest;
import com.aut.shoomal.dto.request.SubmitOrderRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.PaymentMethod;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderItem;
import com.aut.shoomal.payment.order.OrderManager;
import com.aut.shoomal.payment.transaction.PaymentTransactionManager;
import com.aut.shoomal.payment.wallet.WalletManager;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class BuyerOrderHandler extends AbstractHttpHandler
{
    private static final Pattern ORDERS_BASE_PATTERN = Pattern.compile("/orders/?");
    private static final Pattern ORDERS_ID_PATTERN = Pattern.compile("/orders/(\\d+)");
    private static final Pattern ORDERS_HISTORY_PATTERN = Pattern.compile("/orders/history/?");
    private static final Pattern TRANSACTIONS_PATTERN = Pattern.compile("/transactions/?");
    private static final Pattern WALLET_PATTERN = Pattern.compile("/wallet/top-up/?");
    private static final Pattern PAYMENT_PATTERN = Pattern.compile("/payment/online/?");

    private final UserManager userManager;
    private final OrderManager orderManager;
    private final WalletManager walletManager;
    private final PaymentTransactionManager paymentManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    public BuyerOrderHandler(UserManager userManager, OrderManager orderManager, WalletManager walletManager,
                             PaymentTransactionManager paymentManager, BlacklistedTokenDao blacklistedTokenDao)
    {
        this.userManager = userManager;
        this.orderManager = orderManager;
        this.walletManager = walletManager;
        this.paymentManager = paymentManager;
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
        if (!checkUserRole(exchange, user, "buyer"))
            return;

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (method.equalsIgnoreCase("POST"))
        {
            if (ORDERS_BASE_PATTERN.matcher(path).matches())
                submitOrder(exchange, user.getId());
            else if (WALLET_PATTERN.matcher(path).matches())
                chargeWallet(exchange, user.getId());
            else if (PAYMENT_PATTERN.matcher(path).matches())
                onlinePayment(exchange, user.getId());
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for POST."));
        }
        else if (method.equalsIgnoreCase("GET"))
        {
            if (ORDERS_ID_PATTERN.matcher(path).matches())
            {
                Optional<Integer> orderId = extractIdFromPath(path, ORDERS_ID_PATTERN);
                if (orderId.isPresent())
                    getOrderById(exchange, orderId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid order ID."));
            }
            else if (ORDERS_HISTORY_PATTERN.matcher(path).matches())
                getOrdersHistory(exchange);
            else if (TRANSACTIONS_PATTERN.matcher(path).matches())
                getTransactions(exchange, user.getId());
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for GET."));
        }
        else
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "405 Method Not Allowed."));
    }

    private void submitOrder(HttpExchange exchange, Long customerId) throws IOException
    {
        try {
            SubmitOrderRequest requestBody = parseRequestBody(exchange, SubmitOrderRequest.class);
            if (requestBody == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            if (requestBody.getDeliveryAddress() == null || requestBody.getDeliveryAddress().trim().isEmpty())
                throw new InvalidInputException("Delivery address required.");
            if (requestBody.getVendorId() == null)
                throw new InvalidInputException("Vendor id required.");
            if (requestBody.getItems() == null)
                throw new InvalidInputException("Items required.");
            else if (requestBody.getItems().getFirst() == null || requestBody.getItems().getLast() == null)
                throw new InvalidInputException("Items required.");

            Order order = orderManager.submitOrder(
                    customerId,
                    Long.valueOf(requestBody.getVendorId()),
                    (requestBody.getCouponId() != null) ? requestBody.getCouponId() : null,
                    requestBody.getDeliveryAddress(),
                    requestBody.getItems()
            );

            OrderResponse response = this.createResponse(order);
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body.");
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid input: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found:" + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during POST /orders: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void getOrderById(HttpExchange exchange, Integer orderId) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Order order = session.get(Order.class, orderId);
            if (order == null)
                throw new NotFoundException("Order with ID " + orderId + " not found.");
            OrderResponse response = createResponse(order);
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body.");
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (NotFoundException e) {
            System.err.println("404 Not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /orders/" + orderId);
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error during GET /orders/" + orderId));
        }
    }

    private void getOrdersHistory(HttpExchange exchange) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long customerId = authenticate(exchange, userManager, blacklistedTokenDao).getId();
            Map<String, String> queryParams = parseQueryParams(exchange);
            String search = queryParams.get("search");
            String vendorName = queryParams.get("vendor");
            List<Order> orders = orderManager.getOrderHistory(session, customerId, search, vendorName);

            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::createResponse)
                    .toList();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, orderResponses);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /orders/history");
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void getTransactions(HttpExchange exchange, Long userId) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, userId);
            if (user == null)
                throw new NotFoundException("User with ID " + userId + " not found.");

            List<TransactionResponse> responses = user.getTransactions().stream()
                    .map(transaction -> new TransactionResponse(
                            transaction.getId(),
                            transaction.getAmount(),
                            transaction.getStatus().getStatus(),
                            transaction.getTransactionTime().toString(),
                            transaction.getMethod().getName(),
                            (transaction.getOrder() != null) ? transaction.getOrder().getId() : null,
                            transaction.getUser().getId()
                    ))
                    .toList();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responses);
        } catch (NotFoundException e) {
            System.err.println("404 Not found:" + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /transactions: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void chargeWallet(HttpExchange exchange, Long userId) throws IOException
    {
        try {
            WalletRequest request = parseRequestBody(exchange, WalletRequest.class);
            if (request == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            if (request.getAmount() == null)
                throw new InvalidInputException("amount is required.");
            if (request.getMethod() == null || request.getMethod().trim().isEmpty())
                throw new InvalidInputException("method is required.");

            walletManager.depositWallet(userId, request.getAmount(), request.getMethod());
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Wallet topped up successfully."));
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body. " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid request: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found:" + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during POST /wallet/top-up: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void onlinePayment(HttpExchange exchange, Long userId) throws IOException
    {
        try {
            PaymentRequest request = parseRequestBody(exchange, PaymentRequest.class);
            if (request == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            if (request.getMethod() == null || request.getMethod().trim().isEmpty())
                throw new InvalidInputException("method is required.");
            if (request.getOrderId() == null || request.getOrderId().trim().isEmpty())
                throw new InvalidInputException("orderId is required.");

            Order order = orderManager.findOrderById(Integer.parseInt(request.getOrderId()));
            if (order == null)
                throw new NotFoundException("Order with ID " + request.getOrderId() + " not found.");

            PaymentMethod paymentMethod;
            try {
                paymentMethod = PaymentMethod.fromName(request.getMethod());
            } catch (IllegalArgumentException e) {
                throw new InvalidInputException("Invalid payment method: " + request.getMethod());
            }

            int parsedOrderId;
            try {
                parsedOrderId = Integer.parseInt(request.getOrderId());
            } catch (NumberFormatException e) {
                throw new InvalidInputException("Invalid order ID: " + request.getOrderId());
            }

            String redirectUrl;
            if (paymentMethod == PaymentMethod.WALLET)
            {
                walletManager.processWalletPaymentForOrder(userId, parsedOrderId);
                sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Payment successful."));
            }
            else if (paymentMethod == PaymentMethod.PAYWALL)
            {
                redirectUrl = paymentManager.processExternalPayment(userId, parsedOrderId, paymentMethod);
                if (redirectUrl == null || redirectUrl.trim().isEmpty())
                    throw new RuntimeException("Failed to get redirect URL for external payment.");
                sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Payment successful."));
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Unsupported payment method: " + request.getMethod()));
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body. " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid request: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found:" + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during POST /payment/online: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private OrderResponse createResponse(Order order)
    {
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
}