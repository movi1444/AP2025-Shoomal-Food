package com.aut.shoomal.Mamad.menu;

import com.aut.shoomal.Mamad.food.Food;
import com.aut.shoomal.Mamad.restaurant.Restaurant;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_titles")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "menu_food_items",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "food_item_id")
    )
    private List<Food> foods = new ArrayList<>();

    public Menu() {}

    public Menu(String title, Restaurant restaurant) {
        this.title = title;
        this.restaurant = restaurant;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public List<Food> getFoodItems() {
        return foods;
    }

    public void setFoodItems(List<Food> foodItems) {
        this.foods = foodItems;
    }

    public void addFoodItem(Food foodItem) {
        foods.add(foodItem);
    }

    public void removeFoodItem(Food foodItem) {
        foods.remove(foodItem);
    }
}