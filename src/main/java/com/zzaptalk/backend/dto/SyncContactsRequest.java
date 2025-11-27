package com.zzaptalk.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncContactsRequest {

    // 사용자 주소록에 있는 전화번호 목록
    @NotNull(message = "전화번호 목록은 필수입니다.")
    private List<String> phoneNumbers;
}