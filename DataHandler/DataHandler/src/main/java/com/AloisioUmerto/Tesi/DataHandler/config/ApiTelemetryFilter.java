package com.AloisioUmerto.Tesi.DataHandler.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter che intercetta tutte le chiamate API e registra la telemetria:
 * - Tempo di risposta
 * - Dimensione payload richiesta/risposta
 * - Status code
 * - Method e URL
 * 
 * Salvataggio su Excel dopo ogni chiamata.
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class ApiTelemetryFilter implements Filter {

    private final TelemetryService telemetryService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String url = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            url += "?" + queryString;
        }

        long requestSize = httpRequest.getContentLengthLong();
        if (requestSize < 0) {
            requestSize = 0; // multipart/form-data