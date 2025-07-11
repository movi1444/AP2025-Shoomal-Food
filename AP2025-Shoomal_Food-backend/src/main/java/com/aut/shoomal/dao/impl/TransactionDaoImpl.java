package com.aut.shoomal.dao.impl;

import com.aut.shoomal.dao.TransactionDao;
import com.aut.shoomal.payment.transaction.PaymentTransaction;
import com.aut.shoomal.payment.PaymentMethod;
import com.aut.shoomal.payment.transaction.PaymentTransactionStatus;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.JoinType;

import java.util.ArrayList;
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
    public PaymentTransaction findByOrderId(Session session, Integer orderId)
    {
        try {
            Query<PaymentTransaction> query = session.createQuery("from PaymentTransaction where order.id = :orderId", PaymentTransaction.class);
            query.setParameter("orderId", orderId);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("Error finding payment transaction by order id: " + e.getMessage());
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

    @Override
    public List<PaymentTransaction> findAllWithFilters(String search, Long userId, PaymentMethod method, PaymentTransactionStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<PaymentTransaction> cq = cb.createQuery(PaymentTransaction.class);
            Root<PaymentTransaction> transactionRoot = cq.from(PaymentTransaction.class);

            List<Predicate> predicates = new ArrayList<>();

            transactionRoot.fetch("user", JoinType.LEFT);
            transactionRoot.fetch("order", JoinType.LEFT);

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                Predicate userSearch = cb.like(cb.lower(transactionRoot.get("user").get("name")), likePattern);
                Predicate orderIdSearch = cb.like(cb.lower(cb.function("CAST", String.class, transactionRoot.get("order").get("id"), cb.literal("CHAR"))), likePattern);
                predicates.add(cb.or(userSearch, orderIdSearch));
            }
            if (userId != null) {
                predicates.add(cb.equal(transactionRoot.get("user").get("id"), userId));
            }
            if (method != null) {
                predicates.add(cb.equal(transactionRoot.get("method"), method));
            }
            if (status != null) {
                predicates.add(cb.equal(transactionRoot.get("status"), status));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            cq.select(transactionRoot).distinct(true);

            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            System.err.println("Error finding all transactions with filters: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}