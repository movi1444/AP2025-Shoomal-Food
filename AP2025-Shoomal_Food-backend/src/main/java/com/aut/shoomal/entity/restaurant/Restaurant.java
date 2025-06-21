package com.aut.shoomal.entity.restaurant;

import com.aut.shoomal.entity.user.Seller;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.menu.Menu;
import com.aut.shoomal.payment.order.Order;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    private String workingHours;

    @Column(name = "logo_base64", columnDefinition = "TEXT")
    private String logoBase64;

    @Column(length = 1000)
    private String description;

    private Boolean approved = false;

    @Column(name = "tax_fee", nullable = false)
    private Integer taxFee;

    @Column(name = "additional_fee", nullable = false)
    private Integer additionalFee;

    @OneToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller owner;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
    private List<Food> foods = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<Order> orders;

    @ManyToMany(mappedBy = "favorites")
    private List<User> users = new ArrayList<>();

    public Restaurant() {}

    public Restaurant(String name, String phone, String address, String workingHours,
                      String logoBase64, String description,Integer taxFee,Integer additionalFee, Seller owner) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.workingHours = workingHours;
        this.logoBase64 = logoBase64;
        this.description = description;
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
        this.owner = owner;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Seller getOwner() {
        return owner;
    }

    public void setOwner(Seller owner) {
        this.owner = owner;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public Integer getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(Integer taxFee) {
        this.taxFee = taxFee;
    }

    public Integer getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(Integer additionalFee) {
        this.additionalFee = additionalFee;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void addMenu(Menu menu) {
        menus.add(menu);
        menu.setRestaurant(this);
    }

    public void removeMenu(Menu menu) {
        menus.remove(menu);
        menu.setRestaurant(null);
    }

    public List<Food> getFoods() {
        return foods;
    }

    public void setFoods(List<Food> foods) {
        this.foods = foods;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void addFoodItem(Food food) {
        foods.add(food);
        food.setVendor(this);
    }
}