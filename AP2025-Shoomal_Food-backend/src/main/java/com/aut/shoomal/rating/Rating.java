package com.aut.shoomal.rating;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.payment.Order;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
public class Rating
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer rating;
    private String comment;
    private String imageBase64;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Rating()
    {
        this.createdAt = LocalDateTime.now();
    }

    public Rating(Order order, String imageBase64, String comment, Integer rating, User user)
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

    public String getImageBase64()
    {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64)
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
}