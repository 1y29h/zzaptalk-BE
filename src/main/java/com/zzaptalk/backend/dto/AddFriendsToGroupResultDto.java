package com.zzaptalk.backend.dto;

import lombok.*;

import java.util.List;

/**
 * 여러 친구를 그룹에 추가한 결과를 담는 DTO
 * - 성공/실패 개수와 상세 에러 메시지를 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddFriendsToGroupResultDto {
    
    // 요청한 총 친구 수
    private int totalRequested;
    
    // 성공적으로 추가된 친구 수
    private int successCount;
    
    // 추가 실패한 친구 수
    private int failedCount;
    
    // 실패한 경우의 에러 메시지 목록
    private List<String> errors;
}
