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
class QuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private QuestionOptionRepository questionOptionRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private QuestionResponseRepository questionResponseRepository;
    @Mock private UserRepository userRepository;
    @Mock private LessonRepository lessonRepository;

    @InjectMocks
    private QuizService quizService;

    // --- Shared fixtures ---
    private User user;
    private Course course;
    private Module module;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test Student");
        user.setRole(User.Role.USER);

        course = new Course();
        course.setId(10L);

        module = new Module();
        module.setId(5L);
        module.setTitle("Module 1");
        module.setCourse(course);

        quiz = new Quiz();
        quiz.setId(100L);
        quiz.setTitle("Java Basics Quiz");
        quiz.setDescription("A quiz on Java basics");
        quiz.setPassingScore(70);
        quiz.setModule(module);
        quiz.setOrderIndex(1);
    }

    // =========================================================
    // createQuiz
    // =========================================================

    @Test
    void createQuiz_savesQuizWithCorrectFields() {
        CreateQuizDTO dto = new CreateQuizDTO();
        dto.setModuleId(5L);
        dto.setTitle("Java Basics Quiz");
        dto.setDescription("A quiz on Java basics");
        dto.setPassingScore(70);

        when(moduleRepository.findById(5L)).thenReturn(Optional.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(Collections.emptyList());
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> {
            Quiz q = inv.getArgument(0);
            q.setId(100L);
            return q;
        });

        QuizDTO result = quizService.createQuiz(dto);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("Java Basics Quiz");
        assertThat(result.getPassingScore()).isEqualTo(70);
    }

    @Test
    void createQuiz_setsOrderIndexBasedOnExistingQuizCount() {
        CreateQuizDTO dto = new CreateQuizDTO();
        dto.setModuleId(5L);
        dto.setTitle("Quiz 2");

        Quiz existing = new Quiz();
        existing.setId(99L);

        when(moduleRepository.findById(5L)).thenReturn(Optional.of(module));
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(existing));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));

        quizService.createQuiz(dto);

        // One existing quiz → new quiz gets orderIndex 2
        verify(quizRepository).save(argThat(q -> q.getOrderIndex() == 2));
    }

    @Test
    void createQuiz_throwsWhenModuleNotFound() {
        CreateQuizDTO dto = new CreateQuizDTO();
        dto.setModuleId(999L);

        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.createQuiz(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Module not found");
    }

    // =========================================================
    // addQuestion
    // =========================================================

    @Test
    void addQuestion_savesBasicTrueFalseQuestion() {
        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setQuestionText("Is Java object-oriented?");
        dto.setQuestionType("TRUE_FALSE");
        dto.setCorrectAnswer("True");
        dto.setPoints(2);
        dto.setDifficultyLevel("EASY");

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(questionRepository.countByQuizId(100L)).thenReturn(0L);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(50L);
            return q;
        });

        QuestionDTO result = quizService.addQuestion(100L, dto);

        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getQuestionText()).isEqualTo("Is Java object-oriented?");
        assertThat(result.getQuestionType()).isEqualTo("TRUE_FALSE");
        assertThat(result.getPoints()).isEqualTo(2);
    }

    @Test
    void addQuestion_defaultsPointsTo1_whenNotProvided() {
        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setQuestionType("TRUE_FALSE");
        dto.setPoints(null);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(questionRepository.countByQuizId(100L)).thenReturn(0L);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        quizService.addQuestion(100L, dto);

        verify(questionRepository).save(argThat(q -> q.getPoints() == 1));
    }

    @Test
    void addQuestion_defaultsDifficultyToEasy_whenNotProvided() {
        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setQuestionType("TRUE_FALSE");
        dto.setDifficultyLevel(null);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(questionRepository.countByQuizId(100L)).thenReturn(0L);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        quizService.addQuestion(100L, dto);

        verify(questionRepository).save(argThat(q -> "EASY".equals(q.getDifficultyLevel())));
    }

    @Test
    void addQuestion_autoSetsOrderIndex_whenNotProvided() {
        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setQuestionType("TRUE_FALSE");
        dto.setOrderIndex(null);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(questionRepository.countByQuizId(100L)).thenReturn(4L);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        quizService.addQuestion(100L, dto);

        // 4 existing → new question gets orderIndex 5
        verify(questionRepository).save(argThat(q -> q.getOrderIndex() == 5));
    }

    @Test
    void addQuestion_usesProvidedOrderIndex_whenGiven() {
        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setQuestionType("TRUE_FALSE");
        dto.setOrderIndex(3);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        quizService.addQuestion(100L, dto);

        verify(questionRepository).save(argThat(q -> q.getOrderIndex() == 3));
    }

    @Test
    void addQuestion_linksLesson_whenLessonIdProvided() {
        Lesson lesson = new Lesson();
        lesson.setId(20L);
        lesson.setModule(module); // same module as quiz

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setQuestionType("TRUE_FALSE");
        dto.setLessonId(20L);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(questionRepository.countByQuizId(100L)).thenReturn(0L);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        quizService.addQuestion(100L, dto);

        verify(questionRepository).save(argThat(q -> q.getLesson() != null && q.getLesson().getId() == 20L));
    }

    @Test
    void addQuestion_throwsWhenLessonBelongsToDifferentModule() {
        Module otherModule = new Module();
        otherModule.setId(99L);

        Lesson lessonInOtherModule = new Lesson();
        lessonInOtherModule.setId(20L);
        lessonInOtherModule.setModule(otherModule); // different module

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setQuestionType("TRUE_FALSE");
        dto.setLessonId(20L);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lessonInOtherModule));

        assertThatThrownBy(() -> quizService.addQuestion(100L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Lesson does not belong to the same module");
    }

    @Test
    void addQuestion_savesOptions_forMultipleChoiceQuestion() {
        CreateQuestionOptionDTO opt1 = new CreateQuestionOptionDTO();
        opt1.setOptionText("Option A");
        opt1.setIsCorrect(true);
        opt1.setOrderIndex(1);

        CreateQuestionOptionDTO opt2 = new CreateQuestionOptionDTO();
        opt2.setOptionText("Option B");
        opt2.setIsCorrect(false);
        opt2.setOrderIndex(2);

        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setQuestionType("MULTIPLE_CHOICE");
        dto.setOptions(List.of(opt1, opt2));

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(questionRepository.countByQuizId(100L)).thenReturn(0L);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(50L);
            return q;
        });
        when(questionOptionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        quizService.addQuestion(100L, dto);

        verify(questionOptionRepository, times(2)).save(any(QuestionOption.class));
    }

    @Test
    void addQuestion_doesNotSaveOptions_forTrueFalseQuestion() {
        CreateQuestionDTO dto = new CreateQuestionDTO();
        dto.setQuestionType("TRUE_FALSE");

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(questionRepository.countByQuizId(100L)).thenReturn(0L);
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        quizService.addQuestion(100L, dto);

        verifyNoInteractions(questionOptionRepository);
    }

    @Test
    void addQuestion_throwsWhenQuizNotFound() {
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.addQuestion(999L, new CreateQuestionDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quiz not found");
    }

    // =========================================================
    // updateQuestionLesson
    // =========================================================

    @Test
    void updateQuestionLesson_linksNewLesson() {
        Lesson lesson = new Lesson();
        lesson.setId(20L);
        lesson.setModule(module);

        Question question = makeQuestion(50L, "TRUE_FALSE");

        when(questionRepository.findById(50L)).thenReturn(Optional.of(question));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        quizService.updateQuestionLesson(50L, 20L);

        assertThat(question.getLesson()).isEqualTo(lesson);
    }

    @Test
    void updateQuestionLesson_clearsLesson_whenLessonIdIsNull() {
        Lesson lesson = new Lesson();
        lesson.setId(20L);

        Question question = makeQuestion(50L, "TRUE_FALSE");
        question.setLesson(lesson);

        when(questionRepository.findById(50L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        quizService.updateQuestionLesson(50L, null);

        assertThat(question.getLesson()).isNull();
    }

    @Test
    void updateQuestionLesson_throwsWhenLessonBelongsToDifferentModule() {
        Module otherModule = new Module();
        otherModule.setId(99L);

        Lesson lesson = new Lesson();
        lesson.setId(20L);
        lesson.setModule(otherModule);

        Question question = makeQuestion(50L, "TRUE_FALSE");

        when(questionRepository.findById(50L)).thenReturn(Optional.of(question));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> quizService.updateQuestionLesson(50L, 20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Lesson does not belong to the same module");
    }

    // =========================================================
    // deleteQuestion
    // =========================================================

    @Test
    void deleteQuestion_deletesQuestion() {
        Question question = makeQuestion(50L, "TRUE_FALSE");

        when(questionRepository.findById(50L)).thenReturn(Optional.of(question));

        quizService.deleteQuestion(50L);

        verify(questionRepository).delete(question);
    }

    @Test
    void deleteQuestion_deletesOptions_forMultipleChoiceQuestion() {
        Question question = makeQuestion(50L, "MULTIPLE_CHOICE");

        when(questionRepository.findById(50L)).thenReturn(Optional.of(question));

        quizService.deleteQuestion(50L);

        verify(questionOptionRepository).deleteByQuestionId(50L);
        verify(questionRepository).delete(question);
    }

    @Test
    void deleteQuestion_doesNotDeleteOptions_forNonMultipleChoiceQuestion() {
        Question question = makeQuestion(50L, "TRUE_FALSE");

        when(questionRepository.findById(50L)).thenReturn(Optional.of(question));

        quizService.deleteQuestion(50L);

        verifyNoInteractions(questionOptionRepository);
    }

    @Test
    void deleteQuestion_throwsWhenQuestionNotFound() {
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.deleteQuestion(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Question not found");
    }

    // =========================================================
    // startQuiz
    // =========================================================

    @Test
    void startQuiz_createsAttemptWithCorrectFields() {
        StartQuizDTO dto = new StartQuizDTO();
        dto.setUserId(1L);
        dto.setQuizId(100L);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.countByUserIdAndQuizId(1L, 100L)).thenReturn(0L);
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> {
            QuizAttempt a = inv.getArgument(0);
            a.setId(200L);
            return a;
        });
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(100L))
                .thenReturn(Collections.emptyList());

        QuizAttemptDTO result = quizService.startQuiz(dto);

        assertThat(result.getAttemptId()).isEqualTo(200L);
        assertThat(result.getQuizId()).isEqualTo(100L);
        assertThat(result.getQuizTitle()).isEqualTo("Java Basics Quiz");
        assertThat(result.getAttemptNumber()).isEqualTo(1);
    }

    @Test
    void startQuiz_incrementsAttemptNumber_onRetake() {
        StartQuizDTO dto = new StartQuizDTO();
        dto.setUserId(1L);
        dto.setQuizId(100L);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.countByUserIdAndQuizId(1L, 100L)).thenReturn(2L);
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(100L))
                .thenReturn(Collections.emptyList());

        QuizAttemptDTO result = quizService.startQuiz(dto);

        assertThat(result.getAttemptNumber()).isEqualTo(3);
    }

    @Test
    void startQuiz_includesQuestions_inResponse() {
        StartQuizDTO dto = new StartQuizDTO();
        dto.setUserId(1L);
        dto.setQuizId(100L);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(quizAttemptRepository.countByUserIdAndQuizId(1L, 100L)).thenReturn(0L);
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.findByQuizIdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of(makeQuestion(50L, "TRUE_FALSE"), makeQuestion(51L, "TRUE_FALSE")));

        QuizAttemptDTO result = quizService.startQuiz(dto);

        assertThat(result.getQuestions()).hasSize(2);
    }

    @Test
    void startQuiz_throwsWhenQuizNotFound() {
        StartQuizDTO dto = new StartQuizDTO();
        dto.setQuizId(999L);
        dto.setUserId(1L);

        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.startQuiz(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quiz not found");
    }

    @Test
    void startQuiz_throwsWhenUserNotFound() {
        StartQuizDTO dto = new StartQuizDTO();
        dto.setQuizId(100L);
        dto.setUserId(999L);

        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.startQuiz(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    // =========================================================
    // submitQuiz — score calculation
    // =========================================================

    @Test
    void submitQuiz_calculatesScore_allCorrect() {
        QuizAttempt attempt = makeAttempt(200L);

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(200L);
        dto.setAnswers(List.of(
                makeTextAnswer(50L, "True"),
                makeTextAnswer(51L, "True")
        ));

        when(quizAttemptRepository.findById(200L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(50L)).thenReturn(Optional.of(makeTrueFalseQuestion(50L, "True")));
        when(questionRepository.findById(51L)).thenReturn(Optional.of(makeTrueFalseQuestion(51L, "True")));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizResultDTO result = quizService.submitQuiz(dto);

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getCorrectAnswers()).isEqualTo(2);
        assertThat(result.getTotalQuestions()).isEqualTo(2);
    }

    @Test
    void submitQuiz_calculatesScore_partialCorrect() {
        QuizAttempt attempt = makeAttempt(200L);

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(200L);
        dto.setAnswers(List.of(
                makeTextAnswer(50L, "True"),  // correct
                makeTextAnswer(51L, "False")  // wrong
        ));

        when(quizAttemptRepository.findById(200L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(50L)).thenReturn(Optional.of(makeTrueFalseQuestion(50L, "True")));
        when(questionRepository.findById(51L)).thenReturn(Optional.of(makeTrueFalseQuestion(51L, "True")));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizResultDTO result = quizService.submitQuiz(dto);

        assertThat(result.getScore()).isEqualTo(50);
        assertThat(result.getCorrectAnswers()).isEqualTo(1);
    }

    @Test
    void submitQuiz_scoreIsZero_whenNoAnswers() {
        QuizAttempt attempt = makeAttempt(200L);

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(200L);
        dto.setAnswers(Collections.emptyList());

        when(quizAttemptRepository.findById(200L)).thenReturn(Optional.of(attempt));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizResultDTO result = quizService.submitQuiz(dto);

        assertThat(result.getScore()).isEqualTo(0);
    }

    // =========================================================
    // submitQuiz — pass/fail threshold
    // =========================================================

    @Test
    void submitQuiz_marksAsPassed_whenScoreIs70OrAbove() {
        QuizAttempt attempt = makeAttempt(200L);

        // 7 out of 10 correct = 70%
        List<QuizAnswerDTO> answers = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            answers.add(makeTextAnswer((long) (50 + i), i < 7 ? "True" : "False"));
        }

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(200L);
        dto.setAnswers(answers);

        when(quizAttemptRepository.findById(200L)).thenReturn(Optional.of(attempt));
        for (int i = 0; i < 10; i++) {
            when(questionRepository.findById((long) (50 + i)))
                    .thenReturn(Optional.of(makeTrueFalseQuestion((long) (50 + i), "True")));
        }
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizResultDTO result = quizService.submitQuiz(dto);

        assertThat(result.getIsPassed()).isTrue();
        assertThat(result.getScore()).isEqualTo(70);
    }

    @Test
    void submitQuiz_marksAsFailed_whenScoreBelow70() {
        QuizAttempt attempt = makeAttempt(200L);

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(200L);
        dto.setAnswers(List.of(
                makeTextAnswer(50L, "True"),   // correct
                makeTextAnswer(51L, "False"),  // wrong
                makeTextAnswer(52L, "False")   // wrong
        ));

        when(quizAttemptRepository.findById(200L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(50L)).thenReturn(Optional.of(makeTrueFalseQuestion(50L, "True")));
        when(questionRepository.findById(51L)).thenReturn(Optional.of(makeTrueFalseQuestion(51L, "True")));
        when(questionRepository.findById(52L)).thenReturn(Optional.of(makeTrueFalseQuestion(52L, "True")));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizResultDTO result = quizService.submitQuiz(dto);

        assertThat(result.getIsPassed()).isFalse();
    }

    // =========================================================
    // submitQuiz — answer grading
    // =========================================================

    @Test
    void submitQuiz_gradesTrueFalse_caseInsensitive() {
        QuizAttempt attempt = makeAttempt(200L);

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(200L);
        dto.setAnswers(List.of(makeTextAnswer(50L, "TRUE")));

        when(quizAttemptRepository.findById(200L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(50L)).thenReturn(Optional.of(makeTrueFalseQuestion(50L, "true")));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizResultDTO result = quizService.submitQuiz(dto);

        assertThat(result.getCorrectAnswers()).isEqualTo(1);
    }

    @Test
    void submitQuiz_gradesMultipleChoice_correctOption() {
        QuizAttempt attempt = makeAttempt(200L);

        Question q = makeQuestion(50L, "MULTIPLE_CHOICE");
        QuestionOption correctOpt = new QuestionOption();
        correctOpt.setId(300L);
        correctOpt.setIsCorrect(true);

        QuizAnswerDTO answer = new QuizAnswerDTO();
        answer.setQuestionId(50L);
        answer.setSelectedOptionId(300L);

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(200L);
        dto.setAnswers(List.of(answer));

        when(quizAttemptRepository.findById(200L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(50L)).thenReturn(Optional.of(q));
        when(questionOptionRepository.findById(300L)).thenReturn(Optional.of(correctOpt));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizResultDTO result = quizService.submitQuiz(dto);

        assertThat(result.getCorrectAnswers()).isEqualTo(1);
    }

    @Test
    void submitQuiz_gradesMultipleChoice_wrongOption() {
        QuizAttempt attempt = makeAttempt(200L);

        Question q = makeQuestion(50L, "MULTIPLE_CHOICE");
        QuestionOption wrongOpt = new QuestionOption();
        wrongOpt.setId(301L);
        wrongOpt.setIsCorrect(false);

        QuizAnswerDTO answer = new QuizAnswerDTO();
        answer.setQuestionId(50L);
        answer.setSelectedOptionId(301L);

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(200L);
        dto.setAnswers(List.of(answer));

        when(quizAttemptRepository.findById(200L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(50L)).thenReturn(Optional.of(q));
        when(questionOptionRepository.findById(301L)).thenReturn(Optional.of(wrongOpt));
        when(questionResponseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizResultDTO result = quizService.submitQuiz(dto);

        assertThat(result.getCorrectAnswers()).isEqualTo(0);
    }

    @Test
    void submitQuiz_marksAttemptAsCompleted_afterSubmission() {
        QuizAttempt attempt = makeAttempt(200L);

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(200L);
        dto.setAnswers(Collections.emptyList());
        dto.setTimeTakenSeconds(120);

        when(quizAttemptRepository.findById(200L)).thenReturn(Optional.of(attempt));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        quizService.submitQuiz(dto);

        verify(quizAttemptRepository).save(argThat(a ->
                "COMPLETED".equals(a.getStatus()) && a.getTimeTakenSeconds() == 120
        ));
    }

    @Test
    void submitQuiz_throwsWhenAttemptNotFound() {
        when(quizAttemptRepository.findById(999L)).thenReturn(Optional.empty());

        SubmitQuizDTO dto = new SubmitQuizDTO();
        dto.setAttemptId(999L);
        dto.setAnswers(Collections.emptyList());

        assertThatThrownBy(() -> quizService.submitQuiz(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Quiz attempt not found");
    }

    // =========================================================
    // getQuizzesForModule
    // =========================================================

    @Test
    void getQuizzesForModule_returnsAllQuizzes() {
        Quiz q2 = new Quiz();
        q2.setId(101L);
        q2.setTitle("Quiz 2");
        q2.setModule(module);

        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(List.of(quiz, q2));

        List<QuizDTO> result = quizService.getQuizzesForModule(5L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Basics Quiz");
        assertThat(result.get(1).getTitle()).isEqualTo("Quiz 2");
    }

    @Test
    void getQuizzesForModule_returnsEmptyList_whenNoQuizzes() {
        when(quizRepository.findByModuleIdOrderByOrderIndexAsc(5L)).thenReturn(Collections.emptyList());

        List<QuizDTO> result = quizService.getQuizzesForModule(5L);

        assertThat(result).isEmpty();
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

    private Question makeQuestion(Long id, String type) {
        Question q = new Question();
        q.setId(id);
        q.setQuestionText("Sample question " + id);
        q.setQuestionType(type);
        q.setCorrectAnswer("True");
        q.setPoints(1);
        q.setDifficultyLevel("EASY");
        q.setQuiz(quiz);
        return q;
    }

    private Question makeTrueFalseQuestion(Long id, String correctAnswer) {
        Question q = makeQuestion(id, "TRUE_FALSE");
        q.setCorrectAnswer(correctAnswer);
        return q;
    }

    private QuizAnswerDTO makeTextAnswer(Long questionId, String answer) {
        QuizAnswerDTO dto = new QuizAnswerDTO();
        dto.setQuestionId(questionId);
        dto.setUserAnswer(answer);
        return dto;
    }
}
