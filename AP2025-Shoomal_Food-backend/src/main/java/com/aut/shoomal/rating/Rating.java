package com.aut.shoomal.rating;

import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.payment.order.Order;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ratings")
public class Rating
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer rating;
    private String comment;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "rating_images", joinColumns = @JoinColumn(name = "rating_id"))
    @Column(name = "image_base64_url", columnDefinition = "TEXT")
    private List<String> imageBase64;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    public Rating()
    {
        this.createdAt = LocalDateTime.now();
    }

    public Rating(Order order, List<String> imageBase64, String comment, Integer rating, User user)
    {
        this();
        this.order = order;
        this.imageBase64 = imageBase64;
        this.comment = comment;
        this.rating = rating;
        this.user = user;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getRating()
    {
        return rating;
    }

    public void setRating(Integer rating)
    {
        this.rating = rating;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public List<String> getImageBase64()
    {
        return imageBase64;
    }

    public void setImageBase64(List<String> imageBase64)
    {
        this.imageBase64 = imageBase64;
    }

    public Order getOrder()
    {
        return order;
    }

    public void setOrder(Order order)
    {
        this.order = order;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public Food getFood()
    {
        return food;
    }

    public void setFood(Food food)
    {
        this.food = food;
    }
}