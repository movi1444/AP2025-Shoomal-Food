package com.aut.shoomal.payment.wallet;

import com.aut.shoomal.dao.WalletDao;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.PaymentMethod;
import com.aut.shoomal.payment.TopupMethod;
import com.aut.shoomal.payment.order.Order;
import com.aut.shoomal.payment.order.OrderManager;
import com.aut.shoomal.payment.order.OrderStatus;
import com.aut.shoomal.payment.transaction.PaymentTransactionManager;
import com.aut.shoomal.payment.transaction.PaymentTransactionStatus;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.util.function.BiConsumer;

public class WalletManager
{
    private final WalletDao walletDao;
    private final PaymentTransactionManager transactionManager;
    private final OrderManager orderManager;
    public WalletManager(WalletDao walletDao, PaymentTransactionManager transactionManager, OrderManager orderManager)
    {
        this.walletDao = walletDao;
        this.transactionManager = transactionManager;
        this.orderManager = orderManager;
    }

    private void performTransactionalWalletOperation(
            Long userId,
            BigDecimal amount,
            BiConsumer<Wallet, BigDecimal> action,
            PaymentMethod paymentMethod,
            String description
    ) throws InvalidInputException, NotFoundException
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Wallet wallet = this.findWalletByUserId(session, userId);
            User user = session.get(User.class, userId);
            if (wallet == null)
                throw new NotFoundException("Wallet for user id " + userId + " not found.");
            if (user == null)
                throw new NotFoundException("User with user id " + userId + " not found.");
            action.accept(wallet, amount);
            walletDao.update(wallet, session);
            transactionManager.createTransaction(session, user, null, paymentMethod, amount, PaymentTransactionStatus.COMPLETED);
            transaction.commit();
        } catch (InvalidInputException | NotFoundException | IllegalArgumentException e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error during " + description + "operation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to " + description + ": " + e.getMessage(), e);
        }
    }

    public void depositWallet(Long userId, BigDecimal amount, String method)
    {
        PaymentMethod paymentMethod;
        try {
            TopupMethod topupMethod = TopupMethod.fromName(method);
            if (topupMethod == TopupMethod.CARD || topupMethod == TopupMethod.ONLINE)
                paymentMethod = PaymentMethod.PAYWALL;
            else
                throw new InvalidInputException("Invalid top-up method: " + method);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid top-up method: " + method);
        }
        performTransactionalWalletOperation(userId, amount, Wallet::deposit, paymentMethod, "deposit");
    }

    public void withdrawWallet(Long userId, BigDecimal amount, String method)
    {
        PaymentMethod paymentMethod;
        if (method.equalsIgnoreCase(PaymentMethod.WALLET.getName()))
            paymentMethod = PaymentMethod.WALLET;
        else
            throw new InvalidInputException("Invalid method: " + method);
        performTransactionalWalletOperation(userId, amount, Wallet::withdraw, paymentMethod, "withdraw");
    }

    public void processWalletPaymentForOrder(Long userId, Integer orderId)
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Order order = session.get(Order.class, orderId);
            User user = session.get(User.class, userId);
            if (order == null)
                throw new NotFoundException("Order with ID " + orderId + " not found.");
            if (user == null)
                throw new NotFoundException("User with user id " + userId + " not found.");

            if (order.getOrderStatus() != OrderStatus.SUBMITTED && order.getOrderStatus() != OrderStatus.UNPAID_AND_CANCELLED)
                throw new InvalidInputException("Order status does not allow payment from wallet.");
            if (order.getPayPrice() <= 0)
                throw new InvalidInputException("Order has a non-positive pay price.");

            this.withdrawWallet(userId, BigDecimal.valueOf(order.getPayPrice()), PaymentMethod.WALLET.getName());
            order.setOrderStatus(OrderStatus.WAITING_VENDOR);
            orderManager.updateOrder(order, session);
            transaction.commit();
        } catch (InvalidInputException | NotFoundException e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error processing wallet payment for order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process wallet payment: " + e.getMessage(), e);
        }
    }

    public Wallet findWalletByUserId(Session session, Long userId)
    {
        return walletDao.findByUserId(session, userId);
    }
}