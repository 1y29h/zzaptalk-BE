package com.zzaptalk.backend.service;

import com.zzaptalk.backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

// UserDetails 인터페이스를 구현하여 SecurityContext에 저장될 사용자 정보를 정의합니다.
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;     // JWT 토큰의 Subject(ID)와 매핑될 실제 사용자 ID
    private final String phoneNum; // 사용자 ID 대신 토큰에 phoneNum이 포함될 경우 대비
    private final String password; // 비밀번호
    private final User user;

    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.phoneNum = user.getPhoneNum();
        this.password = user.getPwd(); // 엔티티의 비밀번호 필드명 확인
        this.user = user;
    }

    // (선택 사항): 전화번호가 사용자 이름으로 사용된다고 가정
    @Override
    public String getUsername() {
        return this.phoneNum;
    }

    // 비밀번호 반환
    @Override
    public String getPassword() {
        return this.password;
    }

    // 권한 설정 (일반 사용자 권한만 부여)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 단순 구현 시 권한은 비워둡니다.
    }

    // 계정 만료, 잠금, 자격 증명 만료, 활성화 여부는 필요에 따라 true로 설정
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}