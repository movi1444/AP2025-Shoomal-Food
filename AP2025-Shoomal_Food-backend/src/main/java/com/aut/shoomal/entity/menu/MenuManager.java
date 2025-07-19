package com.aut.shoomal.entity.menu;

import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.dao.FoodDao;
import com.aut.shoomal.dao.MenuDao;
import com.aut.shoomal.dao.RestaurantDao;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.ForbiddenException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class MenuManager {

    private final MenuDao menuDao;
    private final RestaurantManager restaurantManager;
    private final RestaurantDao restaurantDao;
    private final FoodDao foodDao;

    public MenuManager(MenuDao menuDao, RestaurantManager restaurantManager, RestaurantDao restaurantDao, FoodDao foodDao){
        this.menuDao = menuDao;
        this.restaurantManager = restaurantManager;
        this.restaurantDao = restaurantDao;
        this.foodDao = foodDao;
    }

    public void addMenu(Menu menu){
        this.menuDao.create(menu);
    }

    public void removeMenu(Long id){
        this.menuDao.delete(id);
    }

    public Menu getMenuById(Long id){
        return this.menuDao.findById(id);
    }

    public List<Menu> getAllMenus(){
        return this.menuDao.findAll();
    }

    public void updateMenu(Menu menu){
        this.menuDao.update(menu);
    }

    public List<Menu> findMenusByRestaurantId(Long restaurantId) {
        return menuDao.findByRestaurantId(restaurantId);
    }

    public Menu findByTitle(Session session, Integer restaurantId, String title) {
        return menuDao.findByTitle(session, restaurantId, title);
    }

    public void addMenuTitle(int restaurantId, String title, String userId) throws NotFoundException, ConflictException, ForbiddenException {
        Restaurant restaurant = restaurantDao.findById((long) restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found.");
        }
        if (!restaurantManager.isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to add menu to this restaurant.");
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
        if (!restaurantManager.isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to delete menu from this restaurant.");
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
        if (!restaurantManager.isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to add item to menu of this restaurant.");
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
        if (!restaurantManager.isOwner(restaurantId, userId)) {
            throw new ForbiddenException("Not authorized to remove item from menu of this restaurant.");
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
}