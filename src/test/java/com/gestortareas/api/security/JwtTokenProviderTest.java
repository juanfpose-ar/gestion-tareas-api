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

    @Test
    public void testSecretCorto_FallaAlConstruir() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("cortito", expiration));
    }

    @Test
    public void testSecretVacioONulo_FallaAlConstruir() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("", expiration));
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider(null, expiration));
    }

    @Test
    public void testSecretDe32Bytes_EsValido() {
        assertDoesNotThrow(() -> new JwtTokenProvider("12345678901234567890123456789012", expiration));
    }
}
