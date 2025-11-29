package com.zzaptalk.backend.scheduler;

import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.entity.UserStatus;
import com.zzaptalk.backend.repository.ChatMessageRepository;
import com.zzaptalk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
 * 회원 탈퇴 후 법적 보관 기간(3개월) 지난 계정 완전 삭제 배치 작업
 *
 * 실행 시간: 매일 새벽 2시
 * 처리 내용:
 * 1. 탈퇴 후 3개월 지난 계정 조회
 * 2. 해당 유저가 보낸 메시지의 발신자를 NULL로 변경
 * 3. User 레코드 완전 삭제 (Hard Delete)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountCleanupScheduler {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    /*
     * 매일 새벽 2시에 실행
     * - cron 표현식: 초 분 시 일 월 요일
     * - "0 0 2 * * *" = 0초 0분 2시 매일 매월 모든 요일
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deleteExpiredAccounts() {
        log.info("========================================");
        log.info("탈퇴 계정 정리 배치 작업 시작");
        log.info("========================================");

        // 1. 3개월 전 날짜 계산
        LocalDateTime thresholdDate = LocalDateTime.now().minusMonths(3);
        log.info("기준 날짜: {} (이 날짜 이전 탈퇴 계정 삭제)", thresholdDate);

        // 2. 탈퇴 후 3개월 지난 계정 조회
        List<User> expiredUsers = userRepository.findByStatusAndDeletedAtBefore(
                UserStatus.DELETED,
                thresholdDate
        );

        if (expiredUsers.isEmpty()) {
            log.info("삭제 대상 계정 없음");
            return;
        }

        log.info("삭제 대상 계정 수: {}", expiredUsers.size());

        // 3. 각 유저별 처리
        int successCount = 0;
        int failCount = 0;

        for (User user : expiredUsers) {
            try {
                log.info("계정 삭제 시작 - userId: {}, 탈퇴일: {}",
                        user.getId(), user.getDeletedAt());

                // 3-1. 해당 유저가 보낸 모든 메시지의 발신자를 NULL로 변경
                // - FK 제약 조건 에러 방지
                chatMessageRepository.updateSenderToNull(user.getId());
                log.info("  → 메시지 발신자 NULL 처리 완료");

                // 3-2. User 레코드 완전 삭제 (Hard Delete)
                userRepository.delete(user);
                log.info("  → User 레코드 삭제 완료");

                successCount++;

            } catch (Exception e) {
                log.error("계정 삭제 실패 - userId: {}, 오류: {}",
                        user.getId(), e.getMessage(), e);
                failCount++;
            }
        }

        log.info("========================================");
        log.info("탈퇴 계정 정리 배치 작업 종료");
        log.info("성공: {}, 실패: {}", successCount, failCount);
        log.info("========================================");
    }
}