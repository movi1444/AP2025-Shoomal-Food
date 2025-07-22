package com.aut.shoomal.dao.impl;

import com.aut.shoomal.dao.RatingDao;
import com.aut.shoomal.rating.Rating;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class RatingDaoImpl extends GenericDaoImpl<Rating> implements RatingDao
{

    @Override
    public List<Rating> checkConflict(Session session, Long userId, Long foodId, Integer orderId)
    {
        try {
            Query<Rating> query = session.createQuery("select r from Rating r where r.user.id = :uid and r.food.id = :fid and r.order.id = :oid", Rating.class);
            query.setParameter("uid", userId);
            query.setParameter("fid", foodId);
            query.setParameter("oid", orderId);
            return query.list();
        } catch (Exception e) {
            System.err.println("Could not check conflict: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Rating> getByOrderId(Session session, Long userId, Integer orderId)
    {
        try {
            Query<Rating> query = session.createQuery("select r from Rating r where r.user.id = :uid and r.order.id = :oid", Rating.class);
            query.setParameter("uid", userId);
            query.setParameter("oid", orderId);
            return query.list();
        } catch (Exception e) {
            System.err.println("Could not check conflict: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}