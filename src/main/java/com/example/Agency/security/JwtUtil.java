package com.example.Agency.security;
import io.jsonwebtoken.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private String secretKey = "ga+bZU1ukkP7lTWFFJdxGJ7zr6kc7c6/NIts+jrwJlw=\n"; // Use a strong secret key
    private long validityInMilliseconds = Long.MAX_VALUE; // No expiration

    public String generateToken(String mobileNumber) {
        return Jwts.builder()
                .setSubject(mobileNumber)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String extractMobileNo(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String mobileNo = extractMobileNo(token);
        return (mobileNo.equals(userDetails.getUsername()));
    }
}