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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdaptiveQuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private QuestionOptionRepository questionOptionRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private QuestionResponseRepository questionResponseRepository;
    @Mock private UserRepository userRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private AdaptiveQuizService adaptiveQuizService;

    // --- Shared fixtures ---
    private User user;
    private Quiz quiz;
    private Module module;
    private Course course;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test Student");
        user.setEmail("student@test.com");
        user.setPassword("password");
        user.setRole(User.Role.USER);

        course = new Course();
        course.setId(10L);

        module = new Module();
        module.setId(5L);
        module.setCourse(course);

        quiz = new Quiz();
        quiz.setId(100L);
        quiz.setTitle("Java Basics Quiz");
        quiz.setModule(module);
    }

    // =========================================================
    // startAdaptiveQuiz — happy path
    // =========================================================

    @Test
    void startAdaptiveQuiz_newAttempt_startsAtEasy() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(false);
        when(quizAttemptRepository.findLastResumableAttempt(1L, 100L)).thenReturn(Optional.empty());
        when(quizAttemptRepository.countByUserIdAndQuizId(1L, 100L)).thenReturn(0L);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> {
            QuizAttempt a = inv.getArgument(0);
            a.setId(999L);
            return a;
        });
        when(questionRepository.findByQuizIdAndDifficultyLevel(100L, "EASY"))
                .thenReturn(makeQuestions(3, "EASY"));

        AdaptiveQuizStartDTO result = adaptiveQuizService.startAdaptiveQuiz(1L, 100L);

        assertThat(result.getCurrentDifficulty()).isEqualTo("EASY");
        assertThat(result.getResumedFromPreviousAttempt()).isFalse();
        assertThat(result.getAttemptId()).isEqualTo(999L);
        assertThat(result.getQuizTitle()).isEqualTo("Java Basics Quiz");
    }

    @Test
    void startAdaptiveQuiz_resumesAtMedium_whenEasyWasPreviouslyPassed() {
        QuizAttempt previousAttempt = new QuizAttempt();
        previousAttempt.setHighestPassedDifficulty("EASY");

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(false);
        when(quizAttemptRepository.findLastResumableAttempt(1L, 100L)).thenReturn(Optional.of(previousAttempt));
        when(quizAttemptRepository.countByUserIdAndQuizId(1L, 100L)).thenReturn(1L);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> {
            QuizAttempt a = inv.getArgument(0);
            a.setId(1000L);
            return a;
        });
        when(questionRepository.findByQuizIdAndDifficultyLevel(100L, "MEDIUM"))
                .thenReturn(makeQuestions(5, "MEDIUM"));

        AdaptiveQuizStartDTO result = adaptiveQuizService.startAdaptiveQuiz(1L, 100L);

        assertThat(result.getCurrentDifficulty()).isEqualTo("MEDIUM");
        assertThat(result.getResumedFromPreviousAttempt()).isTrue();
    }

    @Test
    void startAdaptiveQuiz_resumesAtHard_whenMediumWasPreviouslyPassed() {
        QuizAttempt previousAttempt = new QuizAttempt();
        previousAttempt.setHighestPassedDifficulty("MEDIUM");

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(false);
        when(quizAttemptRepository.findLastResumableAttempt(1L, 100L)).thenReturn(Optional.of(previousAttempt));
        when(quizAttemptRepository.countByUserIdAndQuizId(1L, 100L)).thenReturn(2L);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> {
            QuizAttempt a = inv.getArgument(0);
            a.setId(1001L);
            return a;
        });
        when(questionRepository.findByQuizIdAndDifficultyLevel(100L, "HARD"))
                .thenReturn(makeQuestions(5, "HARD"));

        AdaptiveQuizStartDTO result = adaptiveQuizService.startAdaptiveQuiz(1L, 100L);

        assertThat(result.getCurrentDifficulty()).isEqualTo("HARD");
        assertThat(result.getResumedFromPreviousAttempt()).isTrue();
    }

    @Test
    void startAdaptiveQuiz_limitsQuestionsToFivePerRound() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(false);
        when(quizAttemptRepository.findLastResumableAttempt(1L, 100L)).thenReturn(Optional.empty());
        when(quizAttemptRepository.countByUserIdAndQuizId(1L, 100L)).thenReturn(0L);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> {
            QuizAttempt a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });
        // Return 10 questions — service should only use 5
        when(questionRepository.findByQuizIdAndDifficultyLevel(100L, "EASY"))
                .thenReturn(makeQuestions(10, "EASY"));

        AdaptiveQuizStartDTO result = adaptiveQuizService.startAdaptiveQuiz(1L, 100L);

        assertThat(result.getQuestions()).hasSize(5);
    }

    // =========================================================
    // startAdaptiveQuiz — error cases
    // =========================================================

    @Test
    void startAdaptiveQuiz_throwsWhenQuizNotFound() {
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adaptiveQuizService.startAdaptiveQuiz(1L, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quiz not found");
    }

    @Test
    void startAdaptiveQuiz_throwsWhenUserNotFound() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adaptiveQuizService.startAdaptiveQuiz(999L, 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void startAdaptiveQuiz_throwsWhenQuizAlreadyPassed() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> adaptiveQuizService.startAdaptiveQuiz(1L, 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("QUIZ_ALREADY_PASSED");
    }

    @Test
    void startAdaptiveQuiz_incrementsAttemptNumber() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 100L)).thenReturn(false);
        when(quizAttemptRepository.findLastResumableAttempt(1L, 100L)).thenReturn(Optional.empty());
        when(quizAttemptRepository.countByUserIdAndQuizId(1L, 100L)).thenReturn(3L);
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> {
            QuizAttempt a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(questionRepository.findByQuizIdAndDifficultyLevel(any(), any()))
                .thenReturn(makeQuestions(2, "EASY"));

        adaptiveQuizService.startAdaptiveQuiz(1L, 100L);

        verify(quizAttemptRepository).save(argThat(a -> a.getAttemptNumber() == 4));
    }

    // =========================================================
    // submitRound — TRUE_FALSE / SHORT_ANSWER grading
    // =========================================================

    @Test
    void submitRound_trueFalse_correctAnswer_countsAsCorrect() {
        QuizAttempt attempt = makeAttempt(1L);

        Question q = makeTrueFalseQuestion(10L, "True");
        QuizAnswerDTO answer = makeTextAnswer(10L, "true");

        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", List.of(answer));

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByQuizIdAndDifficultyLevel(any(), eq("MEDIUM")))
                .thenReturn(makeQuestions(3, "MEDIUM"));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getCorrectCount()).isEqualTo(1);
        assertThat(result.getRoundScore()).isEqualTo(100);
    }

    @Test
    void submitRound_trueFalse_wrongAnswer_countsAsIncorrect() {
        QuizAttempt attempt = makeAttempt(1L);

        Question q = makeTrueFalseQuestion(10L, "True");
        QuizAnswerDTO answer = makeTextAnswer(10L, "false");

        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", List.of(answer));

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(any()))
                .thenReturn(Collections.emptyList());

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getCorrectCount()).isEqualTo(0);
        assertThat(result.getRoundScore()).isEqualTo(0);
    }

    @Test
    void submitRound_trueFalse_isCaseInsensitive() {
        QuizAttempt attempt = makeAttempt(1L);

        Question q = makeTrueFalseQuestion(10L, "TRUE");
        QuizAnswerDTO answer = makeTextAnswer(10L, "true");

        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", List.of(answer));

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByQuizIdAndDifficultyLevel(any(), eq("MEDIUM")))
                .thenReturn(makeQuestions(2, "MEDIUM"));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getCorrectCount()).isEqualTo(1);
    }

    @Test
    void submitRound_shortAnswer_trimsWhitespaceBeforeComparing() {
        QuizAttempt attempt = makeAttempt(1L);

        Question q = makeShortAnswerQuestion(10L, "Java");
        QuizAnswerDTO answer = makeTextAnswer(10L, "  java  ");

        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", List.of(answer));

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByQuizIdAndDifficultyLevel(any(), eq("MEDIUM")))
                .thenReturn(makeQuestions(2, "MEDIUM"));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getCorrectCount()).isEqualTo(1);
    }

    // =========================================================
    // submitRound — MULTIPLE_CHOICE grading
    // =========================================================

    @Test
    void submitRound_multipleChoice_correctOption_countsAsCorrect() {
        QuizAttempt attempt = makeAttempt(1L);

        Question q = makeMCQuestion(10L);
        QuestionOption correctOpt = makeOption(20L, true);

        QuizAnswerDTO answer = new QuizAnswerDTO();
        answer.setQuestionId(10L);
        answer.setSelectedOptionId(20L);

        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", List.of(answer));

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q));
        when(questionOptionRepository.findById(20L)).thenReturn(Optional.of(correctOpt));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByQuizIdAndDifficultyLevel(any(), eq("MEDIUM")))
                .thenReturn(makeQuestions(2, "MEDIUM"));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getCorrectCount()).isEqualTo(1);
        assertThat(result.getRoundScore()).isEqualTo(100);
    }

    @Test
    void submitRound_multipleChoice_wrongOption_countsAsIncorrect() {
        QuizAttempt attempt = makeAttempt(1L);

        Question q = makeMCQuestion(10L);
        QuestionOption wrongOpt = makeOption(21L, false);

        QuizAnswerDTO answer = new QuizAnswerDTO();
        answer.setQuestionId(10L);
        answer.setSelectedOptionId(21L);

        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", List.of(answer));

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q));
        when(questionOptionRepository.findById(21L)).thenReturn(Optional.of(wrongOpt));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(any()))
                .thenReturn(Collections.emptyList());

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getCorrectCount()).isEqualTo(0);
    }

    @Test
    void submitRound_multipleChoice_noOptionSelected_countsAsIncorrect() {
        QuizAttempt attempt = makeAttempt(1L);

        Question q = makeMCQuestion(10L);

        QuizAnswerDTO answer = new QuizAnswerDTO();
        answer.setQuestionId(10L);
        answer.setSelectedOptionId(null); // nothing selected

        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", List.of(answer));

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(any()))
                .thenReturn(Collections.emptyList());

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getCorrectCount()).isEqualTo(0);
    }

    // =========================================================
    // submitRound — round score calculation
    // =========================================================

    @Test
    void submitRound_scoreCalculation_4of5Correct_is80Percent() {
        QuizAttempt attempt = makeAttempt(1L);

        // 4 correct + 1 wrong
        List<QuizAnswerDTO> answers = List.of(
                makeTextAnswer(1L, "True"),
                makeTextAnswer(2L, "True"),
                makeTextAnswer(3L, "True"),
                makeTextAnswer(4L, "True"),
                makeTextAnswer(5L, "False") // this one will be wrong
        );

        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", answers);

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        for (long i = 1; i <= 4; i++) {
            when(questionRepository.findById(i)).thenReturn(Optional.of(makeTrueFalseQuestion(i, "True")));
        }
        when(questionRepository.findById(5L)).thenReturn(Optional.of(makeTrueFalseQuestion(5L, "True")));

        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByQuizIdAndDifficultyLevel(any(), eq("MEDIUM")))
                .thenReturn(makeQuestions(3, "MEDIUM"));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getRoundScore()).isEqualTo(80);
    }

    @Test
    void submitRound_emptyAnswers_scoresZero() {
        QuizAttempt attempt = makeAttempt(1L);
        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", Collections.emptyList());

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(any()))
                .thenReturn(Collections.emptyList());

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getRoundScore()).isEqualTo(0);
        assertThat(result.getCorrectCount()).isEqualTo(0);
    }

    // =========================================================
    // submitRound — pass/fail branching logic
    // =========================================================

    @Test
    void submitRound_passingEasy_escalatesToMedium() {
        QuizAttempt attempt = makeAttempt(1L);
        List<QuizAnswerDTO> answers = List.of(makeTextAnswer(10L, "True"));
        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", answers);

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(makeTrueFalseQuestion(10L, "True")));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByQuizIdAndDifficultyLevel(any(), eq("MEDIUM")))
                .thenReturn(makeQuestions(3, "MEDIUM"));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getEscalated()).isTrue();
        assertThat(result.getQuizCompleted()).isFalse();
        assertThat(result.getNextDifficulty()).isEqualTo("MEDIUM");
        assertThat(result.getNextQuestions()).isNotEmpty();
    }

    @Test
    void submitRound_passingMedium_escalatesToHard() {
        QuizAttempt attempt = makeAttempt(1L);
        List<QuizAnswerDTO> answers = List.of(makeTextAnswer(10L, "True"));
        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "MEDIUM", answers);

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(makeTrueFalseQuestion(10L, "True")));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByQuizIdAndDifficultyLevel(any(), eq("HARD")))
                .thenReturn(makeQuestions(3, "HARD"));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getEscalated()).isTrue();
        assertThat(result.getNextDifficulty()).isEqualTo("HARD");
    }

    @Test
    void submitRound_passingHard_completesAndPassesQuiz() {
        QuizAttempt attempt = makeAttempt(1L);
        List<QuizAnswerDTO> answers = List.of(makeTextAnswer(10L, "True"));
        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "HARD", answers);
        dto.setTimeTakenSeconds(120);

        // For updateEnrollmentCompletion
        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(makeTrueFalseQuestion(10L, "True")));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(any())).thenReturn(Collections.emptyList());

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getQuizCompleted()).isTrue();
        assertThat(result.getQuizPassed()).isTrue();
        assertThat(result.getNextDifficulty()).isNull();
        assertThat(result.getNextQuestions()).isNull();
    }

    @Test
    void submitRound_failingEasy_completesQuizAsFailed() {
        QuizAttempt attempt = makeAttempt(1L);
        List<QuizAnswerDTO> answers = List.of(makeTextAnswer(10L, "False")); // wrong
        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", answers);

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(makeTrueFalseQuestion(10L, "True")));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(any()))
                .thenReturn(Collections.emptyList());

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getQuizCompleted()).isTrue();
        assertThat(result.getQuizPassed()).isFalse();
        assertThat(result.getEscalated()).isFalse();
    }

    @Test
    void submitRound_failedRound_returnsLessonsLinkedToFailedQuestions() {
        QuizAttempt attempt = makeAttempt(1L);

        Lesson lesson = new Lesson();
        lesson.setId(50L);
        lesson.setTitle("Intro to Java");
        lesson.setOrderIndex(1);

        Question q = makeTrueFalseQuestion(10L, "True");
        q.setLesson(lesson);

        QuizAnswerDTO answer = makeTextAnswer(10L, "False"); // wrong
        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", List.of(answer));

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getLessonsToRevisit()).hasSize(1);
        assertThat(result.getLessonsToRevisit().get(0).getTitle()).isEqualTo("Intro to Java");
    }

    @Test
    void submitRound_failedRound_fallsBackToModuleLessons_whenNoLessonLinked() {
        QuizAttempt attempt = makeAttempt(1L);

        Question q = makeTrueFalseQuestion(10L, "True");
        q.setLesson(null); // no lesson linked

        Lesson fallbackLesson = new Lesson();
        fallbackLesson.setId(60L);
        fallbackLesson.setTitle("Module Lesson 1");
        fallbackLesson.setOrderIndex(1);

        QuizAnswerDTO answer = makeTextAnswer(10L, "False"); // wrong
        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", List.of(answer));

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId()))
                .thenReturn(List.of(fallbackLesson));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        assertThat(result.getLessonsToRevisit()).hasSize(1);
        assertThat(result.getLessonsToRevisit().get(0).getTitle()).isEqualTo("Module Lesson 1");
    }

    @Test
    void submitRound_failedRound_deduplicatesLessonsFromMultipleFailedQuestions() {
        QuizAttempt attempt = makeAttempt(1L);

        Lesson sharedLesson = new Lesson();
        sharedLesson.setId(50L);
        sharedLesson.setTitle("Shared Lesson");
        sharedLesson.setOrderIndex(1);

        Question q1 = makeTrueFalseQuestion(10L, "True");
        q1.setLesson(sharedLesson);
        Question q2 = makeTrueFalseQuestion(11L, "True");
        q2.setLesson(sharedLesson); // same lesson

        List<QuizAnswerDTO> answers = List.of(
                makeTextAnswer(10L, "False"),
                makeTextAnswer(11L, "False")
        );
        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "EASY", answers);

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(q1));
        when(questionRepository.findById(11L)).thenReturn(Optional.of(q2));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AdaptiveRoundResultDTO result = adaptiveQuizService.submitRound(dto);

        // Two questions failed but both linked to same lesson — should only appear once
        assertThat(result.getLessonsToRevisit()).hasSize(1);
    }

    @Test
    void submitRound_passedHard_marksAttemptAsCompleted() {
        QuizAttempt attempt = makeAttempt(1L);
        SubmitAdaptiveRoundDTO dto = makeRoundDTO(1L, "HARD", List.of(makeTextAnswer(10L, "True")));
        dto.setTimeTakenSeconds(90);

        when(quizAttemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(makeTrueFalseQuestion(10L, "True")));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(any())).thenReturn(Collections.emptyList());

        adaptiveQuizService.submitRound(dto);

        verify(quizAttemptRepository, atLeastOnce()).save(argThat(a ->
                "COMPLETED".equals(a.getStatus()) && Boolean.TRUE.equals(a.getIsPassed())
        ));
    }

    @Test
    void submitRound_throwsWhenAttemptNotFound() {
        when(quizAttemptRepository.findById(999L)).thenReturn(Optional.empty());

        SubmitAdaptiveRoundDTO dto = makeRoundDTO(999L, "EASY", Collections.emptyList());

        assertThatThrownBy(() -> adaptiveQuizService.submitRound(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quiz attempt not found");
    }

    // =========================================================
    // updateEnrollmentCompletion
    // =========================================================

    @Test
    void updateEnrollmentCompletion_setsCorrectPercentage() {
        Module mod = new Module();
        mod.setId(5L);

        Quiz q1 = new Quiz(); q1.setId(1L);
        Quiz q2 = new Quiz(); q2.setId(2L);

        Enrollment enrollment = new Enrollment();
        enrollment.setId(99L);
        enrollment.setCompletionPercentage(0.0);

        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(mod));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(q1, q2));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 1L)).thenReturn(true);
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 2L)).thenReturn(false);
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 10L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adaptiveQuizService.updateEnrollmentCompletion(1L, 10L);

        assertThat(enrollment.getCompletionPercentage()).isEqualTo(50.0);
        assertThat(enrollment.getStatus()).isNotEqualTo("COMPLETED");
    }

    @Test
    void updateEnrollmentCompletion_setsStatusToCompleted_whenAllQuizzesPassed() {
        Module mod = new Module();
        mod.setId(5L);

        Quiz q1 = new Quiz(); q1.setId(1L);

        Enrollment enrollment = new Enrollment();
        enrollment.setId(99L);

        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(List.of(mod));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(q1));
        when(quizAttemptRepository.hasUserPassedQuiz(1L, 1L)).thenReturn(true);
        when(enrollmentRepository.findByUserIdAndCourseId(1L, 10L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        adaptiveQuizService.updateEnrollmentCompletion(1L, 10L);

        assertThat(enrollment.getStatus()).isEqualTo("COMPLETED");
        assertThat(enrollment.getCompletionPercentage()).isEqualTo(100.0);
    }

    @Test
    void updateEnrollmentCompletion_doesNothing_whenCourseHasNoQuizzes() {
        when(moduleRepository.findByCourseIdOrderByOrderIndexAsc(10L)).thenReturn(Collections.emptyList());

        adaptiveQuizService.updateEnrollmentCompletion(1L, 10L);

        verifyNoInteractions(enrollmentRepository);
    }

    // =========================================================
    // Helper factory methods
    // =========================================================

    private QuizAttempt makeAttempt(Long id) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(id);
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setStatus("IN_PROGRESS");
        return attempt;
    }

    private Question makeTrueFalseQuestion(Long id, String correctAnswer) {
        Question q = new Question();
        q.setId(id);
        q.setQuestionText("Is this true?");
        q.setQuestionType("TRUE_FALSE");
        q.setCorrectAnswer(correctAnswer);
        q.setPoints(1);
        q.setDifficultyLevel("EASY");
        q.setQuiz(quiz);
        return q;
    }

    private Question makeShortAnswerQuestion(Long id, String correctAnswer) {
        Question q = new Question();
        q.setId(id);
        q.setQuestionText("What is the answer?");
        q.setQuestionType("SHORT_ANSWER");
        q.setCorrectAnswer(correctAnswer);
        q.setPoints(1);
        q.setDifficultyLevel("EASY");
        q.setQuiz(quiz);
        return q;
    }

    private Question makeMCQuestion(Long id) {
        Question q = new Question();
        q.setId(id);
        q.setQuestionText("Pick the right answer");
        q.setQuestionType("MULTIPLE_CHOICE");
        q.setPoints(1);
        q.setDifficultyLevel("EASY");
        q.setQuiz(quiz);
        return q;
    }

    private QuestionOption makeOption(Long id, boolean isCorrect) {
        QuestionOption opt = new QuestionOption();
        opt.setId(id);
        opt.setOptionText("Option text");
        opt.setIsCorrect(isCorrect);
        opt.setOrderIndex(1);
        return opt;
    }

    private QuizAnswerDTO makeTextAnswer(Long questionId, String userAnswer) {
        QuizAnswerDTO dto = new QuizAnswerDTO();
        dto.setQuestionId(questionId);
        dto.setUserAnswer(userAnswer);
        return dto;
    }

    private SubmitAdaptiveRoundDTO makeRoundDTO(Long attemptId, String difficulty, List<QuizAnswerDTO> answers) {
        SubmitAdaptiveRoundDTO dto = new SubmitAdaptiveRoundDTO();
        dto.setAttemptId(attemptId);
        dto.setDifficulty(difficulty);
        dto.setAnswers(answers);
        return dto;
    }

    private List<Question> makeQuestions(int count, String difficulty) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    Question q = new Question();
                    q.setId((long) (i + 1));
                    q.setQuestionText("Question " + i);
                    q.setQuestionType("TRUE_FALSE");
                    q.setCorrectAnswer("True");
                    q.setPoints(1);
                    q.setDifficultyLevel(difficulty);
                    q.setQuiz(quiz);
                    return q;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
