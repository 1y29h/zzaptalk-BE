package com.zzaptalk.backend.util;

import com.zzaptalk.backend.repository.UserRepository;
import com.zzaptalk.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    // 현재 Spring Security Context에 저장된 사용자의 phoneNum을 기반으로 User 엔티티 반환
    // @return 로그인된 사용자 User 엔티티(Optional)

    public Optional<User> getCurrentUser() {

        // SecurityContext에서 Authentication 객체 가져오기
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 익명 사용자일 경우
        if (authentication == null || authentication.getName() == null) {
            return Optional.empty();
        }

        // Authentication 객체의 phoneNum 가져오기
        String phoneNum = authentication.getName();

        // phoneNum으로 DB에서 User 엔티티 찾기
        return userRepository.findByPhoneNum(phoneNum);

    }

}