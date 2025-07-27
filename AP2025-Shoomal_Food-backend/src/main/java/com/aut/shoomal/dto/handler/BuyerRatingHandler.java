package com.aut.shoomal.dto.handler;

import com.aut.shoomal.dto.request.UpdateRatingRequest;
import com.aut.shoomal.dto.response.ItemRatingResponse;
import com.aut.shoomal.dto.response.RatingResponse;
import com.aut.shoomal.dto.response.UpdateRatingResponse;
import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.SubmitRatingRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderItem;
import com.aut.shoomal.rating.Rating;
import com.aut.shoomal.rating.RatingManager;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BuyerRatingHandler extends AbstractHttpHandler
{
    private final Pattern RATINGS_BASE_PATTERN = Pattern.compile("/ratings/?");
    private final Pattern RATINGS_ID_PATTERN = Pattern.compile("/ratings/(\\d+)");
    private final Pattern RATINGS_ITEM_PATTERN = Pattern.compile("/ratings/items/(\\d+)");

    private final UserManager userManager;
    private final RatingManager ratingManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    public BuyerRatingHandler(UserManager userManager, RatingManager ratingManager, BlacklistedTokenDao tokenDao)
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
                Optional<Integer> orderId = extractIdFromPath(path, RATINGS_ID_PATTERN);
                if (orderId.isPresent())
                    getRatingsByOrderId(exchange, user.getId(), orderId.get());
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
                Optional<Integer> orderId = extractIdFromPath(path, RATINGS_ID_PATTERN);
                if (orderId.isPresent())
                    updateRating(exchange, user.getId(), orderId.get());
                else
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid rating ID."));
            }
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for PUT."));
        }
        else if (method.equalsIgnoreCase("DELETE"))
            if (RATINGS_ID_PATTERN.matcher(path).matches())
            {
                Optional<Integer> orderId = extractIdFromPath(path, RATINGS_ID_PATTERN);
                if (orderId.isPresent())
                    deleteRating(exchange, user.getId(), orderId.get());
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
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            SubmitRatingRequest request = parseRequestBody(exchange, SubmitRatingRequest.class);
            if (request == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
            Long userId = authenticate(exchange, userManager, blacklistedTokenDao).getId();

            Set<Long> foodIds = new HashSet<>();
            if (request.getOrderId() != null)
            {
                Order order = session.get(Order.class, request.getOrderId());
                if (order == null)
                    throw new NotFoundException("Order with id " + request.getOrderId() + " not found.");
                for (OrderItem item : order.getOrderItems())
                    foodIds.add(item.getFood().getId());
            }

            for (Long foodId : foodIds)
            {
                Rating rating = ratingManager.submitRating(
                        request.getOrderId(),
                        request.getRating(),
                        userId,
                        request.getComment(),
                        request.getImageBase64()
                );
                if (ratingManager.checkConflict(session, userId, foodId, request.getOrderId()))
                    throw new ConflictException("Rating already submitted for this order.");
                Food food = session.get(Food.class, foodId);
                food.addRating(rating);
                ratingManager.addRating(rating, session);
                session.merge(food);
            }
            transaction.commit();
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Rating submitted."));
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body. " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            if (transaction != null)
                transaction.rollback();
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid input: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
            if (transaction != null)
                transaction.rollback();
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
            if (transaction != null)
                transaction.rollback();
        } catch (ConflictException e) {
            System.err.println("409 Conflict: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_CONFLICT, new ApiResponse(false, "409 Conflict: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during POST /ratings: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
            if (transaction != null)
                transaction.rollback();
        }
    }

    private void getItemRatings(HttpExchange exchange, Integer itemId) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Food food = session.get(Food.class, Long.valueOf(itemId));
            if (food == null)
                throw new NotFoundException("Item with id " + itemId + " not found.");

            BigDecimal avgRating = food.calculateAverageRating();
            List<RatingResponse> comments = food.getRatings().stream()
                    .map(this::createRatingResponse)
                    .toList();
            ItemRatingResponse response = new ItemRatingResponse(avgRating, comments);
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /ratings/items/" + itemId + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void getRatingsByOrderId(HttpExchange exchange, Long userId, Integer orderId) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Rating> ratings = ratingManager.getByOrderId(session, userId, orderId);
            if (ratings == null)
                throw new NotFoundException("No rating with order id " + orderId + " found.");
            List<RatingResponse> responses = ratings.stream()
                            .map(this::createRatingResponse)
                            .toList();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responses);
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /ratings/" + orderId + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private void deleteRating(HttpExchange exchange, Long userId, Integer orderId) throws IOException
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<Rating> ratings = ratingManager.getByOrderId(session, userId, orderId);
            if (ratings == null || ratings.isEmpty())
                throw new NotFoundException("Rating with order id " + orderId + " not found.");
            for (Rating rating : ratings)
            {
                Integer id = rating.getId();
                ratingManager.deleteRating(session, id);
            }
            transaction.commit();
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Rating deleted."));
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found: " + e.getMessage()));
            if (transaction != null)
                transaction.rollback();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during DELETE /ratings/" + orderId + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
            if (transaction != null)
                transaction.rollback();
        }
    }

    private void updateRating(HttpExchange exchange, Long userId, Integer orderId) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            UpdateRatingRequest request = parseRequestBody(exchange, UpdateRatingRequest.class);
            if (request == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }

            List<Rating> ratings = ratingManager.getByOrderId(session, userId, orderId);
            if (ratings == null)
                throw new NotFoundException("Rating not found.");

            List<UpdateRatingResponse> responses = new ArrayList<>();
            for (Rating rating : ratings)
            {
                Integer ratingId = rating.getId();
                ratingManager.updateRating(session, ratingId, request.getRating(), request.getComment(), request.getImageBase64());
                UpdateRatingResponse response = new UpdateRatingResponse(
                        rating.getId(),
                        (rating.getOrder() != null) ? rating.getOrder().getId() : null,
                        rating.getRating(),
                        rating.getComment(),
                        Math.toIntExact(rating.getUser().getId()),
                        rating.getCreatedAt().toString()
                );
                responses.add(response);
            }

            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responses);
        } catch (IOException e) {
            System.err.println("Error parsing request body: Malformed JSON in request body. " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (InvalidInputException e) {
            System.err.println("400 Invalid input: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        } catch (NotFoundException e) {
            System.err.println("404 Resource not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found."));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during PUT /ratings/" + orderId + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error."));
        }
    }

    private RatingResponse createRatingResponse(Rating rating)
    {
        return new RatingResponse(
                rating.getId(),
                rating.getOrder().getId(),
                rating.getRating(),
                rating.getComment(),
                rating.getImageBase64(),
                Math.toIntExact(rating.getUser().getId()),
                rating.getCreatedAt().toString()
        );
    }
}