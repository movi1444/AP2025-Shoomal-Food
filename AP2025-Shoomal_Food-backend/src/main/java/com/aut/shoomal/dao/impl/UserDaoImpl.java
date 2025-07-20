package com.aut.shoomal.dao.impl;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.user.Courier;
import com.aut.shoomal.entity.user.Seller;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.dao.UserDao;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UserDaoImpl extends GenericDaoImpl<User> implements UserDao
{
    @Override
    public User findByEmail(String email) throws NotFoundException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("from User where email = :email", User.class);
            query.setParameter("email", email);
            User user = query.uniqueResult();
            if (user == null)
                throw new NotFoundException("User not found with email: " + email);
            return user;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error finding user by email " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User findByPhoneNumber(String phoneNumber) throws NotFoundException
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("from User where phoneNumber = :phoneNumber", User.class);
            query.setParameter("phoneNumber", phoneNumber);
            User user = query.uniqueResult();
            if (user == null)
                throw new NotFoundException("User not found with phone number: " + phoneNumber);
            return user;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error finding user by phone number " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User findByUsername(String username) throws NotFoundException
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<User> query = session.createQuery("from User where name = :username", User.class);
            query.setParameter("username", username);
            User user = query.uniqueResult();
            if (user == null)
                throw new NotFoundException("User not found with name: " + username);
            return user;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error finding user by name " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<User> findCustomersWithOrder(Session session, Long restaurantId)
    {
        try {
            Query<User> query = session.createQuery
                    ("select u from User u join u.customerOrders co join co.restaurant r where r.id = :rid", User.class);
            query.setParameter("rid", restaurantId);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error finding users with order " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<User> findCouriersWithOrder(Session session, Long restaurantId)
    {
        try {
            Query<User> query = session.createQuery
                    ("select u from User u join u.courierOrders co join co.restaurant r where r.id = :rid", User.class);
            query.setParameter("rid", restaurantId);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error finding users with order " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateApprovalStatus(Long id, boolean approved) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);
            switch (user) {
                case null -> throw new RuntimeException("User not found with ID: " + id);
                case Courier courier -> courier.setApproved(approved);
                case Seller seller -> seller.setApproved(approved);
                default -> throw new RuntimeException("Approval status update not applicable for this user type.");
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating approval status for user with ID: " + id, e);
        }
    }
}