package com.vahabvahabov.AI_Powered_Question_Generation_Module.service.impl;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Question;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.QuestionRepository;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.service.QuestionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    @Transactional
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public List<Question> saveAll(List<Question> questions) {
        return questionRepository.saveAll(questions);
    }

    @Override
    public Page<Question> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    @Override
    public Page<Question> findByStatus(QuestionStatus status, Pageable pageable) {
        return questionRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Question> findByUser(User user, Pageable pageable) {
        return questionRepository.findByUserAndStatus(user.getId(), QuestionStatus.PENDING, pageable);
    }

    @Override
    public Question findById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + id));
    }

    @Override
    @Transactional
    public Question approveQuestion(Long id, User approvedBy) {
        Question question = findById(id);
        question.setStatus(QuestionStatus.APPROVED);
        question.setApprovedBy(approvedBy);
        question.setApprovedAt(Instant.now());

        log.info("Question {} approved by user {}", id, approvedBy.getUsername());
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public Question rejectQuestion(Long id, User rejectedBy) {
        Question question = findById(id);
        question.setStatus(QuestionStatus.REJECTED);
        question.setApprovedBy(rejectedBy);
        question.setApprovedAt(Instant.now());

        log.info("Question {} rejected by user {}", id, rejectedBy.getUsername());
        return questionRepository.save(question);
    }

    @Override
    @Transactional
    public List<Question> batchApprove(List<Long> ids, User approvedBy) {
        List<Question> questions = questionRepository.findAllById(ids);

        questions.forEach(question -> {
            question.setStatus(QuestionStatus.APPROVED);
            question.setApprovedBy(approvedBy);
            question.setApprovedAt(Instant.now());
        });

        log.info("Batch approved {} questions by user {}", questions.size(), approvedBy.getUsername());
        return questionRepository.saveAll(questions);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Question question = findById(id);
        question.setStatus(QuestionStatus.ARCHIVED);
        questionRepository.save(question);
    }
}