package com.aut.shoomal.dao;

import com.aut.shoomal.payment.transaction.PaymentTransaction;

import java.util.List;

public interface TransactionDao extends GenericDao<PaymentTransaction>
{
    List<PaymentTransaction> findByUserId(Long userId);
}