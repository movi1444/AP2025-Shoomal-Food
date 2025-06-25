package com.aut.shoomal.payment.wallet;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.exceptions.InvalidInputException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
public class Wallet
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(mappedBy = "wallet")
    private User user;
    @Column(nullable = false)
    private BigDecimal balance;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Wallet()
    {
        this.balance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public Wallet(User user)
    {
        this();
        this.user = user;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public BigDecimal getBalance()
    {
        return balance;
    }

    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }

    public void withdraw(BigDecimal amount)
    {
        if (this.balance.compareTo(amount) < 0)
            throw new InvalidInputException("Insufficient balance.");
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidInputException("Withdrawal amount cannot be negative.");
        this.balance = this.balance.subtract(amount);
    }

    public void deposit(BigDecimal amount)
    {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new InvalidInputException("The deposit amount cannot be negative.");
        this.balance = this.balance.add(amount);
    }
}