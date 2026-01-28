import axios from '../api/axiosConfig'; // Use YOUR axios config

export const adminService = {
  // ========== COURSES ==========
  
  // Get all courses (including unpublished)
  getAllCoursesAdmin: async () => {
    const response = await axios.get('/api/courses/all');
    return response.data;
  },

  // Create course
  createCourse: async (courseData) => {
    const response = await axios.post('/api/courses', courseData);
    return response.data;
  },

  // Update course
  updateCourse: async (courseId, courseData) => {
    const response = await axios.put(`/api/courses/${courseId}`, courseData);
    return response.data;
  },

  // Publish course
  publishCourse: async (courseId) => {
    const response = await axios.put(`/api/courses/${courseId}/publish`);
    return response.data;
  },

  // Unpublish course
  unpublishCourse: async (courseId) => {
    const response = await axios.put(`/api/courses/${courseId}/unpublish`);
    return response.data;
  },

  // Delete course
  deleteCourse: async (courseId) => {
    const response = await axios.delete(`/api/courses/${courseId}`);
    return response.data;
  },

  // ========== MODULES ==========
  
  // Create module
  createModule: async (moduleData) => {
    const response = await axios.post('/api/modules', moduleData);
    return response.data;
  },

  // Update module
  updateModule: async (moduleId, moduleData) => {
    const response = await axios.put(`/api/modules/${moduleId}`, moduleData);
    return response.data;
  },

  // Delete module
  deleteModule: async (moduleId) => {
    const response = await axios.delete(`/api/modules/${moduleId}`);
    return response.data;
  },

  // Get modules by course
  getModulesByCourse: async (courseId) => {
    const response = await axios.get(`/api/modules/course/${courseId}`);
    return response.data;
  },

  // ========== LESSONS ==========
  
  // Create lesson
  createLesson: async (lessonData) => {
    const response = await axios.post('/api/lessons', lessonData);
    return response.data;
  },

  // Update lesson
  updateLesson: async (lessonId, lessonData) => {
    const response = await axios.put(`/api/lessons/${lessonId}`, lessonData);
    return response.data;
  },

  // Delete lesson
  deleteLesson: async (lessonId) => {
    const response = await axios.delete(`/api/lessons/${lessonId}`);
    return response.data;
  },

  // Get lessons by module
  getLessonsByModule: async (moduleId) => {
    const response = await axios.get(`/api/lessons/module/${moduleId}`);
    return response.data;
  },
};