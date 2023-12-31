package com.yx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 */
@SpringBootApplication
@MapperScan("com.yx.mapper")
@EnableScheduling
public class UserMatchBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserMatchBackendApplication.class, args);
    }

}
