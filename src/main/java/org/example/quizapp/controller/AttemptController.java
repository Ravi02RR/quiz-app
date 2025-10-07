package org.example.quizapp.controller;

import jakarta.validation.Valid;
import org.example.quizapp.dto.AttemptRequest;
import org.example.quizapp.dto.AttemptResponse;
import org.example.quizapp.service.AttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AttemptController {

    @Autowired
    private AttemptService attemptService;

    @PostMapping("/quizzes/{quizId}/attempt")
    public ResponseEntity<AttemptResponse> submitAttempt(
            @PathVariable Long quizId,
            @Valid @RequestBody AttemptRequest request) {
        return ResponseEntity.ok(attemptService.submitAttempt(quizId, request));
    }

    @GetMapping("/results/{attemptId}")
    public ResponseEntity<AttemptResponse> getAttemptResult(@PathVariable Long attemptId) {
        return ResponseEntity.ok(attemptService.getAttemptResult(attemptId));
    }
}
