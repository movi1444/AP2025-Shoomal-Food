package com.aut.shoomal.payment.order;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.payment.coupon.Coupon;
import com.aut.shoomal.payment.transaction.PaymentTransaction;
import com.aut.shoomal.rating.Rating;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private User courier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;
    @Column(name = "raw_price", nullable = false)
    private Integer rawPrice;
    @Column(name = "tax_fee", nullable = false)
    private Integer taxFee;
    @Column(name = "additional_fee", nullable = false)
    private Integer additionalFee;
    @Column(name = "courier_fee", nullable = false)
    private Integer courierFee;
    @Column(name = "pay_price", nullable = false)
    private Integer payPrice;
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<PaymentTransaction> transactions = new ArrayList<>();

    public Order()
    {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Order(Restaurant restaurant, User customer, User courier, Coupon coupon, List<OrderItem> orderItems,
                 String deliveryAddress, Integer rawPrice, Integer taxFee, Integer additionalFee,
                 Integer courierFee, Integer payPrice, OrderStatus orderStatus)
    {
        this();
        this.restaurant = restaurant;
        this.customer = customer;
        this.courier = courier;
        this.coupon = coupon;
        this.orderItems = orderItems;
        this.deliveryAddress = deliveryAddress;
        this.rawPrice = rawPrice;
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
        this.courierFee = courierFee;
        this.payPrice = payPrice;
        this.orderStatus = orderStatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public User getCourier() {
        return courier;
    }

    public void setCourier(User courier) {
        this.courier = courier;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Integer getRawPrice() {
        return rawPrice;
    }

    public void setRawPrice(Integer rawPrice) {
        this.rawPrice = rawPrice;
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

    public Integer getCourierFee() {
        return courierFee;
    }

    public void setCourierFee(Integer courierFee) {
        this.courierFee = courierFee;
    }

    public Integer getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(Integer payPrice) {
        this.payPrice = payPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OrderStatus getOrderStatus()
    {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void addOrderItem(OrderItem orderItem)
    {
        if (this.orderItems == null)
            this.orderItems = new ArrayList<>();
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public Coupon getCoupon()
    {
        return coupon;
    }

    public void setCoupon(Coupon coupon)
    {
        this.coupon = coupon;
    }

    public List<Rating> getRatings()
    {
        return ratings;
    }

    public void setRatings(List<Rating> ratings)
    {
        this.ratings = ratings;
    }

    public List<PaymentTransaction> getTransactions()
    {
        return transactions;
    }

    public void setTransactions(List<PaymentTransaction> transactions)
    {
        this.transactions = transactions;
    }

    public void addTransaction(PaymentTransaction transaction)
    {
        this.transactions.add(transaction);
        transaction.setOrder(this);
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
    }
}