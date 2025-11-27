package com.zzaptalk.backend.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AES256Converter implements AttributeConverter<String, String> {

    private static AES256Util aes256Util;

    // 🔹 AES256Util에서 static으로 호출할 수 있도록 그대로 static 유지
    public static void setAES256Util(AES256Util util) {
        aes256Util = util;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        // 🔹 아직 AES256Util 이 초기화되지 않은 시점(스키마 생성 등)에서는
        //    암호화를 수행하지 않고 원본 값 그대로 사용하도록 처리
        if (aes256Util == null) {
            return attribute;
        }
        return aes256Util.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // 🔹 마찬가지로 초기화 전에는 복호화 시도하지 않고 그대로 반환
        if (aes256Util == null) {
            return dbData;
        }
        return aes256Util.decrypt(dbData);
    }
}
