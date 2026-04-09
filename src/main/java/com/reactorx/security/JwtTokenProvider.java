package com.reactorx.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;

    @Value("${jwt.expiration.ms:86400000}") // default 1 day
    private long jwtExpirationInMs;

    public JwtTokenProvider(@Value("${jwt.secret:${JWT_SECRET:}}") String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank()) {
            // For local/dev you can set an env var or property. This avoids NPEs.
            base64Secret = "ZGVmYXVsdC1iYXNlNjQtc2VjcmV0LXNob3VsZC1iZS02NGJ5dGVzLWZvci1kZXY=";
        }
        byte[] keyBytes = Base64.getUrlDecoder().decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSigningKey() {
        return key;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationInMs);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException ex) {
            System.out.println("Invalid JWT signature: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            System.out.println("JWT expired: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            System.out.println("JWT unsupported: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("JWT claims string empty: " + ex.getMessage());
        }
        return false;
    }
}
