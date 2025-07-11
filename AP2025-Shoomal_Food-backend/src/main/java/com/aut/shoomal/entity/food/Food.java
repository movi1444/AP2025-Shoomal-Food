package com.aut.shoomal.entity.food;

import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.payment.order.OrderItem;
import com.aut.shoomal.rating.Rating;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "food_items")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int supply;

    @Column(name = "image_base64", columnDefinition = "TEXT")
    private String imageBase64;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Restaurant vendor;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "food_item_keywords", joinColumns = @JoinColumn(name = "food_item_id"))
    @Column(name = "keywords")
    private List<String> keywords;

    @OneToMany(mappedBy = "food", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "food", fetch = FetchType.LAZY)
    private List<Rating> ratings = new ArrayList<>();
    public Food() {}

    public Food(String name, String description, double price, int supply,
                    String imageBase64, Restaurant vendor, List<String> keywords) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.supply = supply;
        this.imageBase64 = imageBase64;
        this.vendor = vendor;
        this.keywords = keywords;
    }

    public Long getId() {
        return id;
    }

    @PrePersist
    @PreUpdate
    private void validateCategories() {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalStateException("Categories list must not be empty");
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        this.supply = supply;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public Restaurant getVendor() {
        return vendor;
    }

    public void setVendor(Restaurant vendor) {
        this.vendor = vendor;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public List<Rating> getRatings()
    {
        return ratings;
    }

    public void setRatings(List<Rating> ratings)
    {
        this.ratings = ratings;
    }

    public BigDecimal calculateAverageRating()
    {
        BigDecimal sum = BigDecimal.ZERO;
        if (ratings == null || ratings.isEmpty())
            return sum;
        for (Rating rating : ratings)
            sum = sum.add(BigDecimal.valueOf(rating.getRating()));
        return sum.divide(BigDecimal.valueOf(ratings.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Objects.equals(id, food.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}