package com.aut.shoomal.payment;

import com.aut.shoomal.Mamad.food.Food;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Food food;
    @Column
    private Integer quantity;
    @Column(name = "order_time_price")
    private Integer priceAtOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private Order order;

    public OrderItem() {}
    public OrderItem(Food food, Integer quantity, Order order, Integer priceAtOrder)
    {
        this.food = food;
        this.quantity = quantity;
        this.order = order;
        this.priceAtOrder = priceAtOrder;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }

    public Food getFood()
    {
        return food;
    }

    public void setFood(Food food)
    {
        this.food = food;
    }

    public Order getOrder()
    {
        return order;
    }

    public void setOrder(Order order)
    {
        this.order = order;
    }

    public Integer getPriceAtOrder()
    {
        return priceAtOrder;
    }

    public void setPriceAtOrder(Integer priceAtOrder)
    {
        this.priceAtOrder = priceAtOrder;
    }
}