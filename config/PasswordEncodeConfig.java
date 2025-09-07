package com.tests.campuslostandfoundsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncodeConfig { // 原来是 PasswodEncodeConfig，建议改名
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 统一使用 BCrypt；数据库里的密码是 $2a/$2b 开头、无 {bcrypt} 前缀
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
