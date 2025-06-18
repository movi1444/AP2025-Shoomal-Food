package com.aut.shoomal.dto.handler;

import com.aut.shoomal.Erfan.User;
import com.aut.shoomal.Erfan.UserManager;
import com.aut.shoomal.Mamad.restaurant.Restaurant;
import com.aut.shoomal.Mamad.restaurant.RestaurantManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class BuyerFavoriteHandler extends AbstractHttpHandler
{
    private final Pattern FAVORITES_BASE_PATTERN = Pattern.compile("/favorites/?");
    private final Pattern FAVORITES_ID_PATTERN = Pattern.compile("/favorites/(\\d+)");

    private final UserManager userManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    public BuyerFavoriteHandler(UserManager userManager, BlacklistedTokenDao blacklistedTokenDao)
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
        if (!checkUserRole(exchange, user, "buyer"))
            return;

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (method.equalsIgnoreCase("GET"))
        {
            if (FAVORITES_BASE_PATTERN.matcher(path).matches())
                getFavoriteRestaurants(exchange, user.getId());
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for GET."));
        }
        else if (method.equalsIgnoreCase("PUT"))
        {
            if (FAVORITES_ID_PATTERN.matcher(path).matches())
            {
                Optional<Integer> restaurantId = extractIdFromPath(path, FAVORITES_ID_PATTERN);
                if (restaurantId.isPresent())
                    addRestaurantToFavorite(exchange, user.getId(), restaurantId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid ID."));
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for PUT."));
        }
        else if (method.equalsIgnoreCase("DELETE"))
        {
            if (FAVORITES_ID_PATTERN.matcher(path).matches())
            {
                Optional<Integer> restaurantId = extractIdFromPath(path, FAVORITES_ID_PATTERN);
                if (restaurantId.isPresent())
                    deleteRestaurantFromFavorite(exchange, user.getId(), restaurantId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid ID."));
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for DELETE."));
        }
        else
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "405 Method Not Allowed."));
    }

    private void getFavoriteRestaurants(HttpExchange exchange, Long customerId) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, customerId);
            if (user == null)
                throw new NotFoundException("User with id " + customerId + " not found.");
            List<RestaurantResponse> responses = user.getFavorites().stream()
                    .map(restaurant -> new RestaurantResponse(
                            restaurant.getId(),
                            restaurant.getName(),
                            restaurant.getAddress(),
                            restaurant.getPhone(),
                            restaurant.getLogoBase64()
                    ))
                    .toList();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responses);
        } catch (NotFoundException e) {
            System.err.println("404 Not Found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Not Found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /favorites: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void addRestaurantToFavorite(HttpExchange exchange, Long customerId, Integer restaurantId) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, Long.valueOf(restaurantId));
            if (restaurant == null)
                throw new NotFoundException("Restaurant with id " + restaurantId + " not found.");
            User user = session.get(User.class, customerId);
            if (user == null)
                throw new NotFoundException("User with id " + customerId + " not found.");
            user.addFavorite(restaurant);
            session.merge(user);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Added to favorites."));
        } catch (NotFoundException e) {
            System.err.println("404 Not Found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Not Found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during PUT /favorites: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void deleteRestaurantFromFavorite(HttpExchange exchange, Long customerId, Integer restaurantId) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, Long.valueOf(restaurantId));
            if (restaurant == null)
                throw new NotFoundException("Restaurant with id " + restaurantId + " not found.");
            User user = session.get(User.class, customerId);
            if (user == null)
                throw new NotFoundException("User with id " + customerId + " not found.");
            user.removeFavorite(restaurant);
            session.merge(user);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 removed from favorites."));
        } catch (NotFoundException e) {
            System.err.println("404 Not Found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Not Found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during DELETE /favorites: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }
}