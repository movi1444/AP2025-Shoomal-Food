package com.aut.shoomal.auth;

import com.aut.shoomal.dao.BlacklistedTokenDao;
import com.aut.shoomal.exceptions.ConflictException;
import com.aut.shoomal.exceptions.InvalidInputException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class LogoutManager
{
    private final BlacklistedTokenDao blacklistedTokenDao;
    public LogoutManager(BlacklistedTokenDao blacklistedTokenDao)
    {
        this.blacklistedTokenDao = blacklistedTokenDao;
    }

    public void blacklistToken(String token) throws InvalidInputException
    {
        if (token == null || token.trim().isEmpty())
            throw new InvalidInputException("Token cannot be null or empty.");
        if (blacklistedTokenDao.isTokenBlacklisted(token))
            throw new ConflictException("409 Conflict: Token has already been blacklisted or is still active in the blacklist.");

        Claims claims;
        try {
            claims = io.jsonwebtoken.Jwts.parserBuilder()
                    .setSigningKey(com.aut.shoomal.util.JwtUtil.getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        } catch (JwtException e) {
            throw new InvalidInputException("Invalid token format or signature for blacklisting: " + e.getMessage());
        }

        if (claims == null || claims.getExpiration() == null)
            throw new InvalidInputException("Token is missing expiration date, cannot blacklist.");

        Date expirationDate = claims.getExpiration();
        LocalDateTime localExpirationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(expirationDate.getTime()), ZoneId.systemDefault());

        BlacklistedToken blacklistedToken = new BlacklistedToken(token, localExpirationDate);
        blacklistedTokenDao.create(blacklistedToken);
    }
}