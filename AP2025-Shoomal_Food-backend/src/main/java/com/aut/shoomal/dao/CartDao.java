package com.aut.shoomal.dao;

import com.aut.shoomal.entity.cart.Cart;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.restaurant.Restaurant;
import org.hibernate.Session;

public interface CartDao extends GenericDao<Cart> {
    Cart findByUserIdAndRestaurantId(Session session, Long userId, Long restaurantId);
    Cart findByUserAndRestaurant(Session session, User user, Restaurant restaurant);
}