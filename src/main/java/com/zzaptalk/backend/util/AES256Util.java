package com.zzaptalk.backend.util; // 예시 패키지

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AES256Util {

    // secret-key 주입
    @Value("${encryption.secret-key}")
    private String secretKey;
    private SecretKeySpec keySpec;
    private IvParameterSpec ivSpec;

    // AES 암호화에 사용될 알고리즘 (CBC 모드와 PKCS5 패딩)
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    // IV(Initialization Vector): 키 길이의 절반인 16바이트 필요(무작위 문자열)
    private static final String IV = "pL2;sNhAKY12e90N";

    // Spring Bean 초기화 후 바로 실행되어 keySpec과 ivSpec을 설정
    @PostConstruct
    public void init() {
        // 비밀 키를 사용하여 KeySpec 객체 생성
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.keySpec = new SecretKeySpec(keyBytes, "AES");

        // IV를 사용하여 IvParameterSpec 객체 생성
        this.ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
    }

    // -------------------------------------------------------------------------
    // 암호화 (평문 -> 암호문)
    // -------------------------------------------------------------------------

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            // 암호화된 바이트 배열을 DB 저장을 위해 Base64로 인코딩
            return Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e) {
            // 암호화 실패 시 예외 처리
            throw new RuntimeException("암호화 실패", e);
        }
    }

    // -------------------------------------------------------------------------
    // 복호화 (암호문 -> 평문)
    // -------------------------------------------------------------------------

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            // Base64로 인코딩된 문자열을 디코딩하여 바이트 배열로 변환
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            // 복호화
            byte[] decrypted = cipher.doFinal(decodedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            // 복호화 실패 시 예외 처리
            throw new RuntimeException("복호화 실패: " + encryptedText, e);
        }
    }

}