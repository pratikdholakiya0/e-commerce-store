package org.akashbag.ecommerce.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.akashbag.ecommerce.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(User user) {
       Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());   // Add ID to payload
        claims.put("role", user.getRole());
        claims.put("isVerified", user.isVerified());
       return createJwtToken(user.getEmail(), claims);
    }

    private String createJwtToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(username)
                .signWith(getSigningKey())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claims(claims)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
