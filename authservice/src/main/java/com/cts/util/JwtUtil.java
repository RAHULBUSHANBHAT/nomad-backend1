package com.cts.util;

import com.cts.model.UserCredential;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates a new JWT for a given user.
     */
    public String generateToken(UserCredential user) {
        Map<String, Object> claims = new HashMap<>();
        
        // These are the custom claims our API Gateway *expects* to find
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                // The "subject" of the token will be the user's unique ID
                .setSubject(user.getUserId()) 
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}