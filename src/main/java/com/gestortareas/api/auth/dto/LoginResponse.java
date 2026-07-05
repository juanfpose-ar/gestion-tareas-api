package com.gestortareas.api.auth.dto;

import com.gestortareas.api.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private String nombreCompleto;
    private Rol rol;
}
