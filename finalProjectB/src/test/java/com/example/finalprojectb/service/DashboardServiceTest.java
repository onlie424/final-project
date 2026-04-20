package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.*;
import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private QuizRepository quizRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    // --- Shared fixtures ---
    private User user;
    private Course course;
    private Module module;
    private Quiz quiz;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test Student");
        user.setRole(User.Role.USER);

        course = new Course();
        course.setId(10L);
        course.setTitle("Java Fundamentals");
        course.setDifficulty("Beginner");

        module = new Module();
        module.setId(5L);
        module.setCourse(course);

        quiz = new Quiz();
        quiz.setId(100L);
        quiz.setTitle("Java Basics Quiz");
        quiz.setModule(module);

        enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setUser(user);
        enrollment.setStatus("ACTIVE");
        enrollment.setCompletionPercentage(50.0);
        enrollment.setEnrollmentDate(LocalDateTime.now().minusDays(10));
        enrollment.setLastAccessed(LocalDateTime.now().minusHours(1));
    }

    // =========================================================
    // Shared stub helpers
    //
    // WHY two helpers:
    //   - getActivities() calls quizRepository.findAll() ONLY inside a
    //     for-loop over enrollments. When enrollments is empty the loop
    //     body never runs, so stubbing findAll() there is dead code and
    //     Mockito's strict mode flags it as UnnecessaryStubbing.
    //   - Tests that exercise sections with no active enrollment must NOT
    //     stub findAll(); tests that have an active enrollment MUST stub it.
    // =========================================================

    /** Use when no active enrollments — findAll is never called. */
    private void stubNoEnrollments() {
        when(enrollmentRepository.findByUserIdAndStatus(1L, "ACTIVE"))
                .thenReturn(Collections.emptyList());
    }

    /** Full empty-dashboard stub (no enrollments, no attempts, no lesson progress). */
    private void stubEmptyDashboard() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L))
                .thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());
    }

    /** Use when an active enrollment exists — findAll IS reached by getActivities(). */
    private void stubWithEnrollmentNoQuizzes(Enrollment e) {
        when(enrollmentRepository.findByUserIdAndStatus(1L, "ACTIVE")).thenReturn(List.of(e));
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L))
                .thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());
        when(quizRepository.findAll()).thenReturn(Collections.emptyList());
    }

    // =========================================================
    // currentFocus — selection logic
    // =========================================================

    @Test
    void getDashboardData_currentFocus_isNull_whenNoActiveEnrollments() {
        stubEmptyDashboard();

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getCurrentFocus()).isNull();
    }

    @Test
    void getDashboardData_currentFocus_populatesCourseFields() {
        stubWithEnrollmentNoQuizzes(enrollment);

        DashboardDTO result = dashboardService.getDashboardData(1L);

        CurrentFocusDTO focus = result.getCurrentFocus();
        assertThat(focus).isNotNull();
        assertThat(focus.getCourseId()).isEqualTo(10L);
        assertThat(focus.getCourseName()).isEqualTo("Java Fundamentals");
        assertThat(focus.getProgress()).isEqualTo(50);
        assertThat(focus.getDifficulty()).isEqualTo("Beginner");
    }

    @Test
    void getDashboardData_currentFocus_picksEnrollmentWithMostRecentLastAccessed() {
        Course courseA = new Course();
        courseA.setId(11L);
        courseA.setTitle("Course A");
        courseA.setDifficulty("Beginner");

        Course courseB = new Course();
        courseB.setId(12L);
        courseB.setTitle("Course B");
        courseB.setDifficulty("Advanced");

        Enrollment older = new Enrollment();
        older.setCourse(courseA);
        older.setCompletionPercentage(30.0);
        older.setEnrollmentDate(LocalDateTime.now().minusDays(20));
        older.setLastAccessed(LocalDateTime.now().minusDays(5));

        Enrollment recent = new Enrollment();
        recent.setCourse(courseB);
        recent.setCompletionPercentage(60.0);
        recent.setEnrollmentDate(LocalDateTime.now().minusDays(10));
        recent.setLastAccessed(LocalDateTime.now().minusHours(1));

        when(enrollmentRepository.findByUserIdAndStatus(1L, "ACTIVE")).thenReturn(List.of(older, recent));
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED")).thenReturn(Collections.emptyList());
        when(quizRepository.findAll()).thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getCurrentFocus().getCourseName()).isEqualTo("Course B");
    }

    // =========================================================
    // estimatedCompletion
    // =========================================================

    @Test
    void getDashboardData_currentFocus_estimatedCompletion_isJustStarted_whenProgressIsZero() {
        enrollment.setCompletionPercentage(0.0);
        stubWithEnrollmentNoQuizzes(enrollment);

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getCurrentFocus().getEstimatedCompletion()).isEqualTo("Just started");
    }

    @Test
    void getDashboardData_currentFocus_estimatedCompletion_isUnknown_whenEnrollmentDateIsNull() {
        enrollment.setEnrollmentDate(null);
        stubWithEnrollmentNoQuizzes(enrollment);

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getCurrentFocus().getEstimatedCompletion()).isEqualTo("Unknown");
    }

    @Test
    void getDashboardData_currentFocus_estimatedCompletion_showsDays_whenLessThanOneWeekRemaining() {
        // 90% done in 9 days → daysPerPercent = 9/90 = 0.1
        // remainingDays = 10 * 0.1 = 1 day → "1 days"
        enrollment.setCompletionPercentage(90.0);
        enrollment.setEnrollmentDate(LocalDateTime.now().minusDays(9));
        stubWithEnrollmentNoQuizzes(enrollment);

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getCurrentFocus().getEstimatedCompletion()).endsWith("days");
    }

    @Test
    void getDashboardData_currentFocus_estimatedCompletion_showsWeeks_whenBetween8And30Days() {
        // 10% done in 3 days → daysPerPercent = 3/10 = 0.3
        // remainingDays = 90 * 0.3 = 27 days → "3 weeks"
        enrollment.setCompletionPercentage(10.0);
        enrollment.setEnrollmentDate(LocalDateTime.now().minusDays(3));
        stubWithEnrollmentNoQuizzes(enrollment);

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getCurrentFocus().getEstimatedCompletion()).containsAnyOf("week", "weeks");
    }

    @Test
    void getDashboardData_currentFocus_estimatedCompletion_showsMonths_whenMoreThan30Days() {
        // 5% done in 3 days → daysPerPercent = 3/5 = 0.6
        // remainingDays = 95 * 0.6 = 57 days → "1 months"
        enrollment.setCompletionPercentage(5.0);
        enrollment.setEnrollmentDate(LocalDateTime.now().minusDays(3));
        stubWithEnrollmentNoQuizzes(enrollment);

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getCurrentFocus().getEstimatedCompletion()).containsAnyOf("month", "months");
    }

    // =========================================================
    // progressOverview — mastery score (grade)
    // =========================================================

    @Test
    void getDashboardData_progressOverview_masteryScore_isNA_whenNoAttempts() {
        stubEmptyDashboard();

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getProgressOverview().getMasteryScore()).isEqualTo("N/A");
    }

    @Test
    void getDashboardData_progressOverview_masteryScore_isA_whenAvgScoreAbove93() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L))
                .thenReturn(List.of(makeAttempt(95), makeAttempt(97)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getProgressOverview().getMasteryScore()).isEqualTo("A");
    }

    @Test
    void getDashboardData_progressOverview_masteryScore_isF_whenAvgScoreBelow60() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L))
                .thenReturn(List.of(makeAttempt(40), makeAttempt(50)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getProgressOverview().getMasteryScore()).isEqualTo("F");
    }

    @Test
    void getDashboardData_progressOverview_masteryScore_isCMinus_whenAvgScoreIs70() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L))
                .thenReturn(List.of(makeAttempt(70)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getProgressOverview().getMasteryScore()).isEqualTo("C-");
    }

    // =========================================================
    // progressOverview — subject breakdown
    // =========================================================

    @Test
    void getDashboardData_progressOverview_subjectBreakdown_defaultsAllZero_whenNoAttempts() {
        stubEmptyDashboard();

        DashboardDTO result = dashboardService.getDashboardData(1L);

        List<SubjectBreakdownDTO> subjects = result.getProgressOverview().getSubjects();
        assertThat(subjects).hasSize(3);
        SubjectBreakdownDTO mastered = subjects.stream()
                .filter(s -> "Mastered".equals(s.getName())).findFirst().orElseThrow();
        assertThat(mastered.getValue()).isEqualTo(0);
    }

    @Test
    void getDashboardData_progressOverview_subjectBreakdown_calculatesCorrectly() {
        // 2 mastered (>=80), 1 in-progress (50-79), 1 needs work (<50)
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L))
                .thenReturn(List.of(makeAttempt(90), makeAttempt(85), makeAttempt(60), makeAttempt(40)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        List<SubjectBreakdownDTO> subjects = result.getProgressOverview().getSubjects();
        SubjectBreakdownDTO mastered   = subjects.stream().filter(s -> "Mastered".equals(s.getName())).findFirst().orElseThrow();
        SubjectBreakdownDTO inProgress = subjects.stream().filter(s -> "In Progress".equals(s.getName())).findFirst().orElseThrow();
        SubjectBreakdownDTO needsWork  = subjects.stream().filter(s -> "Needs Work".equals(s.getName())).findFirst().orElseThrow();

        assertThat(mastered.getValue()).isEqualTo(50);   // 2/4
        assertThat(inProgress.getValue()).isEqualTo(25); // 1/4
        assertThat(needsWork.getValue()).isEqualTo(25);  // 1/4
    }

    // =========================================================
    // recommendations — mastery gaps
    // =========================================================

    @Test
    void getDashboardData_recommendations_masteryGaps_listsQuizzesScoredBelow70() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L))
                .thenReturn(List.of(makeAttemptWithQuiz(50, "Variables Quiz")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getRecommendations().getMasteryGaps()).contains("Variables Quiz");
    }

    @Test
    void getDashboardData_recommendations_masteryGaps_excludesPassedQuizzes() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L))
                .thenReturn(List.of(makeAttemptWithQuiz(80, "Loops Quiz")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getRecommendations().getMasteryGaps()).isEmpty();
    }

    @Test
    void getDashboardData_recommendations_masteryGaps_cappedAtThree() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(List.of(
                makeAttemptWithQuiz(40, "Quiz 1"),
                makeAttemptWithQuiz(40, "Quiz 2"),
                makeAttemptWithQuiz(40, "Quiz 3"),
                makeAttemptWithQuiz(40, "Quiz 4"),
                makeAttemptWithQuiz(40, "Quiz 5")
        ));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getRecommendations().getMasteryGaps()).hasSize(3);
    }

    // =========================================================
    // recommendations — achievement
    // =========================================================

    @Test
    void getDashboardData_recommendations_achievement_isNull_whenNothingSpecial() {
        stubEmptyDashboard();

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getRecommendations().getAchievement()).isNull();
    }

    @Test
    void getDashboardData_recommendations_achievement_isFirstLesson_whenOneCompleted() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(List.of(new LessonProgress()));

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getRecommendations().getAchievement()).contains("First Lesson");
    }

    @Test
    void getDashboardData_recommendations_achievement_isPerfectScore_whenLatestAttemptScores100() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L))
                .thenReturn(List.of(makeAttemptWithQuiz(100, "Java Quiz")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED"))
                .thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getRecommendations().getAchievement()).contains("Perfect Score");
    }

    @Test
    void getDashboardData_recommendations_achievement_isNull_whenUserNotFound() {
        stubNoEnrollments();
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getRecommendations().getAchievement()).isNull();
    }

    // =========================================================
    // activities
    // =========================================================

    @Test
    void getDashboardData_activities_returnsMotivationalMessage_whenNoEnrollments() {
        stubEmptyDashboard();

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getActivities()).hasSize(1);
        assertThat(result.getActivities().get(0).getType()).isEqualTo("info");
        assertThat(result.getActivities().get(0).getCompleted()).isTrue();
    }

    @Test
    void getDashboardData_activities_showsPendingQuiz_whenNotAttempted() {
        when(enrollmentRepository.findByUserIdAndStatus(1L, "ACTIVE")).thenReturn(List.of(enrollment));
        when(quizRepository.findAll()).thenReturn(List.of(quiz));
        when(quizAttemptRepository.findByUserIdAndQuizIdOrderByAttemptedAtDesc(1L, 100L))
                .thenReturn(Collections.emptyList());
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED")).thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getActivities()).hasSize(1);
        assertThat(result.getActivities().get(0).getTitle()).isEqualTo("Java Basics Quiz");
        assertThat(result.getActivities().get(0).getType()).isEqualTo("assignment");
        assertThat(result.getActivities().get(0).getCompleted()).isFalse();
    }

    @Test
    void getDashboardData_activities_capsAtFive() {
        // 6 quizzes created. The loop breaks after 5 activities so quiz 106 is never reached.
        // Only stub findByUserIdAndQuizId for the first 5 — stubbing 106 would be flagged as unused.
        List<Quiz> quizzes = new java.util.ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Quiz q = new Quiz();
            q.setId((long) (100 + i));
            q.setTitle("Quiz " + i);
            q.setModule(module);
            quizzes.add(q);
        }
        for (int i = 1; i <= 5; i++) {
            when(quizAttemptRepository.findByUserIdAndQuizIdOrderByAttemptedAtDesc(1L, (long) (100 + i)))
                    .thenReturn(Collections.emptyList());
        }

        when(enrollmentRepository.findByUserIdAndStatus(1L, "ACTIVE")).thenReturn(List.of(enrollment));
        when(quizRepository.findAll()).thenReturn(quizzes);
        when(quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonProgressRepository.findByUserIdAndStatus(1L, "COMPLETED")).thenReturn(Collections.emptyList());

        DashboardDTO result = dashboardService.getDashboardData(1L);

        assertThat(result.getActivities()).hasSize(5);
    }

    // =========================================================
    // Helper factory methods
    // =========================================================

    private QuizAttempt makeAttempt(int score) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setScore(score);
        attempt.setIsPassed(score >= 70);
        attempt.setQuiz(quiz);
        return attempt;
    }

    private QuizAttempt makeAttemptWithQuiz(int score, String quizTitle) {
        Quiz q = new Quiz();
        q.setId((long) (quizTitle.hashCode() & 0xFFFFFFFFL));
        q.setTitle(quizTitle);
        q.setModule(module);

        QuizAttempt attempt = new QuizAttempt();
        attempt.setScore(score);
        attempt.setIsPassed(score >= 70);
        attempt.setQuiz(q);
        return attempt;
    }
}
