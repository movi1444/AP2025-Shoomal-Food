package com.aut.shoomal.dao;

import com.aut.shoomal.entity.food.Food;
import org.hibernate.Session;

import java.util.List;

public interface FoodDao extends GenericDao<Food> {
    List<Food> searchByName(String keyword);
    List<Food> findByRestaurantId(Long restaurantId);
    List<Food> findByKeyword(String keyword);
    List<Food> getFoodsByMenuTitle(Session session, Long restaurantId, String title);
    Food getFoodByIdAndRestaurantId(Session session, Long id, Long restaurantId);
}