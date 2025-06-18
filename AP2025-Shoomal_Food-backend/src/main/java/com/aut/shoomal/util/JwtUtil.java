package com.aut.shoomal.util;

import com.aut.shoomal.Erfan.User;
import com.aut.shoomal.Erfan.UserManager;
import com.aut.shoomal.exceptions.UnauthorizedException;
import com.aut.shoomal.exceptions.NotFoundException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil
{
    private static final String SECRET = "bXktdXJsLXNlY3JldC1rZXktdGhhdC1pcy1iYXNlNjQtZW5jb2RlZA==";
    private static final Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    private static final long EXPIRATION_MS = 3 * 60 * 60 * 1000; // 3 hours

    public static String generateToken(User user)
    {
        if (user == null)
            throw new IllegalArgumentException("User object cannot be null when generating a token.");

        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().getName());
        claims.put("phoneNumber", user.getPhoneNumber());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private static Claims parseTokenClaims(String token) throws UnauthorizedException
    {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
            throw new UnauthorizedException("401 Unauthorized: Invalid JWT signature or malformed token.");
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("401 Unauthorized: JWT token has expired.");
        } catch (UnsupportedJwtException e) {
            throw new UnauthorizedException("401 Unauthorized: JWT token is unsupported.");
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("401 Unauthorized: JWT token is null or empty.");
        } catch (Exception e) {
            throw new UnauthorizedException("401 Unauthorized: An unexpected error occurred during token validation: " + e.getMessage());
        }
    }

    public static User validateToken(String token, UserManager userManager) throws UnauthorizedException {
        Claims claims = parseTokenClaims(token);
        Long userId = claims.get("userId", Long.class);
        String tokenPhoneNumber = claims.get("phoneNumber", String.class);

        if (userId == null || tokenPhoneNumber == null || tokenPhoneNumber.trim().isEmpty())
            throw new UnauthorizedException("401 Unauthorized: Missing user ID or phone number in token claims.");

        User user;
        try {
            user = userManager.getUserById(userId);
        } catch (NotFoundException e) {
            throw new UnauthorizedException("401 Unauthorized: User not found for token ID.");
        }

        if (user == null || !user.getPhoneNumber().equals(tokenPhoneNumber))
            throw new UnauthorizedException("401 Unauthorized: Token is invalid.");

        return user;
    }

    public static Long getUserIdFromToken(String token, UserManager userManager) throws UnauthorizedException
    {
        return validateToken(token, userManager).getId();
    }

    public static String getRoleFromToken(String token, UserManager userManager) throws UnauthorizedException
    {
        return validateToken(token, userManager).getRole().getName();
    }

    public static Key getKey()
    {
        return key;
    }
}