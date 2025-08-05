package com.aut.shoomal.entity.cart;

import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.dao.CartDao;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Optional;

public class CartManager {
    private final CartDao cartDao;

    public CartManager(CartDao cartDao) {
        this.cartDao = cartDao;
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
                session.flush();
            } catch (ConstraintViolationException e) {
                session.clear();
                cart = cartDao.findByUserAndRestaurant(session, user, restaurant);
                if (cart == null) {
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

            /*cart.getItems().size();
            for (CartItem item : cart.getItems()) {
                item.getFoodItem().getName();
            }*/
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