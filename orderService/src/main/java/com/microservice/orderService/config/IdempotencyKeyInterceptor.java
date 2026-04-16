package com.microservice.orderService.config;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class IdempotencyKeyInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if("POST".equalsIgnoreCase(request.getMethod())){
            String key = request.getHeader("idempotency-key");
            if(key == null || key.isEmpty()){
                throw new IllegalArgumentException("Missing Idempotency-Key header");
            }
        }
        return true;
    }
}