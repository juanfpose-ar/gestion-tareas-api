package com.gestortareas.api.notificaciones.controller;

import com.gestortareas.api.notificaciones.service.DigestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class DigestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DigestService digestService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEnviarAhora_AdminDispara() throws Exception {
        mockMvc.perform(post("/api/admin/digest/enviar"))
                .andExpect(status().isNoContent());

        verify(digestService).enviarDigestATodos();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testEnviarAhora_UsuarioComunProhibido() throws Exception {
        mockMvc.perform(post("/api/admin/digest/enviar"))
                .andExpect(status().isForbidden());

        verify(digestService, never()).enviarDigestATodos();
    }
}
