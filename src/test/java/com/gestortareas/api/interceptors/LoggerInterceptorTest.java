package com.gestortareas.api.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoggerInterceptorTest {

    @InjectMocks
    private loggerInterceptor interceptor;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(interceptor, "loggerShowStart", true);
        ReflectionTestUtils.setField(interceptor, "loggerShowEnd", true);
    }

    @Test
    public void testPreHandle() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Object handler = new Object();

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result);
        verify(request, times(1)).setAttribute(eq("x-endpoint-start-time"), anyString());
    }

    @Test
    public void testAfterCompletion_Success() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Object handler = new Object();

        when(request.getAttribute("x-endpoint-start-time")).thenReturn(String.valueOf(System.currentTimeMillis() - 100));
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, handler, null);
    }

    @Test
    public void testAfterCompletion_WithException() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Object handler = new Object();

        when(request.getAttribute("x-endpoint-start-time")).thenReturn(String.valueOf(System.currentTimeMillis() - 100));
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(500);

        Exception ex = new RuntimeException("Test Exception");
        interceptor.afterCompletion(request, response, handler, ex);
    }
}
