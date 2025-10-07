package org.example.quizapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AttemptService attemptService;

    private User user;
    private Quiz quiz;
    private Question question1;
    private Question question2;
    private AttemptRequest attemptRequest;
    private Attempt attempt;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(User.Role.USER);

        quiz = new Quiz();
        quiz.setId(1L);
        quiz.setTitle("Test Quiz");
        quiz.setCategory("Programming");
        quiz.setDifficulty(Quiz.Difficulty.EASY);
        quiz.setCreatedDate(LocalDateTime.now());

        question1 = new Question();
        question1.setId(1L);
        question1.setQuiz(quiz);
        question1.setText("Question 1");
        question1.setOptions("[\"A\",\"B\",\"C\",\"D\"]");
        question1.setCorrectAnswerIndex(2);

        question2 = new Question();
        question2.setId(2L);
        question2.setQuiz(quiz);
        question2.setText("Question 2");
        question2.setOptions("[\"A\",\"B\",\"C\",\"D\"]");
        question2.setCorrectAnswerIndex(1);

        quiz.setQuestions(Arrays.asList(question1, question2));

        Map<Long, Integer> answers = new HashMap<>();
        answers.put(1L, 2); // Correct
        answers.put(2L, 1); // Correct

        attemptRequest = new AttemptRequest();
        attemptRequest.setAnswers(answers);

        attempt = new Attempt();
        attempt.setId(1L);
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setScore(100.0);
        attempt.setAnswers("{\"1\":2,\"2\":1}");
        attempt.setSubmittedAt(LocalDateTime.now());
    }

    @Test
    void testSubmitAttempt_Success() throws JsonProcessingException {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"1\":2,\"2\":1}");
        when(attemptRepository.save(any(Attempt.class))).thenReturn(attempt);
        doNothing().when(notificationService).sendQuizAttemptNotification(anyString(), anyString(), anyDouble());

        AttemptResponse response = attemptService.submitAttempt(1L, attemptRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getQuizId());
        assertEquals("Test Quiz", response.getQuizTitle());
        assertEquals(100.0, response.getScore());
        assertEquals(2, response.getTotalQuestions());
        assertEquals(2, response.getCorrectAnswers());

        verify(attemptRepository).save(any(Attempt.class));
        verify(notificationService).sendQuizAttemptNotification("testuser", "Test Quiz", 100.0);
    }

    @Test
    void testSubmitAttempt_PartiallyCorrect() throws JsonProcessingException {
        Map<Long, Integer> answers = new HashMap<>();
        answers.put(1L, 2); // Correct
        answers.put(2L, 0); // Wrong

        attemptRequest.setAnswers(answers);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"1\":2,\"2\":0}");

        Attempt partialAttempt = new Attempt();
        partialAttempt.setId(2L);
        partialAttempt.setUser(user);
        partialAttempt.setQuiz(quiz);
        partialAttempt.setScore(50.0);
        partialAttempt.setAnswers("{\"1\":2,\"2\":0}");
        partialAttempt.setSubmittedAt(LocalDateTime.now());

        when(attemptRepository.save(any(Attempt.class))).thenReturn(partialAttempt);
        doNothing().when(notificationService).sendQuizAttemptNotification(anyString(), anyString(), anyDouble());

        AttemptResponse response = attemptService.submitAttempt(1L, attemptRequest);

        assertNotNull(response);
        assertEquals(50.0, response.getScore());
        assertEquals(1, response.getCorrectAnswers());
        verify(notificationService).sendQuizAttemptNotification("testuser", "Test Quiz", 50.0);
    }

    @Test
    void testSubmitAttempt_QuizNotFound() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attemptService.submitAttempt(999L, attemptRequest);
        });

        assertEquals("Quiz not found", exception.getMessage());
    }

    @Test
    void testGetAttemptResult_Success() throws JsonProcessingException {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(attemptRequest.getAnswers());

        AttemptResponse response = attemptService.getAttemptResult(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(100.0, response.getScore());
        verify(attemptRepository).findById(1L);
    }

    @Test
    void testGetAttemptResult_UnauthorizedAccess() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        attempt.setUser(otherUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attemptService.getAttemptResult(1L);
        });

        assertEquals("You can only view your own attempts", exception.getMessage());
    }

    @Test
    void testGetAttemptResult_AttemptNotFound() {
        when(attemptRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attemptService.getAttemptResult(999L);
        });

        assertEquals("Attempt not found", exception.getMessage());
    }
}
