package com.vahabvahabov.AI_Powered_Question_Generation_Module.service;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Question;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionService {

    Question save(Question question);

    List<Question> saveAll(List<Question> questions);

    Page<Question> findAll(Pageable pageable);

    Page<Question> findByStatus(QuestionStatus status, Pageable pageable);

    Page<Question> findByUser(User user, Pageable pageable);

    Question findById(Long id);

    Question approveQuestion(Long id, User approvedBy);

    Question rejectQuestion(Long id, User rejectedBy);

    List<Question> batchApprove(List<Long> ids, User approvedBy);

    void delete(Long id);
}