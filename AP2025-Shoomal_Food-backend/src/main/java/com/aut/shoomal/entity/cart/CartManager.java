// shoomal/entity/cart/CartManager.java
package com.aut.shoomal.entity.cart;

import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.dao.CartDao;
import com.aut.shoomal.dao.FoodDao;
import com.aut.shoomal.dao.UserDao;
import com.aut.shoomal.dao.RestaurantDao;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException; // Import added

import java.util.Optional;

public class CartManager {
    private final CartDao cartDao;
    private final FoodDao foodDao;
    private final UserDao userDao;
    private final RestaurantDao restaurantDao;

    public CartManager(CartDao cartDao, FoodDao foodDao, UserDao userDao, RestaurantDao restaurantDao) {
        this.cartDao = cartDao;
        this.foodDao = foodDao;
        this.userDao = userDao;
        this.restaurantDao = restaurantDao;
    }

    public Cart getOrCreateCartForUserAndRestaurant(Session session, Long userId, Long restaurantId) throws NotFoundException {
        User user = session.get(User.class, userId);
        if (user == null) {
            throw new NotFoundException("User not found with ID: " + userId);
        }
        Restaurant restaurant = session.get(Restaurant.class, restaurantId);
        if (restaurant == null) {
            throw new NotFoundException("Restaurant not found with ID: " + restaurantId);
        }

        Cart cart = cartDao.findByUserAndRestaurant(session, user, restaurant);

        if (cart == null) {
            try {
                cart = new Cart();
                cart.setUser(user);
                cart.setRestaurant(restaurant);
                session.persist(cart);
                // Flush the session immediately to detect unique constraint violations
                // early, before the transaction is committed.
                session.flush();
            } catch (ConstraintViolationException e) {
                // This exception indicates a race condition where a concurrent transaction
                // created the cart after this session checked for its existence.
                // Clear the session's state to avoid stale objects and re-fetch the existing cart.
                session.clear();
                cart = cartDao.findByUserAndRestaurant(session, user, restaurant);
                if (cart == null) {
                    // If, after catching the unique constraint violation and clearing the session,
                    // we still cannot find the cart, it indicates a deeper issue.
                    throw new RuntimeException("Race condition detected, but existing cart could not be retrieved. Original error: " + e.getMessage(), e);
                }
            }
        }
        return cart;
    }

    public Cart addItemToCart(Long userId, Long restaurantId, Long foodItemId, int quantityDelta) throws NotFoundException, InvalidInputException {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // The getOrCreateCartForUserAndRestaurant method now handles the race condition internally.
            Cart cart = getOrCreateCartForUserAndRestaurant(session, userId, restaurantId);

            Food foodItem = session.get(Food.class, foodItemId);
            if (foodItem == null) {
                throw new NotFoundException("Food item not found with ID: " + foodItemId);
            }
            if (!foodItem.getVendor().getId().equals(restaurantId)) {
                throw new InvalidInputException("Food item does not belong to the specified restaurant.");
            }

            Optional<CartItem> existingItemOpt = cart.getItems().stream()
                    .filter(item -> item.getFoodItem().getId().equals(foodItemId))
                    .findFirst();

            int newAbsoluteQuantity;

            if (existingItemOpt.isPresent()) {
                CartItem existingItem = existingItemOpt.get();
                newAbsoluteQuantity = existingItem.getQuantity() + quantityDelta;

                if (newAbsoluteQuantity <= 0) {
                    cart.removeCartItem(existingItem);
                    session.remove(existingItem);
                } else {
                    if (foodItem.getSupply() < newAbsoluteQuantity) {
                        throw new InvalidInputException("Insufficient supply for food item: " + foodItem.getName() + ". Available: " + foodItem.getSupply() + ", Desired: " + newAbsoluteQuantity);
                    }
                    existingItem.setQuantity(newAbsoluteQuantity);
                    session.merge(existingItem);
                }
            } else {
                if (quantityDelta <= 0) {
                    throw new InvalidInputException("Cannot add item with non-positive quantity. Quantity: " + quantityDelta);
                }
                newAbsoluteQuantity = quantityDelta;
                if (foodItem.getSupply() < newAbsoluteQuantity) {
                    throw new InvalidInputException("Insufficient supply for food item: " + foodItem.getName() + ". Available: " + foodItem.getSupply() + ", Desired: " + newAbsoluteQuantity);
                }
                CartItem newCartItem = new CartItem(cart, foodItem, newAbsoluteQuantity);
                cart.addCartItem(newCartItem);
                session.persist(newCartItem);
            }
            session.merge(cart);
            transaction.commit();
            return cart;
        } catch (NotFoundException | InvalidInputException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error adding/updating item to cart: " + e.getMessage());
            throw new RuntimeException("Error adding/updating item to cart: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public Cart updateCartItemQuantity(Long userId, Long restaurantId, Long foodItemId, int newQuantity) throws NotFoundException, InvalidInputException {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Cart cart = cartDao.findByUserIdAndRestaurantId(session, userId, restaurantId);
            if (cart == null) {
                throw new NotFoundException("Cart not found for user " + userId + " and restaurant " + restaurantId);
            }

            CartItem itemToUpdate = cart.getItems().stream()
                    .filter(item -> item.getFoodItem().getId().equals(foodItemId))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Food item not found in cart."));

            Food foodItem = session.get(Food.class, foodItemId);
            if (foodItem == null) {
                throw new NotFoundException("Food item not found with ID: " + foodItemId);
            }
            if (newQuantity > foodItem.getSupply()) {
                throw new InvalidInputException("New quantity (" + newQuantity + ") exceeds available supply (" + foodItem.getSupply() + ") for " + foodItem.getName());
            }

            if (newQuantity == 0) {
                cart.removeCartItem(itemToUpdate);
                session.remove(itemToUpdate);
            } else {
                itemToUpdate.setQuantity(newQuantity);
                session.merge(itemToUpdate);
            }

            session.merge(cart);
            transaction.commit();
            return cart;
        } catch (NotFoundException | InvalidInputException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error updating cart item quantity: " + e.getMessage());
            throw new RuntimeException("Error updating cart item quantity: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public void removeCartItem(Long userId, Long restaurantId, Long foodItemId) throws NotFoundException {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Cart cart = cartDao.findByUserIdAndRestaurantId(session, userId, restaurantId);
            if (cart == null) {
                throw new NotFoundException("Cart not found for user " + userId + " and restaurant " + restaurantId);
            }

            CartItem itemToRemove = cart.getItems().stream()
                    .filter(item -> item.getFoodItem().getId().equals(foodItemId))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Food item not found in cart."));

            cart.removeCartItem(itemToRemove);
            session.remove(itemToRemove);
            session.merge(cart);
            transaction.commit();
        } catch (NotFoundException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error removing item from cart: " + e.getMessage());
            throw new RuntimeException("Error removing item from cart: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public void clearCart(Long userId, Long restaurantId) throws NotFoundException {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            Cart cart = cartDao.findByUserIdAndRestaurantId(session, userId, restaurantId);
            if (cart == null) {
                throw new NotFoundException("Cart not found for user " + userId + " and restaurant " + restaurantId);
            }
            for (CartItem item : new java.util.ArrayList<>(cart.getItems())) {
                cart.removeCartItem(item);
                session.remove(item);
            }
            session.merge(cart);
            transaction.commit();
        } catch (NotFoundException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("Error clearing cart: " + e.getMessage());
            throw new RuntimeException("Error clearing cart: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public Cart getCartByUserIdAndRestaurantId(Long userId, Long restaurantId) throws NotFoundException {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Cart cart = cartDao.findByUserIdAndRestaurantId(session, userId, restaurantId);
            if (cart == null) {
                throw new NotFoundException("Cart not found for user " + userId + " and restaurant " + restaurantId);
            }

            cart.getItems().size();
            for (CartItem item : cart.getItems()) {
                item.getFoodItem().getName();
            }
            return cart;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error getting cart by user and restaurant ID: " + e.getMessage());
            throw new RuntimeException("Error getting cart by user and restaurant ID: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}