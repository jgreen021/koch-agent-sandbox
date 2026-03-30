package com.koch.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Aspect
@Component
public class BackgroundSecurityContextAspect {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundSecurityContextAspect.class);

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled) || @annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object setSecurityContextForBackgroundTask(ProceedingJoinPoint joinPoint) throws Throwable {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Setting SYSTEM security context for background task: {}", joinPoint.getSignature().toShortString());
            
            // Create a system authentication with administrative roles to bypass method security
            UsernamePasswordAuthenticationToken systemAuth = new UsernamePasswordAuthenticationToken(
                    "SYSTEM", 
                    null, 
                    List.of(
                        new SimpleGrantedAuthority("ROLE_OPERATOR"),
                        new SimpleGrantedAuthority("ROLE_GATEWAY_ADMIN")
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(systemAuth);
            try {
                return joinPoint.proceed();
            } finally {
                SecurityContextHolder.clearContext();
                logger.debug("Cleared SYSTEM security context for background task.");
            }
        }
        
        return joinPoint.proceed();
    }
}
