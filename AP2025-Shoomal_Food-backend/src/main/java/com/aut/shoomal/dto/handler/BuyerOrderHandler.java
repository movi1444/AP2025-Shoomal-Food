package com.aut.shoomal.dto.handler;

import com.aut.shoomal.dto.request.PaymentRequest;
import com.aut.shoomal.dto.request.WalletRequest;
import com.aut.shoomal.dto.response.TransactionResponse;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.SubmitOrderRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.PaymentMethod;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderManager;
import com.aut.shoomal.payment.transaction.PaymentTransaction;
import com.aut.shoomal.payment.transaction.PaymentTransactionManager;
import com.aut.shoomal.payment.wallet.Wallet;
import com.aut.shoomal.payment.wallet.WalletManager;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.aut.shoomal.entity.cart.CartManager;
import com.aut.shoomal.dto.request.AddItemToCartRequest;
import com.aut.shoomal.dto.request.RemoveItemFromCartRequest;
import com.aut.shoomal.dto.response.CartResponse;
import com.aut.shoomal.dto.response.CartItemResponse;
import com.aut.shoomal.entity.cart.Cart;
import com.aut.shoomal.payment.coupon.Coupon;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BuyerOrderHandler extends AbstractHttpHandler
{
    private static final Pattern ORDERS_BASE_PATTERN = Pattern.compile("/orders/?");
    private static final Pattern ORDERS_ID_PATTERN = Pattern.compile("/orders/(\\d+)");
    private static final Pattern ORDERS_HISTORY_PATTERN = Pattern.compile("/orders/history/?");
    private static final Pattern TRANSACTIONS_PATTERN = Pattern.compile("/transactions/?");
    private static final Pattern WALLET_PATTERN = Pattern.compile("/wallet/top-up/?");
    private static final Pattern WALLET_AMOUNT_PATTERN = Pattern.compile("/wallet/amount/?");
    private static final Pattern PAYMENT_PATTERN = Pattern.compile("/payment/online/?");

    private static final Pattern ADD_TO_CART_PATTERN = Pattern.compile("/cart/add/?");
    private static final Pattern REMOVE_FROM_CART_PATTERN = Pattern.compile("/cart/remove/?");
    private static final Pattern GET_CART_PATTERN = Pattern.compile("/cart/(\\d+)/(\\d+)/?");
    private static final Pattern CLEAR_CART_PATTERN = Pattern.compile("/cart/clear/(\\d+)/(\\d+)/?");


    private final UserManager userManager;
    private final OrderManager orderManager;
    private final WalletManager walletManager;
    private final PaymentTransactionManager paymentManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    private final CartManager cartManager;

    public BuyerOrderHandler(UserManager userManager, OrderManager orderManager, WalletManager walletManager,
                             PaymentTransactionManager paymentManager, BlacklistedTokenDao blacklistedTokenDao, CartManager cartManager)
    {
        this.userManager = userManager;
        this.orderManager = orderManager;
        this.walletManager = walletManager;
        this.paymentManager = paymentManager;
        this.blacklistedTokenDao = blacklistedTokenDao;
        this.cartManager = cartManager;
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
            else if (ADD_TO_CART_PATTERN.matcher(path).matches())
                addItemToCart(exchange, user.getId());
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
            else if (WALLET_AMOUNT_PATTERN.matcher(path).matches())
                getWalletAmount(exchange, user.getId());
            else if (GET_CART_PATTERN.matcher(path).matches()) {
                Matcher matcher = GET_CART_PATTERN.matcher(path);
                if (matcher.find()) {
                    Long userId = Long.parseLong(matcher.group(1));
                    Long restaurantId = Long.parseLong(matcher.group(2));
                    getCart(exchange, userId, restaurantId);
                } else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid cart path."));
                }
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for GET."));
        }
        else if (method.equalsIgnoreCase("DELETE")) {
            if (REMOVE_FROM_CART_PATTERN.matcher(path).matches())
                removeItemFromCart(exchange, user.getId());
            else if (CLEAR_CART_PATTERN.matcher(path).matches()) {
                Matcher matcher = CLEAR_CART_PATTERN.matcher(path);
                if (matcher.find()) {
                    Long userId = Long.parseLong(matcher.group(1));
                    Long restaurantId = Long.parseLong(matcher.group(2));
                    clearCart(exchange, userId, restaurantId);
                } else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid clear cart path."));
                }
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for DELETE."));
        }
        else
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "405 Method Not Allowed."));
    }

    private void getWalletAmount(HttpExchange exchange, Long id) throws IOException
    {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = session.get(User.class, id);
            if (user == null)
                throw new NotFoundException("User with id " + id + " not found.");
            Wallet wallet = user.getWallet();
            if (wallet == null)
                throw new NotFoundException("Wallet with user id " + id + " not found.");
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, wallet.getBalance());
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found."));
        } catch (Exception e) {
            System.err.println("500 Internal Server Error: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: " + e.getMessage()));
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private void submitOrder(HttpExchange exchange, Long customerId) throws IOException
    {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
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
            else if (requestBody.getItems().isEmpty())
                throw new InvalidInputException("Items list cannot be empty.");

            Order order = orderManager.submitOrder(
                    customerId,
                    Long.valueOf(requestBody.getVendorId()),
                    (requestBody.getCouponId() != null) ? requestBody.getCouponId() : null,
                    requestBody.getDeliveryAddress(),
                    requestBody.getItems()
            );

            if (requestBody.getCouponId() != null) {
                Coupon coupon = session.get(Coupon.class, requestBody.getCouponId());
                if (coupon == null) {
                    throw new NotFoundException("Coupon with ID " + requestBody.getCouponId() + " not found.");
                }
                if (coupon.getUserCount() != null && coupon.getUserCount() > 0) {
                    coupon.setUserCount(coupon.getUserCount() - 1);
                    session.merge(coupon);
                } else {
                    throw new InvalidInputException("Coupon " + requestBody.getCouponId() + " has no remaining uses.");
                }
            }
            Cart cart = cartManager.getCartByUserIdAndRestaurantId(customerId, (long) requestBody.getVendorId());
            if (cart == null)
                throw new NotFoundException("Cart not found.");
            session.remove(cart);

            transaction.commit();
            OrderResponse response = this.createOrderResponse(order);
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid input: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during POST /orders: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }

    private void getOrderById(HttpExchange exchange, Integer orderId) throws IOException
    {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Order order = session.get(Order.class, orderId);
            if (order == null)
                throw new NotFoundException("Order with ID " + orderId + " not found.");
            OrderResponse response = createOrderResponse(order);
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (NotFoundException e) {
            System.err.println("404 Not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /orders/" + orderId + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error during GET /orders/" + orderId));
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private void getOrdersHistory(HttpExchange exchange) throws IOException
    {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Long customerId = authenticate(exchange, userManager, blacklistedTokenDao).getId();
            Map<String, String> queryParams = parseQueryParams(exchange);
            String search = queryParams.get("search");
            String vendorName = queryParams.get("vendor");
            List<Order> orders = orderManager.getOrderHistory(session, customerId, search, vendorName);

            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::createOrderResponse)
                    .toList();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, orderResponses);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /orders/history: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private void getTransactions(HttpExchange exchange, Long userId) throws IOException
    {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = session.get(User.class, userId);
            if (user == null)
                throw new NotFoundException("User with ID " + userId + " not found.");

            List<TransactionResponse> responses = user.getTransactions().stream()
                    .map(this::createTransactionResponse)
                    .toList();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responses);
        } catch (NotFoundException e) {
            System.err.println("404 Not found:" + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /transactions: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private void chargeWallet(HttpExchange exchange, Long userId) throws IOException
    {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            WalletRequest request = parseRequestBody(exchange, WalletRequest.class);
            if (request == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            if (request.getAmount() == null)
                throw new InvalidInputException("amount is required.");

            walletManager.depositWallet(session, userId, request.getAmount());
            transaction.commit();
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Wallet topped up successfully."));
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body. " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid request: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during POST /wallet/top-up: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }

    private void onlinePayment(HttpExchange exchange, Long userId) throws IOException
    {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            PaymentRequest request = parseRequestBody(exchange, PaymentRequest.class);
            if (request == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            if (request.getMethod() == null || request.getMethod().trim().isEmpty())
                throw new InvalidInputException("method is required.");
            if (request.getOrderId() == null)
                throw new InvalidInputException("orderId is required.");

            Order order = session.get(Order.class, request.getOrderId());
            if (order == null)
                throw new NotFoundException("Order with ID " + request.getOrderId() + " not found.");
            if (!Objects.equals(order.getCustomer().getId(), userId))
                throw new NotFoundException("Order with ID " + request.getOrderId() + " is not owner of user with ID " + userId);

            PaymentMethod paymentMethod;
            try {
                paymentMethod = PaymentMethod.fromName(request.getMethod());
            } catch (IllegalArgumentException e) {
                throw new InvalidInputException("Invalid payment method: " + request.getMethod());
            }

            String redirectUrl;
            if (paymentMethod == PaymentMethod.WALLET) {
                walletManager.processWalletPaymentForOrder(session, userId, request.getOrderId());
            }
            else if (paymentMethod == PaymentMethod.ONLINE)
            {
                redirectUrl = paymentManager.processExternalPayment(session, userId, request.getOrderId(), paymentMethod);
                if (redirectUrl == null || redirectUrl.trim().isEmpty())
                    throw new RuntimeException("Failed to get redirect URL for external payment.");
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Unsupported payment method: " + request.getMethod()));

            PaymentTransaction paymentTransaction = paymentManager.getByOrderId(session, order.getId());
            if (paymentTransaction == null)
                throw new NotFoundException("Payment transaction with order id " + order.getId() + " not found.");
            TransactionResponse response = this.createTransactionResponse(paymentTransaction);
            transaction.commit();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body. " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid request: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during POST /payment/online: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
            if (transaction != null && transaction.isActive())
                transaction.rollback();
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }

    private void addItemToCart(HttpExchange exchange, Long userId) throws IOException {
        try {
            AddItemToCartRequest requestBody = parseRequestBody(exchange, AddItemToCartRequest.class);
            if (requestBody == null) {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            if (requestBody.getRestaurantId() == null || requestBody.getFoodItemId() == null || requestBody.getQuantity() == null) {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: restaurant_id, food_item_id, and quantity are required."));
                return;
            }

            Cart cart = cartManager.addItemToCart(userId, requestBody.getRestaurantId(), requestBody.getFoodItemId(), requestBody.getQuantity());
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, convertToCartResponse(cart));
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body. " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid input: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        }  catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during POST /cart/add: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void removeItemFromCart(HttpExchange exchange, Long userId) throws IOException {
        try {
            RemoveItemFromCartRequest requestBody = parseRequestBody(exchange, RemoveItemFromCartRequest.class);
            if (requestBody == null) {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            if (requestBody.getRestaurantId() == null || requestBody.getFoodItemId() == null) {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: restaurant_id and food_item_id are required."));
                return;
            }

            cartManager.removeCartItem(userId, requestBody.getRestaurantId(), requestBody.getFoodItemId());
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Item removed from cart successfully."));
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body. " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during DELETE /cart/remove: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void getCart(HttpExchange exchange, Long userId, Long restaurantId) throws IOException {
        try {
            Cart cart = cartManager.getCartByUserIdAndRestaurantId(userId, restaurantId);
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, convertToCartResponse(cart));
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /cart/{userId}/{restaurantId}: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void clearCart(HttpExchange exchange, Long userId, Long restaurantId) throws IOException {
        try {
            cartManager.clearCart(userId, restaurantId);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Cart cleared successfully."));
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during DELETE /cart/clear/{userId}/{restaurantId}: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }


    private OrderResponse createOrderResponse(com.aut.shoomal.payment.order.Order order)
    {
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

    private TransactionResponse createTransactionResponse(PaymentTransaction transaction)
    {
        return new TransactionResponse(
                Math.toIntExact(transaction.getId()),
                transaction.getStatus().getStatus(),
                transaction.getMethod().getName(),
                (transaction.getOrder() != null) ? transaction.getOrder().getId() : null,
                Math.toIntExact(transaction.getUser().getId()),
                transaction.getTransactionTime().toString(),
                transaction.getAmount()
        );
    }

    private CartResponse convertToCartResponse(Cart cart) {
        if (cart == null) return null;

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getFoodItem().getId(),
                        item.getFoodItem().getName(),
                        item.getQuantity(),
                        (int)item.getFoodItem().getPrice(),
                        (int)(item.getQuantity() * item.getFoodItem().getPrice())
                ))
                .collect(Collectors.toList());

        int totalPrice = itemResponses.stream()
                .mapToInt(CartItemResponse::getItemTotalPrice)
                .sum();

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getRestaurant().getId(),
                itemResponses,
                totalPrice
        );
    }
}