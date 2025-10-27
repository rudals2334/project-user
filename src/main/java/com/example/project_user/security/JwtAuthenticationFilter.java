package com.example.project_user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.NonNull;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // ✅ 추가
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    // ✅ 토큰 검사 제외 경로 (SecurityConfig의 permitAll과 대응)
    private static final String[] WHITELIST = {
            "/actuator/health", "/actuator/health/**", "/actuator/info",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/health",
            "/auth/register", "/auth/login",
            "/members/lookup",
            //"/members/*" // 단건 공개 조회를 허용한 경우
            // 게이트웨이 프리픽스가 있다면 아래도 추가 (예: /v1 프록시)
            // "/v1/actuator/health", "/v1/actuator/health/**", "/v1/actuator/info",
            // "/v1/swagger-ui.html", "/v1/swagger-ui/**", "/v1/v3/api-docs/**",
            // "/v1/health", "/v1/auth/register", "/v1/auth/login",
            // "/v1/members/lookup", "/v1/members/*"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // ✅ CORS 프리플라이트는 무조건 패스 (SecurityConfig에 OPTIONS permitAll과 동일한 효과)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        for (String pattern : WHITELIST) {
            if (new AntPathRequestMatcher(pattern).matches(request)) {
                return true;
            }
            // 간단한 확장: "/something/**" 패턴을 startsWith로 관대하게 허용
            if (pattern.endsWith("/**")) {
                String base = pattern.substring(0, pattern.length() - 3);
                if (path.startsWith(base)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        // 진입 로그
        System.out.println("[JWT] filter entered, uri=" + uri);

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                JwtUserPrincipal principal = tokenProvider.parsePrincipal(token); // 여기서 exp/서명 검증 포함돼야 함
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                ((UsernamePasswordAuthenticationToken) auth)
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("[JWT] authenticated userId=" + principal.userId());
            } catch (Exception ex) {
                System.out.println("[JWT] invalid token: " + ex.getMessage());
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

}
