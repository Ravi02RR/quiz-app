package org.example.quizapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {
    
    @NotBlank(message = "Question text is required")
    private String text;
    
    @NotEmpty(message = "Options are required")
    private List<String> options;
    
    @NotNull(message = "Correct answer index is required")
    private Integer correctAnswerIndex;
}
