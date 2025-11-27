package com.zzaptalk.backend.entity;

import com.zzaptalk.backend.util.AES256Converter;
import jakarta.persistence.*;
import lombok.*;

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

}