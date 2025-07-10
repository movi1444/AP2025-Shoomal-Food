package com.aut.shoomal.dao.impl;

import com.aut.shoomal.entity.menu.Menu;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.user.Seller;
import com.aut.shoomal.dao.RestaurantDao;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class RestaurantDaoImpl extends GenericDaoImpl<Restaurant> implements RestaurantDao {
    @Override
    public Restaurant findByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery("FROM Restaurant WHERE name = :name", Restaurant.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error finding restaurant by name: " + name, e);
        }
    }

    @Override
    public void addMenu(Long restaurantId, Long menuId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            Menu menu = session.get(Menu.class, menuId);
            if (restaurant != null && menu != null) {
                menu.setRestaurant(restaurant);
                session.merge(menu);
                restaurant.addMenu(menu);
                session.merge(restaurant);
            } else {
                throw new RuntimeException("Restaurant or Menu not found for addMenu operation.");
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error adding menu to restaurant.", e);
        }
    }

    @Override
    public void removeMenu(Long restaurantId, Long menuId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            Menu menu = session.get(Menu.class, menuId);
            if (restaurant != null && menu != null) {
                restaurant.removeMenu(menu);
                session.merge(restaurant);
            } else {
                throw new RuntimeException("Restaurant or Menu not found for removeMenu operation.");
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error removing menu from restaurant.", e);
        }
    }

    @Override
    public List<Restaurant> searchByName(String partialName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery(
                    "FROM Restaurant WHERE lower(name) LIKE :name AND approved = true", Restaurant.class);
            query.setParameter("name", "%" + partialName.toLowerCase() + "%");
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error searching restaurants by name: " + partialName, e);
        }
    }

    @Override
    public List<Restaurant> findByOwner(Seller owner) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery("FROM Restaurant WHERE owner = :owner", Restaurant.class);
            query.setParameter("owner", owner);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding restaurants by owner: " + owner.getId(), e);
        }
    }

    @Override
    public List<Restaurant> findAllApproved() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Restaurant> query = session.createQuery("FROM Restaurant WHERE approved = true", Restaurant.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error finding approved restaurants.");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateApprovalStatus(Long restaurantId, boolean approved) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                throw new RuntimeException("Restaurant not found with ID: " + restaurantId);
            }

            restaurant.setApproved(approved);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating approval status for restaurant with ID: " + restaurantId, e);
        }
    }
}