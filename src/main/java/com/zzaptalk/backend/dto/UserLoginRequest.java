package com.zzaptalk.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {

    // -------------------------------------------------------------------------
    // 아이디
    // -------------------------------------------------------------------------
    private String phoneNum;
    private String email;
    private String zzapID;

    // -------------------------------------------------------------------------
    // 비밀번호
    // -------------------------------------------------------------------------
    @NotBlank(message = "비밀번호는 필수로 입력해야 합니다.")
    private String pwd;

}