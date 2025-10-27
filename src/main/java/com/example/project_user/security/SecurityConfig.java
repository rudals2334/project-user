package com.example.project_user.security;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

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
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(restAuthHandlers)
                .accessDeniedHandler(restAuthHandlers)
            )
            // ✅ 기본 로그인/폼 완전히 비활성화 (기본 비밀번호 로그 방지)
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable())

           .authorizeHttpRequests(auth -> auth
                // 1) 민감 경로는 먼저 잠그기
                .requestMatchers(HttpMethod.GET,    "/members/me", "/members/me/**").authenticated()
                .requestMatchers(HttpMethod.PATCH,  "/members/me/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/members/me").authenticated()

                // 2) 공개 경로만 허용
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,  "/members/lookup").permitAll()


                .anyRequest().authenticated()
            )



            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

