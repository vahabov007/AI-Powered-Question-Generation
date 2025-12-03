package com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RateLimitException extends CustomException {

    public RateLimitException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS);
    }
}
