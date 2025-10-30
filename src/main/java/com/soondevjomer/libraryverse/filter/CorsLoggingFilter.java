package com.soondevjomer.libraryverse.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class CorsLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.info("🔥 CORS Preflight Request Received:");
            log.info("   Origin: {}", request.getHeader("Origin"));
            log.info("   Access-Control-Request-Method: {}", request.getHeader("Access-Control-Request-Method"));
            log.info("   Access-Control-Request-Headers: {}", request.getHeader("Access-Control-Request-Headers"));
        }

        filterChain.doFilter(request, response);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.info("✅ CORS Preflight Response Headers:");
            response.getHeaderNames()
                    .forEach(name -> log.info("   {}: {}", name, response.getHeader(name)));
        }
    }
}
