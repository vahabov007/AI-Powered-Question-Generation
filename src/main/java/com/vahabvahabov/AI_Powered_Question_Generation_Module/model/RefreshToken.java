package com.vahabvahabov.AI_Powered_Question_Generation_Module.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;

@Data @AllArgsConstructor @NoArgsConstructor
@RedisHash(value = "Refresh Token", timeToLive = 259200) // 30 days
public class RefreshToken {

    @Id
    private Long id;

    @Indexed
    private String token;

    private Instant expirationTime;

    private Long userId;


}
