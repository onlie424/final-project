package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.*;
import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdaptiveQuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuestionResponseRepository questionResponseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private static final int MAX_QUESTIONS_PER_ROUND = 5;

    @Transactional
    public AdaptiveQuizStartDTO startAdaptiveQuiz(Long userId, Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Block retake if the user has already fully passed this quiz
        if (quizAttemptRepository.hasUserPassedQuiz(userId, quizId)) {
            throw new RuntimeException("QUIZ_ALREADY_PASSED");
        }

        // Check if the user has a previous failed attempt with passed rounds (for resume)
        String startDifficulty = "EASY";
        boolean resumed = false;
        java.util.Optional<QuizAttempt> lastResumable = quizAttemptRepository.findLastResumableAttempt(userId, quizId);
        if (lastResumable.isPresent()) {
            String highestPassed = lastResumable.get().getHighestPassedDifficulty();
            String nextAfterPassed = getNextDifficulty(highestPassed);
            if (nextAfterPassed != null) {
                startDifficulty = nextAfterPassed;
                resumed = true;
            }
        }

        // Create a new attempt
        Long attemptCount = quizAttemptRepository.countByUserIdAndQuizId(userId, quizId);
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setAttemptNumber((int) (attemptCount + 1));
        attempt.setStatus("IN_PROGRESS");
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Fetch questions for the starting difficulty and shuffle them
        List<Question> startQuestions = questionRepository.findByQuizIdAndDifficultyLevel(quizId, startDifficulty);
        Collections.shuffle(startQuestions);
        List<Question> roundQuestions = startQuestions.stream()
                .limit(MAX_QUESTIONS_PER_ROUND)
                .collect(Collectors.toList());

        List<QuestionDTO> questionDTOs = roundQuestions.stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());

        // Build response
        AdaptiveQuizStartDTO response = new AdaptiveQuizStartDTO();
        response.setAttemptId(savedAttempt.getId());
        response.setQuizId(quiz.getId());
        response.setQuizTitle(quiz.getTitle());
        response.setCurrentDifficulty(startDifficulty);
        response.setQuestions(questionDTOs);
        response.setResumedFromPreviousAttempt(resumed);

        return response;
    }

    @Transactional
    public AdaptiveRoundResultDTO submitRound(SubmitAdaptiveRoundDTO dto) {
        QuizAttempt attempt = quizAttemptRepository.findById(dto.getAttemptId())
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        int correctCount = 0;
        int totalQuestions = dto.getAnswers().size();
        List<QuestionResultDTO> questionResults = new ArrayList<>();
        List<Question> failedQuestions = new ArrayList<>();

        // Grade each answer
        for (QuizAnswerDTO answer : dto.getAnswers()) {
            Question question = questionRepository.findById(answer.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            QuestionResponse response = new QuestionResponse();
            response.setAttempt(attempt);
            response.setQuestion(question);
            response.setUserAnswer(answer.getUserAnswer());
            response.setTimeSpentSeconds(answer.getTimeSpentSeconds());

            boolean isCorrect = checkAnswer(question, answer);
            response.setIsCorrect(isCorrect);
            response.setPointsEarned(isCorrect ? question.getPoints() : 0);
            questionResponseRepository.save(response);

            if (isCorrect) {
                correctCount++;
            } else {
                failedQuestions.add(question);
            }

            QuestionResultDTO resultDTO = new QuestionResultDTO();
            resultDTO.setQuestionId(question.getId());
            resultDTO.setQuestionText(question.getQuestionText());
            resultDTO.setUserAnswer(answer.getUserAnswer());
            resultDTO.setCorrectAnswer(question.getCorrectAnswer());
            resultDTO.setIsCorrect(isCorrect);
            resultDTO.setPointsEarned(response.getPointsEarned());
            resultDTO.setExplanation(question.getExplanation());
            questionResults.add(resultDTO);
        }

        int roundScore = totalQuestions > 0 ? (int) ((correctCount * 100.0) / totalQuestions) : 0;

        // Build the result
        AdaptiveRoundResultDTO result = new AdaptiveRoundResultDTO();
        result.setRoundScore(roundScore);
        result.setCorrectCount(correctCount);
        result.setTotalQuestions(totalQuestions);
        result.setQuestionResults(questionResults);

        if (roundScore >= 70) {
            // Passed this round - track highest passed difficulty
            attempt.setHighestPassedDifficulty(dto.getDifficulty());
            quizAttemptRepository.save(attempt);

            // Escalate or complete
            String nextDifficulty = getNextDifficulty(dto.getDifficulty());

            if (nextDifficulty == null) {
                // Passed HARD round - quiz complete and passed!
                result.setEscalated(true);
                result.setQuizCompleted(true);
                result.setQuizPassed(true);
                result.setNextDifficulty(null);
                result.setNextQuestions(null);

                // Update attempt
                updateAttemptAsCompleted(attempt, true, dto.getTimeTakenSeconds());

                // Update enrollment completion percentage
                Long courseId = attempt.getQuiz().getModule().getCourse().getId();
                updateEnrollmentCompletion(attempt.getUser().getId(), courseId);
            } else {
                // Escalate to next difficulty
                List<Question> nextQuestions = questionRepository
                        .findByQuizIdAndDifficultyLevel(attempt.getQuiz().getId(), nextDifficulty);
                Collections.shuffle(nextQuestions);
                List<Question> roundQuestions = nextQuestions.stream()
                        .limit(MAX_QUESTIONS_PER_ROUND)
                        .collect(Collectors.toList());

                result.setEscalated(true);
                result.setQuizCompleted(false);
                result.setQuizPassed(false);
                result.setNextDifficulty(nextDifficulty);
                result.setNextQuestions(roundQuestions.stream()
                        .map(this::convertToQuestionDTO)
                        .collect(Collectors.toList()));
            }
        } else {
            // Failed this round - quiz failed
            result.setEscalated(false);
            result.setQuizCompleted(true);
            result.setQuizPassed(false);
            result.setNextDifficulty(null);
            result.setNextQuestions(null);

            // Get targeted lessons to revisit based on failed questions
            List<Lesson> lessonsToRevisit = failedQuestions.stream()
                    .map(Question::getLesson)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (lessonsToRevisit.isEmpty()) {
                // No failed questions have lesson links (old data) — fall back to all module lessons
                Module module = attempt.getQuiz().getModule();
                lessonsToRevisit = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());
            }

            // Deduplicate and sort by orderIndex
            List<Lesson> uniqueLessons = lessonsToRevisit.stream()
                    .collect(Collectors.toMap(Lesson::getId, l -> l, (a, b) -> a))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(Lesson::getOrderIndex))
                    .collect(Collectors.toList());

            result.setLessonsToRevisit(uniqueLessons.stream()
                    .map(this::convertToLessonDTO)
                    .collect(Collectors.toList()));

            // Update attempt
            updateAttemptAsCompleted(attempt, false, dto.getTimeTakenSeconds());
        }

        return result;
    }

    @Transactional
    public void updateEnrollmentCompletion(Long userId, Long courseId) {
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<Long> allQuizIds = modules.stream()
                .flatMap(m -> quizRepository.findByModuleIdOrderByOrderIndexAsc(m.getId()).stream())
                .map(Quiz::getId)
                .collect(Collectors.toList());

        if (allQuizIds.isEmpty()) return;

        long passedCount = allQuizIds.stream()
                .filter(qId -> quizAttemptRepository.hasUserPassedQuiz(userId, qId))
                .count();

        int completionPct = (int) ((passedCount * 100.0) / allQuizIds.size());

        enrollmentRepository.findByUserIdAndCourseId(userId, courseId).ifPresent(enrollment -> {
            enrollment.setCompletionPercentage((double) completionPct);
            if (completionPct >= 100) {
                enrollment.setStatus("COMPLETED");
            }
            enrollmentRepository.save(enrollment);
        });
    }

    private void updateAttemptAsCompleted(QuizAttempt attempt, boolean passed, Integer timeTaken) {
        attempt.setStatus("COMPLETED");
        attempt.setIsPassed(passed);
        attempt.setTimeTakenSeconds(timeTaken);
        quizAttemptRepository.save(attempt);
    }

    private String getNextDifficulty(String current) {
        if ("EASY".equals(current)) return "MEDIUM";
        if ("MEDIUM".equals(current)) return "HARD";
        return null; // HARD is the last level
    }

    private boolean checkAnswer(Question question, QuizAnswerDTO answer) {
        if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
            if (answer.getSelectedOptionId() != null) {
                QuestionOption selectedOption = questionOptionRepository.findById(answer.getSelectedOptionId())
                        .orElse(null);
                return selectedOption != null && selectedOption.getIsCorrect();
            }
            return false;
        } else if ("TRUE_FALSE".equals(question.getQuestionType()) || "SHORT_ANSWER".equals(question.getQuestionType())) {
            return answer.getUserAnswer() != null &&
                    answer.getUserAnswer().trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
        }
        return false;
    }

    private QuestionDTO convertToQuestionDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setPoints(question.getPoints());
        dto.setDifficultyLevel(question.getDifficultyLevel());

        if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
            List<QuestionOption> options = questionOptionRepository
                    .findByQuestionIdOrderByOrderIndexAsc(question.getId());
            List<QuestionOptionDTO> optionDTOs = options.stream()
                    .map(this::convertToOptionDTO)
                    .collect(Collectors.toList());
            dto.setOptions(optionDTOs);
        }

        return dto;
    }

    private QuestionOptionDTO convertToOptionDTO(QuestionOption option) {
        QuestionOptionDTO dto = new QuestionOptionDTO();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setOrderIndex(option.getOrderIndex());
        return dto;
    }

    private LessonDTO convertToLessonDTO(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setContentType(lesson.getContentType());
        dto.setContentUrl(lesson.getContentUrl());
        dto.setContentText(lesson.getContentText());
        dto.setDurationMinutes(lesson.getDurationMinutes());
        dto.setOrderIndex(lesson.getOrderIndex());
        dto.setIsCompleted(false);
        return dto;
    }
}
