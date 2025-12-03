package com.vahabvahabov.AI_Powered_Question_Generation_Module.controller;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionGenerationRequestDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionResponseDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Question;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.HuggingFaceService;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Question Management", description = "APIs for generating and managing questions")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class QuestionControllerImpl {

    @Autowired
    private HuggingFaceService huggingFaceService;

    @Autowired
    private QuestionService questionService;

    @PostMapping("/generate")
    @Operation(summary = "Generate questions using AI",
            description = "Generate quiz questions based on topic, difficulty, and type using HuggingFace AI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Questions generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "Internal server error or AI service unavailable")
    })
    public ResponseEntity<List<QuestionResponseDTO>> generateQuestions(
            @Valid @RequestBody QuestionGenerationRequestDTO request,
            @AuthenticationPrincipal User user) {
        log.info("Question generation requested by user {} for topic: {}", user.getUsername(), request.getTopic());
        List<Question> questions = huggingFaceService.generateQuestions(request, user)
                .collectList()
                .block();
        List<Question> savedQuestions = questionService.saveAll(questions);
        List<QuestionResponseDTO> response = savedQuestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all questions", description = "Retrieve paginated list of questions")
    public ResponseEntity<Page<QuestionResponseDTO>> getAllQuestions(
            Pageable pageable,
            @RequestParam(required = false) QuestionStatus status) {
        Page<Question> questions = status != null
                ? questionService.findByStatus(status, pageable)
                : questionService.findAll(pageable);
        Page<QuestionResponseDTO> response = questions.map(this::convertToDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a question",
            description = "Approve a generated question for use in quizzes")
    @ApiResponse(responseCode = "200", description = "Question approved successfully")
    @ApiResponse(responseCode = "404", description = "Question not found")
    public ResponseEntity<QuestionResponseDTO> approveQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        Question question = questionService.approveQuestion(id, user);
        return ResponseEntity.ok(convertToDTO(question));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a question",
            description = "Reject a generated question")
    public ResponseEntity<QuestionResponseDTO> rejectQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        Question question = questionService.rejectQuestion(id, user);
        return ResponseEntity.ok(convertToDTO(question));
    }

    @GetMapping("/my-questions")
    @Operation(summary = "Get my questions",
            description = "Retrieve questions generated by the current user")
    public ResponseEntity<Page<QuestionResponseDTO>> getMyQuestions(
            Pageable pageable,
            @AuthenticationPrincipal User user) {
        Page<Question> questions = questionService.findByUser(user, pageable);
        Page<QuestionResponseDTO> response = questions.map(this::convertToDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-approve")
    @Operation(summary = "Approve multiple questions")
    public ResponseEntity<List<QuestionResponseDTO>> batchApproveQuestions(
            @RequestBody List<Long> questionIds,
            @AuthenticationPrincipal User user) {
        List<Question> questions = questionService.batchApprove(questionIds, user);
        List<QuestionResponseDTO> response = questions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private QuestionResponseDTO convertToDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setTopic(question.getTopic());
        dto.setDifficulty(question.getDifficulty());
        dto.setType(question.getType());
        dto.setContent(question.getContent());
        dto.setOptions(question.getOptions());
        dto.setCorrectOptionIndex(question.getCorrectOptionIndex());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());
        dto.setStatus(question.getStatus());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setCreatedByUsername(question.getCreatedBy().getUsername());
        return dto;
    }
}