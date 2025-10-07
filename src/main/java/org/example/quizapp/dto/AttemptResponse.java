package org.example.quizapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptResponse {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private Double score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private LocalDateTime submittedAt;
    private Map<Long, Integer> userAnswers; // questionId -> selectedAnswerIndex
}
