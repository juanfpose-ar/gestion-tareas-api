package com.gestortareas.api.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.logging.MDC;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestLoggingFilterTest {

    @InjectMocks
    private RequestLoggingFilter filter;

    @Test
    public void testDoFilterInternal() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("x-request-id")).thenReturn("req-123");
        when(request.getHeader("x-user-id")).thenReturn("user-456");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(MDC.get("requestId")); // should be removed in finally block
    }
}
