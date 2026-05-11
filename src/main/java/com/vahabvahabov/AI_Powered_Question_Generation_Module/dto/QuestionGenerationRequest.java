package com.vahabvahabov.AI_Powered_Question_Generation_Module.dto;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.ChoiceType;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.DifficultyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGenerationRequest {

    @NotBlank(message = "Topic cannot be empty")
    @Size(min = 2, max = 100, message = "Topic must be between 2 and 100 characters")
    private String topic;

    @Min(value = 1, message = "At least 1 question must be generated")
    @Max(value = 20, message = "Maximum 20 questions per request")
    private Integer questionCount = 5;

    @NotNull(message = "Difficulty level is required")
    private DifficultyType difficulty;

    @NotNull(message = "Question type is required")
    private ChoiceType questionType;

    @Min(value = 1, message = "You must generate at least 1 question")
    @Max(value = 10, message = "You can generate a maximum of 10 questions per request")
    private Integer count;

    // Optional: Language support (e.g., "en", "az")
    private String language = "en";

    private Boolean includeExplanation = true;

}
