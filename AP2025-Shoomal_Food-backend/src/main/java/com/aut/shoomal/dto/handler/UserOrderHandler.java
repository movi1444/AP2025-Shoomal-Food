package com.aut.shoomal.dto.handler;

import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserOrderHandler extends AbstractHttpHandler
{
    private static final Pattern CUSTOMER_WITH_ORDER_PATTERN = Pattern.compile("/order/customer/(\\d+)");
    private static final Pattern COURIER_WITH_ORDER_PATTERN = Pattern.compile("/order/courier/(\\d+)");

    private final UserManager userManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    public UserOrderHandler(UserManager userManager, BlacklistedTokenDao blacklistedTokenDao)
    {
        this.userManager = userManager;
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

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equalsIgnoreCase("GET"))
        {
            if (CUSTOMER_WITH_ORDER_PATTERN.matcher(path).matches()) {
                Optional<Integer> restaurantId = extractIdFromPath(path, CUSTOMER_WITH_ORDER_PATTERN);
                if (restaurantId.isPresent())
                    getCustomersWithOrder(exchange, (long) restaurantId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid restaurant ID."));
            } else if (COURIER_WITH_ORDER_PATTERN.matcher(path).matches()) {
                Optional<Integer> restaurantId = extractIdFromPath(path, COURIER_WITH_ORDER_PATTERN);
                if (restaurantId.isPresent())
                    getCouriersWithOrder(exchange, (long) restaurantId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid restaurant ID."));
            }
        }
    }

    private void getCustomersWithOrder(HttpExchange exchange, Long restaurant) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> customers = userManager.findCustomersWithOrder(session, restaurant);
            if (customers == null)
                throw new NotFoundException("No customers found for restaurant " + restaurant);

            List<String> names = customers.stream().map(User::getName).toList();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, names);
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("500 Internal Server Error: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: " + e.getMessage()));
        }
    }

    private void getCouriersWithOrder(HttpExchange exchange, Long restaurant) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> couriers = userManager.findCouriersWithOrder(session, restaurant);
            if (couriers == null)
                throw new NotFoundException("No customers found for restaurant " + restaurant);

            List<String> names = couriers.stream().map(User::getName).toList();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, names);
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("500 Internal Server Error: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: " + e.getMessage()));
        }
    }
}