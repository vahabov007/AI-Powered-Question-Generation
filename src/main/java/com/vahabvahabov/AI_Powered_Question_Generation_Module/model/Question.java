package com.vahabvahabov.AI_Powered_Question_Generation_Module.model;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.ChoiceType;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.DifficultyType;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.QuestionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String topic;

    @Enumerated(EnumType.STRING)
    private DifficultyType difficulty;

    @Enumerated(EnumType.STRING)
    private ChoiceType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text", columnDefinition = "TEXT")
    private List<String> options;

    private Integer correctOptionIndex;

    @Column(columnDefinition = "TEXT")
    private String correctAnswer;  // For short answer questions

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    private QuestionStatus status = QuestionStatus.PENDING;

    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private Instant approvedAt;
}