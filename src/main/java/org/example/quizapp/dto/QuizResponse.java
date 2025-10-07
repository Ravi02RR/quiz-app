package org.example.quizapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.quizapp.entity.Quiz;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {
    private Long id;
    private String title;
    private String category;
    private Quiz.Difficulty difficulty;
    private LocalDateTime createdDate;
    private List<QuestionResponse> questions;
}
