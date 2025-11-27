package com.zzaptalk.backend.util;

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
    private static final String IV = "vokUgYNn+lQz4gM/+5ylkg==";

    // Spring Bean 초기화 후 바로 실행되어 keySpec과 ivSpec을 설정
    @PostConstruct
    public void init() {
        byte[] keyBytes = new byte[32];
        byte[] secretBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        int len = Math.min(secretBytes.length, keyBytes.length);
        System.arraycopy(secretBytes, 0, keyBytes, 0, len);

        this.keySpec = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[16];
        System.arraycopy(keyBytes, 0, iv, 0, 16);
        this.ivSpec = new IvParameterSpec(iv);

        AES256Converter.setAES256Util(this);
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