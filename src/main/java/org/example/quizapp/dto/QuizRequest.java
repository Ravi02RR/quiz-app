package org.example.quizapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.quizapp.entity.Quiz;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    @NotNull(message = "Difficulty is required")
    private Quiz.Difficulty difficulty;
}
