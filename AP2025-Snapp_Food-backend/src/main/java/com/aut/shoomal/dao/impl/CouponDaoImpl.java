package com.aut.shoomal.dao.impl;

import com.aut.shoomal.dao.CouponDao;
import com.aut.shoomal.payment.coupon.Coupon;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class CouponDaoImpl extends GenericDaoImpl<Coupon> implements CouponDao
{

    @Override
    public Coupon getCouponByCode(String couponCode)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery("from Coupon where couponCode = :couponCode", Coupon.class);
            query.setParameter("couponCode", couponCode);
            return query.uniqueResult();
        } catch (Exception e) {
            System.out.println("Error getting coupon by couponCode: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}