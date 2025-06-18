package com.aut.shoomal.Mamad.cart;

import com.aut.shoomal.Mamad.food.Food;
import com.aut.shoomal.Erfan.User;
import com.aut.shoomal.dao.CartDao;
import com.aut.shoomal.dao.FoodDao;
import com.aut.shoomal.dao.UserDao;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;

public class CartManager {
    private final CartDao CartDao;
    private final FoodDao foodDao;
    private final UserDao userDao;

    public CartManager(CartDao CartDao, FoodDao foodDao, UserDao userDao) {
        this.CartDao = CartDao;
        this.foodDao = foodDao;
        this.userDao = userDao;
    }

    public Cart getOrCreateCartForUser(String userId) throws NotFoundException {
        User user = userDao.findById(Long.parseLong(userId));
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        Cart cart = CartDao.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            CartDao.create(cart);
        }
        return cart;
    }

    public Cart addItemToCart(String userId, Long foodItemId, int quantity) throws NotFoundException, InvalidInputException {
        if (quantity <= 0) {
            throw new InvalidInputException("Quantity must be positive.");
        }

        Cart cart = getOrCreateCartForUser(userId);
        Food foodItem = foodDao.findById(foodItemId);
        if (foodItem == null) {
            throw new NotFoundException("Food item not found.");
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getFoodItem().getId().equals(foodItemId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newCartItem = new CartItem(cart, foodItem, quantity);
            cart.addCartItem(newCartItem);
        }

        CartDao.update(cart);
        return cart;
    }

    public Cart updateCartItemQuantity(String userId, Long foodItemId, int newQuantity) throws NotFoundException, InvalidInputException {
        if (newQuantity < 0) {
            throw new InvalidInputException("Quantity cannot be negative.");
        }

        Cart cart = getOrCreateCartForUser(userId);
        CartItem itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getFoodItem().getId().equals(foodItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Food item not found in cart."));

        if (newQuantity == 0) {
            cart.removeCartItem(itemToUpdate);
        } else {
            itemToUpdate.setQuantity(newQuantity);
        }

        CartDao.update(cart);
        return cart;
    }

    public void removeCartItem(String userId, Long foodItemId) throws NotFoundException {
        Cart cart = getOrCreateCartForUser(userId);
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getFoodItem().getId().equals(foodItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Food item not found in cart."));

        cart.removeCartItem(itemToRemove);
        CartDao.update(cart);
    }

    public void clearCart(String userId) throws NotFoundException {
        Cart cart = getOrCreateCartForUser(userId);
        cart.getItems().clear();
        CartDao.update(cart);
    }

    public Cart getCartByUserId(String userId) throws NotFoundException {
        return getOrCreateCartForUser(userId);
    }
}