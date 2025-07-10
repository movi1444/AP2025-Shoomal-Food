package com.aut.shoomal.payment.coupon;

import com.aut.shoomal.payment.order.Order;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
<<<<<<< HEAD
    private Integer value;
=======
    private BigDecimal value;
>>>>>>> a2f6b05ac90114b00207d6b28ffe919b5874949a
    @Column(name = "min_price", nullable = false)
    private Integer minPrice;
    @Column(name = "user_count", nullable = false)
    private Integer userCount;
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    public Coupon() {}

<<<<<<< HEAD
    public Coupon(String couponCode, CouponType couponType, Integer value, Integer minPrice, Integer userCount,
                  LocalDate startDate, LocalDate endDate, String scope)
=======
    public Coupon(String couponCode, CouponType couponType, BigDecimal value, Integer minPrice, Integer userCount,
                  LocalDateTime startDate, LocalDateTime endDate)
>>>>>>> a2f6b05ac90114b00207d6b28ffe919b5874949a
    {
        this.couponCode = couponCode;
        this.couponType = couponType;
        this.value = value;
        this.minPrice = minPrice;
        this.userCount = userCount;
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

    public LocalDateTime getEndDate()
    {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate)
    {
        this.endDate = endDate;
    }

    public LocalDateTime getStartDate()
    {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate)
    {
        this.startDate = startDate;
    }

    public BigDecimal getValue()
    {
        return value;
    }

    public void setValue(BigDecimal value)
    {
        this.value = value;
    }

    public Integer getMinPrice()
    {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice)
    {
        this.minPrice = minPrice;
    }

    public Integer getUserCount()
    {
        return userCount;
    }

    public void setUserCount(Integer userCount)
    {
        this.userCount = userCount;
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

<<<<<<< HEAD
    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

=======
>>>>>>> a2f6b05ac90114b00207d6b28ffe919b5874949a
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