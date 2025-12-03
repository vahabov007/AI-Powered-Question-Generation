package com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationException extends CustomException {

    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_ERROR", HttpStatus.UNAUTHORIZED);
    }
}