package com.gestortareas.api.config;

import com.gestortareas.api.enums.Rol;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // En prod, setear ADMIN_INITIAL_PASSWORD en el .env: sin eso el primer admin nace con
    // el default "admin" (aceptable solo en dev). Solo aplica al primer arranque — después
    // el password vive en la DB y se cambia desde la app.
    @Value("${app.seed.admin-password:admin}")
    private String adminInitialPassword;

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = Usuario.builder()
                    .username("admin")
                    .password(passwordEncoder.encode(adminInitialPassword))
                    .nombreCompleto("Administrador del Sistema")
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
        }
    }
}

