package com.aut.shoomal.dao;

import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.user.Seller;
import org.hibernate.Session;

import java.util.List;

public interface RestaurantDao extends GenericDao<Restaurant> {
    Restaurant findByName(String name);
    void addMenu(Long restaurantId, Long menuId);
    void removeMenu(Long restaurantId, Long menuId);
    List<Restaurant> searchByName(String partialName);
    List<Restaurant> findByOwner(Seller owner);
    List<Restaurant> findByCourier(Session session, Long courierId);
}