package group.rxcloud.ava.aigc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token provider for generating and validating tokens
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:defaultSecretKeyThatShouldBeChangedInProductionEnvironment12345678901234567890}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long jwtExpirationInMs;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private long refreshExpirationInMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Ensure the secret key is at least 256 bits (32 bytes) for HS256
        String secret = jwtSecret.length() < 32
            ? jwtSecret + "0".repeat(32 - jwtSecret.length())
            : jwtSecret;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate access token from authentication
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails.getUsername());
    }

    /**
     * Generate access token from username
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Get username from token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    /**
     * Calculate token remaining time in milliseconds
     */
    public long getExpirationTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }
}
