package com.example.project_user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessExpMillis;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.key = createKey(jwtProperties.getSecret());
        this.accessExpMillis = jwtProperties.getAccessExpMinutes() * 60_000L;
    }

    private SecretKey createKey(String secret) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        // HS256 requires >= 256 bits (32 bytes). 개발 환경에서 짧으면 반복 패딩해 보강
        if (raw.length < 32) {
            byte[] extended = new byte[32];
            for (int i = 0; i < extended.length; i++) extended[i] = raw[i % raw.length];
            raw = extended;
        }
        return Keys.hmacShaKeyFor(raw);
    }

    public String createToken(Long userId, String email, String nickname) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("nickname", nickname)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessExpMillis)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    public JwtUserPrincipal parsePrincipal(String token) {
        Claims claims = parse(token);
        Long userId = claims.get("userId", Number.class).longValue();
        String email = claims.getSubject();
        return new JwtUserPrincipal(userId, email);
    }
}


