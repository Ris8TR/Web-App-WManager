package com.myTesi.aloisioUmberto.config;


import com.myTesi.aloisioUmberto.data.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    @Value("${jwt.refreshExpirationMs}")
    private long refreshExpirationMs;

    public String generateUserToken(Optional<User> user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        return Jwts.builder().claim("email", user.get().getEmail())
                .claim("userId", user.get().getId().toString())  // Aggiunge l'ID come claim
                .claim("role", user.get().getRole())  // Aggiunge il ruolo come claim
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }


    public String generateResetPassToken(String email, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .claim("email", email)
                .claim("role", "pass")
                .claim("type", type)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String generateRefreshToken(Optional<User> user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);
        return Jwts.builder()
                .setSubject(user.get().getEmail())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }


    public String getEmailFromUserToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        return (String) claims.get("email");
    }

    public String getEmailFromRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        return (String) claims.getSubject();
    }

    public String getUserIdFromUserToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        Object userIdObj = claims.get("userId");

        // Verifica il tipo di userId e converti di conseguenza
        if (userIdObj instanceof String) {
            return (String) userIdObj;
        } else if (userIdObj instanceof Map) {
            // Assumiamo che l'oggetto complesso contenga un campo "timestamp" o "date" che può essere usato come identificatore
            Map<String, Object> userIdMap = (Map<String, Object>) userIdObj;
            return userIdMap.toString(); // o una logica di conversione più specifica
        } else {
            throw new IllegalArgumentException("Invalid userId type in token");
        }
    }

    public String getTypeFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        return (String) claims.get("type");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(refreshToken);
            return true;
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}

