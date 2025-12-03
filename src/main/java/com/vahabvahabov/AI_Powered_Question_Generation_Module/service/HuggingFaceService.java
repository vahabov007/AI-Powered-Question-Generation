package com.vahabvahabov.AI_Powered_Question_Generation_Module.service;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionGenerationRequestDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Question;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import reactor.core.publisher.Flux;

public interface HuggingFaceService {

    public Flux<Question> generateQuestions(QuestionGenerationRequestDTO request, User user);
}
