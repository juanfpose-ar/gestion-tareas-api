package com.gestortareas.api.interceptors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class loggerInterceptor implements HandlerInterceptor {

    private static final String KEY_TIME = "x-endpoint-start-time";
    private static final String START = "Start";
    private static final String END = "End";
    private static final String THROW = "Throw";
    private static final String SEPARATOR = "|";

    @Value("${app.logger.showStart:true}")
    private boolean loggerShowStart;

    @Value("${app.logger.showEnd:true}")
    private boolean loggerShowEnd;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(KEY_TIME, String.valueOf(System.currentTimeMillis()));
        if (loggerShowStart) {
            log.info(buildLogMessage(START, 0.0, request, null));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (!loggerShowEnd && ex == null) return;

        var startTime = (String) request.getAttribute(KEY_TIME);
        var duration = (System.currentTimeMillis() - Long.parseLong(startTime)) / 1000.0;

        if (ex != null) {
            log.error("{} {} {}", buildLogMessage(THROW, duration, request, response), SEPARATOR, ex.getMessage());
        } else {
            log.info(buildLogMessage(END, duration, request, response));
        }
    }

    private String buildLogMessage(String type, Double duration, HttpServletRequest request,
            HttpServletResponse response) {
        return String.format("%s %s %.3f %s %s %s %s %s %s %s",
                type, SEPARATOR,
                duration, SEPARATOR,
                response != null ? response.getStatus() : "", SEPARATOR,
                request.getMethod(), SEPARATOR,
                request.getRequestURI(), SEPARATOR,
                request.getQueryString() == null ? "" : ("?" + request.getQueryString()));
    }
}
