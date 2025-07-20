package com.aut.shoomal.dao.impl;

import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.dao.FoodDao;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class FoodDaoImpl extends GenericDaoImpl<Food> implements FoodDao {
    @Override
    public List<Food> searchByName(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Food> query = session.createQuery(
                    "FROM Food WHERE lower(name) LIKE :kw AND supply > 0", Food.class);
            query.setParameter("kw", "%" + keyword.toLowerCase() + "%");
            return query.list();
        } catch (Exception e) {
            System.err.println("Error searching foods: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Food> findByRestaurantId(Long restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Food> query = session.createQuery(
                    "FROM Food WHERE vendor.id = :rid", Food.class);
            query.setParameter("rid", restaurantId);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error finding food by restaurantId: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Food> findByKeyword(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Food> query = session.createQuery(
                    "SELECT f FROM Food f JOIN f.keywords c WHERE LOWER(c) = :cat", Food.class);
            query.setParameter("cat", keyword.toLowerCase());
            return query.list();
        } catch (Exception e) {
            System.err.println("Error finding food by keyword: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Food> getFoodsByMenuTitle(Session session, Long restaurantId, String title)
    {
        try {
            Query<Food> query = session.createQuery
                    ( "SELECT f FROM Food f JOIN f.menus m WHERE f.vendor.id = :rid AND m.title = :title", Food.class);
            query.setParameter("title", title);
            query.setParameter("rid", restaurantId);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error getting food list: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Food getFoodByIdAndRestaurantId(Session session, Long id, Long restaurantId) {
        try {
            Query<Food> query = session.createQuery("FROM Food WHERE id = :fid AND vendor.id = :rid", Food.class);
            query.setParameter("fid", id);
            query.setParameter("rid", restaurantId);
            return query.uniqueResult();
        } catch (Exception e) {
            System.err.println("Error finding food by id and restaurant id: " + e.getMessage());
            return null;
        }
    }
}