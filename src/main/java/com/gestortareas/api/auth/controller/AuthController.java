package com.gestortareas.api.auth.controller;

import com.gestortareas.api.auth.dto.CambiarPasswordRequest;
import com.gestortareas.api.auth.dto.LoginRequest;
import com.gestortareas.api.auth.dto.LoginResponse;
import com.gestortareas.api.auth.dto.ProfileUpdateRequest;
import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.security.JwtTokenProvider;
import com.gestortareas.api.usuario.dto.UsuarioDTO;
import com.gestortareas.api.usuario.entity.Usuario;
import com.gestortareas.api.usuario.repository.UsuarioRepository;
import com.gestortareas.api.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername()).orElseThrow();

        ResponseCookie cookie = buildAuthCookie(jwt, Duration.ofMillis(jwtExpirationMs));

        LoginResponse body = LoginResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                // El token también se devuelve en el body para compatibilidad con clientes API/móvil.
                // El frontend SPA usa la cookie; puede ignorar este campo.
                .token(jwt)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie expiredCookie = buildAuthCookie("", Duration.ZERO);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usuarioService.obtenerPorUsername(userDetails.getUsername()));
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CambiarPasswordRequest request) {

        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new BusinessValidationException("La contraseña actual es incorrecta");
        }
        if (passwordEncoder.matches(request.getPasswordNueva(), usuario.getPassword())) {
            throw new BusinessValidationException("La nueva contraseña debe ser diferente a la actual");
        }

        usuario.setPassword(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<UsuarioDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {

        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (!usuario.getUsername().equals(request.getUsername())
                && usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessValidationException("El nombre de usuario ya está en uso");
        }

        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setUsername(request.getUsername());

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new BusinessValidationException("Debe ingresar su contraseña actual para establecer una nueva");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
                throw new BusinessValidationException("La contraseña actual es incorrecta");
            }
            usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        Usuario saved = usuarioRepository.save(usuario);
        return ResponseEntity.ok(usuarioService.obtenerPorId(saved.getId()));
    }

    private ResponseCookie buildAuthCookie(String value, Duration maxAge) {
        return ResponseCookie.from("gt_token", value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api")
                .maxAge(maxAge)
                .build();
    }
}
