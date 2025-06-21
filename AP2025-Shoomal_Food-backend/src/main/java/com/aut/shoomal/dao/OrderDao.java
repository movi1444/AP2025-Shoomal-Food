package com.aut.shoomal.dao;

import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderStatus;
import org.hibernate.Session;

import java.util.List;

public interface OrderDao extends GenericDao<Order>
{
    List<Order> findByVendorId(Integer vendorId);
    List<Order> findAllWithFilters(String search, Long vendorId, Long customerId, Long courierId, OrderStatus status);
    List<Order> findWithFilters(Session session, Long customerId, String search, String vendorName);
}