package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // Find all lessons for a module, ordered by index
    List<Lesson> findByModuleIdOrderByOrderIndexAsc(Long moduleId);

    // Find lessons by content type
    List<Lesson> findByContentType(String contentType);

    // Find lesson by module and order index
    Optional<Lesson> findByModuleIdAndOrderIndex(Long moduleId, Integer orderIndex);
}