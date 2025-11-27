package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.Friendship;
import com.zzaptalk.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // -------------------------------------------------------------------------
    // 친구 목록 조회
    // -------------------------------------------------------------------------

    // '나'의 전체 친구 목록 조회 (Friendship 객체 리스트 반환)
    List<Friendship> findByUser(User user);

    // -------------------------------------------------------------------------
    // 친구 추가/중복 확인
    // -------------------------------------------------------------------------

    // '나'와 '특정 친구'의 관계가 이미 존재하는지 확인 (친구 추가 시 중복 방지)
    Optional<Friendship> findByUserAndFriend(User user, User friend);

    // 친구 관계 존재 여부 (boolean 반환)
    boolean existsByUserAndFriend(User user, User friend);

    // -------------------------------------------------------------------------
    // 친구 검색
    // -------------------------------------------------------------------------

    // '나'의 친구 목록에서 '닉네임'으로 친구 검색
    // JPA 쿼리 메소드로 Friend 엔티티의 nickname 필드 기준
    List<Friendship> findByUserAndFriendNicknameContaining(User user, String nicknameQuery);

    // 본명으로도 검색
    @Query("SELECT f FROM Friendship f WHERE f.user = :user AND " +
            "(f.friend.nickname LIKE %:query% OR f.friend.name LIKE %:query%)")
    List<Friendship> searchFriendsByNameOrNickname(@Param("user") User user, @Param("query") String query);

    // Service 개선 (1:1 , 그룹 나눔)
    @Query("SELECT DISTINCT f FROM Friendship f " +
            "LEFT JOIN FETCH f.friend " +
            "LEFT JOIN FETCH f.groupMappings gm " +
            "LEFT JOIN FETCH gm.friendGroup " +
            "WHERE f.user = :user")
    List<Friendship> findByUserWithFetchJoin(@Param("user") User user);
}