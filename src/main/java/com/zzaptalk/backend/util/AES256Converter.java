package com.zzaptalk.backend.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

// autoApply = true로 설정하면, 해당 타입(String)의 모든 필드에 자동 적용됨
// 여기서는 rrn, phoneNum 필드에만 수동으로 적용하기 위해 false 또는 생략

@Converter
@Component
public class AES256Converter implements AttributeConverter<String, String>, ApplicationContextAware {

    private static AES256Util aes256Util;

    // ApplicationContextAware를 사용하여 Spring Bean을 주입받는 정적(static) 방식
    // Converter는 일반 Bean 주입이 어렵기 때문에 이 방식을 사용
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        AES256Converter.aes256Util = applicationContext.getBean(AES256Util.class);
    }

    // -------------------------------------------------------------------------
    // 엔티티 -> DB 컬럼 (데이터베이스에 저장할 때: 암호화)
    // -------------------------------------------------------------------------

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return aes256Util.encrypt(attribute);
    }

    // -------------------------------------------------------------------------
    // DB 컬럼 -> 엔티티 (데이터베이스에서 불러올 때: 복호화)
    // -------------------------------------------------------------------------

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return aes256Util.decrypt(dbData);
    }

}