package com.aut.shoomal.dao;

import com.aut.shoomal.payment.wallet.Wallet;
import org.hibernate.Session;

public interface WalletDao extends GenericDao<Wallet>
{
    Wallet findByUserId(Session session, Long userId);
}