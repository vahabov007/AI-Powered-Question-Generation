package com.vahabvahabov.AI_Powered_Question_Generation_Module.repository;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.Question;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Page<Question> findByStatus(QuestionStatus status, Pageable pageable);

    Page<Question> findByTopicContainingIgnoreCaseAndStatus(String topic, QuestionStatus status, Pageable pageable);

    List<Question> findByCreatedById(Long userId);

    @Query("SELECT q FROM Question q WHERE q.createdBy.id = :userId AND q.status = :status")
    Page<Question> findByUserAndStatus(@Param("userId") Long userId,
                                       @Param("status") QuestionStatus status,
                                       Pageable pageable);

    long countByStatus(QuestionStatus status);
}