package com.zzaptalk.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication //(exclude = {SecurityAutoConfiguration.class}) // Security 자동 설정 제외
@EnableScheduling
public class ZzaptalkBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZzaptalkBackendApplication.class, args);
    }

}