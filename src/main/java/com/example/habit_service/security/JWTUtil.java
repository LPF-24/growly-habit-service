package com.example.habit_service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTUtil {
    @Value("${jwt_secret}")
    private String secret;

    public DecodedJWT validateAccessToken(String token) throws JWTVerificationException {
        System.out.println("Validating access token...");
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("ADMIN")
                .build();
        return verifier.verify(token);
    }

    public String generateAccessToken(Long id, String username, String role) {
        System.out.println("generateAccessToken successfully started");
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        String token = JWT.create()
                .withSubject("User details")
                .withClaim("id", id)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withIssuer("ADMIN")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));

        System.out.println("Generated token: " + token);
        return token;
    }
}
