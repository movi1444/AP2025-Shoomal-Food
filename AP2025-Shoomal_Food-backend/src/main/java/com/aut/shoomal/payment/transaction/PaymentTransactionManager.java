package com.aut.shoomal.payment.transaction;

import com.aut.shoomal.dao.TransactionDao;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.PaymentMethod;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderManager;
import com.aut.shoomal.payment.order.OrderStatus;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class PaymentTransactionManager
{
    private final TransactionDao transactionDao;
    private final OrderManager orderManager;
    public PaymentTransactionManager(TransactionDao transactionDao, OrderManager orderManager)
    {
        this.transactionDao = transactionDao;
        this.orderManager = orderManager;
    }

    public PaymentTransaction createTransaction(Session session, User user, Order order, PaymentMethod PaymentMethod, BigDecimal amount, PaymentTransactionStatus transactionStatus)
    {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidInputException("Amount must be greater than zero.");
        PaymentTransaction paymentTransaction = new PaymentTransaction(order, user, amount, PaymentMethod);
        paymentTransaction.setStatus(transactionStatus);
        transactionDao.create(paymentTransaction, session);
        return paymentTransaction;
    }

    public List<PaymentTransaction> getAllTransactions(String search, Long userId, String methodStr, String statusStr) {
        PaymentMethod method = null;
        if (methodStr != null && !methodStr.isEmpty()) {
            try {
                method = PaymentMethod.fromName(methodStr);
            } catch (IllegalArgumentException e) {
                throw new InvalidInputException("Invalid payment method: " + methodStr);
            }
        }

        PaymentTransactionStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = PaymentTransactionStatus.fromStatusName(statusStr);
            } catch (IllegalArgumentException e) {
                throw new InvalidInputException("Invalid transaction status: " + statusStr);
            }
        }
        return transactionDao.findAllWithFilters(search, userId, method, status);
    }

    public PaymentTransaction getByOrderId(Session session, Integer orderId)
    {
        return transactionDao.findByOrderId(session, orderId);
    }

    public String processExternalPayment(Session session, Long userId, Integer orderId, PaymentMethod paymentMethod)
    {
        if (paymentMethod != PaymentMethod.PAYWALL)
            throw new InvalidInputException("Payment method must be PAYWALL.");

        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            Order order = session.get(Order.class, orderId);
            User user = session.get(User.class, userId);
            if (order == null)
                throw new NotFoundException("Order with ID " + orderId + " not found.");
            if (user == null)
                throw new NotFoundException("User with ID " + userId + " not found.");
            if (order.getOrderStatus() != OrderStatus.SUBMITTED && order.getOrderStatus() != OrderStatus.UNPAID_AND_CANCELLED)
                throw new InvalidInputException("Order status does not allow payment initiation.");

            PaymentTransaction paymentTransaction = createTransaction(session, user, order, paymentMethod, BigDecimal.valueOf(order.getPayPrice()), PaymentTransactionStatus.PENDING);
            String externalGatewayRedirectUrl = callExternalPaymentGatewayApi(order, paymentTransaction);
            if (externalGatewayRedirectUrl == null || externalGatewayRedirectUrl.trim().isEmpty())
            {
                paymentTransaction.setStatus(PaymentTransactionStatus.FAILED);
                this.transactionDao.update(paymentTransaction, session);
                throw new RuntimeException("External payment gateway failed to provide redirect URL.");
            }

            order.setOrderStatus(OrderStatus.WAITING_VENDOR);
            orderManager.updateOrder(order, session);
            transaction.commit();
            return externalGatewayRedirectUrl;
        } catch (NotFoundException | InvalidInputException e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error initiating external payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initiate external payment: " + e.getMessage(), e);
        }
    }

    private String callExternalPaymentGatewayApi(Order order, PaymentTransaction transaction)
    {
        System.out.println("Calling external payment gateway for Order ID: " + order.getId());
        return "https://external-paywall.com/pay?transaction_id=" + transaction.getId() + "&amount=" + order.getPayPrice();
    }
}