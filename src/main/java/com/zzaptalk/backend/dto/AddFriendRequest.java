package com.zzaptalk.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddFriendRequest {

    // 식별자 (전화번호 또는 zzapID)
    @NotBlank(message = "식별자를 입력해주세요.")
    private String identifier;

    // 타입 ("PHONE" 또는 "ZZAPID")
    @NotNull(message = "타입을 지정해주세요.")
    private String type; // "PHONE" or "ZZAPID"

    // 참고: ENUM으로 만들면 더 좋음
    // private IdentifierType type;
    // public enum IdentifierType { PHONE, ZZAPID }
}