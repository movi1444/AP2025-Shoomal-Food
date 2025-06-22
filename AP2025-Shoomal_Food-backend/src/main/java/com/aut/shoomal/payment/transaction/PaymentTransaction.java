package com.aut.shoomal.payment.transaction;

import com.aut.shoomal.entity.user.User;
import com.aut.shoomal.payment.PaymentMethod;
import com.aut.shoomal.payment.order.Order;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class PaymentTransaction
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(name = "transaction_time", nullable = false, updatable = false)
    private LocalDateTime transactionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public PaymentTransaction()
    {
        this.status = TransactionStatus.PENDING;
        this.transactionTime = LocalDateTime.now();
    }

    public PaymentTransaction(Order order, User user, BigDecimal amount, PaymentMethod method)
    {
        this();
        this.order = order;
        this.user = user;
        this.amount = amount;
        this.method = method;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Order getOrder()
    {
        return order;
    }

    public void setOrder(Order order)
    {
        this.order = order;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public LocalDateTime getTransactionTime()
    {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime)
    {
        this.transactionTime = transactionTime;
    }

    public PaymentMethod getMethod()
    {
        return method;
    }

    public void setMethod(PaymentMethod method)
    {
        this.method = method;
    }

    public TransactionStatus getStatus()
    {
        return status;
    }

    public void setStatus(TransactionStatus status)
    {
        this.status = status;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }
}