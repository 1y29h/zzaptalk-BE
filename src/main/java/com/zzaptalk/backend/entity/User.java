package com.zzaptalk.backend.entity;

import com.zzaptalk.backend.util.AES256Converter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // 로그인/식별자
    // -------------------------------------------------------------------------

    // 전화번호
    @Convert(converter = AES256Converter.class)
    private String phoneNum;

    // 비밀번호
    @Column(nullable = false, length = 255)    // 해싱(단방향 암호화) 대비 길이 확장
    private String pwd;

    // -------------------------------------------------------------------------
    // 기타 프로필 및 식별 정보
    // -------------------------------------------------------------------------

    // 본명
    @Column(nullable = false)
    private String name;

    // 닉네임
    @Column(nullable = false)
    private String nickname;

    // 주민번호 7자리
    @Convert(converter = AES256Converter.class)
    private String rrn;

    // 이메일
    @Column(unique = true)
    private String email;

    // ZzapTalk ID
    @Column(unique = true)
    private String zzapID;

// -------------------------------------------------------------------------
// 친구 프로필 및 생일 기능용 필드
// -------------------------------------------------------------------------
    // 이거 다시 봐야될듯!!/////////////////////////////////
    // 생일 (LocalDate 타입) - rrn에서 파싱하여 저장하거나 동적 계산
    @Column
    private java.time.LocalDate birthday;

    // 상태 메시지
    @Column(length = 500)
    private String statusMessage;

    // 프로필 사진 URL
    @Column
    private String profilePhotoUrl;

    // 배경 사진 URL
    @Column
    private String backgroundPhotoUrl;


    // -------------------------------------------------------------------------
    // 회원 탈퇴 관련 필드 (Soft Delete)
    // -------------------------------------------------------------------------

    // 계정 상태 (ACTIVE: 활성, DELETED: 탈퇴)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    // 탈퇴 일시 (탈퇴 시에만 값이 들어감)
    @Column
    private LocalDateTime deletedAt;

    // -------------------------------------------------------------------------
    // 회원 탈퇴 시 개인정보 마스킹 메서드
    // -------------------------------------------------------------------------

    /**
     * 회원 탈퇴 시 개인정보를 마스킹 처리
     * - 법적으로 즉시 파기해야 하는 정보들을 삭제/마스킹
     * - 로그 보관을 위해 User 레코드 자체는 유지
     */
    public void maskPersonalData() {
        // 식별 정보 삭제
        this.phoneNum = null;              // 전화번호 삭제 (재가입 방지용으로 별도 관리 필요 시 보관)
        this.pwd = "DELETED";              // 비밀번호 마스킹
        this.name = "알 수 없음";           // 본명 마스킹
        this.rrn = null;                   // 주민번호 삭제
        this.email = null;                 // 이메일 삭제
        this.zzapID = null;                // ZzapID 삭제

        // 프로필 정보 초기화
        this.nickname = "알 수 없는 사용자";
        this.profilePhotoUrl = null;       // 프로필 사진 삭제 (Cloudflare R2에서도 삭제 필요)
        this.backgroundPhotoUrl = null;    // 배경 사진 삭제
        this.statusMessage = null;         // 상태 메시지 삭제
        this.birthday = null;              // 생일 정보 삭제
    }


}