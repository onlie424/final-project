package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.Module;  // ← ADD THIS IMPORT!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    // Find all modules for a course, ordered by index
    List<Module> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    // Find module by course and order index
    Optional<Module> findByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);
}