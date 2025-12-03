package com.vahabvahabov.AI_Powered_Question_Generation_Module.dto;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.ChoiceType;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.DifficultyType;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class QuestionResponseDTO {
    private Long id;
    private String topic;
    private DifficultyType difficulty;
    private ChoiceType type;
    private String content;
    private List<String> options;
    private Integer correctOptionIndex;
    private String correctAnswer;
    private String explanation;
    private QuestionStatus status;
    private Instant createdAt;
    private String createdByUsername;
}