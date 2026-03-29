package com.koch.security.filter;

import com.koch.security.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class WebAuditFilter extends OncePerRequestFilter {

    private final AuditService auditService;

    public WebAuditFilter(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            String path = request.getRequestURI();
            
            // Skip logging for static assets or internal health checks if necessary
            if (path.startsWith("/api/")) {
                long duration = System.currentTimeMillis() - startTime;
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = (auth != null) ? auth.getName() : "anonymous";
                int status = response.getStatus();
                
                String eventType = (status >= 400) ? "API_FAILURE" : "API_SUCCESS";
                String details = String.format("Method: %s, Status: %d, Duration: %dms", 
                                             request.getMethod(), status, duration);
                
                auditService.logEvent(eventType, username, path, details);
            }
        }
    }
}
