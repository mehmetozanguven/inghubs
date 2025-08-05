package com.mehmetozanguven.inghubs_digital_wallet.security.config;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
public class JwtUtil {
    private final long JWT_EXPIRATION_IN_MS;
    private final String JWT_SECRET_KEY;

    public JwtUtil(long jwtExpirationInMs, String jwtSecretKey) {
        JWT_EXPIRATION_IN_MS = jwtExpirationInMs;
        JWT_SECRET_KEY = jwtSecretKey;
    }

    public String generateJwtToken(String username) {
        Instant today = Instant.now();
        Instant expiration = today.plus(JWT_EXPIRATION_IN_MS, ChronoUnit.MILLIS);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(today))
                .setExpiration(Date.from(expiration))
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET_KEY)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(JWT_SECRET_KEY)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isValidJwt(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
