package com.aut.shoomal.payment.coupon;

import com.aut.shoomal.payment.order.Order;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "coupons")
public class Coupon
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "coupon_code", nullable = false, unique = true)
    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CouponType couponType;

    @OneToMany(mappedBy = "coupon", fetch = FetchType.LAZY)
    private Set<Order> orders;

    @Column(name = "value", nullable = false)
    private Integer value;
    @Column(name = "min_price", nullable = false)
    private Integer minPrice;
    @Column(name = "user_count", nullable = false)
    private Integer userCount;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public Coupon() {}

    public Coupon(String couponCode, CouponType couponType, Integer value,
                  Integer userCount, Integer minPrice, LocalDate startDate, LocalDate endDate)
    {
        this.couponCode = couponCode;
        this.couponType = couponType;
        this.value = value;
        this.userCount = userCount;
        this.minPrice = minPrice;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public LocalDate getEndDate()
    {
        return endDate;
    }

    public void setEndDate(LocalDate endDate)
    {
        this.endDate = endDate;
    }

    public LocalDate getStartDate()
    {
        return startDate;
    }

    public void setStartDate(LocalDate startDate)
    {
        this.startDate = startDate;
    }

    public Integer getUserCount()
    {
        return userCount;
    }

    public void setUserCount(Integer userCount)
    {
        this.userCount = userCount;
    }

    public Integer getMinPrice()
    {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice)
    {
        this.minPrice = minPrice;
    }

    public Integer getValue()
    {
        return value;
    }

    public void setValue(Integer value)
    {
        this.value = value;
    }

    public CouponType getCouponType()
    {
        return couponType;
    }

    public void setCouponType(CouponType couponType)
    {
        this.couponType = couponType;
    }

    public String getCouponCode()
    {
        return couponCode;
    }

    public void setCouponCode(String couponCode)
    {
        this.couponCode = couponCode;
    }

    public Set<Order> getOrders()
    {
        return orders;
    }

    public void setOrders(Set<Order> orders)
    {
        this.orders = orders;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(id, coupon.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(id);
    }
}