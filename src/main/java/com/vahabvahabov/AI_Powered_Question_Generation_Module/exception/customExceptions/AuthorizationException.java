package com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthorizationException extends CustomException {

    public AuthorizationException(String message) {
        super(message, "AUTHORIZATION_ERROR", HttpStatus.FORBIDDEN);
    }
}
