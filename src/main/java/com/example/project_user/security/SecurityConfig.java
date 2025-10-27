package com.example.project_user.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// (Spring Security 6 / Boot 3)
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// 필요 시: JwtAuthenticationFilter, JwtTokenProvider, RestAuthHandlers import 유지
// import com.example.project_user.security.JwtAuthenticationFilter;
// import com.example.project_user.security.JwtTokenProvider;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final RestAuthHandlers restAuthHandlers;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(RestAuthHandlers restAuthHandlers, JwtTokenProvider jwtTokenProvider) {
        this.restAuthHandlers = restAuthHandlers;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // ✅ CORS 활성화 (아래 corsConfigurationSource()와 함께 동작)
            .cors(cors -> {})
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(restAuthHandlers)
                .accessDeniedHandler(restAuthHandlers)
            )
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable())

            .authorizeHttpRequests(auth -> auth
                // ✅ 프리플라이트 전부 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ 민감 경로는 인증 필요
                .requestMatchers(HttpMethod.GET,    "/members/me", "/members/me/**", "/v1/members/me", "/v1/members/me/**").authenticated()
                .requestMatchers(HttpMethod.PATCH,  "/members/me/**", "/v1/members/me/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/members/me", "/v1/members/me").authenticated()

                // ✅ 공개 경로
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/health").permitAll()

                // 🔑 회원가입/로그인 공개 (v1 유무 모두 대응)
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login",
                                              "/v1/auth/register", "/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,  "/members/lookup", "/v1/members/lookup").permitAll()

                .anyRequest().authenticated()
            )

            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ✅ 전역 CORS 설정 (개발용)
     * 프론트 오리진을 명시 허용. 운영에선 실제 도메인으로 제한하세요.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();

        // 프론트(Nginx)가 서빙되는 오리진을 허용
        c.setAllowedOrigins(List.of("http://localhost:8088"));
        // 패턴을 쓰고 싶으면:
        // c.setAllowedOriginPatterns(List.of("http://localhost:8088"));

        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));   // Content-Type 등 모든 헤더 허용
        c.setExposedHeaders(List.of("*"));   // 클라이언트가 읽을 수 있게 노출
        c.setAllowCredentials(false);        // 쿠키를 쓰지 않으므로 false 권장
        c.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        // 모든 엔드포인트에 CORS 적용
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}
