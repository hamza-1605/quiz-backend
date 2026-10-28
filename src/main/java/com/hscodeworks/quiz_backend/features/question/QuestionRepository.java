package com.hscodeworks.quiz_backend.features.question;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @EntityGraph(attributePaths = "options")
    Optional<Question> findWithOptionsById(Long id);

    @EntityGraph(attributePaths = "options")
    List<Question> findByCategoryIdOrderById(Long categoryId);

    boolean existsByCategoryId(Long categoryId);

    // get all questions with options
    @EntityGraph(attributePaths = "options")
    List<Question> findAllByOrderById();
}