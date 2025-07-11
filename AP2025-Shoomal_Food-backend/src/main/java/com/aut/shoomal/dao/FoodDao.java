package com.aut.shoomal.dao;

import com.aut.shoomal.entity.food.Food;

import java.util.List;

public interface FoodDao extends GenericDao<Food> {
    List<Food> searchByName(String keyword);
    List<Food> findByRestaurantId(Long restaurantId);
    List<Food> findByKeyword(String keyword);
}