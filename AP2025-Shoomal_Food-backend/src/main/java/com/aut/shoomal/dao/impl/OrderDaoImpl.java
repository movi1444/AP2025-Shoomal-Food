package com.aut.shoomal.dao.impl;

import com.aut.shoomal.dao.OrderDao;
import com.aut.shoomal.payment.Order;
import com.aut.shoomal.payment.OrderStatus;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class OrderDaoImpl extends GenericDaoImpl<Order> implements OrderDao
{
    @Override
    public List<Order> findByVendorId(Integer vendorId)
    {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Order> query = session.createQuery("from Order where restaurant.id = :vendorId", Order.class);
            query.setParameter("vendorId", Long.valueOf(vendorId));
            return query.list();
        } catch (Exception e) {
            System.err.println("Error finding orders from vendor.");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Order> findAllWithFilters(String search, Long vendorId, Long customerId, Long courierId, OrderStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Order> cq = cb.createQuery(Order.class);
            Root<Order> orderRoot = cq.from(Order.class);

            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(orderRoot.get("deliveryAddress")), likePattern));
            }
            if (vendorId != null) {
                predicates.add(cb.equal(orderRoot.get("restaurant").get("id"), vendorId));
            }
            if (customerId != null) {
                predicates.add(cb.equal(orderRoot.get("customer").get("id"), customerId));
            }
            if (courierId != null) {
                predicates.add(cb.equal(orderRoot.get("courier").get("id"), courierId));
            }
            if (status != null) {
                predicates.add(cb.equal(orderRoot.get("orderStatus"), status));
            }

            cq.where(predicates.toArray(new Predicate[0]));

            return session.createQuery(cq).getResultList();
        } catch (Exception e) {
            System.err.println("Error finding all orders with filters: " + e.getMessage());
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
        StringBuilder hql = new StringBuilder("from distinct Order o where o.customer.id = :customerId");
        if (search != null && !search.trim().isEmpty())
            hql.append(" and (lower(o.deliveryAddress) like :search)"); // add more if needed
        if (vendorName != null && !vendorName.trim().isEmpty())
            hql.append(" and lower(o.restaurant.name) like :vendorName");

        hql.append(" left join fetch o.customer");
        hql.append(" left join fetch o.restaurant");
        hql.append(" left join fetch o.courier");
        hql.append(" left join fetch o.coupon");
        hql.append(" left join fetch o.orderItems");
        return hql;
    }
}