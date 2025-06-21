package com.aut.shoomal.dao;

import com.aut.shoomal.entity.menu.Menu;

import java.util.List;
import java.util.Optional;

public interface MenuDao extends GenericDao<Menu> {
    List<Menu> findByRestaurantId(Long restaurantId);
    Optional<Menu> findByRestaurantIdAndTitle(Long restaurantId, String title);
    List<Menu> findByFoodItemId(Long foodItemId);
}