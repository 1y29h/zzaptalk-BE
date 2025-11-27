package com.zzaptalk.backend.service;

import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Long userId;

        try {
            // JWT Subject로 넘어온 문자열을 Long ID로 변환
            userId = Long.valueOf(username);
        }
        catch (NumberFormatException e) {
            // JWT의 Subject 형식이 예상(ID)과 다를 경우
            log.error("JWT Subject(username)가 유효한 ID(Long) 형식이 아닙니다. 값: {}", username, e);
            throw new UsernameNotFoundException("유효하지 않은 Subject 형식(ID 예상): " + username);
        }

        // ID를 사용하여 사용자 조회(PK로 조회하는 findById 사용)
        return userRepository.findById(userId)
                .map(this::createUserDetails)
                .orElseThrow(() -> {
                    // DB에 해당 ID 사용자가 없을 경우
                    log.warn("DB에서 해당 ID의 사용자를 찾을 수 없습니다. ID: {}", userId);
                    return new UsernameNotFoundException("해당 ID를 가진 사용자를 찾을 수 없습니다: " + username);
                });

    }

    // DB에서 가져온 User Entity 정보를 Spring Security의 UserDetails 형식으로 변환
    private UserDetails createUserDetails(User user) {
        return new CustomUserDetails(user);
    }
}