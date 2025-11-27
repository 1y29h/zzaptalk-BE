package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.FriendGroupMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendGroupMappingRepository extends JpaRepository<FriendGroupMapping, Long> {

    // 특정 친구가 속한 모든 그룹 조회
    List<FriendGroupMapping> findByFriendshipId(Long friendshipId);

    // 특정 그룹에 속한 모든 친구 조회
    List<FriendGroupMapping> findByFriendGroupId(Long friendGroupId);

    // 특정 친구-그룹 매핑 삭제
    void deleteByFriendshipIdAndFriendGroupId(Long friendshipId, Long friendGroupId);

    // 특정 친구가 특정 그룹에 속해있는지 확인
    boolean existsByFriendshipIdAndFriendGroupId(Long friendshipId, Long friendGroupId);

    // 특정 사용자의 특정 그룹에 속한 친구 목록 조회 (JOIN 쿼리)
    @Query("SELECT fgm FROM FriendGroupMapping fgm " +
            "JOIN fgm.friendship f " +
            "WHERE f.user.id = :userId AND fgm.friendGroup.id = :groupId")
    List<FriendGroupMapping> findFriendsByUserIdAndGroupId(
            @Param("userId") Long userId,
            @Param("groupId") Long groupId
    );
}
