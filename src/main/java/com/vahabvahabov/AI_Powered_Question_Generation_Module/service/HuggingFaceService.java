package com.vahabvahabov.AI_Powered_Question_Generation_Module.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.HuggingFaceRequest;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionGenerationRequest;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionGenerationRequestDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionResponseDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.exception.customExceptions.CustomException;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Question;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.ChoiceType;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service @Slf4j @RequiredArgsConstructor
public class HuggingFaceService {

    private final WebClient huggingFaceWebClient;
    private final ObjectMapper objectMapper;
    private final QuestionService questionService;
    private final ResourceLoader resourceLoader;

    @Value("${huggingface.model.question-generation}")
    private String questionModel;

    @Value("${huggingface.model.explanation-generation}")
    private String explanationModel;

    @Value("${huggingface.generation.max-tokens}")
    private int maxTokens;

    @Value("${huggingface.generation.temperature}")
    private double temperature;

    @Value("${huggingface.api.max-retries}")
    private int maxRetries;

    public Flux<QuestionResponseDTO> generateQuestions(QuestionGenerationRequest request, User user) {
        return Flux.range(1, request.getQuestionCount())
                .flatMap(i -> generateSingleQuestion(request, user, i))
                .map(this::convertToDTO);
    }

    private Mono<Question> generateSingleQuestion(QuestionGenerationRequest request, User user, int questionNumber) {
        String prompt = buildQuestionPrompt(request, questionNumber);

        return callHuggingFaceAPI(prompt, questionModel)
                .flatMap(response -> parseAndValidateResponse(response, request))
                .flatMap(data -> {
                    Question question = buildQuestionEntity(data, request, user);

                    // Properly chain the explanation so the save waits for it
                    if (request.getIncludeExplanation() && (question.getExplanation() == null || question.getExplanation().isEmpty())) {
                        return generateExplanation(question)
                                .map(exp -> {
                                    question.setExplanation(exp);
                                    return question;
                                });
                    }
                    return Mono.just(question);
                })
                // CRITICAL: Save here, inside the stream
                .map(question -> questionService.save(question))
                .timeout(Duration.ofSeconds(60))
                .onErrorResume(e -> {
                    log.error("AI GENERATION FAILED! Error type: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage());
                    e.printStackTrace(); // This will show exactly what went wrong in the console
                    Question fallback = createFallbackQuestion(request, user, questionNumber);
                    return Mono.just(questionService.save(fallback));
                });
    }

    private Question buildQuestionEntity(Map<String, Object> data, QuestionGenerationRequest request, User user) {
        Question question = new Question();
        question.setTopic(request.getTopic());
        question.setDifficulty(request.getDifficulty());
        question.setType(request.getQuestionType());
        question.setContent(data.get("question").toString());
        question.setCreatedBy(user);
        question.setCreatedAt(Instant.now());
        question.setStatus(QuestionStatus.PENDING);

        // If explanation is already in the first AI response, set it
        if (request.getIncludeExplanation() && data.containsKey("explanation")) {
            question.setExplanation(data.get("explanation").toString());
        }

        if (request.getQuestionType() == ChoiceType.MULTIPLE_CHOICE) {
            List<String> options = (List<String>) data.get("options");
            question.setOptions(options);
            question.setCorrectOptionIndex((Integer) data.get("correctAnswer"));
        } else {
            question.setCorrectAnswer(data.get("correctAnswer").toString());
        }
        return question;
    }

    private String buildQuestionPrompt(QuestionGenerationRequest request, int questionNumber) {
        String fileName = request.getQuestionType().name().toLowerCase() + ".txt";
        String template = loadTemplateFromResources(fileName);

        String basePrompt = template
                .replace("{difficulty}", request.getDifficulty().name().toLowerCase())
                .replace("{topic}", request.getTopic())
                .replace("{number}", String.valueOf(questionNumber));

        return "<s>[INST] " + basePrompt + " [/INST]";
    }

    private Mono<String> callHuggingFaceAPI(String prompt, String model) {
        HuggingFaceRequest requestBody = HuggingFaceRequest.builder()
                .inputs(prompt)
                .parameters(HuggingFaceRequest.Parameters.builder()
                        .maxNewTokens(maxTokens)
                        .temperature(temperature)
                        .returnFullText(false)
                        .doSample(true) // give different questions
                        .topK(50)
                        .topP(0.95)
                        .build())
                .build();

        return huggingFaceWebClient.post()
                .uri(uriBuilder -> uriBuilder.pathSegment("models", model).build())
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("HuggingFace API failure: {} - {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new CustomException("AI Model Error: " + errorBody, HttpStatus.BAD_GATEWAY));
                                })
                )
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))) // Resilience: Retry 3 times if API is busy
                .doOnSuccess(res -> log.info("Successfully generated question from model: {}", model))
                .doOnError(e -> log.error("Failed to call HuggingFace: {}", e.getMessage()));
    }

    private Mono<Map<String, Object>> parseAndValidateResponse(String response, QuestionGenerationRequest request) {
        return Mono.fromCallable(() -> {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                String jsonText;

                if (rootNode.isArray() && rootNode.has(0)) {
                    jsonText = rootNode.get(0).path("generated_text").asText();
                } else {
                    jsonText = response;
                }

                jsonText = cleanJsonFormatting(jsonText);

                Map<String, Object> result = objectMapper.readValue(jsonText, Map.class);
                validateQuestionData(result, request);
                return result;
            } catch (Exception e) {
                log.error("JSON Parsing failed: {}", e.getMessage());
                throw new RuntimeException("AI returned invalid JSON structure");
            }
        });
    }

    private String cleanJsonFormatting(String input) {
        if (input.contains("```json")) {
            input = input.substring(input.indexOf("```json") + 7);
        } else if (input.contains("```")) {
            input = input.substring(input.indexOf("```") + 3);
        }
        if (input.contains("```")) {
            input = input.substring(0, input.lastIndexOf("```"));
        }
        return input.trim();
    }

    private void validateQuestionData(Map<String, Object> data, QuestionGenerationRequest request) {
        if (!data.containsKey("question") || data.get("question") == null) {
            throw new IllegalArgumentException("Missing question field in AI response");
        }

        if (request.getQuestionType() == ChoiceType.MULTIPLE_CHOICE) {
            if (!data.containsKey("options") || !(data.get("options") instanceof List)) {
                throw new IllegalArgumentException("Missing or invalid options field");
            }

            List<?> options = (List<?>) data.get("options");
            if (options.size() != 4) {
                throw new IllegalArgumentException("Expected exactly 4 options");
            }

            if (!data.containsKey("correctAnswer") ||
                    !(data.get("correctAnswer") instanceof Integer)) {
                throw new IllegalArgumentException("Missing or invalid correctAnswer field");
            }

            int correctIndex = (Integer) data.get("correctAnswer");
            if (correctIndex < 0 || correctIndex >= options.size()) {
                throw new IllegalArgumentException("Invalid correct answer index");
            }
        }
    }

    private Mono<String> generateExplanation(Question question) {
        String prompt = String.format(
                "Explain why the answer is correct for this question: \"%s\". " +
                        "Provide a concise, educational explanation in 1-2 sentences.",
                question.getContent()
        );

        return callHuggingFaceAPI(prompt, explanationModel)
                .map(response -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(response);
                        if (jsonNode.isArray() && jsonNode.size() > 0) {
                            return jsonNode.get(0).get("generated_text").asText();
                        }
                        return response;
                    } catch (Exception e) {
                        log.warn("Failed to parse explanation response: {}", e.getMessage());
                        return "The correct answer is determined by the fundamental principles of the subject matter.";
                    }
                })
                .onErrorReturn("Explanation not available at this time.");
    }

    private Question createFallbackQuestion(QuestionGenerationRequest request, User user, int questionNumber) {
        Question question = new Question();
        question.setTopic(request.getTopic());
        question.setDifficulty(request.getDifficulty());
        question.setType(request.getQuestionType());
        question.setContent(String.format("What is a key concept about %s? (Question %d)",
                request.getTopic(), questionNumber));
        question.setCreatedBy(user);
        question.setCreatedAt(Instant.now());
        question.setStatus(QuestionStatus.PENDING);
        question.setExplanation("This question was generated as a fallback. Please review and edit.");

        if (request.getQuestionType() == ChoiceType.MULTIPLE_CHOICE) {
            question.setOptions(Arrays.asList(
                    "Option A",
                    "Option B",
                    "Option C",
                    "Option D"
            ));
            question.setCorrectOptionIndex(0);
        } else {
            question.setCorrectAnswer("Sample answer");
        }

        return question;
    }

    private String loadTemplateFromResources(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/" + fileName);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            log.error("Failed to load prompt template: {}", fileName);
            throw new CustomException("Internal server error: template missing", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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