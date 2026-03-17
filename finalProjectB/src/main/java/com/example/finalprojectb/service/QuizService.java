package com.example.finalprojectb.service;
import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.*;
import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ModuleRepository moduleRepository;

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

    public QuizDTO createQuiz(CreateQuizDTO dto) {
        Module module = moduleRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found with id: " + dto.getModuleId()));

        Quiz quiz = new Quiz();
        quiz.setModule(module);
        quiz.setTitle(dto.getTitle());
        quiz.setDescription(dto.getDescription());
        quiz.setPassingScore(dto.getPassingScore());
        quiz.setTimeLimitMinutes(dto.getTimeLimitMinutes());

        // Set order index based on existing quizzes in the module
        List<Quiz> existingQuizzes = quizRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());
        quiz.setOrderIndex(existingQuizzes.size() + 1);

        Quiz saved = quizRepository.save(quiz);
        return convertToQuizDTO(saved);
    }

    @Transactional
    public QuestionDTO addQuestion(Long quizId, CreateQuestionDTO dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        Question question = new Question();
        question.setQuiz(quiz);
        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setExplanation(dto.getExplanation());
        question.setPoints(dto.getPoints() != null ? dto.getPoints() : 1);
        question.setDifficultyLevel(dto.getDifficultyLevel() != null ? dto.getDifficultyLevel() : "EASY");

        // Link to a specific lesson if provided
        if (dto.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(dto.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + dto.getLessonId()));
            if (!lesson.getModule().getId().equals(quiz.getModule().getId())) {
                throw new RuntimeException("Lesson does not belong to the same module as this quiz");
            }
            question.setLesson(lesson);
        }

        // Auto-set order index if not provided
        if (dto.getOrderIndex() != null) {
            question.setOrderIndex(dto.getOrderIndex());
        } else {
            Long count = questionRepository.countByQuizId(quizId);
            question.setOrderIndex((int) (count + 1));
        }

        Question savedQuestion = questionRepository.save(question);

        // Save options for multiple choice questions
        if ("MULTIPLE_CHOICE".equals(dto.getQuestionType()) && dto.getOptions() != null) {
            for (CreateQuestionOptionDTO optionDTO : dto.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestion(savedQuestion);
                option.setOptionText(optionDTO.getOptionText());
                option.setIsCorrect(optionDTO.getIsCorrect());
                option.setOrderIndex(optionDTO.getOrderIndex());
                questionOptionRepository.save(option);
            }
        }

        return convertToQuestionDTO(savedQuestion);
    }

    @Transactional
    public QuestionDTO updateQuestionLesson(Long questionId, Long lessonId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        if (lessonId != null) {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + lessonId));
            if (!lesson.getModule().getId().equals(question.getQuiz().getModule().getId())) {
                throw new RuntimeException("Lesson does not belong to the same module as this quiz");
            }
            question.setLesson(lesson);
        } else {
            question.setLesson(null);
        }

        questionRepository.save(question);
        return convertToAdminQuestionDTO(question);
    }

    public List<QuizDTO> getQuizzesForModule(Long moduleId) {
        List<Quiz> quizzes = quizRepository.findByModuleIdOrderByOrderIndexAsc(moduleId);
        return quizzes.stream().map(this::convertToQuizDTO).collect(Collectors.toList());
    }

    public List<QuestionDTO> getQuestionsForAdmin(Long quizId) {
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
        return questions.stream().map(this::convertToAdminQuestionDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
        if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
            questionOptionRepository.deleteByQuestionId(questionId);
        }
        questionRepository.delete(question);
    }

    @Transactional
    public QuizAttemptDTO startQuiz(StartQuizDTO dto) {
        Quiz quiz = quizRepository.findById(dto.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + dto.getQuizId()));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        Long attemptCount = quizAttemptRepository.countByUserIdAndQuizId(dto.getUserId(), dto.getQuizId());

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setAttemptNumber((int) (attemptCount + 1));
        attempt.setStatus("IN_PROGRESS");

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(dto.getQuizId());
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());

        QuizAttemptDTO attemptDTO = new QuizAttemptDTO();
        attemptDTO.setAttemptId(savedAttempt.getId());
        attemptDTO.setQuizId(quiz.getId());
        attemptDTO.setQuizTitle(quiz.getTitle());
        attemptDTO.setAttemptNumber(savedAttempt.getAttemptNumber());
        attemptDTO.setStartedAt(LocalDateTime.now());
        attemptDTO.setQuestions(questionDTOs);

        return attemptDTO;
    }

    @Transactional
    public QuizResultDTO submitQuiz(SubmitQuizDTO dto) {
        QuizAttempt attempt = quizAttemptRepository.findById(dto.getAttemptId())
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        int correctCount = 0;
        int totalQuestions = dto.getAnswers().size();
        List<QuestionResultDTO> questionResults = new ArrayList<>();

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

            if (isCorrect) correctCount++;

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

        int score = totalQuestions > 0 ? (int) ((correctCount * 100.0) / totalQuestions) : 0;

        attempt.setScore(score);
        attempt.setCorrectAnswers(correctCount);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setTimeTakenSeconds(dto.getTimeTakenSeconds());
        attempt.setStatus("COMPLETED");
        attempt.setIsPassed(score >= 70);

        quizAttemptRepository.save(attempt);

        QuizResultDTO resultDTO = new QuizResultDTO();
        resultDTO.setAttemptId(attempt.getId());
        resultDTO.setScore(score);
        resultDTO.setTotalQuestions(totalQuestions);
        resultDTO.setCorrectAnswers(correctCount);
        resultDTO.setIsPassed(attempt.getIsPassed());
        resultDTO.setTimeTakenSeconds(dto.getTimeTakenSeconds());
        resultDTO.setQuestionResults(questionResults);

        return resultDTO;
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
                    .map(this::convertToQuestionOptionDTO)
                    .collect(Collectors.toList());
            dto.setOptions(optionDTOs);
        }

        return dto;
    }

    private QuestionOptionDTO convertToQuestionOptionDTO(QuestionOption option) {
        QuestionOptionDTO dto = new QuestionOptionDTO();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setOrderIndex(option.getOrderIndex());
        // isCorrect left null for student responses
        return dto;
    }

    private QuestionDTO convertToAdminQuestionDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setPoints(question.getPoints());
        dto.setDifficultyLevel(question.getDifficultyLevel());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setExplanation(question.getExplanation());

        if (question.getLesson() != null) {
            dto.setLessonId(question.getLesson().getId());
            dto.setLessonTitle(question.getLesson().getTitle());
        }

        if ("MULTIPLE_CHOICE".equals(question.getQuestionType())) {
            List<QuestionOption> options = questionOptionRepository
                    .findByQuestionIdOrderByOrderIndexAsc(question.getId());
            List<QuestionOptionDTO> optionDTOs = options.stream()
                    .map(this::convertToAdminOptionDTO)
                    .collect(Collectors.toList());
            dto.setOptions(optionDTOs);
        }

        return dto;
    }

    private QuestionOptionDTO convertToAdminOptionDTO(QuestionOption option) {
        QuestionOptionDTO dto = new QuestionOptionDTO();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setOrderIndex(option.getOrderIndex());
        dto.setIsCorrect(option.getIsCorrect());
        return dto;
    }
}
