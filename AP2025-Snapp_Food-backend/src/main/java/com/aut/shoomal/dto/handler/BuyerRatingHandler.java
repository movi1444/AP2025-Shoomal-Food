package com.aut.shoomal.dto.handler;

import com.aut.shoomal.Erfan.User;
import com.aut.shoomal.Erfan.UserManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.SubmitRatingRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.rating.Rating;
import com.aut.shoomal.rating.RatingManager;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.regex.Pattern;

public class BuyerRatingHandler extends AbstractHttpHandler
{
    private final Pattern RATINGS_BASE_PATTERN = Pattern.compile("/ratings/?");
    private final Pattern RATINGS_ID_PATTERN = Pattern.compile("/ratings/(\\d+)");
    private final Pattern RATINGS_ITEM_PATTERN = Pattern.compile("/ratings/items/(\\d+)");

    private final UserManager userManager;
    private final RatingManager ratingManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    public BuyerRatingHandler(UserManager userManager, RatingManager ratingManager,
                              BlacklistedTokenDao tokenDao)
    {
        this.userManager = userManager;
        this.ratingManager = ratingManager;
        this.blacklistedTokenDao = tokenDao;
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
            if (RATINGS_BASE_PATTERN.matcher(path).matches())
                submitRating(exchange);
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for POST."));
        }
        else if (method.equalsIgnoreCase("GET"))
        {
            if (RATINGS_ID_PATTERN.matcher(path).matches())
            {
                Optional<Integer> ratingId = extractIdFromPath(path, RATINGS_ID_PATTERN);
                if (ratingId.isPresent())
                    getRating(exchange, ratingId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid rating ID."));
            }
            else if (RATINGS_ITEM_PATTERN.matcher(path).matches())
            {
                Optional<Integer> itemId = extractIdFromPath(path, RATINGS_ITEM_PATTERN);
                if (itemId.isPresent())
                    getItemRatings(exchange, itemId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid item ID."));
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for GET."));
        }
        else if (method.equalsIgnoreCase("PUT"))
        {
            if (RATINGS_ID_PATTERN.matcher(path).matches())
            {
                Optional<Integer> ratingId = extractIdFromPath(path, RATINGS_ID_PATTERN);
                if (ratingId.isPresent())
                    updateRating(exchange, ratingId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid rating ID."));
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for PUT."));
        }
        else if (method.equalsIgnoreCase("DELETE"))
            if (RATINGS_ID_PATTERN.matcher(path).matches())
            {
                Optional<Integer> ratingId = extractIdFromPath(path, RATINGS_ID_PATTERN);
                if (ratingId.isPresent())
                    deleteRating(exchange, ratingId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid rating ID."));
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for DELETE."));
        else
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "405 Method Not Allowed."));
    }

    private void submitRating(HttpExchange exchange) throws IOException
    {
        try {
            SubmitRatingRequest request = parseRequestBody(exchange, SubmitRatingRequest.class);
            if (request == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            Long userId = authenticate(exchange, userManager, blacklistedTokenDao).getId();

            Rating rating = ratingManager.submitRating(
                    request.getOrderId(),
                    request.getRating(),
                    userId,
                    request.getComment(),
                    request.getImageBase64()
            );
            ratingManager.addRating(rating);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Rating submitted."));
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid input: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during POST /ratings: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void getItemRatings(HttpExchange exchange, Integer itemId)
    {

    }

    private void getRating(HttpExchange exchange, Integer ratingId)
    {

    }

    private void deleteRating(HttpExchange exchange, Integer ratingId)
    {

    }

    private void updateRating(HttpExchange exchange, Integer ratingId)
    {

    }
}