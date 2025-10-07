package org.example.quizapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.quizapp.dto.AttemptRequest;
import org.example.quizapp.dto.AttemptResponse;
import org.example.quizapp.entity.Attempt;
import org.example.quizapp.entity.Question;
import org.example.quizapp.entity.Quiz;
import org.example.quizapp.entity.User;
import org.example.quizapp.repository.AttemptRepository;
import org.example.quizapp.repository.QuizRepository;
import org.example.quizapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AttemptService {

    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public AttemptResponse submitAttempt(Long quizId, AttemptRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Calculate score
        int correctAnswers = 0;
        int totalQuestions = quiz.getQuestions().size();

        for (Question question : quiz.getQuestions()) {
            Integer userAnswer = request.getAnswers().get(question.getId());
            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswerIndex())) {
                correctAnswers++;
            }
        }

        double score = totalQuestions > 0 ? (correctAnswers * 100.0) / totalQuestions : 0.0;

        // Save attempt
        Attempt attempt = new Attempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setScore(score);
        try {
            attempt.setAnswers(objectMapper.writeValueAsString(request.getAnswers()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing answers", e);
        }

        attempt = attemptRepository.save(attempt);

        // Create response
        AttemptResponse response = new AttemptResponse();
        response.setId(attempt.getId());
        response.setQuizId(quiz.getId());
        response.setQuizTitle(quiz.getTitle());
        response.setScore(score);
        response.setTotalQuestions(totalQuestions);
        response.setCorrectAnswers(correctAnswers);
        response.setSubmittedAt(attempt.getSubmittedAt());
        response.setUserAnswers(request.getAnswers());

        return response;
    }

    public AttemptResponse getAttemptResult(Long attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!attempt.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only view your own attempts");
        }

        // Parse answers
        Map<Long, Integer> userAnswers;
        try {
            userAnswers = objectMapper.readValue(attempt.getAnswers(), new TypeReference<Map<Long, Integer>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing answers", e);
        }

        // Calculate correct answers count
        int correctAnswers = 0;
        int totalQuestions = attempt.getQuiz().getQuestions().size();

        for (Question question : attempt.getQuiz().getQuestions()) {
            Integer userAnswer = userAnswers.get(question.getId());
            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswerIndex())) {
                correctAnswers++;
            }
        }

        // Create response
        AttemptResponse response = new AttemptResponse();
        response.setId(attempt.getId());
        response.setQuizId(attempt.getQuiz().getId());
        response.setQuizTitle(attempt.getQuiz().getTitle());
        response.setScore(attempt.getScore());
        response.setTotalQuestions(totalQuestions);
        response.setCorrectAnswers(correctAnswers);
        response.setSubmittedAt(attempt.getSubmittedAt());
        response.setUserAnswers(userAnswers);

        return response;
    }
}
