package com.yx.config;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 返回一个Gson单例对象
 */
@Configuration
public class GsonConfig {
    @Bean
    public Gson gson(){
        return new Gson();
    }
}
