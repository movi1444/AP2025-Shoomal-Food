package com.aut.shoomal.payment.coupon;

import com.aut.shoomal.dao.CouponDao;
import com.aut.shoomal.exceptions.InvalidCouponException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.ServiceUnavailableException;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.time.format.DateTimeParseException;

public class CouponManager
{
    private final CouponDao couponDao;
    public CouponManager(CouponDao couponDao)
    {
        this.couponDao = couponDao;
    }

    public void updateCoupon(Coupon coupon)
    {
        couponDao.update(coupon);
    }

    public void updateCoupon(Coupon coupon, Session session)
    {
        couponDao.update(coupon, session);
    }

    public Coupon getCouponById(Integer id)
    {
        return couponDao.findById(Long.valueOf(id));
    }

    public Coupon getCouponByCode(String couponCode)
    {
        return couponDao.getCouponByCode(couponCode);
    }

    public void deleteCoupon(Integer couponId)
    {
        couponDao.delete(Long.valueOf(couponId));
    }

    public void validateCoupon(Coupon coupon, Integer totalPrice) throws InvalidCouponException
    {
        if (coupon == null)
            throw new InvalidCouponException("Coupon is null or does not exist.");
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate().atStartOfDay()) || now.isAfter(coupon.getEndDate().atTime(LocalTime.MAX)))
            throw new InvalidCouponException("Coupon has expired.");
    }

    public void decrementCouponCount(Coupon coupon, Session session)
    {
        if (coupon != null) {
            this.updateCoupon(coupon, session);
        }
    }

    public List<Coupon> getAllCoupons() {
        return couponDao.findAll();
    }

    public Coupon createCoupon(String couponCode, String type, BigDecimal value, String startDate, String endDate,
                               Integer userCount, Integer minPrice)
    {
        StringBuilder errors = new StringBuilder();

        if (couponCode == null || couponCode.trim().isEmpty())
            errors.append(" 'couponCode' is required.");
        if (type == null || type.trim().isEmpty())
            errors.append(" 'type' is required.");

        CouponType couponType = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                couponType = CouponType.fromName(type);
            } catch (IllegalArgumentException e) {
                throw new InvalidCouponException("Invalid coupon type.");
            }
        }

        if (value == null)
            errors.append(" 'value' is required.");
        else if (value.compareTo(BigDecimal.ZERO) <= 0)
            errors.append(" 'value' must be positive.");

        LocalDate start;
        if (startDate == null || startDate.trim().isEmpty())
            throw new InvalidInputException(" 'startDate' is required.");
        try {
            start = LocalDate.parse(startDate);
        } catch (DateTimeParseException e) {
            throw new InvalidCouponException("Invalid start date.");
        }

        LocalDate end;
        if (endDate == null || endDate.trim().isEmpty())
            throw new InvalidInputException(" 'endDate' is required.");
        try {
            end = LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            throw new InvalidCouponException("Invalid end date.");
        }

        if (userCount == null)
            errors.append(" 'userCount' is required.");
        if (minPrice == null)
            errors.append(" 'minPrice' is required.");

        if (!errors.isEmpty())
            throw new InvalidCouponException("400 Invalid input: " + errors);

        Coupon coupon = new Coupon(
                couponCode,
                couponType,
                value,
                minPrice,
                userCount,
                start,
                end
        );
        try {
            couponDao.create(coupon);
            return coupon;
        } catch (Exception e) {
            throw new ServiceUnavailableException("500 Internal Server Error: Failed to create coupon.");
        }
    }
}