package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // -------------------------------------------------------------------------
    // 회원가입
    // -------------------------------------------------------------------------

    // 전화번호 중복 체크
    boolean existsByPhoneNum(String phoneNum);

    // -------------------------------------------------------------------------
    // 로그인
    // -------------------------------------------------------------------------

    // 전화번호로 사용자 찾기
    // 조회 실패 가능성 때문에 Optional 사용
    Optional<User> findByPhoneNum(String phoneNum);

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // ZzapTalk ID로 사용자 찾기
    Optional<User> findByZzapID(String zzapID);

// -------------------------------------------------------------------------
// 주소록 동기화 (친구 자동 추가)
// -------------------------------------------------------------------------

    // 전화번호 목록(List)으로 여러 사용자 한 번에 찾기
    List<User> findByPhoneNumIn(List<String> phoneNumbers);



}