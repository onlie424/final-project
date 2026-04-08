package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.DashboardRecommendationDTO;
import com.example.finalprojectb.DTO.LessonWeaknessDTO;
import com.example.finalprojectb.DTO.NextStepDTO;
import com.example.finalprojectb.model.Lesson;
import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.model.Quiz;
import com.example.finalprojectb.model.QuestionResponse;
import com.example.finalprojectb.repo.LessonRepository;
import com.example.finalprojectb.repo.ModuleRepository;
import com.example.finalprojectb.repo.QuestionResponseRepository;
import com.example.finalprojectb.repo.QuizAttemptRepository;
import com.example.finalprojectb.repo.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private QuestionResponseRepository questionResponseRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Transactional(readOnly = true)
    public DashboardRecommendationDTO getRecommendations(Long userId, Long courseId) {
        DashboardRecommendationDTO result = new DashboardRecommendationDTO();
        result.setWeakLessons(Collections.emptyList());
        result.setNextStep(null);

        // 1. Get all modules ordered by index
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        if (modules.isEmpty()) return result;

        // 2. Collect all quiz IDs across the course
        List<Long> allQuizIds = modules.stream()
                .flatMap(m -> quizRepository.findByModuleIdOrderByOrderIndexAsc(m.getId()).stream())
                .map(Quiz::getId)
                .collect(Collectors.toList());

        if (allQuizIds.isEmpty()) return result;

        // 3. Get failed responses that have a lesson link
        List<QuestionResponse> failedResponses =
                questionResponseRepository.findFailedResponsesWithLessons(userId, allQuizIds);

        // 4. Group by lesson, count failures; also record the quiz ID per lesson
        Map<Lesson, Long> failCountByLesson = new LinkedHashMap<>();
        Map<Long, Long> quizIdByLessonId = new HashMap<>();

        for (QuestionResponse qr : failedResponses) {
            Lesson lesson = qr.getQuestion().getLesson();
            failCountByLesson.merge(lesson, 1L, Long::sum);
            quizIdByLessonId.putIfAbsent(lesson.getId(), qr.getQuestion().getQuiz().getId());
        }

        // 5. Build LessonWeaknessDTO list
        List<LessonWeaknessDTO> weakLessons = new ArrayList<>();
        for (Map.Entry<Lesson, Long> entry : failCountByLesson.entrySet()) {
            Lesson lesson = entry.getKey();
            int failCount = entry.getValue().intValue();
            Long quizId = quizIdByLessonId.get(lesson.getId());
            boolean passed = quizAttemptRepository.hasUserPassedQuiz(userId, quizId);

            String severity;
            if (!passed && failCount >= 3) {
                severity = "STRONG_REVIEW";
            } else if (!passed) {
                severity = "REVIEW";
            } else if (failCount >= 3) {
                severity = "PRACTICE";
            } else {
                continue; // passed quiz with ≤ 2 wrong — not a significant gap
            }

            Module module = lesson.getModule();

            LessonWeaknessDTO dto = new LessonWeaknessDTO();
            dto.setLessonId(lesson.getId());
            dto.setLessonTitle(lesson.getTitle());
            dto.setModuleId(module.getId());
            dto.setModuleTitle(module.getTitle());
            dto.setCourseId(courseId);
            dto.setFailCount(failCount);
            dto.setQuizPassed(passed);
            dto.setSeverity(severity);
            weakLessons.add(dto);
        }

        weakLessons.sort(Comparator.comparingInt(LessonWeaknessDTO::getFailCount).reversed());
        if (weakLessons.size() > 5) weakLessons = weakLessons.subList(0, 5);
        result.setWeakLessons(weakLessons);

        // 6. Next step: first module where user hasn't passed all quizzes
        for (Module module : modules) {
            boolean allPassed = quizAttemptRepository.hasUserPassedAllModuleQuizzes(userId, module.getId());
            if (!allPassed) {
                NextStepDTO nextStep = new NextStepDTO();
                nextStep.setModuleId(module.getId());
                nextStep.setModuleTitle(module.getTitle());
                nextStep.setCourseId(courseId);

                List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());
                if (!lessons.isEmpty()) {
                    nextStep.setFirstLessonId(lessons.get(0).getId());
                    nextStep.setFirstLessonTitle(lessons.get(0).getTitle());
                }

                result.setNextStep(nextStep);
                break;
            }
        }

        return result;
    }
}
