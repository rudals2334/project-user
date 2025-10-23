/**
 * 사용자 엔티티 클래스
 * 
 * Health Routine Tracker 애플리케이션의 사용자 정보를 나타내는 JPA 엔티티입니다.
 * 사용자의 기본 정보(이메일, 사용자명, 닉네임)와 인증 정보(비밀번호 해시)를 관리합니다.
 * 
 * 주요 기능:
 * - 사용자 등록 및 인증
 * - 이메일과 닉네임의 유니크 제약조건
 * - 생성/수정 시간 자동 관리
 * 
 * @author Health Routine Tracker Team
 * @version 1.0
 * @since 2024
 */
// com/hrt/health_routine_tracker/domain/User.java
package com.example.project_user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * JPA 엔티티로 사용자 테이블과 매핑
 * 
 * @param name 데이터베이스 테이블명
 * @param uniqueConstraints 이메일과 닉네임에 대한 유니크 제약조건 설정
 */
@Entity
@Table(name="users",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_users_email", columnNames = "email"),
                @UniqueConstraint(name="uk_users_nickname", columnNames = "nickname")
        })
/**
 * Lombok 어노테이션으로 보일러플레이트 코드 자동 생성
 * - @Getter: 모든 필드에 대한 getter 메서드 생성
 * - @Setter: 모든 필드에 대한 setter 메서드 생성
 * - @NoArgsConstructor: 기본 생성자 생성
 * - @AllArgsConstructor: 모든 필드를 매개변수로 받는 생성자 생성
 * - @Builder: 빌더 패턴 구현
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    /**
     * 사용자 고유 식별자 (Primary Key)
     * 
     * 데이터베이스에서 자동으로 증가하는 BIGINT 타입의 기본키입니다.
     * GenerationType.IDENTITY를 사용하여 데이터베이스의 AUTO_INCREMENT 기능을 활용합니다.
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    /**
     * 사용자 이메일 주소
     * 
     * 로그인 시 사용되는 고유한 식별자입니다.
     * 최대 100자까지 입력 가능하며, NULL을 허용하지 않습니다.
     * 데이터베이스 레벨에서 유니크 제약조건이 적용됩니다.
     */
    @Column(nullable=false, length=100)
    private String email;

    /**
     * 사용자명 (실제 이름)
     * 
     * 사용자의 실제 이름을 저장합니다.
     * 최대 50자까지 입력 가능하며, NULL을 허용하지 않습니다.
     */
    @Column(nullable=false, length=50)
    private String username;

    /**
     * 사용자 닉네임
     * 
     * 다른 사용자들에게 표시되는 고유한 닉네임입니다.
     * 최대 50자까지 입력 가능하며, NULL을 허용하지 않습니다.
     * 데이터베이스 레벨에서 유니크 제약조건이 적용됩니다.
     */
    @Column(nullable=false, length=50)
    private String nickname;

    /**
     * 비밀번호 해시값
     * 
     * 보안을 위해 평문 비밀번호가 아닌 해시된 값을 저장합니다.
     * BCrypt 등 안전한 해시 알고리즘을 사용하여 암호화됩니다.
     * 최대 200자까지 저장 가능하며, NULL을 허용하지 않습니다.
     */
    @Column(name="password_hash", nullable=false, length=200)
    private String passwordHash;

    /**
     * 계정 생성 시간
     * 
     * 사용자가 처음 등록된 시간을 기록합니다.
     * 한 번 설정되면 수정할 수 없습니다 (updatable=false).
     * 기본값으로 현재 시간이 자동 설정됩니다.
     */
    @Column(name="created_at", nullable=false, updatable=false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * 계정 정보 수정 시간
     * 
     * 사용자 정보가 마지막으로 수정된 시간을 기록합니다.
     * 정보가 수정될 때마다 자동으로 현재 시간으로 업데이트됩니다.
     * 기본값으로 현재 시간이 자동 설정됩니다.
     */
    @Column(name="updated_at", nullable=false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    /**
     * 엔티티 저장 전 실행되는 콜백 메서드
     * 
     * 새로운 사용자 엔티티가 데이터베이스에 저장되기 전에
     * 생성 시간과 수정 시간을 현재 시간으로 설정합니다.
     */
    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 엔티티 수정 전 실행되는 콜백 메서드
     * 
     * 기존 사용자 엔티티가 수정되기 전에
     * 수정 시간을 현재 시간으로 업데이트합니다.
     */
    @PreUpdate
    void onUpdate() { this.updatedAt = Instant.now(); }
}

