package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.FriendBlock;
import com.zzaptalk.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendBlockRepository extends JpaRepository<FriendBlock, Long> {

    // 특정 사용자가 차단한 목록 조회
    List<FriendBlock> findByUser(User user);

    // 특정 차단 관계 조회
    Optional<FriendBlock> findByUserAndBlockedUser(User user, User blockedUser);

    // 차단 여부 확인
    boolean existsByUserAndBlockedUser(User user, User blockedUser);

    // 내가 차단당했는지 확인 (역방향)
    boolean existsByUserAndBlockedUser_Id(User blockedBy, Long myUserId);

    // 내가 차단한 사용자 ID 리스트
    @Query("SELECT fb.blockedUser.id FROM FriendBlock fb WHERE fb.user = :user")
    List<Long> findBlockedUserIdsByUser(@Param("user") User user);

    // 회원 탈퇴 관련 쿼리
    @Query ("SELECT fb FROM FriendBlock fb WHERE fb.blockedUser = :blockedUser")
    List<FriendBlock> findByBlockedUser(@Param("blockedUser") User blockedUser);

    // 나를 차단한 사용자 ID 리스트
    @Query("SELECT fb.user.id FROM FriendBlock fb WHERE fb.blockedUser = :user")
    List<Long> findBlockingUserIdsByBlockedUser(@Param("user") User user);

    // MESSAGE_AND_PROFILE로 나를 차단한 사용자 ID만 조회
    @Query("SELECT fb.user.id FROM FriendBlock fb " +
            "WHERE fb.blockedUser = :user " +
            "AND fb.blockType = 'MESSAGE_AND_PROFILE'")
    List<Long> findBlockingUserIdsWithProfileHidden(@Param("user") User user);

}
