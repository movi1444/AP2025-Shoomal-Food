package com.aut.shoomal.payment.coupon;

import com.aut.shoomal.dao.CouponDao;
import com.aut.shoomal.exceptions.InvalidCouponException;
import com.aut.shoomal.exceptions.ServiceUnavailableException;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeParseException;


public class CouponManager
{
    private final CouponDao couponDao;
    public CouponManager(CouponDao couponDao)
    {
        this.couponDao = couponDao;
    }

    public void addCoupon(Coupon coupon)
    {
        couponDao.create(coupon);
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
        LocalDate now = LocalDate.now();
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate()))
            throw new InvalidCouponException("Coupon has expired.");

        if (coupon.getMinPrice() > totalPrice)
            throw new InvalidCouponException("Order price does not meet the minimum price.");
        if (coupon.getUserCount() <= 0)
            throw new InvalidCouponException("There are no coupons left to process.");
    }

    public void decrementCouponCount(Coupon coupon, Session session)
    {
        if (coupon != null && coupon.getUserCount() > 0)
        {
            coupon.setUserCount(coupon.getUserCount() - 1);
            this.updateCoupon(coupon, session);
        }
    }

    public List<Coupon> getAllCoupons() {
        return couponDao.findAll();
    }

    public Coupon createCoupon(String couponCode, String type, Integer value, Integer minPrice,
                               Integer userCount, String startDate, String endDate)
    {
        StringBuilder errors = new StringBuilder();
        if (couponCode == null || couponCode.trim().isEmpty())
            errors.append(" 'couponCode' is required.");

        CouponType couponType = null;
        if (type == null || type.trim().isEmpty())
            errors.append(" 'type' is required.");
        else
        {
            try {
                couponType = CouponType.fromName(type);
            } catch (IllegalArgumentException e) {
                System.err.println("400 Invalid input: undefined coupon type.");
                throw new InvalidCouponException("Invalid coupon type.");
            }
        }

        if (value == null)
            errors.append(" 'value' is required.");
        else if (value <= 0)
            errors.append(" 'value' must be positive.");
        if (minPrice == null)
            errors.append(" 'min_price' is required.");
        else if (minPrice < 0)
            errors.append(" 'min_price' can not be negative.");
        if (userCount == null)
            errors.append(" 'user_count' is required.");
        else if (userCount <= 0)
            errors.append(" 'user_count' must be positive.");

        LocalDate start;
        if (startDate == null || startDate.trim().isEmpty())
            errors.append(" 'start_date' is required.");
        try {
            start = LocalDate.parse(startDate);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing 'start_date': " + e.getMessage());
            e.printStackTrace();
            throw new InvalidCouponException("Invalid start date.");
        }

        LocalDate end;
        if (endDate == null || endDate.trim().isEmpty())
            errors.append(" 'end_date' is required.");
        try {
            end = LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing 'end_date': " + e.getMessage());
            e.printStackTrace();
            throw new InvalidCouponException("Invalid end date.");
        }

        if (!errors.isEmpty())
            throw new InvalidCouponException("400 Invalid input: " + errors);

        Coupon coupon = new Coupon(
                couponCode,
                couponType,
                value,
                userCount,
                minPrice,
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