package com.aut.shoomal.Mamad.food;

import com.aut.shoomal.Mamad.restaurant.Restaurant;
import com.aut.shoomal.payment.OrderItem;
import jakarta.persistence.*;
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
    @CollectionTable(name = "food_item_categories", joinColumns = @JoinColumn(name = "food_item_id"))
    @Column(name = "category")
    private List<String> categories;

    @OneToMany(mappedBy = "food", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
    public Food() {}

    public Food(String name, String description, double price, int supply,
                    String imageBase64, Restaurant vendor, List<String> categories) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.supply = supply;
        this.imageBase64 = imageBase64;
        this.vendor = vendor;
        this.categories = categories;
    }

    public Long getId() {
        return id;
    }

    @PrePersist
    @PreUpdate
    private void validateCategories() {
        if (categories == null || categories.isEmpty()) {
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

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
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