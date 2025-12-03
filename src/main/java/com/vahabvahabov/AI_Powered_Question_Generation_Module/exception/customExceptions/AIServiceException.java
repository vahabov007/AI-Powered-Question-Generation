package com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AIServiceException extends CustomException {

    public AIServiceException(String message) {
        super(message, "AI_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE);
    }
}

