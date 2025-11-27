package com.zzaptalk.backend.util;

import java.time.LocalDate;

public class BirthdayUtil {

    /**
     * RRN(주민번호 앞 7자리)에서 생일을 파싱
     *
     * @param rrn 주민번호 앞 7자리 (예: "960315-1")
     * @return 생일 (LocalDate) 또는 null
     */
    public static LocalDate parseBirthdayFromRrn(String rrn) {
        if (rrn == null || rrn.length() < 7) {
            return null;
        }

        try {
            // RRN 형식: YYMMDD-G (7자리)
            // 예: 960315-1 -> 1996년 3월 15일

            // 1. YYMMDD 추출
            String yymmdd = rrn.substring(0, 6);
            int yy = Integer.parseInt(yymmdd.substring(0, 2));
            int mm = Integer.parseInt(yymmdd.substring(2, 4));
            int dd = Integer.parseInt(yymmdd.substring(4, 6));

            // 2. 성별 코드로 세기 구분
            char genderCode = rrn.charAt(6);
            int year;

            switch (genderCode) {
                case '1': case '2': // 1900년대생
                    year = 1900 + yy;
                    break;
                case '3': case '4': // 2000년대생
                    year = 2000 + yy;
                    break;
                case '9': case '0': // 1800년대생 (거의 없음)
                    year = 1800 + yy;
                    break;
                default:
                    return null;
            }

            // 3. LocalDate 생성
            return LocalDate.of(year, mm, dd);

        } catch (Exception e) {
            // 파싱 실패 시 null 반환
            return null;
        }
    }

    /**
     * 주어진 생일이 오늘 기준 ±N일 이내인지 확인
     *
     * @param birthday 생일
     * @param days 기준 일수 (예: 7 -> ±7일)
     * @return 범위 내 여부
     */
    public static boolean isWithinBirthdayRange(LocalDate birthday, int days) {
        if (birthday == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        // 올해 기준 생일 날짜
        LocalDate birthdayThisYear = birthday.withYear(today.getYear());

        // ±N일 범위
        LocalDate startRange = today.minusDays(days);
        LocalDate endRange = today.plusDays(days);

        return !birthdayThisYear.isBefore(startRange) &&
                !birthdayThisYear.isAfter(endRange);
    }
}