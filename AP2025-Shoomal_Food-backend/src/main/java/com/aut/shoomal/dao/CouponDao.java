package com.aut.shoomal.dao;

import com.aut.shoomal.payment.coupon.Coupon;

public interface CouponDao extends GenericDao<Coupon>
{
    Coupon getCouponByCode(String couponCode);
}