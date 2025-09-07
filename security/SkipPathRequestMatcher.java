package com.tests.campuslostandfoundsystem.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class SkipPathRequestMatcher implements RequestMatcher {
    private final String prefix;

    public SkipPathRequestMatcher(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = request.getServletPath();
        // return true to trigger filter, false to skip
        return !(path.startsWith(prefix) || "OPTIONS".equalsIgnoreCase(request.getMethod()));
    }
}
