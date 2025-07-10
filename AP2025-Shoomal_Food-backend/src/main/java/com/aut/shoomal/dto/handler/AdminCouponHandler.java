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

public class AdminCouponHandler extends AbstractHttpHandler {

    private final UserManager userManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    private final CouponManager couponManager;

    private static final Pattern COUPON_ID_PATH = Pattern.compile("^/admin/coupons/(\\d+)$");

    public AdminCouponHandler(UserManager userManager, BlacklistedTokenDao blacklistedTokenDao, CouponManager couponManager) {
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

            if (path.equals("/admin/coupons")) {
                if (method.equalsIgnoreCase("POST")) {
                    handleCreateCoupon(exchange);
                } else if (method.equalsIgnoreCase("GET")) {
                    handleListAllCoupons(exchange);
                }
                else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD,
                            new ApiResponse(false, "Method Not Allowed. Expected POST or GET"));
                }
            }
            else if (COUPON_ID_PATH.matcher(path).matches()) {
                Optional<Integer> couponIdOptional = extractIdFromPath(path, COUPON_ID_PATH);
                if (couponIdOptional.isEmpty()) {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST,
                            new ApiResponse(false, "Invalid coupon ID in path."));
                    return;
                }
                int couponId = couponIdOptional.get();

                if (method.equalsIgnoreCase("DELETE")) {
                    handleDeleteCoupon(exchange, couponId);
                } else if (method.equalsIgnoreCase("PUT")) {
                    handleUpdateCoupon(exchange, couponId);
                } else if (method.equalsIgnoreCase("GET")) {
                    handleGetCouponDetails(exchange, couponId);
                }
                else {
                    sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD,
                            new ApiResponse(false, "Method Not Allowed for /admin/coupons/{id}. Expected GET, PUT or DELETE"));
                }
            }
            else {
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND,
                        new ApiResponse(false, "Coupon resource not found"));
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
                    requestBody.getMinPrice(),
                    requestBody.getUserCount(),
                    requestBody.getStartDate(),
                    requestBody.getEndDate(),
                    requestBody.getScope()
            );
            sendResponse(exchange, HttpURLConnection.HTTP_CREATED, new ApiResponse(true, "Coupon created successfully", convertToCouponResponse(newCoupon)));
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

    private void handleUpdateCoupon(HttpExchange exchange, int couponId) throws IOException {
        if (!checkHttpMethod(exchange, "PUT")) return;
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
            Coupon existingCoupon = couponManager.getCouponById(couponId);
            if (existingCoupon == null) {
                throw new NotFoundException("404 Not Found: Coupon with ID " + couponId + " not found.");
            }

            if (requestBody.getCouponCode() != null) existingCoupon.setCouponCode(requestBody.getCouponCode());
            if (requestBody.getType() != null) existingCoupon.setCouponType(com.aut.shoomal.payment.coupon.CouponType.fromName(requestBody.getType()));
            if (requestBody.getValue() != null) existingCoupon.setValue(requestBody.getValue());
            if (requestBody.getMinPrice() != null) existingCoupon.setMinPrice(requestBody.getMinPrice());
            if (requestBody.getUserCount() != null) existingCoupon.setUserCount(requestBody.getUserCount());
            if (requestBody.getStartDate() != null) existingCoupon.setStartDate(java.time.LocalDate.parse(requestBody.getStartDate()));
            if (requestBody.getEndDate() != null) existingCoupon.setEndDate(java.time.LocalDate.parse(requestBody.getEndDate()));
            if (requestBody.getScope() != null) existingCoupon.setScope(requestBody.getScope());


            couponManager.updateCoupon(existingCoupon);
            sendResponse(exchange, HttpURLConnection.HTTP_OK, new ApiResponse(true, "Coupon updated successfully", convertToCouponResponse(existingCoupon)));
        } catch (NotFoundException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, e.getMessage()));
        } catch (InvalidInputException | IllegalArgumentException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        }
    }

    private void handleGetCouponDetails(HttpExchange exchange, int couponId) throws IOException {
        if (!checkHttpMethod(exchange, "GET")) return;

        try {
            Coupon coupon = couponManager.getCouponById(couponId);
            if (coupon == null) {
                throw new NotFoundException("404 Not Found: Coupon with ID " + couponId + " not found.");
            }
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, convertToCouponResponse(coupon));
        } catch (NotFoundException e) {
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        }
    }

    private void handleListAllCoupons(HttpExchange exchange) throws IOException {
        if (!checkHttpMethod(exchange, "GET")) return;

        try {
            java.util.List<Coupon> coupons = couponManager.getAllCoupons();
            java.util.List<CouponResponse> responseList = coupons.stream()
                    .map(this::convertToCouponResponse)
                    .collect(java.util.stream.Collectors.toList());
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responseList);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: An unexpected error occurred."));
        }
    }


    private CouponResponse convertToCouponResponse(Coupon coupon) {
        if (coupon == null) return null;
        return new CouponResponse(
                coupon.getId(),
                coupon.getCouponCode(),
                coupon.getCouponType().getName(),
                coupon.getValue(),
                coupon.getMinPrice(),
                coupon.getUserCount(),
                coupon.getStartDate(),
                coupon.getEndDate(),
                coupon.getScope()
        );
    }

    private boolean isAdmin(User user) {
        return user.getRole().getName().equalsIgnoreCase("Admin");
    }
}