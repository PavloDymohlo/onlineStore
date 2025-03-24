package ua.dymohlo.onlineStore.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.dymohlo.onlineStore.entity.User;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(User user) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

            return Jwts.builder()
                    .setSubject(user.getEmail())
                    .claim("email", user.getEmail())
                    .claim("fullName", user.getFullname())
                    .claim("role", user.getRole())
                    .claim("authorities", List.of("ROLE_" + user.getRole()))
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }
}