package com.aut.shoomal.dao.impl;

import com.aut.shoomal.Mamad.menu.Menu;
import com.aut.shoomal.dao.MenuDao;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class MenuDaoImpl extends GenericDaoImpl<Menu> implements MenuDao {

    @Override
    public List<Menu> findByRestaurantId(Long restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Menu> query = session.createQuery("FROM Menu WHERE restaurant.id = :restaurantId", Menu.class);
            query.setParameter("restaurantId", restaurantId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding menus by restaurant ID: " + restaurantId, e);
        }
    }

    @Override
    public Optional<Menu> findByRestaurantIdAndTitle(Long restaurantId, String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Menu> query = session.createQuery("FROM Menu WHERE restaurant.id = :restaurantId AND title = :title", Menu.class);
            query.setParameter("restaurantId", restaurantId);
            query.setParameter("title", title);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            throw new RuntimeException("Error finding menu by restaurant ID and title: " + restaurantId + ", " + title, e);
        }
    }

    @Override
    public List<Menu> findByFoodItemId(Long foodItemId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Menu> query = session.createQuery(
                    "SELECT DISTINCT m FROM Menu m JOIN m.foods f WHERE f.id = :foodItemId", Menu.class);
            query.setParameter("foodItemId", foodItemId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding menus containing food item ID: " + foodItemId, e);
        }
    }
}