package com.saldium.saldium.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.saldium.saldium.exceptions.auth.TokenInvalidoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    public String generateAccessToken(UserDetails userDetails) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withIssuer("saldium-api")
                .withSubject(userDetails.getUsername())
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .sign(algorithm);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withIssuer("saldium-api")
                .withSubject(userDetails.getUsername())
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(Instant.now().plus(14, ChronoUnit.DAYS))
                .sign(algorithm);
    }

    public String validateAccessToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("saldium-api")
                    .build()
                    .verify(token);

            String tokenType = jwt.getClaim("type").asString();

            if (!tokenType.equals("access")) {
                throw new TokenInvalidoException("Token inválido ou expirado");
            }
            return jwt.getSubject();
        }catch (TokenInvalidoException | JWTVerificationException e) {
            throw new TokenInvalidoException("Token inválido ou expirado");
        }
    }

    public String validateRefreshToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("saldium-api")
                    .build()
                    .verify(token);

            String tokenType = jwt.getClaim("type").asString();

            if (!tokenType.equals("refresh")) {
                throw new TokenInvalidoException("Token inválido ou expirado");
            }
            return jwt.getSubject();
        }catch (TokenInvalidoException | JWTVerificationException e) {
            throw new TokenInvalidoException("Token inválido ou expirado");
        }
    }

    public JwtService(
            @Value("${jwt.secret}")
            String secret) {
        this.secret = secret;
    }
}
