package com.tests.campuslostandfoundsystem.config;

import com.tests.campuslostandfoundsystem.dao.UserDAO;
import com.tests.campuslostandfoundsystem.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDAO userDAO;

    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/auth/", "/v3/api-docs", "/swagger-ui", "/swagger-ui.html"
    );

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        for (String p : PUBLIC_PREFIXES) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String header = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(header) && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                try {
                    boolean valid = jwtUtils.validateToken(token);
                    log.debug("[JWT] validateToken={} uri={}", valid, request.getRequestURI());
                    if (valid) {
                        String userIdStr = jwtUtils.getUserId(token); // ★ 必须是 users.id
                        log.debug("[JWT] userId={}", userIdStr);

                        List<String> roleKeys = userDAO.selectRoleKeysByUserId(Long.parseLong(userIdStr));
                        log.debug("[JWT] roleKeys={}", roleKeys);

                        List<GrantedAuthority> authorities = roleKeys.stream()
                                .filter(StringUtils::isNotBlank)
                                .map(SimpleGrantedAuthority::new) // ADMIN / ITEM_ADMIN
                                .collect(Collectors.toList());

                        if (!authorities.isEmpty()) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userIdStr, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.debug("[JWT] setAuthentication ok, authorities={}", authorities);
                        } else {
                            log.warn("[JWT] authorities is EMPTY for userId={}", userIdStr);
                        }
                    } else {
                        log.warn("[JWT] token invalid, uri={}", request.getRequestURI());
                    }
                } catch (Exception e) {
                    log.warn("[JWT] exception: {}", e.toString());
                }
            } else {
                log.debug("[JWT] no Bearer token, uri={}", request.getRequestURI());
            }
        }
        filterChain.doFilter(request, response);
    }
}
