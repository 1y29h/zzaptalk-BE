package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.FriendGroup;
import com.zzaptalk.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendGroupRepository extends JpaRepository<FriendGroup, Long> {

    // 사용자의 모든 그룹 조회
    List<FriendGroup> findByUserId(Long userId);

    // 특정 사용자의 특정 그룹 조회
    Optional<FriendGroup> findByUserIdAndGroupName(Long userId, String groupName);

    // 그룹명으로 존재 여부 확인
    boolean existsByUserIdAndGroupName(Long userId, String groupName);

    // 회원 탈퇴 관련 쿼리
    // 특정 유저의 모든 그룹 조회 (User 엔티티로)
    @Query("SELECT fg FROM FriendGroup fg WHERE fg.user = :user")
    List<FriendGroup> findByUser(@Param("user") User user);
}
