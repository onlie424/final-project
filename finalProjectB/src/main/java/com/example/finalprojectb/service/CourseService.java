package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.*;
import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuestionResponseRepository questionResponseRepository;

    /**
     * Create a new course
     */
    public CourseDTO createCourse(CreateCourseDTO dto) {
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setDifficulty(dto.getDifficulty());
        course.setCategory(dto.getCategory());
        course.setEstimatedHours(dto.getEstimatedHours());
        course.setThumbnailUrl(dto.getThumbnailUrl());
        course.setIsPublished(false); // Default to unpublished
        course.setTotalLessons(0); // Will be updated when lessons are added

        Course saved = courseRepository.save(course);
        return convertToCourseDTO(saved);
    }

    /**
     * Get all published courses
     */
    public List<CourseDTO> getAllPublishedCourses() {
        return courseRepository.findByIsPublishedTrue()
                .stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all courses (including unpublished - for admin)
     */
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get courses by category
     */
    public List<CourseDTO> getCoursesByCategory(String category) {
        return courseRepository.findByCategory(category)
                .stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get courses by difficulty
     */
    public List<CourseDTO> getCoursesByDifficulty(String difficulty) {
        return courseRepository.findByDifficulty(difficulty)
                .stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search courses by keyword in title
     */
    public List<CourseDTO> searchCourses(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToCourseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get course by ID
     */
    public CourseDTO getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
        return convertToCourseDTO(course);
    }

    /**
     * Get course detail with modules and lessons
     */
    @Transactional
    public CourseDetailDTO getCourseDetail(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        CourseDetailDTO detailDTO = new CourseDetailDTO();
        detailDTO.setId(course.getId());
        detailDTO.setTitle(course.getTitle());
        detailDTO.setDescription(course.getDescription());
        detailDTO.setDifficulty(course.getDifficulty());
        detailDTO.setCategory(course.getCategory());
        detailDTO.setEstimatedHours(course.getEstimatedHours());
        detailDTO.setThumbnailUrl(course.getThumbnailUrl());
        detailDTO.setTotalLessons(course.getTotalLessons());

        // Check if user is enrolled (if userId provided)
        if (userId != null) {
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
            detailDTO.setIsEnrolled(isEnrolled);
        } else {
            detailDTO.setIsEnrolled(false);
        }

        // Get all modules with lessons
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<ModuleDTO> moduleDTOs = modules.stream()
                .map(this::convertToModuleDTOWithLessons)
                .collect(Collectors.toList());

        detailDTO.setModules(moduleDTOs);

        return detailDTO;
    }

    /**
     * Update course
     */
    public CourseDTO updateCourse(Long courseId, CreateCourseDTO dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setDifficulty(dto.getDifficulty());
        course.setCategory(dto.getCategory());
        course.setEstimatedHours(dto.getEstimatedHours());
        course.setThumbnailUrl(dto.getThumbnailUrl());

        Course updated = courseRepository.save(course);
        return convertToCourseDTO(updated);
    }

    /**
     * Publish course
     */
    public void publishCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        course.setIsPublished(true);
        courseRepository.save(course);
    }

    /**
     * Unpublish course
     */
    public void unpublishCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        course.setIsPublished(false);
        courseRepository.save(course);
    }

    /**
     * Delete course and all related data (enrollments, progress, modules, lessons, etc.)
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        // 1. Collect all lesson IDs and quiz IDs for this course
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<Long> lessonIds = modules.stream()
                .flatMap(m -> lessonRepository.findByModuleIdOrderByOrderIndexAsc(m.getId()).stream())
                .map(Lesson::getId)
                .collect(Collectors.toList());

        if (!lessonIds.isEmpty()) {
            // 2. Delete quiz attempts and their responses
            List<Long> quizIds = quizRepository.findByLessonIdIn(lessonIds).stream()
                    .map(quiz -> quiz.getId())
                    .collect(Collectors.toList());

            if (!quizIds.isEmpty()) {
                List<Long> attemptIds = quizAttemptRepository.findByQuizIdIn(quizIds).stream()
                        .map(attempt -> attempt.getId())
                        .collect(Collectors.toList());

                if (!attemptIds.isEmpty()) {
                    questionResponseRepository.deleteByAttemptIdIn(attemptIds);
                }
                quizAttemptRepository.deleteByQuizIdIn(quizIds);
            }

            // 3. Delete lesson progress
            lessonProgressRepository.deleteByLessonIdIn(lessonIds);
        }

        // 4. Delete all enrollments for this course
        enrollmentRepository.deleteByCourseId(courseId);

        // 5. Delete the course (cascades to modules -> lessons -> quizzes -> questions -> options)
        courseRepository.delete(course);
    }

    /**
     * Update total lessons count for a course
     */
    @Transactional
    public void updateCourseTotalLessons(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));

        course.calculateTotalLessons();
        courseRepository.save(course);
    }

    /**
     * Get modules for a course
     */
    public List<ModuleDTO> getCourseModules(Long courseId) {
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        return modules.stream()
                .map(this::convertToModuleDTOWithLessons)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Convert Course entity to CourseDTO
     */
    private CourseDTO convertToCourseDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setDifficulty(course.getDifficulty());
        dto.setCategory(course.getCategory());
        dto.setEstimatedHours(course.getEstimatedHours());
        dto.setThumbnailUrl(course.getThumbnailUrl());
        dto.setIsPublished(course.getIsPublished());
        dto.setTotalLessons(course.getTotalLessons());
        dto.setCreatedAt(course.getCreatedAt());
        return dto;
    }

    /**
     * Convert Module entity to ModuleDTO with lessons
     */
    private ModuleDTO convertToModuleDTOWithLessons(Module module) {
        ModuleDTO dto = new ModuleDTO();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setOrderIndex(module.getOrderIndex());

        // Get lessons for this module
        List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());
        List<LessonDTO> lessonDTOs = lessons.stream()
                .map(this::convertToLessonDTO)
                .collect(Collectors.toList());

        dto.setLessons(lessonDTOs);

        return dto;
    }

    /**
     * Convert Lesson entity to LessonDTO
     */
    private LessonDTO convertToLessonDTO(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setContentType(lesson.getContentType());
        dto.setContentUrl(lesson.getContentUrl());
        dto.setContentText(lesson.getContentText());
        dto.setDurationMinutes(lesson.getDurationMinutes());
        dto.setOrderIndex(lesson.getOrderIndex());
        dto.setIsCompleted(false); // Will be set based on user progress if needed
        return dto;
    }
}