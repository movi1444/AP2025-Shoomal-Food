package com.aut.shoomal.dto.handler;

import com.aut.shoomal.dto.request.CreateCouponRequest;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.CouponResponse;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.InvalidCouponException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.exceptions.ServiceUnavailableException;
import com.aut.shoomal.payment.coupon.Coupon;
import com.aut.shoomal.payment.coupon.CouponManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.regex.Pattern;

public class AdminDiscountHandler extends AbstractHttpHandler {

    private final UserManager userManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    private final CouponManager couponManager;

    private static final Pattern DISCOUNT_ID_PATH = Pattern.compile("^/admin/discounts/(\\d+)$");

    public AdminDiscountHandler(UserManager userManager, BlacklistedTokenDao blacklistedTokenDao, CouponManager couponManager) {
        this.userManager = userManager;
        this.blacklistedTokenDao = blacklistedTokenDao;
        this.couponManager = couponManager;
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

            if (path.equals("/admin/discounts")) {
                if (method.equalsIgnoreCase("POST")) {
                    handleCreateCoupon(exchange);
                } else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD,
                            new ApiResponse(false, "Method Not Allowed. Expected POST"));
                }
            }
            else if (DISCOUNT_ID_PATH.matcher(path).matches()) {
                Optional<Integer> couponIdOptional = extractIdFromPath(path, DISCOUNT_ID_PATH);
                if (couponIdOptional.isEmpty()) {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST,
                            new ApiResponse(false, "Invalid discount ID in path."));
                    return;
                }
                int couponId = couponIdOptional.get();

                if (method.equalsIgnoreCase("DELETE")) {
                    handleDeleteCoupon(exchange, couponId);
                } else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD,
                            new ApiResponse(false, "Method Not Allowed for /admin/discounts/{id}. Expected DELETE"));
                }
            }
            else {
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND,
                        new ApiResponse(false, "Discount resource not found"));
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
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        } finally {
            exchange.close();
        }
    }

    private void handleCreateCoupon(HttpExchange exchange) throws IOException {
        if (!checkHttpMethod(exchange, "POST")) return;
        if (!checkContentType(exchange)) return;

        CreateCouponRequest requestBody;
        try {
            requestBody = parseRequestBody(exchange, CreateCouponRequest.class);
            if (requestBody == null) {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST,
                        new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
            return;
        }

        try {
            Coupon newCoupon = couponManager.createCoupon(
                    requestBody.getCouponCode(),
                    requestBody.getType(),
                    requestBody.getValue(),
                    requestBody.getStartDate(),
                    requestBody.getEndDate(),
                    requestBody.getUserCount(),
                    requestBody.getMinPrice()
            );
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_CREATED, convertToCouponResponse(newCoupon));
        } catch (InvalidCouponException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        } catch (ServiceUnavailableException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        }
    }

    private void handleDeleteCoupon(HttpExchange exchange, int couponId) throws IOException {
        if (!checkHttpMethod(exchange, "DELETE")) return;

        try {
            Coupon existingCoupon = couponManager.getCouponById(couponId);
            if (existingCoupon == null) {
                throw new NotFoundException("404 Not Found: Coupon with ID " + couponId + " not found.");
            }

            couponManager.deleteCoupon(couponId);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "200 Coupon deleted successfully."));
        } catch (NotFoundException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        }
    }

    private CouponResponse convertToCouponResponse(Coupon coupon) {
        if (coupon == null)
            return null;
        return new CouponResponse(
                coupon.getId(),
                coupon.getCouponCode(),
                coupon.getCouponType().getName(),
                coupon.getValue(),
                coupon.getMinPrice(),
                coupon.getUserCount(),
                coupon.getStartDate(),
                coupon.getEndDate()
        );
    }

    private boolean isAdmin(User user) {
        return user.getRole().getName().equalsIgnoreCase("Admin");
    }
}