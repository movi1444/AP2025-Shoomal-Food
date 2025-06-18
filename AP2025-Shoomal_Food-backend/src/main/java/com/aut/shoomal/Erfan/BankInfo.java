package com.aut.shoomal.Erfan;
import jakarta.persistence.*;

@Entity
@Table(name = "banks")
public class BankInfo
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @Column
    private String accountNumber;
    @OneToOne(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true)
    private User user;

    public BankInfo() {}
    public BankInfo(String name, String accountNumber)
    {
        this.name = name;
        this.accountNumber = accountNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}