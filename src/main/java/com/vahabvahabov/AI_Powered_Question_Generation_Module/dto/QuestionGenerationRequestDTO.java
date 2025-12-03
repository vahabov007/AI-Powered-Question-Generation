package com.vahabvahabov.AI_Powered_Question_Generation_Module.dto;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.ChoiceType;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.DifficultyType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionGenerationRequestDTO {

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Question type is required")
    private ChoiceType questionType;

    @NotNull(message = "Difficulty is required")
    private DifficultyType difficulty;

    @Min(value = 1, message = "At least 1 question must be generated")
    @Max(value = 20, message = "Maximum 20 questions per request")
    private Integer questionCount = 5;

    private Boolean includeExplanation = true;
}