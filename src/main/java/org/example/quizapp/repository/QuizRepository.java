package org.example.quizapp.repository;

import org.example.quizapp.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByCategoryAndDifficulty(String category, Quiz.Difficulty difficulty, Pageable pageable);
    Page<Quiz> findByCategory(String category, Pageable pageable);
    Page<Quiz> findByDifficulty(Quiz.Difficulty difficulty, Pageable pageable);
}
