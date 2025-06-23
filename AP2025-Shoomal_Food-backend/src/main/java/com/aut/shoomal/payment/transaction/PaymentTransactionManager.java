package com.aut.shoomal.payment.transaction;

import com.aut.shoomal.dao.TransactionDao;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.payment.PaymentMethod;
import com.aut.shoomal.payment.order.Order;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.List;

public class PaymentTransactionManager
{
    private final TransactionDao transactionDao;
    public PaymentTransactionManager(TransactionDao transactionDao)
    {
        this.transactionDao = transactionDao;
    }

    public void createTransaction(Session session, User user, Order order, PaymentMethod PaymentMethod, BigDecimal amount)
    {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidInputException("Amount must be greater than zero.");
        PaymentTransaction paymentTransaction = new PaymentTransaction(order, user, amount, PaymentMethod);
        paymentTransaction.setStatus(PaymentTransactionStatus.COMPLETED);
        transactionDao.create(paymentTransaction, session);
    }

    public List<PaymentTransaction> getAllTransactions()
    {
        return transactionDao.findAll();
    }
}