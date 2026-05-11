package com.vahabvahabov.AI_Powered_Question_Generation_Module.controller;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionGenerationRequest;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionGenerationRequestDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionResponseDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.ApiResponse;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Question;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.HuggingFaceService;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController @Slf4j @RequiredArgsConstructor
@RequestMapping("/api/v1/questions")
@Tag(name = "Question Management",
     description = "APIs for generating and managing questions")
public class QuestionController {

    private final HuggingFaceService huggingFaceService;
    private final QuestionService questionService;

    @PostMapping
    @Operation(summary = "Generate questions using AI",
               description = "Generate quiz questions based on topic, difficulty, and type using HuggingFace AI")
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> generateQuestions(@Valid @RequestBody QuestionGenerationRequest request,
                                                                                    @AuthenticationPrincipal User user,
                                                                                    HttpServletRequest servletRequest) {
        log.info("Question generation requested by user {} for topic: {}", user.getUsername(), request.getTopic());
        List<QuestionResponseDTO> response = huggingFaceService.generateQuestions(request, user)
                .collectList()
                .block();
        return ResponseEntity.ok(ApiResponse.success("Questions were generated successfully.",response, 200,servletRequest.getServletPath()));
    }

    @GetMapping
    @Operation(summary = "Get all questions",
               description = "Retrieve paginated list of questions")
    public ResponseEntity<ApiResponse<Page<QuestionResponseDTO>>> getAllQuestions(Pageable pageable,
                                                                                  @RequestParam(required = false) QuestionStatus status,
                                                                                  HttpServletRequest servletRequest) {
        Page<QuestionResponseDTO> response = questionService.findQuestionByStatusIfNeeded(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Questions were retrieved successfully.",
                                                              response, 200,
                                                              servletRequest.getServletPath()));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a question",
               description = "Approve a generated question for use in quizzes")
    public ResponseEntity<ApiResponse<QuestionResponseDTO>> approveQuestion(@PathVariable Long id,
                                                                            @AuthenticationPrincipal User user,
                                                                            HttpServletRequest servletRequest) {
        QuestionResponseDTO response = questionService.approveQuestion(id, user);
        return ResponseEntity.ok(ApiResponse.success("Question was approved successfully.",
                                                              response, 200,
                                                              servletRequest.getServletPath()));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a question",
            description = "Reject a generated question")
    public ResponseEntity<ApiResponse<QuestionResponseDTO>> rejectQuestion(@PathVariable Long id,
                                                                           @AuthenticationPrincipal User user,
                                                                           HttpServletRequest servletRequest) {
        QuestionResponseDTO response =  questionService.rejectQuestion(id, user);
        return ResponseEntity.ok(ApiResponse.success("Question rejected.",
                                                              response, 200,
                                                              servletRequest.getServletPath()));
    }

    @GetMapping("/my-questions")
    @Operation(summary = "Get my questions",
               description = "Retrieve questions generated by the current user")
    public ResponseEntity<ApiResponse<Page<QuestionResponseDTO>>> getMyQuestions(Pageable pageable,
                                                                                 @AuthenticationPrincipal User user,
                                                                                 HttpServletRequest request) {
        Page<QuestionResponseDTO> response = questionService.findByUser(user, pageable);
        return ResponseEntity.ok(ApiResponse.success("Questions retrieved successfully.",
                                                              response, 200,
                                                              request.getServletPath()));
    }

    @PostMapping("/batch-approve")
    @Operation(summary = "Approve multiple questions")
    public ResponseEntity<ApiResponse<List<QuestionResponseDTO>>> batchApproveQuestions(@RequestBody List<Long> questionIds,
                                                                                        @AuthenticationPrincipal User user,
                                                                                        HttpServletRequest request) {
        List<QuestionResponseDTO> response = questionService.batchApprove(questionIds, user);

        return ResponseEntity.ok(ApiResponse.success("Multiple questions approved.",
                                                              response, 200,
                                                              request.getServletPath()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete question by id.")
    public ResponseEntity<ApiResponse<Void>> deleteQuestionById(@PathVariable Long id,
                                                                HttpServletRequest request) {
        questionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Question was deleted successfully.", null,
                                                        200, request.getServletPath()));
    }


}