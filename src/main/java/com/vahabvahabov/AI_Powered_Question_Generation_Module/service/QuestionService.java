package com.vahabvahabov.AI_Powered_Question_Generation_Module.service;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.dto.QuestionResponseDTO;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Question;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service @RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;

    @Transactional
    public QuestionResponseDTO approveQuestion(Long id, User approvedBy) {
        Question question = findById(id);
        question.setStatus(QuestionStatus.APPROVED);
        question.setApprovedBy(approvedBy);
        question.setApprovedAt(Instant.now());
        log.info("Question {} approved by user {}", id, approvedBy.getUsername());
        question = questionRepository.save(question);
        return convertToDTO(question);
    }

    @Transactional
    public QuestionResponseDTO rejectQuestion(Long id, User rejectedBy) {
        Question question = findById(id);
        question.setStatus(QuestionStatus.REJECTED);
        question.setApprovedBy(rejectedBy);
        question.setApprovedAt(Instant.now());

        log.info("Question {} rejected by user {}", id, rejectedBy.getUsername());
        question = questionRepository.save(question);
        return convertToDTO(question);
    }

    @Transactional
    public List<QuestionResponseDTO> batchApprove(List<Long> ids, User approvedBy) {
        List<Question> questions = questionRepository.findAllById(ids);

        questions.forEach(question -> {
            question.setStatus(QuestionStatus.APPROVED);
            question.setApprovedBy(approvedBy);
            question.setApprovedAt(Instant.now());
        });

        log.info("Batch approved {} questions by user {}", questions.size(), approvedBy.getUsername());
        List<Question> savedQuestions = questionRepository.saveAll(questions);
        List<QuestionResponseDTO> responseDTOS = new ArrayList<>();
        for (Question question : savedQuestions) {
            responseDTOS.add(this.convertToDTO(question));
        }
        return responseDTOS;
    }

    @Transactional
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Transactional
    public void delete(Long id) {
        Question question = findById(id);
        question.setStatus(QuestionStatus.ARCHIVED);
        questionRepository.save(question);
    }

    public Page<QuestionResponseDTO> findQuestionByStatusIfNeeded(QuestionStatus questionStatus, Pageable pageable) {
        Page<Question> questions = questionStatus != null ? findByStatus(questionStatus, pageable) : findAll(pageable);
        Page<QuestionResponseDTO> responseDTOS = questions.map(this::convertToDTO);
        return responseDTOS;

    }

    private Page<Question> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    private Page<Question> findByStatus(QuestionStatus status, Pageable pageable) {
        return questionRepository.findByStatus(status, pageable);
    }

    public Page<QuestionResponseDTO> findByUser(User user, Pageable pageable) {
        Page<Question> questions = questionRepository.findByUserAndStatus(user.getId(), QuestionStatus.PENDING, pageable);
        return questions.map(this::convertToDTO);
    }

    public Question findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + id));
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