package com.aut.shoomal.dao.impl;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.user.Courier;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.dao.UserDao;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

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
    public void updateApprovalStatus(Long id, boolean approved) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);
            if (user == null) {
                throw new RuntimeException("User not found with ID: " + id);
            }

            if (!(user instanceof Courier)) {
                throw new RuntimeException("Approval status update not applicable for this user type.");
            }

            ((Courier) user).setApproved(approved);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating approval status for user with ID: " + id, e);
        }
    }
}