// com/hrt/health_routine_tracker/repository/UserRepository.java
package com.example.project_user.repository;

import com.example.project_user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
}
