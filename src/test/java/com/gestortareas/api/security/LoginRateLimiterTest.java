package com.gestortareas.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginRateLimiterTest {

    private LoginRateLimiter limiter;

    @BeforeEach
    public void setup() {
        limiter = new LoginRateLimiter(3, 15);
    }

    @Test
    public void testNoBloqueaAntesDelUmbral() {
        limiter.registrarFallo("user");
        limiter.registrarFallo("user");

        assertFalse(limiter.estaBloqueado("user"));
    }

    @Test
    public void testBloqueaAlLlegarAlUmbral() {
        limiter.registrarFallo("user");
        limiter.registrarFallo("user");
        limiter.registrarFallo("user");

        assertTrue(limiter.estaBloqueado("user"));
    }

    @Test
    public void testExitoReseteaElContador() {
        limiter.registrarFallo("user");
        limiter.registrarFallo("user");
        limiter.registrarExito("user");
        limiter.registrarFallo("user");

        assertFalse(limiter.estaBloqueado("user"));
    }

    @Test
    public void testUsernameEsCaseInsensitive() {
        limiter.registrarFallo("User");
        limiter.registrarFallo("USER ");
        limiter.registrarFallo("user");

        assertTrue(limiter.estaBloqueado("user"));
    }

    @Test
    public void testUsuariosIndependientes() {
        limiter.registrarFallo("uno");
        limiter.registrarFallo("uno");
        limiter.registrarFallo("uno");

        assertTrue(limiter.estaBloqueado("uno"));
        assertFalse(limiter.estaBloqueado("otro"));
    }
}
