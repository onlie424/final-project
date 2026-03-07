package com.example.finalprojectb.service;

import com.example.finalprojectb.DTO.MLPredictionRequestDTO;
import com.example.finalprojectb.DTO.MLPredictionResponseDTO;
import com.example.finalprojectb.repo.QuestionResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProficiencyService {

    @Autowired
    private QuestionResponseRepository questionResponseRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    public MLPredictionResponseDTO getPrediction(Long userId, Long quizId) {
        // Calculate the 3 ML features from the database
        Double userMeanCorrect = questionResponseRepository.findUserMeanCorrect(userId);
        Long userInteractionCount = questionResponseRepository.countUserInteractions(userId);
        Double skillMeanCorrect = questionResponseRepository.findSkillMeanCorrect(quizId);

        // Default values for new users or quizzes with no data
        if (userMeanCorrect == null) userMeanCorrect = 0.5;
        if (userInteractionCount == null) userInteractionCount = 0L;
        if (skillMeanCorrect == null) skillMeanCorrect = 0.5;

        // Build request to ML service
        MLPredictionRequestDTO request = new MLPredictionRequestDTO();
        request.setUserMeanCorrect(userMeanCorrect);
        request.setUserInteractionCount(userInteractionCount);
        request.setSkillMeanCorrect(skillMeanCorrect);

        try {
            // Call ML service
            MLPredictionResponseDTO response = restTemplate.postForObject(
                    mlServiceUrl + "/api/predictions/success",
                    request,
                    MLPredictionResponseDTO.class
            );
            return response;
        } catch (Exception e) {
            // Fallback if ML service is down
            MLPredictionResponseDTO fallback = new MLPredictionResponseDTO();
            fallback.setSuccessProbability(userMeanCorrect);
            if (userMeanCorrect >= 0.7) {
                fallback.setRecommendation("ready");
            } else if (userMeanCorrect >= 0.4) {
                fallback.setRecommendation("needs_review");
            } else {
                fallback.setRecommendation("not_ready");
            }
            return fallback;
        }
    }
}
