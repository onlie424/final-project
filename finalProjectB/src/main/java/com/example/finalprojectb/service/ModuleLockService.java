package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.MLPredictionResponseDTO;
import com.example.finalprojectb.DTO.ModuleLockStatusDTO;
import com.example.finalprojectb.model.Module;
import com.example.finalprojectb.model.Quiz;
import com.example.finalprojectb.repo.ModuleRepository;
import com.example.finalprojectb.repo.QuizAttemptRepository;
import com.example.finalprojectb.repo.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ModuleLockService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ProficiencyService proficiencyService;

    private static final double ML_READINESS_THRESHOLD = 0.4;

    @Transactional
    public List<ModuleLockStatusDTO> getCourseModuleLockStatus(Long userId, Long courseId) {
        List<Module> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        List<ModuleLockStatusDTO> lockStatuses = new ArrayList<>();

        boolean previousLocked = false;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            ModuleLockStatusDTO status = new ModuleLockStatusDTO();
            status.setModuleId(module.getId());
            status.setModuleTitle(module.getTitle());
            status.setOrderIndex(module.getOrderIndex());

            if (i == 0) {
                // First module is always unlocked
                status.setIsLocked(false);
                status.setLockReason(null);
                status.setPrerequisiteQuizPassed(true);
                status.setMlReadinessProbability(1.0);
            } else if (previousLocked) {
                // If previous module was locked, this one is locked too (cascade)
                status.setIsLocked(true);
                status.setLockReason("prerequisite_not_met");
                status.setPrerequisiteQuizPassed(false);
                status.setMlReadinessProbability(null);
            } else {
                // Check if user passed all quizzes in the previous module
                Module previousModule = modules.get(i - 1);
                List<Quiz> previousQuizzes = quizRepository
                        .findByModuleIdOrderByOrderIndexAsc(previousModule.getId());

                boolean previousPassed;
                if (previousQuizzes.isEmpty()) {
                    // No quizzes in previous module means it's automatically passed
                    previousPassed = true;
                } else {
                    previousPassed = quizAttemptRepository
                            .hasUserPassedAllModuleQuizzes(userId, previousModule.getId());
                }

                status.setPrerequisiteQuizPassed(previousPassed);

                if (!previousPassed) {
                    // Previous module quiz not passed
                    status.setIsLocked(true);
                    status.setLockReason("prerequisite_not_met");
                    status.setMlReadinessProbability(null);
                } else {
                    // Previous passed - check ML readiness
                    List<Quiz> currentQuizzes = quizRepository
                            .findByModuleIdOrderByOrderIndexAsc(module.getId());

                    if (!currentQuizzes.isEmpty()) {
                        MLPredictionResponseDTO prediction = proficiencyService
                                .getPrediction(userId, currentQuizzes.get(0).getId());
                        status.setMlReadinessProbability(prediction.getSuccessProbability());

                        if (prediction.getSuccessProbability() < ML_READINESS_THRESHOLD) {
                            status.setIsLocked(true);
                            status.setLockReason("ml_not_ready");
                        } else {
                            status.setIsLocked(false);
                            status.setLockReason(null);
                        }
                    } else {
                        // No quizzes in current module - unlock it
                        status.setIsLocked(false);
                        status.setLockReason(null);
                        status.setMlReadinessProbability(1.0);
                    }
                }
            }

            previousLocked = status.getIsLocked();
            lockStatuses.add(status);
        }

        return lockStatuses;
    }
}
