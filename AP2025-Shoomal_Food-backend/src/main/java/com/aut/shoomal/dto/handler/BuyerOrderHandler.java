package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.OrderItemRequest;
import com.aut.shoomal.dto.request.SubmitOrderRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.OrderResponse;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.Order;
import com.aut.shoomal.payment.OrderItem;
import com.aut.shoomal.payment.OrderManager;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
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
    private final BlacklistedTokenDao blacklistedTokenDao;
    public BuyerOrderHandler(UserManager userManager, OrderManager orderManager,
                             BlacklistedTokenDao blacklistedTokenDao)
    {
        this.userManager = userManager;
        this.orderManager = orderManager;
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
                chargeWallet(exchange);
            else if (PAYMENT_PATTERN.matcher(path).matches())
                onlinePayment(exchange);
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
                getTransactions(exchange);
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

            Order order = orderManager.submitOrder(
                    customerId,
                    Long.valueOf(requestBody.getVendorId()),
                    (requestBody.getCouponId() != null) ? requestBody.getCouponId() : null,
                    requestBody.getDeliveryAddress(),
                    requestBody.getItems()
            );

            OrderResponse response = new OrderResponse(
                    order.getId(),
                    order.getDeliveryAddress(),
                    Math.toIntExact(customerId),
                    requestBody.getVendorId(),
                    (order.getCourier() != null) ? Math.toIntExact(order.getCourier().getId()) : null,
                    (order.getCoupon() != null) ? order.getCoupon().getId() : null,
                    requestBody.getItems().stream().map(OrderItemRequest::getItemId).toList(),
                    order.getRawPrice(),
                    order.getTaxFee(),
                    order.getCourierFee(),
                    order.getPayPrice(),
                    order.getOrderStatus().getName(),
                    order.getCreatedAt().toString(),
                    order.getUpdatedAt().toString()
            );
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body.");
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid input:" + e.getMessage());
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
            System.err.println("404 Not found:" + e.getMessage());
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

    private void getTransactions(HttpExchange exchange) throws IOException
    {

    }

    private void chargeWallet(HttpExchange exchange) throws IOException
    {

    }

    private void onlinePayment(HttpExchange exchange) throws IOException
    {

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
                order.getOrderItems().stream().map(OrderItem::getId).toList(),
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