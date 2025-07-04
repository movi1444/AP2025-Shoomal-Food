package com.aut.shoomal.entity.restaurant;

import com.aut.shoomal.dao.RestaurantDao;
import com.aut.shoomal.dao.FoodDao;
import com.aut.shoomal.dao.MenuDao;
import com.aut.shoomal.dao.UserDao;
import com.aut.shoomal.dto.request.CreateRestaurantRequest;
import com.aut.shoomal.dto.request.UpdateRestaurantRequest;
import com.aut.shoomal.dto.request.AddFoodItemRequest;
import com.aut.shoomal.dto.request.UpdateFoodItemRequest;
import com.aut.shoomal.entity.user.UserStatus;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.ForbiddenException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.entity.user.Seller;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.menu.Menu;

import java.util.List;
import java.util.Optional;

public class RestaurantManager {

    private final RestaurantDao restaurantDao;
    private final FoodDao foodDao;
    private final MenuDao menuDao;
    private final UserDao userDao;

    public RestaurantManager(RestaurantDao restaurantDao, FoodDao foodDao, MenuDao menuDao, UserDao userDao) {
        this.restaurantDao = restaurantDao;
        this.foodDao = foodDao;
        this.menuDao = menuDao;
        this.userDao = userDao;
    }

    public boolean isOwner(int restaurantId, String userId) {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            return false;
        }
        User ownerUser = userDao.findById(Long.parseLong(userId));
        if (ownerUser == null) {
            return false;
        }

        return restaurant.getOwner() != null && restaurant.getOwner().getId().equals(ownerUser.getId());
    }

    public Restaurant createRestaurant(CreateRestaurantRequest request, String userId) throws InvalidInputException, ConflictException, ForbiddenException, NotFoundException {
        if (request.getName() == null || request.getName().trim().isEmpty() ||
                request.getAddress() == null || request.getAddress().trim().isEmpty() ||
                request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new InvalidInputException("Name, address, and phone are required.");
        }

        Seller owner = (Seller) userDao.findById(Long.parseLong(userId));
        if (owner == null) {
            throw new NotFoundException("Seller not found for the given user ID.");
        }

        List<Restaurant> existingRestaurants = restaurantDao.findByOwner(owner);
        if (existingRestaurants != null && !existingRestaurants.isEmpty()) {
            throw new ConflictException("This seller already owns a restaurant. Each seller can only have one restaurant.");
        }


        if (restaurantDao.findByName(request.getName()) != null) {
            throw new ConflictException("Restaurant with this name already exists.");
        }


        Restaurant newRestaurant = new Restaurant();
        newRestaurant.setName(request.getName());
        newRestaurant.setPhone(request.getPhone());
        newRestaurant.setAddress(request.getAddress());
        newRestaurant.setLogoBase64(request.getLogoBase64());
        newRestaurant.setTaxFee(request.getTaxFee() != null ? request.getTaxFee() : 0);
        newRestaurant.setAdditionalFee(request.getAdditionalFee() != null ? request.getAdditionalFee() : 0);
        newRestaurant.setOwner(owner);
        newRestaurant.setApproved(false);
        newRestaurant.setWorkingHours("Not set");
        newRestaurant.setDescription("No description provided.");

        restaurantDao.create(newRestaurant);
        return newRestaurant;
    }

    public List<Restaurant> getRestaurantsBySeller(String userId) throws NotFoundException {
        Seller seller = (Seller) userDao.findById(Long.parseLong(userId));
        if (seller == null) {
            throw new NotFoundException("Seller not found.");
        }
        return restaurantDao.findByOwner(seller);
    }

    public Restaurant updateRestaurant(int restaurantId, UpdateRestaurantRequest request, String userId) throws NotFoundException, InvalidInputException, ForbiddenException {
        Restaurant existingRestaurant = restaurantDao.findById((long) restaurantId);
        if (existingRestaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to update this restaurant.");
        }
        if (!existingRestaurant.isApproved()) {
            throw new ForbiddenException("Restaurant is not yet approved. Please wait for admin approval.");
        }

        if (request.getName() != null) existingRestaurant.setName(request.getName());
        if (request.getAddress() != null) existingRestaurant.setAddress(request.getAddress());
        if (request.getPhone() != null) existingRestaurant.setPhone(request.getPhone());
        if (request.getLogoBase64() != null) existingRestaurant.setLogoBase64(request.getLogoBase64());
        if (request.getTaxFee() != null) existingRestaurant.setTaxFee(request.getTaxFee());
        if (request.getAdditionalFee() != null) existingRestaurant.setAdditionalFee(request.getAdditionalFee());

        restaurantDao.update(existingRestaurant);
        return existingRestaurant;
    }

    public Food addFoodItem(int restaurantId, AddFoodItemRequest request, String userId) throws NotFoundException, InvalidInputException, ForbiddenException {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to add food item to this restaurant.");
        }
        if (!restaurant.isApproved()) {
            throw new ForbiddenException("Restaurant is not yet approved. Please wait for admin approval.");
        }

        Food newFood = new Food();
        newFood.setName(request.getName());
        newFood.setDescription(request.getDescription());
        newFood.setPrice(request.getPrice().doubleValue());
        newFood.setSupply(request.getSupply());
        newFood.setCategories(request.getCategories());
        newFood.setVendor(restaurant);

        foodDao.create(newFood);
        return newFood;
    }

    public Food updateFoodItem(int restaurantId, int itemId, UpdateFoodItemRequest request, String userId) throws NotFoundException, InvalidInputException, ForbiddenException {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to update food item in this restaurant.");
        }
        if (!restaurant.isApproved()) {
            throw new ForbiddenException("Restaurant is not yet approved. Please wait for admin approval.");
        }

        Food existingFood = foodDao.findById((long) itemId);
        if (existingFood == null || !existingFood.getVendor().getId().equals(restaurant.getId())) {
            throw new NotFoundException("Food item not found in this restaurant.");
        }

        if (request.getName() != null) existingFood.setName(request.getName());
        if (request.getDescription() != null) existingFood.setDescription(request.getDescription());
        if (request.getPrice() != null) existingFood.setPrice(request.getPrice().doubleValue());
        if (request.getSupply() != null) existingFood.setSupply(request.getSupply());
        if (request.getImageBase64() != null) existingFood.setImageBase64(request.getImageBase64());
        if (request.getCategories() != null && !request.getCategories().isEmpty()) existingFood.setCategories(request.getCategories());

        foodDao.update(existingFood);
        return existingFood;
    }

    public void deleteFoodItem(int restaurantId, int itemId, String userId) throws NotFoundException, ForbiddenException {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to delete food item from this restaurant.");
        }
        if (!restaurant.isApproved()) {
            throw new ForbiddenException("Restaurant is not yet approved. Please wait for admin approval.");
        }

        Food foodToDelete = foodDao.findById((long) itemId);
        if (foodToDelete == null || !foodToDelete.getVendor().getId().equals(restaurant.getId())) {
            throw new NotFoundException("Food item not found in this restaurant.");
        }
        foodDao.delete((long) itemId);
    }

    public void addMenuTitle(int restaurantId, String title, String userId) throws NotFoundException, ConflictException, ForbiddenException {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to add menu to this restaurant.");
        }
        if (!restaurant.isApproved()) {
            throw new ForbiddenException("Restaurant is not yet approved. Please wait for admin approval.");
        }

        Optional<Menu> existingMenu = menuDao.findByRestaurantIdAndTitle((long) restaurantId, title);
        if (existingMenu.isPresent()) {
            throw new ConflictException("Menu with title '" + title + "' already exists for this restaurant.");
        }

        Menu newMenu = new Menu();
        newMenu.setTitle(title);
        newMenu.setRestaurant(restaurant);

        menuDao.create(newMenu);
    }

    public void deleteMenuTitle(int restaurantId, String menuTitle, String userId) throws NotFoundException, ForbiddenException {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to delete menu from this restaurant.");
        }
        if (!restaurant.isApproved()) {
            throw new ForbiddenException("Restaurant is not yet approved. Please wait for admin approval.");
        }

        Optional<Menu> menuToDelete = menuDao.findByRestaurantIdAndTitle((long) restaurantId, menuTitle);
        if (menuToDelete.isEmpty()) {
            throw new NotFoundException("Menu with title '" + menuTitle + "' not found for this restaurant.");
        }

        menuDao.delete(menuToDelete.get().getId());
    }

    public void addItemToMenu(int restaurantId, String menuTitle, int itemId, String userId) throws NotFoundException, ForbiddenException, InvalidInputException, ConflictException {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to add item to menu of this restaurant.");
        }
        if (!restaurant.isApproved()) {
            throw new ForbiddenException("Restaurant is not yet approved. Please wait for admin approval.");
        }

        Optional<Menu> menu = menuDao.findByRestaurantIdAndTitle((long) restaurantId, menuTitle);
        if (menu.isEmpty()) {
            throw new NotFoundException("Menu with title '" + menuTitle + "' not found for this restaurant.");
        }

        Food foodItem = foodDao.findById((long) itemId);
        if (foodItem == null || !foodItem.getVendor().getId().equals(restaurant.getId())) {
            throw new NotFoundException("Food item not found or does not belong to this restaurant.");
        }

        if (menu.get().getFoodItems().contains(foodItem)) {
            throw new ConflictException("Food item is already in this menu.");
        }

        menu.get().addFoodItem(foodItem);
        menuDao.update(menu.get());
    }

    public void deleteItemFromMenu(int restaurantId, String menuTitle, int itemId, String userId) throws NotFoundException, ForbiddenException {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to remove item from menu of this restaurant.");
        }
        if (!restaurant.isApproved()) {
            throw new ForbiddenException("Restaurant is not yet approved. Please wait for admin approval.");
        }

        Optional<Menu> menu = menuDao.findByRestaurantIdAndTitle((long) restaurantId, menuTitle);
        if (menu.isEmpty()) {
            throw new NotFoundException("Menu with title '" + menuTitle + "' not found for this restaurant.");
        }

        Food foodItemToRemove = foodDao.findById((long) itemId);
        if (foodItemToRemove == null) {
            throw new NotFoundException("Food item not found.");
        }

        if (!menu.get().getFoodItems().contains(foodItemToRemove)) {
            throw new NotFoundException("Food item is not present in this menu.");
        }

        menu.get().removeFoodItem(foodItemToRemove);
        menuDao.update(menu.get());
    }

    public List<Restaurant> getAllApprovedRestaurants() {
        return restaurantDao.findAllApproved();
    }

    public List<Restaurant> searchRestaurantByName(String restaurantName) {
        return restaurantDao.searchByName(restaurantName);
    }

    public Restaurant findById(Long restaurantId) {
        Restaurant restaurant = restaurantDao.findById(restaurantId);
        if (restaurant == null || !restaurant.isApproved())
            return null;
        return restaurant;
    }

    public void setApprovalStatus(String Id, UserStatus userStatus)
            throws NotFoundException, ForbiddenException
    {
        Long  sellerId = Long.parseLong(Id);
        User user = userDao.findById(sellerId);
        if (user == null)
            throw new NotFoundException("User not found.");
        if (!(user instanceof Seller seller))
            throw new ForbiddenException("User is not a seller.");

        List<Restaurant> restaurants = restaurantDao.findByOwner(seller);

        if (restaurants == null || restaurants.isEmpty())
            throw new NotFoundException("No restaurant found for this seller.");

        Restaurant restaurant = restaurants.getFirst();

        if (!restaurant.getOwner().getId().equals(sellerId))
            throw new ForbiddenException("Seller does not own this restaurant.");

        boolean approved = (userStatus == UserStatus.APPROVED);
        restaurantDao.updateApprovalStatus(restaurant.getId(), approved);
    }
}