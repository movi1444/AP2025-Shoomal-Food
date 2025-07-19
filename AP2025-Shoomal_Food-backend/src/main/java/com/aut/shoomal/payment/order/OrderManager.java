package com.aut.shoomal.payment.order;

import com.aut.shoomal.dao.OrderDao;
import com.aut.shoomal.dto.request.OrderItemRequest;
import com.aut.shoomal.entity.food.Food;
import com.aut.shoomal.entity.food.FoodManager;
import com.aut.shoomal.entity.restaurant.Restaurant;
import com.aut.shoomal.entity.restaurant.RestaurantManager;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.exceptions.InvalidCouponException;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.coupon.Coupon;
import com.aut.shoomal.payment.coupon.CouponManager;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderManager
{
    private final OrderDao orderDao;
    private final UserManager userManager;
    private final RestaurantManager restaurantManager;
    private final FoodManager foodManager;
    private final CouponManager couponManager;
    public OrderManager(OrderDao orderDao, UserManager userManager, CouponManager couponManager,
                        RestaurantManager restaurantManager, FoodManager foodManager)
    {
        this.orderDao = orderDao;
        this.userManager = userManager;
        this.couponManager = couponManager;
        this.restaurantManager = restaurantManager;
        this.foodManager = foodManager;
    }

    public void createOrder(Order order)
    {
        orderDao.create(order);
    }

    public void createOrder(Order order, Session session)
    {
        orderDao.create(order, session);
    }

    public void updateOrder(Order order)
    {
        orderDao.update(order);
    }

    public void updateOrder(Order order, Session session)
    {
        orderDao.update(order, session);
    }

    public void deleteOrder(Integer orderId)
    {
        orderDao.delete(Long.valueOf(orderId));
    }

    public Order findOrderById(Integer orderId)
    {
        return orderDao.findById(Long.valueOf(orderId));
    }

    public List<Order> getOrdersByVendor(Integer vendorId)
    {
        return orderDao.findOrdersWithFilters(null, null, null, null, null, null); // Adjust if vendor filtering is needed here
    }

    public List<Order> getOrderHistory(Session session, Long customerId, String search, String vendorName)
    {

        return orderDao.findOrdersWithFilters(customerId, search, vendorName, null, null, null);
    }

    public List<Order> getAllOrders(String search, String vendorName, String customerName, String courierName, String statusString) {
        OrderStatus status = (statusString != null && !statusString.isEmpty()) ? OrderStatus.fromName(statusString) : null;
        return orderDao.findOrdersWithFilters(null, search, vendorName, customerName, courierName, status);
    }

    public Order submitOrder(Long customerId, Long vendorId, Integer couponId,
                             String deliveryAddress, List<OrderItemRequest> items)
    {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            User customer = session.get(User.class, customerId);
            if (customer == null) throw new NotFoundException("Customer with ID " + customerId + " not found.");
            if (customer.getRole().getName().equals("seller") || customer.getRole().getName().equals("courier") || customer.getRole().getName().equals("admin"))
            {
                throw new InvalidInputException("Customer with ID " + customerId + " is not a buyer.");
            }
            Restaurant restaurant = session.get(Restaurant.class, vendorId);
            if (restaurant == null) throw new NotFoundException("Restaurant with ID " + vendorId + " not found.");

            Order order = new Order();
            order.setRestaurant(restaurant);
            order.setCustomer(customer);
            order.setDeliveryAddress(deliveryAddress);
            order.setOrderStatus(OrderStatus.UNPAID_AND_CANCELLED);

            Coupon coupon;
            BigDecimal discountAmount = BigDecimal.ZERO;

            BigDecimal rawPrice = BigDecimal.ZERO;
            for (OrderItemRequest request : items)
            {
                Food food = session.get(Food.class, Long.valueOf(request.getItemId()));
                if (food == null) throw new NotFoundException("Food with ID " + request.getItemId() + " not found.");
                if (request.getQuantity() <= 0) throw new InvalidInputException("Quantity must be positive.");
                if (food.getSupply() < request.getQuantity()) throw new InvalidInputException("Supply is less than the quantity.");

                int priceAtOrder = (int) food.getPrice();
                OrderItem newItem = new OrderItem(food, request.getQuantity(), null, priceAtOrder);
                order.addOrderItem(newItem);

                rawPrice = rawPrice.add(BigDecimal.valueOf(priceAtOrder).multiply(BigDecimal.valueOf(request.getQuantity())));
                food.setSupply(food.getSupply() - request.getQuantity());
                foodManager.updateFood(food, session);
            }

            order.setRawPrice(rawPrice.intValue());

            BigDecimal taxFee = BigDecimal.valueOf(restaurant.getTaxFee());
            BigDecimal additionalFee = BigDecimal.valueOf(restaurant.getAdditionalFee());
            BigDecimal courierFee = BigDecimal.valueOf(500);
            BigDecimal subtotal = rawPrice.add(taxFee).add(additionalFee).add(courierFee);

            if (couponId != null) {
                coupon = couponManager.getCouponById(couponId);
                if (coupon == null) throw new NotFoundException("Coupon with ID " + couponId + " not found.");
                couponManager.validateCoupon(coupon, subtotal.intValue());
                if (coupon.getCouponType().getName().equalsIgnoreCase("fixed"))
                    discountAmount = coupon.getValue();
                else
                    discountAmount = subtotal.multiply(BigDecimal.valueOf(5).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP));

                if (discountAmount.compareTo(subtotal) > 0)
                    discountAmount = subtotal;
                couponManager.decrementCouponCount(coupon, session);
                order.setCoupon(coupon);
            }

            BigDecimal payPrice = subtotal.subtract(discountAmount);
            order.setTaxFee(taxFee.intValue());
            order.setAdditionalFee(additionalFee.intValue());
            order.setCourierFee(courierFee.intValue());
            order.setPayPrice(payPrice.intValue());

            this.createOrder(order, session);

            transaction.commit();
            return order;
        } catch (InvalidCouponException e) {
            if (transaction != null) transaction.rollback();
            System.out.println("Order submission failed due to invalid coupon: " + e.getMessage());
            throw new InvalidInputException("Invalid coupon: " + e.getMessage());
        } catch (InvalidInputException e) {
            if (transaction != null) transaction.rollback();
            System.out.println("Order submission failed due to invalid input: " + e.getMessage());
            throw e;
        } catch (NotFoundException e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Order submission failed. Resource not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Order submission failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to submit order: " + e.getMessage(), e);
        } finally {
            if (session != null) session.close();
        }
    }
}