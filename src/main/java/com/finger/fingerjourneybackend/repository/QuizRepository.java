package com.finger.fingerjourneybackend.repository;
import com.finger.fingerjourneybackend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
