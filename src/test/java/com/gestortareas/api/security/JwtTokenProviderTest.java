package com.gestortareas.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "GestorTareasSecretKeySuperSeguraConMasDe32CaracteresParaHS256!";
    private final long expiration = 3600000;

    @BeforeEach
    public void setup() {
        tokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    public void testGenerateAndValidateToken() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test.user");

        String token = tokenProvider.generateToken(authentication);
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("test.user", tokenProvider.getUsernameFromJWT(token));
    }

    @Test
    public void testValidateInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.token.here"));
    }
}
