package com.aut.shoomal.entity.food;

import com.aut.shoomal.entity.menu.Menu;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.dao.FoodDao;
import com.aut.shoomal.dao.MenuDao;
import com.aut.shoomal.dao.RestaurantDao;
import com.aut.shoomal.dto.request.AddFoodItemRequest;
import com.aut.shoomal.dto.request.UpdateFoodItemRequest;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.ForbiddenException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import org.hibernate.Session;

import java.util.List;

public class FoodManager {

    private final FoodDao foodDao;
    private final RestaurantManager restaurantManager;
    private final RestaurantDao restaurantDao;
    private final MenuDao menuDao;

    public FoodManager(FoodDao foodDao, RestaurantManager restaurantManager, RestaurantDao restaurantDao, MenuDao menuDao) {
        this.foodDao = foodDao;
        this.restaurantManager = restaurantManager;
        this.restaurantDao = restaurantDao;
        this.menuDao = menuDao;
    }

    public void updateFood(Food food, Session session) {
        this.foodDao.update(food, session);
    }

    public List<Food> getAllFoods(){
        return this.foodDao.findAll();
    }

    public Food getFoodById(Long id){
        return this.foodDao.findById(id);
    }

    public List<Food> searchFoodByName(String name){
        return this.foodDao.searchByName(name);
    }

    public List<Food> getFoodsByRestaurantId(Long restaurantId) {
        return this.foodDao.findByRestaurantId(restaurantId);
    }

    public List<Food> getFoodsByMenuTitle(Session session, Long restaurantId, String title) {
        return foodDao.getFoodsByMenuTitle(session, restaurantId, title);
    }

    public Food addFoodItem(int restaurantId, AddFoodItemRequest request, String userId) throws NotFoundException, InvalidInputException, ForbiddenException {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!restaurantManager.isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to add food item to this restaurant.");
        }

        Food newFood = new Food();
        newFood.setName(request.getName());
        newFood.setDescription(request.getDescription());
        newFood.setPrice(request.getPrice().doubleValue());
        newFood.setSupply(request.getSupply());
        newFood.setKeywords(request.getKeywords());
        newFood.setVendor(restaurant);

        foodDao.create(newFood);
        return newFood;
    }

    public Food updateFoodItem(int itemId, UpdateFoodItemRequest request) throws NotFoundException, InvalidInputException {

        Food existingFood = foodDao.findById((long) itemId);
        if (existingFood == null) {
            throw new NotFoundException("Food item not found in this restaurant.");
        }

        if (request.getName() != null) existingFood.setName(request.getName());
        if (request.getDescription() != null) existingFood.setDescription(request.getDescription());
        if (request.getPrice() != null) existingFood.setPrice(request.getPrice().doubleValue());
        if (request.getSupply() != null) existingFood.setSupply(request.getSupply());
        if (request.getImageBase64() != null) existingFood.setImageBase64(request.getImageBase64());
        if (request.getKeywords() != null && !request.getKeywords().isEmpty()) existingFood.setKeywords(request.getKeywords());

        foodDao.update(existingFood);
        return existingFood;
    }

    public boolean deleteFoodItem(Session session, int itemId) throws NotFoundException, ConflictException {

        Food foodToDelete = foodDao.findById((long) itemId);
        if (foodToDelete == null) {
            throw new NotFoundException("Food item not found in this restaurant.");
        }

        List<Menu> menusContainingFoodItem = menuDao.findByFoodItemId((long) itemId);
        for (Menu menu : menusContainingFoodItem) {
            menu.getFoodItems().removeIf(foodInMenu -> foodInMenu.getId().equals((long) itemId));
            session.merge(menu);
        }
        try {
            session.remove(foodToDelete);
            return true;
        } catch (Exception e) {
            throw new ConflictException("Failed to delete food item. item is a part of an order.");
        }
    }
}