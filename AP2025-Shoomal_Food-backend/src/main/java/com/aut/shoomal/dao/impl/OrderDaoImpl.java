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

    public List<Order> findOrdersWithFilters(Long customerId, String search, String vendorName, String customerName, String courierName, OrderStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> orderRoot = cq.from(Order.class);

            orderRoot.fetch("orderItems", JoinType.LEFT);
            orderRoot.fetch("restaurant", JoinType.LEFT);
            orderRoot.fetch("customer", JoinType.LEFT);
            orderRoot.fetch("courier", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            if (customerId != null) {
                predicates.add(cb.equal(orderRoot.get("customer").get("id"), customerId));
            }

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(orderRoot.join("orderItems").get("food").get("name")), likePattern));
            }

            if (vendorName != null && !vendorName.trim().isEmpty()) {
                String likePattern = "%" + vendorName.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(orderRoot.get("restaurant").get("name")), likePattern));
            }
            if (customerName != null && !customerName.trim().isEmpty()) {
                String likePattern = "%" + customerName.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(orderRoot.get("customer").get("name")), likePattern));
            }
            if (courierName != null && !courierName.trim().isEmpty()) {
                predicates.add(cb.equal(orderRoot.get("courier").get("id"), courierName));
            }
            if (status != null) {
                predicates.add(cb.equal(orderRoot.get("orderStatus"), status));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            cq.select(orderRoot).distinct(true);
            cq.orderBy(cb.desc(orderRoot.get("createdAt")));

            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            System.err.println("Error finding orders with filters: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
}