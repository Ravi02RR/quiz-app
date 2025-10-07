package org.example.quizapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.quizapp.dto.*;
import org.example.quizapp.entity.Question;
import org.example.quizapp.entity.Quiz;
import org.example.quizapp.repository.QuestionRepository;
import org.example.quizapp.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public Quiz createQuiz(QuizRequest request) {
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setCategory(request.getCategory());
        quiz.setDifficulty(request.getDifficulty());
        return quizRepository.save(quiz);
    }

    @Transactional
    public Quiz addQuestionsToQuiz(Long quizId, List<QuestionRequest> questionRequests) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        for (QuestionRequest qr : questionRequests) {
            Question question = new Question();
            question.setQuiz(quiz);
            question.setText(qr.getText());
            try {
                question.setOptions(objectMapper.writeValueAsString(qr.getOptions()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing options", e);
            }
            question.setCorrectAnswerIndex(qr.getCorrectAnswerIndex());
            quiz.getQuestions().add(question);
        }

        return quizRepository.save(quiz);
    }

    public Page<QuizResponse> getQuizzes(String category, Quiz.Difficulty difficulty, Pageable pageable) {
        Page<Quiz> quizzes;
        
        if (category != null && difficulty != null) {
            quizzes = quizRepository.findByCategoryAndDifficulty(category, difficulty, pageable);
        } else if (category != null) {
            quizzes = quizRepository.findByCategory(category, pageable);
        } else if (difficulty != null) {
            quizzes = quizRepository.findByDifficulty(difficulty, pageable);
        } else {
            quizzes = quizRepository.findAll(pageable);
        }

        return quizzes.map(this::convertToQuizResponse);
    }

    public QuizResponse getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return convertToQuizResponseWithQuestions(quiz);
    }

    private QuizResponse convertToQuizResponse(Quiz quiz) {
        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setCategory(quiz.getCategory());
        response.setDifficulty(quiz.getDifficulty());
        response.setCreatedDate(quiz.getCreatedDate());
        response.setQuestions(new ArrayList<>()); // Empty for list view
        return response;
    }

    private QuizResponse convertToQuizResponseWithQuestions(Quiz quiz) {
        QuizResponse response = convertToQuizResponse(quiz);
        response.setQuestions(
                quiz.getQuestions().stream()
                        .map(this::convertToQuestionResponse)
                        .collect(Collectors.toList())
        );
        return response;
    }

    private QuestionResponse convertToQuestionResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setText(question.getText());
        try {
            List<String> options = objectMapper.readValue(question.getOptions(), List.class);
            response.setOptions(options);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing options", e);
        }
        return response;
    }
}
