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
public class UserSignUpRequest {

    @NotBlank private String phoneNum;
    @NotBlank private String pwd;
    @NotBlank private String name;
    @NotBlank private String rrn;

}