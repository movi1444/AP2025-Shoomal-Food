package com.aut.shoomal.dao;
import com.aut.shoomal.auth.BlacklistedToken;

public interface BlacklistedTokenDao extends GenericDao<BlacklistedToken>
{
    boolean isTokenBlacklisted(String token);
}