package com.aut.shoomal.payment.wallet;

import com.aut.shoomal.dao.WalletDao;
import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.entity.user.UserManager;
import com.aut.shoomal.exceptions.InvalidInputException;
import com.aut.shoomal.exceptions.NotFoundException;
import com.aut.shoomal.payment.PaymentMethod;
import com.aut.shoomal.payment.TopupMethod;
import com.aut.shoomal.payment.transaction.PaymentTransaction;
import com.aut.shoomal.payment.transaction.PaymentTransactionManager;
import com.aut.shoomal.payment.transaction.TransactionStatus;
import com.aut.shoomal.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;

public class WalletManager
{
    private final WalletDao walletDao;
    private final UserManager userManager;
    private final PaymentTransactionManager transactionManager;
    public WalletManager(WalletDao walletDao, UserManager userManager, PaymentTransactionManager transactionManager)
    {
        this.walletDao = walletDao;
        this.userManager = userManager;
        this.transactionManager = transactionManager;
    }

    public void depositWallet(Long userId, BigDecimal amount, String method)
    {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Wallet wallet = walletDao.findByUserId(session, userId);
            User user = userManager.getUserById(userId);
            if (wallet == null)
                throw new NotFoundException("Wallet for user id " + userId + " not found.");
            if (user == null)
                throw new NotFoundException("User with user id " + userId + " not found.");
            try {
                TopupMethod topupMethod = TopupMethod.fromName(method);
            } catch (IllegalArgumentException e) {
                throw new InvalidInputException("Invalid top-up method: " + method);
            }

            transactionManager.createTransaction(session, user, null, PaymentMethod.PAYWALL, amount);
            wallet.deposit(amount);
            walletDao.update(wallet, session);
            transaction.commit();
        } catch (InvalidInputException | NotFoundException e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Can not deposit wallet: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            System.err.println("Error during depositFunds: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to deposit funds: " + e.getMessage(), e);
        }
    }

    public void withdrawWallet(Wallet wallet, BigDecimal amount)
    {
        wallet.withdraw(amount);
        walletDao.update(wallet);
    }

    public Wallet findWalletByUserId(Session session, Long userId)
    {
        return walletDao.findByUserId(session, userId);
    }
}