package com.vahabvahabov.AI_Powered_Question_Generation_Module.controller;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionGenerationRequestDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionResponseDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface QuestionController {

    public ResponseEntity<List<QuestionResponseDTO>> generateQuestions(QuestionGenerationRequestDTO request, User user);

    public ResponseEntity<Page<QuestionResponseDTO>> getAllQuestions(Pageable pageable, QuestionStatus status);

    public ResponseEntity<QuestionResponseDTO> approveQuestion(Long id, User user);

    public ResponseEntity<QuestionResponseDTO> rejectQuestion(Long id, User user);

    public ResponseEntity<Page<QuestionResponseDTO>> batchApproveQuestion(List<Long> questionIds, User user);




}
