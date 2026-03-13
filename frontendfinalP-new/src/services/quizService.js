import axios from '../api/axiosConfig';

export const quizService = {
  // ==================== ADMIN ====================

  // Create a quiz for a module
  createQuiz: async (data) => {
    const response = await axios.post('/api/quizzes', data);
    return response.data;
  },

  // Add a question to a quiz
  addQuestion: async (quizId, data) => {
    const response = await axios.post(`/api/quizzes/${quizId}/questions`, data);
    return response.data;
  },

  // Get all questions for a quiz (admin - includes correct answers)
  getQuestionsForAdmin: async (quizId) => {
    const response = await axios.get(`/api/quizzes/${quizId}/questions/admin`);
    return response.data;
  },

  // Delete a question
  deleteQuestion: async (questionId) => {
    await axios.delete(`/api/quizzes/questions/${questionId}`);
  },

  // ==================== QUIZ RETRIEVAL ====================

  // Get all quizzes for a module
  getQuizzesForModule: async (moduleId) => {
    const response = await axios.get(`/api/quizzes/module/${moduleId}`);
    return response.data;
  },

  // ==================== ADAPTIVE QUIZ ====================

  // Start an adaptive quiz (returns EASY round)
  startAdaptiveQuiz: async (quizId, userId) => {
    const response = await axios.post(`/api/quizzes/${quizId}/adaptive/start`, null, {
      params: { userId },
    });
    return response.data;
  },

  // Submit a round and get next round or results
  submitRound: async (data) => {
    const response = await axios.post('/api/quizzes/adaptive/submit-round', data);
    return response.data;
  },

  // ==================== MODULE LOCKS ====================

  // Get lock status for all modules in a course
  getModuleLockStatus: async (courseId, userId) => {
    const response = await axios.get(`/api/quizzes/module-locks/course/${courseId}`, {
      params: { userId },
    });
    return response.data;
  },

  // ==================== ML PREDICTION ====================

  // Get ML prediction for a user on a quiz
  getPrediction: async (quizId, userId) => {
    const response = await axios.get(`/api/quizzes/${quizId}/prediction`, {
      params: { userId },
    });
    return response.data;
  },

  // ==================== ATTEMPT HISTORY ====================

  // Get attempt history for a user on a quiz
  getAttempts: async (quizId, userId) => {
    const response = await axios.get(`/api/quizzes/${quizId}/attempts`, {
      params: { userId },
    });
    return response.data;
  },
};
