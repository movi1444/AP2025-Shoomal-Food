package com.aut.shoomal.dao.impl;

import com.aut.shoomal.dao.WalletDao;
import com.aut.shoomal.payment.wallet.Wallet;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class WalletDaoImpl extends GenericDaoImpl<Wallet> implements WalletDao
{
    @Override
    public Wallet findByUserId(Session session, Long userId)
    {
        Query<Wallet> query = session.createQuery("from Wallet w left join fetch w.user where w.user.id = :userId", Wallet.class);
        query.setParameter("userId", userId);
        return query.uniqueResult();
    }
}