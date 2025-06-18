package com.aut.shoomal.Mamad.cart;

import com.aut.shoomal.Mamad.food.Food;
import jakarta.persistence.*;

@Entity
@Table(name = "shopping_cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food foodItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public CartItem() {}

    public CartItem(Cart cart, Food foodItem, Integer quantity) {
        this.cart = cart;
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Food getFoodItem() { return foodItem; }
    public void setFoodItem(Food foodItem) { this.foodItem = foodItem; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}