package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // -------------------------------------------------------------------------
    // 회원가입
    // -------------------------------------------------------------------------

    // 전화번호 중복 체크
    boolean existsByPhoneNum(String phoneNum);

    // -------------------------------------------------------------------------
    // 로그인, 활성 사용자 조회 (탈퇴 계정 제외)
    // -------------------------------------------------------------------------

    // 전화번호로 사용자 찾기
    @Query("SELECT u FROM User u WHERE u.phoneNum = :phoneNum AND u.status = 'ACTIVE'")
    Optional<User> findByPhoneNum(@Param("phoneNum") String phoneNum);

    // 조회 실패 가능성 때문에 Optional 사용
    //Optional<User> findByPhoneNum(String phoneNum);

    // 이메일로 사용자 찾기
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = 'ACTIVE'")
    Optional<User> findByEmail(String email);

    // ZzapTalk ID로 사용자 찾기
    @Query("SELECT u FROM User u WHERE u.zzapID = :zzapID AND u.status = 'ACTIVE'")
    Optional<User> findByZzapID(String zzapID);

// -------------------------------------------------------------------------
// 주소록 동기화 (친구 자동 추가)
// -------------------------------------------------------------------------

    // 전화번호 목록(List)으로 활성 사용자만 찾기
    @Query("SELECT u FROM User u WHERE u.phoneNum IN :phoneNumbers AND u.status = 'ACTIVE'")
    List<User> findByPhoneNumIn(@Param("phoneNumbers") List<String> phoneNumbers);

    // -------------------------------------------------------------------------
    // 회원 탈퇴 관련 쿼리
    // -------------------------------------------------------------------------

    // 탈퇴 계정 포함하여 전화번호로 조회 (재가입 방지용)
    @Query("SELECT u FROM User u WHERE u.phoneNum = :phoneNum")
    Optional<User> findByPhoneNumIncludingDeleted(@Param("phoneNum") String phoneNum);

    // 상태와 탈퇴일로 조회 (배치 작업용)
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deletedAt < :thresholdDate")
    List<User> findByStatusAndDeletedAtBefore(
            @Param("status") UserStatus status,
            @Param("thresholdDate") LocalDateTime thresholdDate
    );



}