package com.janaai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.issuer}")
    private String issuer;


    private SecretKey getSigningKey() {
        StringBuilder sb = new StringBuilder(secret);

        while (sb.toString().getBytes().length < 32) {
            sb.append(secret);
        }

        return Keys.hmacShaKeyFor(sb.toString().getBytes());
    }


    public String generateAccessToken(UserDetails userDetails, UUID userId, String role) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("userId", userId.toString());
        claims.put("role", role);

        return buildToken(
                claims,
                userDetails.getUsername(),
                accessTokenExpirationMs
        );
    }


    private String buildToken(
            Map<String, Object> claims,
            String subject,
            long expirationMs
    ) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public UUID extractUserId(String token) {

        String userId = extractClaim(
                token,
                claims -> claims.get("userId", String.class)
        );

        return UUID.fromString(userId);
    }


    public String extractRole(String token) {

        return extractClaim(
                token,
                claims -> claims.get("role", String.class)
        );
    }


    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {

        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }


    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        try {

            final String username = extractUsername(token);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);

        } catch (
                ExpiredJwtException |
                io.jsonwebtoken.security.SecurityException |
                io.jsonwebtoken.MalformedJwtException e
        ) {

            return false;
        }
    }


    private boolean isTokenExpired(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        ).before(new Date());
    }
}