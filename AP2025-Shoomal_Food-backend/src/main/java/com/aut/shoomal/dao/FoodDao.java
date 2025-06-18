package com.aut.shoomal.dao;

import com.aut.shoomal.Mamad.food.Food;

import java.util.List;

public interface FoodDao extends GenericDao<Food> {
    List<Food> searchByName(String keyword);
    List<Food> findByRestaurantId(Long restaurantId);
    List<Food> findByCategory(String category);
}