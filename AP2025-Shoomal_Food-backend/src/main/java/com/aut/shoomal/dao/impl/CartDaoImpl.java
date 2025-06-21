package com.aut.shoomal.dao.impl;

import com.aut.shoomal.entity.cart.Cart;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.dao.CartDao;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class CartDaoImpl extends GenericDaoImpl<Cart> implements CartDao {

    @Override
    public Cart findByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Cart> query = session.createQuery("FROM Cart sc WHERE sc.user.id = :userId", Cart.class);
            query.setParameter("userId", userId);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error finding shopping cart by user ID: " + userId, e);
        }
    }

    @Override
    public Cart findByUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Cart> query = session.createQuery("FROM Cart sc WHERE sc.user = :user", Cart.class);
            query.setParameter("user", user);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error finding shopping cart by user: " + user.getId(), e);
        }
    }
}