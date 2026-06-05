package com.saldium.saldium.service;


import com.saldium.saldium.exceptions.auth.TokenInvalidoException;
import com.saldium.saldium.security.jwt.JwtService;
import com.saldium.saldium.security.user.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InvalidClassException;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "secret-key"
        );
    }

    @Test
    void generateAccessToken_ShouldGenerateAccessToken() {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");

        String accessToken = jwtService.generateAccessToken(usuario);

        assertNotNull(accessToken);
    }

    @Test
    void generateRefreshToken_ShouldGenerateAccessToken() {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");

        String refreshToken = jwtService.generateRefreshToken(usuario);

        assertNotNull(refreshToken);
    }

    @Test
    void validateAccessToken_ShouldReturnSubject_WhenTokenIsValid() {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");

        String accessToken = jwtService.generateAccessToken(usuario);
        String subject = jwtService.validateAccessToken(accessToken);

        assertNotNull(accessToken);
        assertEquals(usuario.getEmail(), subject);
    }

    @Test
    void validateRefreshToken_ShouldReturnSubject_WhenTokenIsValid() {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");

        String refreshToken = jwtService.generateRefreshToken(usuario);
        String subject = jwtService.validateRefreshToken(refreshToken);

        assertNotNull(refreshToken);
        assertEquals(usuario.getEmail(), subject);
    }

    @Test
    void validateAccessToken_ShouldThrowException_WhenTokenIsTypeRefresh() {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");

        String refreshToken = jwtService.generateRefreshToken(usuario);
        assertThrows(TokenInvalidoException.class, () -> jwtService.validateAccessToken(refreshToken));
    }

    @Test
    void validateRefreshToken_ShouldThrowException_WhenTokenIsTypeAccess() {
        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");

        String accessToken = jwtService.generateAccessToken(usuario);
        assertThrows(TokenInvalidoException.class, () -> jwtService.validateRefreshToken(accessToken));
    }

    @Test
    void validateRefreshToken_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid-token";

        assertThrows(TokenInvalidoException.class, () -> jwtService.validateRefreshToken(invalidToken));
    }

    @Test
    void validateAccessToken_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid-token";

        assertThrows(TokenInvalidoException.class, () -> jwtService.validateAccessToken(invalidToken));
    }

    @Test
    void validateAccessToken_ShouldThrowException_WhenTokenIsAnotherSign() {
        JwtService anotherJwtService = new JwtService("another-secret-key");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");

        anotherJwtService.generateAccessToken(usuario);
        assertThrows(TokenInvalidoException.class, () -> anotherJwtService.validateAccessToken("another-token"));
    }

    @Test
    void validateRefreshToken_ShouldThrowException_WhenTokenIsAnotherSign() {
        JwtService anotherJwtService = new JwtService("another-secret-key");

        Usuario usuario = new Usuario();
        usuario.setEmail("user@email.com");

        anotherJwtService.generateAccessToken(usuario);
        assertThrows(TokenInvalidoException.class, () -> anotherJwtService.validateRefreshToken("another-token"));
    }
}
