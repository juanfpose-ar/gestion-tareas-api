package com.gestortareas.api.config;

import com.gestortareas.api.exceptions.BusinessValidationException;
import com.gestortareas.api.exceptions.ConflictException;
import com.gestortareas.api.exceptions.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RestResponseEntityExceptionHandlerTest {

    private RestResponseEntityExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    public void setup() {
        handler = new RestResponseEntityExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    public void testHandleDisabled() {
        ResponseEntity<Map<String, Object>> response = handler.handleDisabled(new DisabledException("inactive"), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("USUARIO_INACTIVO", response.getBody().get("message"));
    }

    @Test
    public void testHandleBadCredentials() {
        ResponseEntity<Map<String, Object>> response = handler.handleBadCredentials(new BadCredentialsException("bad credentials"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Credenciales inválidas", response.getBody().get("message"));
    }

    @Test
    public void testHandleEntityNotFound() {
        ResponseEntity<Map<String, Object>> response = handler.handleNoContent(new EntityNotFoundException("not found"), request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("not found", response.getBody().get("message"));
    }

    @Test
    public void testHandleConflict() {
        ResponseEntity<Map<String, Object>> response = handler.handleConflict(new ConflictException("conflict"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("conflict", response.getBody().get("message"));
    }

    @Test
    public void testHandleBusinessValidation() {
        ResponseEntity<Map<String, Object>> response = handler.handleBusinessValidation(new BusinessValidationException("invalid business"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid business", response.getBody().get("message"));
    }

    @Test
    public void testHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("field must not be null", response.getBody().get("message"));
    }

    @Test
    public void testHandleBadRequest_ConstraintViolation() {
        ConstraintViolationException ex = new ConstraintViolationException("violation", new HashSet<>());
        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testHandleGenericException() {
        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(new RuntimeException("unexpected"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("unexpected", response.getBody().get("message"));
    }
}
