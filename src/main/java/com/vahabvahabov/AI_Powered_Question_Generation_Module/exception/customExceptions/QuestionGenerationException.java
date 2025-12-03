package com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class QuestionGenerationException extends CustomException {

    public QuestionGenerationException(String message) {
        super(message, "QUESTION_GENERATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    public QuestionGenerationException(String message, HttpStatus httpStatus) {
        super(message, "QUESTION_GENERATION_ERROR", httpStatus);
    }
}
