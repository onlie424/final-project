import axios from '../api/axiosConfig'; // Use YOUR axios config

export const courseService = {
  // Get all published courses
  getAllCourses: async () => {
    const response = await axios.get('/api/courses');
    return response.data;
  },

  // Get course by ID with details
  getCourseById: async (courseId, userId = null) => {
    const params = userId ? { userId } : {};
    const response = await axios.get(`/api/courses/${courseId}`, { params });
    return response.data;
  },

  // Search courses
  searchCourses: async (keyword) => {
    const response = await axios.get('/api/courses/search', {
      params: { keyword },
    });
    return response.data;
  },

  // Get courses by category
  getCoursesByCategory: async (category) => {
    const response = await axios.get(`/api/courses/category/${category}`);
    return response.data;
  },

  // Get courses by difficulty
  getCoursesByDifficulty: async (difficulty) => {
    const response = await axios.get(`/api/courses/difficulty/${difficulty}`);
    return response.data;
  },

  // Get lesson by ID
  getLessonById: async (lessonId, userId = null) => {
    const params = userId ? { userId } : {};
    const response = await axios.get(`/api/lessons/${lessonId}`, { params });
    return response.data;
  },

  // Get module by ID
  getModuleById: async (moduleId) => {
    const response = await axios.get(`/api/modules/${moduleId}`);
    return response.data;
  },
};