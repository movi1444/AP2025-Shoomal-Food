package com.aut.shoomal.dao;

import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderStatus;
import org.hibernate.Session;

import java.util.List;

public interface OrderDao extends GenericDao<Order>
{
    List<Order> findByVendorId(Integer vendorId);
    List<Order> findOrdersWithFilters(String search, String vendorName, String customerName, String courierName, OrderStatus status);
    List<Order> findWithFilters(Session session, Long customerId, String search, String vendorName);
    List<Food> findTop5MostOrderedFoods(String searchKeyword);
    List<Restaurant> findTop5MostOrderedRestaurants(String searchKeyword);
}