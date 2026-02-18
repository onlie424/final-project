package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.*;
import com.example.finalprojectb.model.*;
import com.example.finalprojectb.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public DashboardDTO getDashboardData(Long userId) {
        DashboardDTO dashboard = new DashboardDTO();

        dashboard.setCurrentFocus(getCurrentFocus(userId));
        dashboard.setProgressOverview(getProgressOverview(userId));
        dashboard.setRecommendations(getRecommendations(userId));
        dashboard.setActivities(getActivities(userId));

        return dashboard;
    }


    private CurrentFocusDTO getCurrentFocus(Long userId) {
        // Find the most recently accessed enrollment
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(userId, "ACTIVE");

        if (enrollments.isEmpty()) {
            return null;
        }

        // Sort by last accessed (most recent first)
        Enrollment activeEnrollment = enrollments.stream()
                .filter(e -> e.getLastAccessed() != null)
                .max((e1, e2) -> e1.getLastAccessed().compareTo(e2.getLastAccessed()))
                .orElse(enrollments.get(0));

        Course course = activeEnrollment.getCourse();

        CurrentFocusDTO dto = new CurrentFocusDTO();
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getTitle());
        dto.setProgress(activeEnrollment.getCompletionPercentage().intValue());
        dto.setDifficulty(course.getDifficulty());

        // Calculate estimated completion
        String estimatedCompletion = calculateEstimatedCompletion(activeEnrollment);
        dto.setEstimatedCompletion(estimatedCompletion);

        return dto;
    }

    /**
     * Calculate estimated completion time
     */
    private String calculateEstimatedCompletion(Enrollment enrollment) {
        if (enrollment.getEnrollmentDate() == null) {
            return "Unknown";
        }

        long daysSinceStart = ChronoUnit.DAYS.between(
                enrollment.getEnrollmentDate(),
                LocalDateTime.now()
        );

        double progress = enrollment.getCompletionPercentage();

        if (progress <= 0) {
            return "Just started";
        }

        // Calculate pace: days per percent
        double daysPerPercent = daysSinceStart / progress;

        // Calculate remaining days
        double remainingPercent = 100 - progress;
        long remainingDays = (long) (remainingPercent * daysPerPercent);

        if (remainingDays <= 0) {
            return "Almost done!";
        } else if (remainingDays <= 7) {
            return remainingDays + " days";
        } else if (remainingDays <= 30) {
            long weeks = remainingDays / 7;
            return weeks + " week" + (weeks > 1 ? "s" : "");
        } else {
            long months = remainingDays / 30;
            return months + " month" + (months > 1 ? "s" : "");
        }
    }

    /**
     * Get progress overview with mastery score and breakdown
     */
    private ProgressOverviewDTO getProgressOverview(Long userId) {
        List<QuizAttempt> allAttempts = quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(userId);

        if (allAttempts.isEmpty()) {
            // No quiz data yet
            ProgressOverviewDTO dto = new ProgressOverviewDTO();
            dto.setMasteryScore("N/A");
            dto.setCompletionPrediction("Take quizzes to see predictions");
            dto.setSubjects(getDefaultSubjectBreakdown());
            return dto;
        }

        // Calculate average score
        double avgScore = allAttempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0.0);

        ProgressOverviewDTO dto = new ProgressOverviewDTO();
        dto.setMasteryScore(convertScoreToGrade(avgScore));

        // Calculate completion prediction based on enrollments
        dto.setCompletionPrediction(calculateCompletionPrediction(userId));

        // Calculate subject breakdown
        dto.setSubjects(calculateSubjectBreakdown(allAttempts));

        return dto;
    }

    /**
     * Convert numeric score to letter grade
     */
    private String convertScoreToGrade(double score) {
        if (score >= 93) return "A";
        if (score >= 90) return "A-";
        if (score >= 87) return "B+";
        if (score >= 83) return "B";
        if (score >= 80) return "B-";
        if (score >= 77) return "C+";
        if (score >= 73) return "C";
        if (score >= 70) return "C-";
        if (score >= 67) return "D+";
        if (score >= 63) return "D";
        if (score >= 60) return "D-";
        return "F";
    }

    /**
     * Calculate completion prediction based on active enrollments
     */
    private String calculateCompletionPrediction(Long userId) {
        List<Enrollment> activeEnrollments = enrollmentRepository.findByUserIdAndStatus(userId, "ACTIVE");

        if (activeEnrollments.isEmpty()) {
            return "No active courses";
        }

        // Find the enrollment closest to completion
        Enrollment closestToCompletion = activeEnrollments.stream()
                .max((e1, e2) -> Double.compare(e1.getCompletionPercentage(), e2.getCompletionPercentage()))
                .orElse(activeEnrollments.get(0));

        if (closestToCompletion.getEnrollmentDate() == null) {
            return "Unknown";
        }

        long daysSinceStart = ChronoUnit.DAYS.between(
                closestToCompletion.getEnrollmentDate(),
                LocalDateTime.now()
        );

        double progress = closestToCompletion.getCompletionPercentage();

        if (progress <= 0) {
            return "Start learning to see prediction";
        }

        // Calculate estimated completion date
        double daysPerPercent = daysSinceStart / progress;
        double remainingPercent = 100 - progress;
        long remainingDays = (long) (remainingPercent * daysPerPercent);

        LocalDateTime estimatedDate = LocalDateTime.now().plusDays(remainingDays);

        // Format as "Month Day" (e.g., "Feb 15th")
        String month = estimatedDate.getMonth().name().substring(0, 3);
        month = month.substring(0, 1) + month.substring(1).toLowerCase();
        int day = estimatedDate.getDayOfMonth();
        String daySuffix = getDaySuffix(day);

        return month + " " + day + daySuffix;
    }

    /**
     * Get day suffix (st, nd, rd, th)
     */
    private String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    /**
     * Calculate subject breakdown (Mastered, In Progress, Needs Work)
     */
    private List<SubjectBreakdownDTO> calculateSubjectBreakdown(List<QuizAttempt> attempts) {
        if (attempts.isEmpty()) {
            return getDefaultSubjectBreakdown();
        }

        // Count quizzes by score ranges
        long mastered = attempts.stream().filter(a -> a.getScore() >= 80).count();
        long inProgress = attempts.stream().filter(a -> a.getScore() >= 50 && a.getScore() < 80).count();
        long needsWork = attempts.stream().filter(a -> a.getScore() < 50).count();

        long total = attempts.size();

        List<SubjectBreakdownDTO> subjects = new ArrayList<>();

        SubjectBreakdownDTO masteredDTO = new SubjectBreakdownDTO();
        masteredDTO.setName("Mastered");
        masteredDTO.setValue((int) ((mastered * 100) / total));
        masteredDTO.setColor("#4CAF50");
        subjects.add(masteredDTO);

        SubjectBreakdownDTO inProgressDTO = new SubjectBreakdownDTO();
        inProgressDTO.setName("In Progress");
        inProgressDTO.setValue((int) ((inProgress * 100) / total));
        inProgressDTO.setColor("#FFC107");
        subjects.add(inProgressDTO);

        SubjectBreakdownDTO needsWorkDTO = new SubjectBreakdownDTO();
        needsWorkDTO.setName("Needs Work");
        needsWorkDTO.setValue((int) ((needsWork * 100) / total));
        needsWorkDTO.setColor("#F44336");
        subjects.add(needsWorkDTO);

        return subjects;
    }

    /**
     * Default subject breakdown when no data available
     */
    private List<SubjectBreakdownDTO> getDefaultSubjectBreakdown() {
        List<SubjectBreakdownDTO> subjects = new ArrayList<>();

        SubjectBreakdownDTO dto1 = new SubjectBreakdownDTO();
        dto1.setName("Mastered");
        dto1.setValue(0);
        dto1.setColor("#4CAF50");
        subjects.add(dto1);

        SubjectBreakdownDTO dto2 = new SubjectBreakdownDTO();
        dto2.setName("In Progress");
        dto2.setValue(0);
        dto2.setColor("#FFC107");
        subjects.add(dto2);

        SubjectBreakdownDTO dto3 = new SubjectBreakdownDTO();
        dto3.setName("Needs Work");
        dto3.setValue(100);
        dto3.setColor("#F44336");
        subjects.add(dto3);

        return subjects;
    }

    /**
     * Get personalized recommendations
     */
    private RecommendationsDTO getRecommendations(Long userId) {
        RecommendationsDTO dto = new RecommendationsDTO();

        // Find mastery gaps (quizzes with low scores)
        List<String> masteryGaps = findMasteryGaps(userId);
        dto.setMasteryGaps(masteryGaps);

        // Suggested topics
        List<String> suggestedTopics = generateSuggestedTopics(userId);
        dto.setSuggestedTopics(suggestedTopics);

        // Check for achievements
        String achievement = checkLatestAchievement(userId);
        dto.setAchievement(achievement);

        return dto;
    }

    /**
     * Find topics where student scored below 70%
     */
    private List<String> findMasteryGaps(Long userId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(userId);

        return attempts.stream()
                .filter(a -> a.getScore() < 70)
                .map(a -> a.getQuiz().getTitle())
                .distinct()
                .limit(3) // Show top 3 gaps
                .collect(Collectors.toList());
    }

    /**
     * Generate suggested topics based on performance
     */
    private List<String> generateSuggestedTopics(Long userId) {
        List<String> suggestions = new ArrayList<>();

        // Find incomplete lessons
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(userId, "ACTIVE");

        if (!enrollments.isEmpty()) {
            Enrollment current = enrollments.get(0);
            suggestions.add("Continue " + current.getCourse().getTitle());
        }

        // Check if performing well
        List<QuizAttempt> recentAttempts = quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(userId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        if (!recentAttempts.isEmpty()) {
            long passedCount = recentAttempts.stream().filter(QuizAttempt::getIsPassed).count();
            double passRate = (passedCount * 100.0) / recentAttempts.size();

            if (passRate > 80) {
                suggestions.add("You're performing above average! 🎉");
            }
        }

        return suggestions;
    }

    /**
     * Check for latest achievement
     */
    private String checkLatestAchievement(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null;
        }

        // Check for completed lessons
        long completedLessons = lessonProgressRepository
                .findByUserIdAndStatus(userId, "COMPLETED")
                .size();

        if (completedLessons == 1) {
            return "First Lesson Complete! 🎯";
        } else if (completedLessons == 10) {
            return "10 Lessons Complete! 🌟";
        } else if (completedLessons == 50) {
            return "50 Lessons Complete! 🏆";
        }

        // Check for quiz performance
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(userId);
        if (!attempts.isEmpty()) {
            QuizAttempt latest = attempts.get(0);
            if (latest.getScore() == 100) {
                return "Perfect Score! 💯";
            }
        }
        return null;
    }

    private List<ActivityDTO> getActivities(Long userId) {
        List<ActivityDTO> activities = new ArrayList<>();

        // Get incomplete lessons from active courses
        List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(userId, "ACTIVE");

        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();

            // Find quizzes not yet taken
            List<Quiz> courseQuizzes = quizRepository.findAll().stream()
                    .filter(q -> q.getLesson().getModule().getCourse().getId().equals(course.getId()))
                    .collect(Collectors.toList());

            for (Quiz quiz : courseQuizzes) {
                // Check if user has attempted this quiz
                boolean attempted = quizAttemptRepository
                        .findByUserIdAndQuizIdOrderByAttemptedAtDesc(userId, quiz.getId())
                        .stream()
                        .anyMatch(a -> a.getIsPassed());

                if (!attempted) {
                    ActivityDTO activity = new ActivityDTO();
                    activity.setId(quiz.getId());
                    activity.setType("assignment");
                    activity.setTitle(quiz.getTitle());
                    activity.setDate("Pending");
                    activity.setCompleted(false);
                    activities.add(activity);

                    if (activities.size() >= 5) break; // Limit to 5 activities
                }
            }

            if (activities.size() >= 5) break;
        }

        // If no activities, add a motivational message
        if (activities.isEmpty()) {
            ActivityDTO activity = new ActivityDTO();
            activity.setId(0L);
            activity.setType("info");
            activity.setTitle("You're all caught up! 🎉");
            activity.setDate("Keep up the great work");
            activity.setCompleted(true);
            activities.add(activity);
        }

        return activities;
    }
}