package com.hscodeworks.quiz_backend.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.hscodeworks.quiz_backend.common.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String HEADER = "X-API-Key";
    private static final Set<String> READ_ONLY_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final byte[] expectedKey;

    
    public ApiKeyInterceptor( @Value("${app.security.api-key:}") String apiKey ) {
        this.expectedKey = apiKey.getBytes(StandardCharsets.UTF_8);
        if (expectedKey.length == 0) {
            log.warn("app.security.api-key is not set: all write requests (POST/PUT/DELETE) will be rejected");
        }
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (READ_ONLY_METHODS.contains(request.getMethod())) {
            return true;
        }

        String provided = request.getHeader(HEADER);
        boolean valid = expectedKey.length > 0
                && provided != null
                && MessageDigest.isEqual(expectedKey, provided.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            throw new UnauthorizedException("A valid API key is required for this operation.");
        }
        return true;
    }
}