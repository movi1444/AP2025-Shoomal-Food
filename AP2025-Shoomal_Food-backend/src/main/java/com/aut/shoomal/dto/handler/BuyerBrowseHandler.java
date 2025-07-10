package com.aut.shoomal.dto.handler;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.food.FoodManager;
import com.aut.shoomal.entity.menu.Menu;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.dto.request.ListItemRequest;
import com.aut.shoomal.dto.request.ListVendorsRequest;
import com.aut.shoomal.dto.response.ApiResponse;
import com.aut.shoomal.dto.response.ListItemResponse;
import com.aut.shoomal.dto.response.RestaurantResponse;
import com.aut.shoomal.dto.response.ViewMenuResponse;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.coupon.Coupon;
import com.aut.shoomal.payment.coupon.CouponManager;
import com.aut.shoomal.util.HibernateUtil;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.List;

public class BuyerBrowseHandler extends AbstractHttpHandler
{
    private static final Pattern VENDORS_BASE_PATH_PATTERN = Pattern.compile("/vendors/?");
    private static final Pattern VENDOR_ID_PATTERN = Pattern.compile("/vendors/(\\d+)");
    private static final Pattern ITEMS_BASE_PATH_PATTERN = Pattern.compile("/items/?");
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("/items/(\\d+)");
    private static final Pattern COUPONS_BASE_PATH_PATTERN = Pattern.compile("/coupons/?");

    private final UserManager userManager;
    private final RestaurantManager restaurantManager;
    private final FoodManager foodManager;
    private final CouponManager couponManager;
    private final BlacklistedTokenDao blacklistedTokenDao;
    public BuyerBrowseHandler(UserManager userManager, RestaurantManager restaurantManager, CouponManager couponManager,
                              FoodManager foodManager, BlacklistedTokenDao blacklistedTokenDao)
    {
        this.userManager = userManager;
        this.restaurantManager = restaurantManager;
        this.couponManager = couponManager;
        this.foodManager = foodManager;
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
            if (VENDORS_BASE_PATH_PATTERN.matcher(path).matches())
                postVendors(exchange);
            else if (ITEMS_BASE_PATH_PATTERN.matcher(path).matches())
                postItems(exchange);
            else
                sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for POST."));
        }
        else if (method.equalsIgnoreCase("GET"))
        {
            Optional<Integer> vendorId = extractIdFromPath(path, VENDOR_ID_PATTERN);
            Optional<Integer> itemId = extractIdFromPath(path, ITEM_ID_PATTERN);
            if (vendorId.isPresent())
            {
                getMenuByVendorId(exchange, vendorId.get());
                return;
            }
            if (itemId.isPresent())
            {
                getItemById(exchange, itemId.get());
                return;
            }
            if (COUPONS_BASE_PATH_PATTERN.matcher(path).matches())
            {
                checkCoupon(exchange);
                return;
            }
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Resource not found for GET."));
        }
        else
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, new ApiResponse(false, "Method Not Allowed."));
    }

    private void postVendors(HttpExchange exchange) throws IOException
    {
        try {
            ListVendorsRequest requestBody = parseRequestBody(exchange, ListVendorsRequest.class);
            if (requestBody == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }

            List<Restaurant> restaurants, finalResult = new ArrayList<>();
            String search = requestBody.getSearch();
            List<String> keywords = requestBody.getKeywords();
            boolean hasSearch = (search != null) && (!search.trim().isEmpty());
            boolean hasKeywords = (keywords != null) && (!keywords.isEmpty());
            if (hasSearch)
                restaurants = restaurantManager.searchRestaurantByName(search);
            else
                restaurants = restaurantManager.getAllRestaurants();

            if (hasKeywords)
            {
                for (Restaurant restaurant : restaurants)
                {
                    boolean match = true;
                    for (String keyword : keywords)
                    {
                        List<Food> foodsMatching = foodManager.getFoodsByRestaurantId(restaurant.getId()).stream()
                                .filter(food -> food.getKeywords().stream().anyMatch(cat -> cat.equalsIgnoreCase(keyword)))
                                .toList();
                        if (foodsMatching.isEmpty())
                        {
                            match = false;
                            break;
                        }
                    }
                    if (match)
                        finalResult.add(restaurant);
                }
            }
            else
                finalResult = restaurants;

            List<RestaurantResponse> responses = finalResult.stream()
                    .map(r -> new RestaurantResponse(
                            r.getId(),
                            r.getName(),
                            r.getAddress(),
                            r.getPhone(),
                            r.getLogoBase64()
                    )).toList();
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responses);
        } catch (IOException e) {
            System.err.println("Error parsing request body: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (Exception e) {
            System.err.println("500 An unexpected error occurred during POST /vendors: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: " + e.getMessage()));
        }
    }

    private void postItems(HttpExchange exchange) throws IOException
    {
        try {
            ListItemRequest requestBody = parseRequestBody(exchange, ListItemRequest.class);
            if (requestBody == null)
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Request body is empty."));
                return;
            }

            List<Food> foods;
            String search = requestBody.getSearch();
            String price = requestBody.getPrice();
            List<String> keywords = requestBody.getKeywords();

            boolean hasSearch = (search != null) && (!search.trim().isEmpty());
            boolean hasKeywords = (keywords != null) && (!keywords.isEmpty());
            boolean hasPrice = (price != null) && (!price.trim().isEmpty());

            if (hasSearch)
                foods = foodManager.searchFoodByName(search);
            else
                foods = foodManager.getAllFoods();

            if (hasKeywords)
                foods = foods.stream()
                        .filter(food -> keywords.stream()
                                .allMatch(keyword -> food.getKeywords().stream()
                                        .anyMatch(cat -> cat.equalsIgnoreCase(keyword))))
                        .toList();

            if (hasPrice)
                foods = foods.stream()
                        .filter(food -> String.valueOf((int)food.getPrice()).equals(price))
                        .toList();

            List<ListItemResponse> responses = foods.stream()
                    .map(food -> new ListItemResponse(
                            food.getId(),
                            food.getName(),
                            food.getImageBase64(),
                            food.getDescription(),
                            (food.getVendor() != null) ? food.getVendor().getId() : null,
                            (int) food.getPrice(),
                            food.getSupply(),
                            food.getKeywords()
                    ))
                    .toList();

            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, responses);
        } catch (IOException e) {
            System.err.println("Error parsing request body: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: Malformed JSON in request body."));
        } catch (Exception e) {
            System.err.println("500 An unexpected error occurred during POST /vendors: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: " + e.getMessage()));
        }
    }

    private void getMenuByVendorId(HttpExchange exchange, Integer vendorId) throws IOException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Restaurant restaurant = session.get(Restaurant.class, Long.valueOf(vendorId));
            if (restaurant == null)
                throw new NotFoundException("404 Restaurant with id " + vendorId + " not found or not approved.");

            List<String> categories = restaurant.getMenus().stream()
                    .map(Menu::getTitle)
                    .distinct()
                    .toList();

            List<ListItemResponse> foods = restaurant.getFoods().stream()
                    .map(food -> new ListItemResponse(
                            food.getId(),
                            food.getName(),
                            food.getImageBase64(),
                            food.getDescription(),
                            (food.getVendor() != null) ? food.getVendor().getId() : null,
                            (int) food.getPrice(),
                            food.getSupply(),
                            food.getKeywords()
                    ))
                    .toList();

            ViewMenuResponse response = new ViewMenuResponse(categories, foods);
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, response);
        } catch (NotFoundException e) {
            System.err.println("Restaurant not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Restaurant not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("500 An unexpected error occurred during GET /vendors/" + vendorId + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: " + e.getMessage()));
        }
    }

    private void getItemById(HttpExchange exchange, Integer itemId) throws IOException
    {
        try {
            Food food = foodManager.getFoodById(Long.valueOf(itemId));
            if (food == null)
                throw new NotFoundException("404 Item with id " + itemId + " not found.");
            ListItemResponse item = new ListItemResponse(
                    food.getId(),
                    food.getName(),
                    food.getImageBase64(),
                    food.getDescription(),
                    (food.getVendor() != null) ? food.getVendor().getId() : null,
                    (int) food.getPrice(),
                    food.getSupply(),
                    food.getKeywords()
            );
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, item);
        } catch (NotFoundException e) {
            System.err.println("Item not found: " + e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, "404 Item not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("500 An unexpected error occurred during GET /items/" + itemId + ": " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: " + e.getMessage()));
        }
    }

    private void checkCoupon(HttpExchange exchange) throws IOException
    {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);
            String couponCode = queryParams.get("coupon_code");
            if (couponCode == null || couponCode.trim().isEmpty())
            {
                sendResponse(exchange, HttpURLConnection.HTTP_BAD_REQUEST, new ApiResponse(false, "400 Invalid input: 'coupon_code' query parameter is required."));
                return;
            }
            Coupon coupon = couponManager.getCouponByCode(couponCode);
            if (coupon == null)
                throw new NotFoundException("404 Coupon with code " + couponCode + " not found.");
            sendRawJsonResponse(exchange, HttpURLConnection.HTTP_OK, coupon);
        } catch (NotFoundException e) {
            System.err.println(e.getMessage());
            sendResponse(exchange, HttpURLConnection.HTTP_NOT_FOUND, new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during GET /coupons: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR, new ApiResponse(false, "500 Internal Server Error: " + e.getMessage()));
        }
    }
}