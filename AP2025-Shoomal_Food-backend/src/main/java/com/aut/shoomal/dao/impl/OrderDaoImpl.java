package com.aut.shoomal.dao.impl;

import com.aut.shoomal.dao.OrderDao;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderStatus;
import com.aut.shoomal.util.HibernateUtil;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import com.aut.shoomal.entity.food.Food;

public class OrderDaoImpl extends GenericDaoImpl<Order> implements OrderDao
{
    @Override
    public List<Order> findByVendorId(Integer vendorId)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.restaurant.id = :vendorId", Order.class);
            query.setParameter("vendorId", Long.valueOf(vendorId));
            return query.list();
        } catch (Exception e) {
            System.err.println("Error finding orders from vendor.");
            e.printStackTrace();
            return null;
        }
    }

    public List<Order> findOrdersWithFilters(String search, String vendorName, String customerName, String courierName, OrderStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = createQuery(search, vendorName, customerName, courierName, status);
            Query<Order> query = session.createQuery(hql, Order.class);
            if (search != null && !search.trim().isEmpty())
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            if (vendorName != null && !vendorName.trim().isEmpty())
                query.setParameter("vendorName", "%" + vendorName.toLowerCase() + "%");
            if (customerName != null && !customerName.trim().isEmpty())
                query.setParameter("customerName", "%" + customerName.toLowerCase() + "%");
            if (courierName != null && !courierName.trim().isEmpty())
                query.setParameter("courierName", "%" + courierName.toLowerCase() + "%");
            if (status != null)
                 query.setParameter("status", status);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error finding orders with filters: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String createQuery(String search, String vendorName, String customerName, String courierName, OrderStatus status)
    {
        StringBuilder hql = new StringBuilder("SELECT DISTINCT o FROM Order o ");
        hql.append("LEFT JOIN FETCH o.customer c ");
        hql.append("LEFT JOIN FETCH o.restaurant r ");
        hql.append("LEFT JOIN FETCH o.courier co ");
        hql.append("LEFT JOIN FETCH o.orderItems oi ");
        hql.append("LEFT JOIN FETCH oi.food f ");

        boolean firstCondition = true;
        if (search != null && !search.trim().isEmpty()) {
            if (firstCondition) {
                hql.append("WHERE ");
                firstCondition = false;
            } else {
                hql.append("AND ");
            }
            hql.append("LOWER(f.name) LIKE :search ");
        }

        if (vendorName != null && !vendorName.trim().isEmpty()) {
            if (firstCondition) {
                hql.append("WHERE ");
                firstCondition = false;
            } else {
                hql.append("AND ");
            }
            hql.append("LOWER(r.name) LIKE :vendorName ");
        }

        if (customerName != null && !customerName.trim().isEmpty()) {
            if (firstCondition) {
                hql.append("WHERE ");
                firstCondition = false;
            } else {
                hql.append("AND ");
            }
            hql.append("LOWER(c.name) LIKE :customerName ");
        }

        if (courierName != null && !courierName.trim().isEmpty()) {
            if (firstCondition) {
                hql.append("WHERE ");
                firstCondition = false;
            } else {
                hql.append("AND ");
            }
            hql.append("LOWER(co.name) LIKE :courierName ");
        }

        if (status != null) {
            if (firstCondition) {
                hql.append("WHERE ");
                firstCondition = false;
            } else {
                hql.append("AND ");
            }
            hql.append("o.orderStatus = :status ");
        }

        hql.append("ORDER BY o.createdAt DESC");

        return hql.toString();
    }

    @Override
    public List<Order> findWithFilters(Session session, Long customerId, String search, String vendorName)
    {
        StringBuilder hql = getHql(search, vendorName);

        Query<Order> query = session.createQuery(hql.toString(), Order.class);
        query.setParameter("customerId", customerId);
        if (search != null && !search.trim().isEmpty())
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        if (vendorName != null && !vendorName.trim().isEmpty())
            query.setParameter("vendorName", "%" + vendorName.toLowerCase() + "%");
        return query.list();
    }

    private static StringBuilder getHql(String search, String vendorName)
    {
        StringBuilder hql = new StringBuilder("select o from Order o where o.customer.id = :customerId");
        if (search != null && !search.trim().isEmpty())
            hql.append(" and exists (select oi from OrderItem oi join oi.food f where oi.order.id = o.id and lower(f.name) like :search)");
        if (vendorName != null && !vendorName.trim().isEmpty())
            hql.append(" and lower(o.restaurant.name) like :vendorName");
        return hql;
    }

    public List<Restaurant> findTop5MostOrderedRestaurants(String searchKeyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT o.restaurant FROM Order o " +
                    "WHERE LOWER(o.restaurant.name) LIKE :searchKeyword " +
                    "GROUP BY o.restaurant.id, o.restaurant.name " +
                    "ORDER BY COUNT(o.id) DESC";
            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            query.setParameter("searchKeyword", "%" + searchKeyword.toLowerCase() + "%");
            query.setMaxResults(5);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error finding top 5 most ordered restaurants: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Food> findTop5MostOrderedFoods(String searchKeyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT oi.food FROM OrderItem oi " +
                    "WHERE LOWER(oi.food.name) LIKE :searchKeyword " +
                    "AND oi.food.supply > 0 " +
                    "GROUP BY oi.food.id, oi.food.name " +
                    "ORDER BY COUNT(oi.id) DESC";
            Query<Food> query = session.createQuery(hql, Food.class);
            query.setParameter("searchKeyword", "%" + searchKeyword.toLowerCase() + "%");
            query.setMaxResults(5);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error finding top 5 most ordered foods: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}