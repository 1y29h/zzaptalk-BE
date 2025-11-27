package com.zzaptalk.backend.entity;

public enum BlockType {
    MESSAGE_ONLY,              // 메시지만 차단 (프로필 볼 수 있음)
    MESSAGE_AND_PROFILE,       // 메시지 차단 + 프로필 비공개
    NONE                       // 차단 안함 (기본값)
}
