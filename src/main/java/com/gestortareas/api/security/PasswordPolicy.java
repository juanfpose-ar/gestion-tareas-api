package com.gestortareas.api.security;

import com.gestortareas.api.exceptions.BusinessValidationException;

/**
 * Política mínima de contraseñas, aplicada en todos los puntos donde se setea una:
 * alta de usuario, edición, blanqueo por admin y cambio por el propio usuario.
 */
public final class PasswordPolicy {

    public static final int LARGO_MINIMO = 8;

    private PasswordPolicy() {
    }

    public static void validar(String password) {
        if (password == null || password.length() < LARGO_MINIMO) {
            throw new BusinessValidationException(
                    "La contraseña debe tener al menos " + LARGO_MINIMO + " caracteres");
        }
    }
}
