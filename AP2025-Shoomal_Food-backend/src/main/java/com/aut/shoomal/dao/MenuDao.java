package com.aut.shoomal.dao;

import com.aut.shoomal.entity.menu.Menu;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public interface MenuDao extends GenericDao<Menu> {
    List<Menu> findByRestaurantId(Long restaurantId);
    Optional<Menu> findByRestaurantIdAndTitle(Long restaurantId, String title);
    List<Menu> findByFoodItemId(Long foodItemId);
    Menu findByTitle(Session session, Integer restaurantId, String title);
}