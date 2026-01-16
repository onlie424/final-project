package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.Lesson;
import com.example.finalprojectb.model.Quiz;
import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    public LessonDTO createLesson(CreateLessonDTO dto) {
        Module module = moduleRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found with id: " + dto.getModuleId()));

        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(dto.getTitle());
        lesson.setContentType(dto.getContentType());
        lesson.setContentUrl(dto.getContentUrl());
        lesson.setContentText(dto.getContentText());
        lesson.setDurationMinutes(dto.getDurationMinutes());
        lesson.setOrderIndex(dto.getOrderIndex());

        Lesson saved = lessonRepository.save(lesson);
        return convertToLessonDTO(saved);
    }

    @Transactional
    public LessonDetailDTO getLessonDetail(Long lessonId, Long userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));

        LessonDetailDTO dto = new LessonDetailDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setContentType(lesson.getContentType());
        dto.setContentUrl(lesson.getContentUrl());
        dto.setContentText(lesson.getContentText());
        dto.setDurationMinutes(lesson.getDurationMinutes());

        List<Quiz> quizzes = quizRepository.findByLessonId(lessonId);
        List<QuizDTO> quizDTOs = quizzes.stream()
                .map(this::convertToQuizDTO)
                .collect(Collectors.toList());
        dto.setQuizzes(quizDTOs);

        if (userId != null) {
            boolean isCompleted = lessonProgressRepository
                    .findByUserIdAndLessonId(userId, lessonId)
                    .map(lp -> "COMPLETED".equals(lp.getStatus()))
                    .orElse(false);
            dto.setIsCompleted(isCompleted);
        } else {
            dto.setIsCompleted(false);
        }

        return dto;
    }

    public LessonDTO updateLesson(Long lessonId, CreateLessonDTO dto) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));

        lesson.setTitle(dto.getTitle());
        lesson.setContentType(dto.getContentType());
        lesson.setContentUrl(dto.getContentUrl());
        lesson.setContentText(dto.getContentText());
        lesson.setDurationMinutes(dto.getDurationMinutes());
        lesson.setOrderIndex(dto.getOrderIndex());

        Lesson updated = lessonRepository.save(lesson);
        return convertToLessonDTO(updated);
    }

    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    private LessonDTO convertToLessonDTO(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setContentType(lesson.getContentType());
        dto.setContentUrl(lesson.getContentUrl());
        dto.setDurationMinutes(lesson.getDurationMinutes());
        dto.setOrderIndex(lesson.getOrderIndex());
        dto.setIsCompleted(false);
        return dto;
    }

    private QuizDTO convertToQuizDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setPassingScore(quiz.getPassingScore());
        dto.setTimeLimitMinutes(quiz.getTimeLimitMinutes());
        dto.setQuestionCount(quiz.getQuestions().size());
        return dto;
    }
}