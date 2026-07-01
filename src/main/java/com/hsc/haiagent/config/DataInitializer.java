package com.hsc.haiagent.config;

import com.hsc.haiagent.entity.User;
import com.hsc.haiagent.repository.UserRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 系统初始化 — 首次启动自动创建系统管理员
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Resource
    private UserRepository userRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("init123456"));
            admin.setEmail("admin@shopmate.com");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            log.info("✅ 系统管理员已创建: admin / init123456");
        } else {
            log.info("系统管理员已存在，跳过初始化");
        }
    }
}
