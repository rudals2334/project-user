package com.example.project_user.service;

import com.example.project_user.domain.User;
import com.example.project_user.dto.UserCreateReq;
import com.example.project_user.exception.BusinessException;
import com.example.project_user.exception.ErrorCode;
import com.example.project_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User create(UserCreateReq req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE, "Email exists");
        }
        if (userRepository.existsByNickname(req.getNickname())) {
            throw new BusinessException(ErrorCode.USER_DUPLICATE, "Nickname exists");
        }

        User user = User.builder()
                .email(req.getEmail())
                .username(req.getUsername())
                .nickname(req.getNickname())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));
    }

    /** 로그인: email + password(BCrypt) 검증 */
    @Transactional(readOnly = true)
    public User login(String email, String password) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "invalid credentials"));
        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED, "invalid credentials");
        }
        return u;
    }

    /** 비밀번호 변경 */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        boolean matches;
        try {
            matches = passwordEncoder.matches(currentPassword, u.getPasswordHash());
        } catch (IllegalArgumentException ex) { // 잘못된 해시 형식 등
            matches = false;
        }
        if (!matches) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "current password not match");
        }
        u.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    /** 회원 탈퇴(완전 삭제) — 루틴/댓글/좋아요는 다른 서비스 책임 */
    @Transactional
    public void deleteMe(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        // ⚠️ 모놀리식 때의 연쇄삭제(루틴/댓글/좋아요)는 제거
        // MSA에선 다음 중 하나로 처리:
        // 1) SAGA/이벤트 발행(예: USER_DELETED) → routine-service가 구독 후 정리
        // 2) API 호출(동기)로 routine-service에 사용자 데이터 정리 요청

        userRepository.delete(u);
    }
}
