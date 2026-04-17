package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.DashboardRecommendationDTO;
import com.example.finalprojectb.DTO.LessonWeaknessDTO;
import com.example.finalprojectb.model.*;
import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock private ModuleRepository moduleRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private QuestionResponseRepository questionResponseRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    // --- Shared fixtures ---
    private Course course;
    private Module module;
    private Quiz quiz;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(10L);

        module = new Module();
        module.setId(5L);
        module.setTitle("Module 1");
        module.setCourse(course);

        quiz = new Quiz();
        quiz.setId(100L);
        quiz.setTitle("Quiz 1");
        quiz.setModule(module);

        lesson = new Lesson();
        lesson.setId(20L);
        lesson.setTitle("Intro to Java");
        lesson.setOrderIndex(1);
        lesson.setModule(module);
    }

    // =========================================================
    // Early-exit guards
    // =========================================================

    @Test
    void getRecommendations_returnsEmpty_whenCourseHasNoModules() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L))
                .thenReturn(Collections.emptyList());

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getWeakLessons()).isEmpty();
        assertThat(result.getNextStep()).isNull();
        verifyNoInteractions(quizRepository);
    }

    @Test
    void getRecommendations_returnsEmpty_whenModulesHaveNoQuizzes() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L))
                .thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L))
                .thenReturn(Collections.emptyList());

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getWeakLessons()).isEmpty();
        assertThat(result.getNextStep()).isNull();
        verifyNoInteractions(questionResponseRepository);
    }

    // =========================================================
    // Severity classification
    // =========================================================

    @Test
    void getRecommendations_severity_isSTRONG_REVIEW_whenQuizFailedAndFailCountAtLeast3() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(1L, List.of(100L)))
                .thenReturn(makeFailedResponses(lesson, quiz, 3));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(false);
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getWeakLessons()).hasSize(1);
        assertThat(result.getWeakLessons().get(0).getSeverity()).isEqualTo("STRONG_REVIEW");
    }

    @Test
    void getRecommendations_severity_isREVIEW_whenQuizFailedAndFailCountBelow3() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(1L, List.of(100L)))
                .thenReturn(makeFailedResponses(lesson, quiz, 2));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(false);
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getWeakLessons()).hasSize(1);
        assertThat(result.getWeakLessons().get(0).getSeverity()).isEqualTo("REVIEW");
    }

    @Test
    void getRecommendations_severity_isPRACTICE_whenQuizPassedButFailCountAtLeast3() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(1L, List.of(100L)))
                .thenReturn(makeFailedResponses(lesson, quiz, 3));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(true);
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getWeakLessons()).hasSize(1);
        assertThat(result.getWeakLessons().get(0).getSeverity()).isEqualTo("PRACTICE");
    }

    @Test
    void getRecommendations_skipsLesson_whenQuizPassedAndFailCountBelow3() {
        // Passed quiz + only 2 failures = not a significant gap, should be excluded
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(1L, List.of(100L)))
                .thenReturn(makeFailedResponses(lesson, quiz, 2));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(true);
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getWeakLessons()).isEmpty();
    }

    // =========================================================
    // Sorting and capping
    // =========================================================

    @Test
    void getRecommendations_sortsByFailCountDescending() {
        Lesson lessonA = makeLesson(21L, "Lesson A", 1);
        Lesson lessonB = makeLesson(22L, "Lesson B", 2);

        // lessonA has 2 failures, lessonB has 4 — lessonB should come first
        List<QuestionResponse> responses = makeFailedResponses(lessonA, quiz, 2);
        responses.addAll(makeFailedResponses(lessonB, quiz, 4));

        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(1L, List.of(100L)))
                .thenReturn(responses);
        when(quizAttemptRepository.hasUserPassedQuiz(eq(1L), anyLong())).thenReturn(false);
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        List<LessonWeaknessDTO> weak = result.getWeakLessons();
        assertThat(weak.get(0).getFailCount()).isGreaterThanOrEqualTo(weak.get(1).getFailCount());
        assertThat(weak.get(0).getLessonTitle()).isEqualTo("Lesson B");
    }

    @Test
    void getRecommendations_capsWeakLessonsAtFive() {
        // Create 7 lessons all with 3+ failures and quiz not passed
        List<QuestionResponse> allResponses = new java.util.ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            Lesson l = makeLesson((long) (30 + i), "Lesson " + i, i);
            allResponses.addAll(makeFailedResponses(l, quiz, 3));
        }

        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(1L, List.of(100L)))
                .thenReturn(allResponses);
        when(quizAttemptRepository.hasUserPassedQuiz(eq(1L), anyLong())).thenReturn(false);
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getWeakLessons()).hasSize(5);
    }

    @Test
    void getRecommendations_deduplicatesLessonFailures() {
        // 4 failed responses for the SAME lesson — should count as failCount=4, not 4 entries
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(1L, List.of(100L)))
                .thenReturn(makeFailedResponses(lesson, quiz, 4));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(false);
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getWeakLessons()).hasSize(1);
        assertThat(result.getWeakLessons().get(0).getFailCount()).isEqualTo(4);
    }

    // =========================================================
    // LessonWeaknessDTO field mapping
    // =========================================================

    @Test
    void getRecommendations_populatesLessonWeaknessFields_correctly() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(1L, List.of(100L)))
                .thenReturn(makeFailedResponses(lesson, quiz, 2));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(false);
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        LessonWeaknessDTO dto = result.getWeakLessons().get(0);
        assertThat(dto.getLessonId()).isEqualTo(20L);
        assertThat(dto.getLessonTitle()).isEqualTo("Intro to Java");
        assertThat(dto.getModuleId()).isEqualTo(5L);
        assertThat(dto.getModuleTitle()).isEqualTo("Module 1");
        assertThat(dto.getCourseId()).isEqualTo(10L);
        assertThat(dto.getFailCount()).isEqualTo(2);
        assertThat(dto.isQuizPassed()).isFalse();
    }

    // =========================================================
    // Next step logic
    // =========================================================

    @Test
    void getRecommendations_nextStep_pointsToFirstUnfinishedModule() {
        Module mod1 = makeModule(5L, "Module 1", 1);
        Module mod2 = makeModule(6L, "Module 2", 2);

        Lesson firstLesson = makeLesson(30L, "First Lesson", 1);

        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(mod1, mod2));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(anyLong())).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(anyLong(), anyList()))
                .thenReturn(Collections.emptyList());
        // mod1 all passed, mod2 not
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 6L)).thenReturn(false);
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(6L)).thenReturn(List.of(firstLesson));

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getNextStep()).isNotNull();
        assertThat(result.getNextStep().getModuleId()).isEqualTo(6L);
        assertThat(result.getNextStep().getModuleTitle()).isEqualTo("Module 2");
        assertThat(result.getNextStep().getFirstLessonId()).isEqualTo(30L);
        assertThat(result.getNextStep().getFirstLessonTitle()).isEqualTo("First Lesson");
    }

    @Test
    void getRecommendations_nextStep_isNull_whenAllModulesPassed() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(anyLong(), anyList()))
                .thenReturn(Collections.emptyList());
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getNextStep()).isNull();
    }

    @Test
    void getRecommendations_nextStep_setsNullLessonFields_whenModuleHasNoLessons() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(anyLong(), anyList()))
                .thenReturn(Collections.emptyList());
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(false);
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(Collections.emptyList());

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getNextStep()).isNotNull();
        assertThat(result.getNextStep().getFirstLessonId()).isNull();
        assertThat(result.getNextStep().getFirstLessonTitle()).isNull();
    }

    @Test
    void getRecommendations_nextStep_picksFirstModule_whenNoneArePassed() {
        Module mod1 = makeModule(5L, "Module 1", 1);
        Module mod2 = makeModule(6L, "Module 2", 2);

        Lesson firstLesson = makeLesson(30L, "First Lesson", 1);

        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(mod1, mod2));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(anyLong())).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(anyLong(), anyList()))
                .thenReturn(Collections.emptyList());
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(false);
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(firstLesson));

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        // Should stop at the FIRST unfinished module — mod1
        assertThat(result.getNextStep().getModuleId()).isEqualTo(5L);
    }

    // =========================================================
    // No failed responses
    // =========================================================

    @Test
    void getRecommendations_noWeakLessons_whenNoFailedResponses() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz));
        when(questionResponseRepository.findFailedResponsesWithLessons(1L, List.of(100L)))
                .thenReturn(Collections.emptyList());
        when(quizAttemptRepository.hasUserPassedAllModuleQuizzes(1L, 5L)).thenReturn(true);

        DashboardRecommendationDTO result = recommendationService.getRecommendations(1L, 10L);

        assertThat(result.getWeakLessons()).isEmpty();
    }

    // =========================================================
    // Helper factory methods
    // =========================================================

    private Lesson makeLesson(Long id, String title, int orderIndex) {
        Lesson l = new Lesson();
        l.setId(id);
        l.setTitle(title);
        l.setOrderIndex(orderIndex);
        l.setModule(module);
        return l;
    }

    private Module makeModule(Long id, String title, int orderIndex) {
        Module m = new Module();
        m.setId(id);
        m.setTitle(title);
        m.setOrderIndex(orderIndex);
        m.setCourse(course);
        return m;
    }

    /**
     * Builds {@code count} failed QuestionResponse objects all linked to the given lesson and quiz.
     * Each gets a unique Question so fail-counting works correctly.
     */
    private List<QuestionResponse> makeFailedResponses(Lesson forLesson, Quiz forQuiz, int count) {
        List<QuestionResponse> list = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Question q = new Question();
            q.setId((long) (1000 + forLesson.getId() * 10 + i));
            q.setQuiz(forQuiz);
            q.setLesson(forLesson);

            QuestionResponse qr = new QuestionResponse();
            qr.setId((long) (2000 + forLesson.getId() * 10 + i));
            qr.setQuestion(q);
            qr.setIsCorrect(false);
            list.add(qr);
        }
        return list;
    }
}
