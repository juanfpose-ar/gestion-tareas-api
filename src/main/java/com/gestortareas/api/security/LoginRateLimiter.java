package com.gestortareas.api.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Frena fuerza bruta en el login: tras N intentos fallidos seguidos sobre un mismo username,
 * bloquea nuevos intentos hasta que pase la ventana. En memoria vía Caffeine (misma librería
 * que ya usa la cache de membresía) — suficiente para una instancia única; si algún día hay
 * réplicas, esto pasa a Redis o a una regla en el reverse proxy.
 */
@Component
@Log4j2
public class LoginRateLimiter {

    private final int maxIntentos;
    private final Cache<String, AtomicInteger> intentosFallidos;

    public LoginRateLimiter(
            @Value("${app.login.max-intentos:10}") int maxIntentos,
            @Value("${app.login.bloqueo-minutos:15}") long bloqueoMinutos) {
        this.maxIntentos = maxIntentos;
        // expireAfterWrite: cada fallo renueva la ventana; sin fallos nuevos, a los N minutos se libera solo.
        this.intentosFallidos = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(bloqueoMinutos))
                .maximumSize(100_000)
                .build();
    }

    public boolean estaBloqueado(String username) {
        AtomicInteger intentos = intentosFallidos.getIfPresent(normalizar(username));
        return intentos != null && intentos.get() >= maxIntentos;
    }

    public void registrarFallo(String username) {
        String key = normalizar(username);
        AtomicInteger intentos = intentosFallidos.get(key, k -> new AtomicInteger(0));
        int total = intentos.incrementAndGet();
        // Reescribe la entrada para que expireAfterWrite cuente desde el último fallo.
        intentosFallidos.put(key, intentos);
        if (total == maxIntentos) {
            log.warn("Login bloqueado para '{}' tras {} intentos fallidos", key, total);
        }
    }

    public void registrarExito(String username) {
        intentosFallidos.invalidate(normalizar(username));
    }

    private String normalizar(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
