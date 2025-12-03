package com.vahabvahabov.AI_Powered_Question_Generation_Module.security;

import org.apache.catalina.filters.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RateLimitConfig {

    @Bean
    @Autowired
    public RateLimitFilter rateLimitFilter(RedisTemplate<String, Object> redisTemplate) {
        return new RateLimitFilter();
    }
}

