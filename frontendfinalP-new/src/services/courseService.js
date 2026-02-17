import axios from '../api/axiosConfig'; // Use YOUR axios config

export const courseService = {
  // Get all published courses
  getAllCourses: async () => {
    const response = await axios.get('/api/courses');
    return response.data;
  },

  // Get course by ID with details (including modules and lessons)
  getCourseById: async (courseId, userId = null) => {
    const params = userId ? { userId } : {};
    const response = await axios.get(`/api/courses/${courseId}`, { params });
    const courseData = response.data;
    console.log('Course data from API:', courseData);

    // Fetch modules for this course
    try {
      const modulesResponse = await axios.get(`/api/modules/course/${courseId}`);
      const modules = modulesResponse.data || [];
      console.log('Modules from API:', modules);

      // Fetch lessons for each module
      const modulesWithLessons = await Promise.all(
        modules.map(async (module) => {
          // Try both 'id' and 'moduleId' field names
          const moduleId = module.id || module.moduleId;
          console.log(`Fetching lessons for module ${moduleId}:`, module);

          try {
            const lessonsResponse = await axios.get(`/api/lessons/module/${moduleId}`);
            console.log(`Lessons for module ${moduleId}:`, lessonsResponse.data);
            return {
              ...module,
              id: moduleId,
              lessons: lessonsResponse.data || []
            };
          } catch (err) {
            console.error(`Error fetching lessons for module ${moduleId}:`, err);
            return { ...module, id: moduleId, lessons: [] };
          }
        })
      );

      // Sort modules by orderIndex
      modulesWithLessons.sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0));

      // Sort lessons within each module by orderIndex
      modulesWithLessons.forEach(module => {
        if (module.lessons) {
          module.lessons.sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0));
        }
      });

      courseData.modules = modulesWithLessons;
      console.log('Final course data with modules and lessons:', courseData);
    } catch (err) {
      console.error('Error fetching modules:', err);
      courseData.modules = courseData.modules || [];
    }

    return courseData;
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