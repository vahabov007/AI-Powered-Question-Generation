package com.vahabvahabov.AI_Powered_Question_Generation_Module.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionGenerationRequestDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Question;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.ChoiceType;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.HuggingFaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class HuggingFaceServiceImpl implements HuggingFaceService {

    private final WebClient huggingFaceWebClient;
    private final ObjectMapper objectMapper;

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

    public HuggingFaceServiceImpl(WebClient huggingFaceWebClient, ObjectMapper objectMapper) {
        this.huggingFaceWebClient = huggingFaceWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<Question> generateQuestions(QuestionGenerationRequestDTO request, User user) {
        validateRequest(request);

        return Flux.range(0, request.getQuestionCount())
                .flatMap(i -> generateSingleQuestion(request, user, i + 1)
                        .onErrorResume(e -> {
                            log.error("Failed to generate question {}: {}", i + 1, e.getMessage());
                            return Mono.just(createFallbackQuestion(request, user, i + 1));
                        })
                );
    }

    private Mono<Question> generateSingleQuestion(QuestionGenerationRequestDTO request, User user, int questionNumber) {
        String prompt = buildQuestionPrompt(request, questionNumber);

        return callHuggingFaceAPI(prompt, questionModel)
                .flatMap(response -> parseAndValidateResponse(response, request))
                .map(questionData -> buildQuestionEntity(questionData, request, user, questionNumber))
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.fixedDelay(maxRetries, Duration.ofSeconds(2))
                        .filter(this::isRetryableError)
                        .doBeforeRetry(retry -> log.info("Retrying question generation, attempt {}", retry.totalRetries() + 1)))
                .onErrorResume(e -> {
                    log.error("Failed to generate question after retries: {}", e.getMessage());
                    return Mono.just(createFallbackQuestion(request, user, questionNumber));
                });
    }

    private String buildQuestionPrompt(QuestionGenerationRequestDTO request, int questionNumber) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate a ");
        prompt.append(request.getDifficulty().toString().toLowerCase());
        prompt.append(" difficulty ");
        prompt.append(request.getQuestionType().toString().toLowerCase().replace("_", " "));
        prompt.append(" question about '");
        prompt.append(request.getTopic());
        prompt.append("'. This is question ");
        prompt.append(questionNumber);
        prompt.append(".\n\n");

        if (request.getQuestionType() == ChoiceType.MULTIPLE_CHOICE) {
            prompt.append("Format: Provide exactly 4 options with one correct answer.\n");
            prompt.append("Response format (JSON):\n");
            prompt.append("{\n");
            prompt.append("  \"question\": \"question text\",\n");
            prompt.append("  \"options\": [\"option1\", \"option2\", \"option3\", \"option4\"],\n");
            prompt.append("  \"correctAnswer\": 0 (index of correct option),\n");
            prompt.append("  \"explanation\": \"explanation text\"\n");
            prompt.append("}");
        } else if (request.getQuestionType() == ChoiceType.TRUE_FALSE) {
            prompt.append("Response format (JSON):\n");
            prompt.append("{\n");
            prompt.append("  \"question\": \"question text\",\n");
            prompt.append("  \"correctAnswer\": \"true or false\",\n");
            prompt.append("  \"explanation\": \"explanation text\"\n");
            prompt.append("}");
        } else {
            prompt.append("Response format (JSON):\n");
            prompt.append("{\n");
            prompt.append("  \"question\": \"question text\",\n");
            prompt.append("  \"correctAnswer\": \"short answer\",\n");
            prompt.append("  \"explanation\": \"explanation text\"\n");
            prompt.append("}");
        }

        return prompt.toString();
    }

    private Mono<String> callHuggingFaceAPI(String prompt, String model) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", prompt);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_new_tokens", maxTokens);
        parameters.put("temperature", temperature);
        parameters.put("return_full_text", false);
        parameters.put("do_sample", true);
        parameters.put("top_k", 50);
        parameters.put("top_p", 0.95);

        requestBody.put("parameters", parameters);

        return huggingFaceWebClient.post()
                .uri("/models/{model}", model)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> {
                            log.error("HuggingFace API error: {}", response.statusCode());
                            return Mono.error(new RuntimeException("HuggingFace API error: " + response.statusCode()));
                        })
                .bodyToMono(String.class)
                .doOnSuccess(response -> log.debug("API response received"))
                .doOnError(error -> log.error("API call failed: {}", error.getMessage()));
    }

    private Mono<Map<String, Object>> parseAndValidateResponse(String response, QuestionGenerationRequestDTO request) {
        return Mono.fromCallable(() -> {
            try {
                JsonNode jsonNode = objectMapper.readTree(response);

                if (jsonNode.isArray() && jsonNode.size() > 0) {
                    jsonNode = jsonNode.get(0).get("generated_text");
                }
                String jsonText = jsonNode != null ? jsonNode.asText() : response;
                if (jsonText.contains("```json")) {
                    jsonText = jsonText.substring(jsonText.indexOf("```json") + 7);
                    jsonText = jsonText.substring(0, jsonText.indexOf("```")).trim();
                } else if (jsonText.contains("```")) {
                    jsonText = jsonText.substring(jsonText.indexOf("```") + 3);
                    jsonText = jsonText.substring(0, jsonText.indexOf("```")).trim();
                }
                Map<String, Object> result = objectMapper.readValue(jsonText, Map.class);
                validateQuestionData(result, request);

                return result;
            } catch (JsonProcessingException e) {
                log.error("Failed to parse API response: {}", e.getMessage());
                throw new RuntimeException("Invalid response format from AI service");
            }
        });
    }

    private void validateQuestionData(Map<String, Object> data, QuestionGenerationRequestDTO request) {
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

    private Question buildQuestionEntity(Map<String, Object> data,
                                         QuestionGenerationRequestDTO request,
                                         User user,
                                         int questionNumber) {
        Question question = new Question();
        question.setTopic(request.getTopic());
        question.setDifficulty(request.getDifficulty());
        question.setType(request.getQuestionType());
        question.setContent(data.get("question").toString());
        question.setCreatedBy(user);
        question.setCreatedAt(Instant.now());
        question.setStatus(QuestionStatus.PENDING);

        if (request.getIncludeExplanation() && data.containsKey("explanation")) {
            question.setExplanation(data.get("explanation").toString());
        } else if (request.getIncludeExplanation()) {
            // Generate explanation separately if not provided
            generateExplanation(question).subscribe(question::setExplanation);
        }

        if (request.getQuestionType() == ChoiceType.MULTIPLE_CHOICE) {
            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) data.get("options");
            question.setOptions(options);
            question.setCorrectOptionIndex((Integer) data.get("correctAnswer"));
        } else {
            question.setCorrectAnswer(data.get("correctAnswer").toString());
        }

        return question;
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

    private Question createFallbackQuestion(QuestionGenerationRequestDTO request, User user, int questionNumber) {
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

    private void validateRequest(QuestionGenerationRequestDTO request) {
        if (request.getQuestionCount() > 20) {
            throw new IllegalArgumentException("Maximum 20 questions allowed per request");
        }
    }

    private boolean isRetryableError(Throwable error) {
        return error instanceof WebClientResponseException &&
                ((WebClientResponseException) error).getStatusCode().is5xxServerError();
    }
}