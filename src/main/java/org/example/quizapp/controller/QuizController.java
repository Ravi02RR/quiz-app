package org.example.quizapp.controller;

import jakarta.validation.Valid;
import org.example.quizapp.dto.QuestionRequest;
import org.example.quizapp.dto.QuizRequest;
import org.example.quizapp.dto.QuizResponse;
import org.example.quizapp.entity.Quiz;
import org.example.quizapp.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Quiz> createQuiz(@Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.createQuiz(request));
    }

    @PostMapping("/{quizId}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Quiz> addQuestionsToQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody List<QuestionRequest> questionRequests) {
        return ResponseEntity.ok(quizService.addQuestionsToQuiz(quizId, questionRequests));
    }

    @GetMapping
    public ResponseEntity<Page<QuizResponse>> getQuizzes(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Quiz.Difficulty difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(quizService.getQuizzes(category, difficulty, pageable));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizById(quizId));
    }
}
