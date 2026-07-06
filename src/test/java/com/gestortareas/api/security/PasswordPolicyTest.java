package com.gestortareas.api.security;

import com.gestortareas.api.exceptions.BusinessValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PasswordPolicyTest {

    @Test
    public void testPasswordValida() {
        assertDoesNotThrow(() -> PasswordPolicy.validar("12345678"));
        assertDoesNotThrow(() -> PasswordPolicy.validar("una-clave-larga-y-segura"));
    }

    @Test
    public void testPasswordCorta() {
        assertThrows(BusinessValidationException.class, () -> PasswordPolicy.validar("1234567"));
    }

    @Test
    public void testPasswordNula() {
        assertThrows(BusinessValidationException.class, () -> PasswordPolicy.validar(null));
    }
}
