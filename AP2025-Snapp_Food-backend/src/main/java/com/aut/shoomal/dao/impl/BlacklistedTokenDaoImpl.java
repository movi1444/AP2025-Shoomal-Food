package com.aut.shoomal.dao.impl;

import com.aut.shoomal.auth.BlacklistedToken;
import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class BlacklistedTokenDaoImpl extends GenericDaoImpl<BlacklistedToken> implements BlacklistedTokenDao
{
    @Override
    public boolean isTokenBlacklisted(String token)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(bt.id) FROM BlacklistedToken bt WHERE bt.token = :token AND bt.expirationDate > CURRENT_TIMESTAMP", Long.class);
            query.setParameter("token", token);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            System.err.println("Error checking if token is blacklisted: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to check blacklist status.", e);
        }
    }
}