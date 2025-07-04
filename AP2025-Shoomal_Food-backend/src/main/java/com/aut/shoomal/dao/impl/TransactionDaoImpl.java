package com.aut.shoomal.dao.impl;

import com.aut.shoomal.dao.TransactionDao;
import com.aut.shoomal.payment.transaction.PaymentTransaction;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class TransactionDaoImpl extends GenericDaoImpl<PaymentTransaction> implements TransactionDao
{
    @Override
    public List<PaymentTransaction> findByUserId(Long userId)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<PaymentTransaction> query = session.createQuery("from PaymentTransaction where user.id = :userId", PaymentTransaction.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error finding payment transaction by user id: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public List<PaymentTransaction> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from PaymentTransaction", PaymentTransaction.class).list();
        } catch (Exception e) {
            System.err.println("Error listing all payment transactions: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}