package com.aut.shoomal.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "blacklisted_tokens")
public class BlacklistedToken
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;
    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blacklistedAt;

    public BlacklistedToken()
    {
        this.blacklistedAt = LocalDateTime.now();
    }

    public BlacklistedToken(String token, LocalDateTime expirationDate)
    {
        this();
        this.token = token;
        this.expirationDate = expirationDate;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public LocalDateTime getExpirationDate()
    {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public LocalDateTime getBlacklistedAt()
    {
        return blacklistedAt;
    }

    public void setBlacklistedAt(LocalDateTime blacklistedAt)
    {
        this.blacklistedAt = blacklistedAt;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
            return false;
        BlacklistedToken that = (BlacklistedToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(token);
    }
}