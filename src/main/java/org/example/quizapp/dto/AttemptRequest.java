package org.example.quizapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptRequest {
    
    @NotNull(message = "Answers are required")
    private Map<Long, Integer> answers; // questionId -> selectedAnswerIndex
}
