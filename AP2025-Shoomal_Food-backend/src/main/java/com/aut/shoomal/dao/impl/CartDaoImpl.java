package com.aut.shoomal.dao.impl;

import com.aut.shoomal.entity.cart.Cart;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.dao.CartDao;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class CartDaoImpl extends GenericDaoImpl<Cart> implements CartDao {

    @Override
    public Cart findByUserIdAndRestaurantId(Session session, Long userId, Long restaurantId) {
        try {
            Query<Cart> query = session.createQuery(
                    "FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.foodItem WHERE c.user.id = :userId AND c.restaurant.id = :restaurantId", Cart.class);
            query.setParameter("userId", userId);
            query.setParameter("restaurantId", restaurantId);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("Error finding cart by user ID " + userId + " and restaurant ID " + restaurantId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Cart findByUserAndRestaurant(Session session, User user, Restaurant restaurant) {
        try {
            if (user == null || user.getId() == null) {
                System.err.println("Error: User object or User ID is null when trying to find cart by user and restaurant.");
                return null;
            }
            if (restaurant == null || restaurant.getId() == null) {
                System.err.println("Error: Restaurant object or Restaurant ID is null when trying to find cart by user and restaurant.");
                return null;
            }
            return findByUserIdAndRestaurantId(session, user.getId(), restaurant.getId());
        } catch (Exception e) {
            System.err.println("Error finding cart by user " + user.getId() + " and restaurant " + restaurant.getId() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
