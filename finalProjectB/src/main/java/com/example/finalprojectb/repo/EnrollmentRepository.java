package com.example.finalprojectb.repo;

import com.example.finalprojectb.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Find all enrollments for a user
    List<Enrollment> findByUserId(Long userId);

    // Find active enrollments for a user
    List<Enrollment> findByUserIdAndStatus(Long userId, String status);

    // Find enrollment for a user in a specific course
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    // Check if user is enrolled in a course
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // Find all students enrolled in a course
    List<Enrollment> findByCourseId(Long courseId);

    // Delete all enrollments for a course
    void deleteByCourseId(Long courseId);
}
