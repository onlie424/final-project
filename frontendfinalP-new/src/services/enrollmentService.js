import axios from '../api/axiosConfig';

export const enrollmentService = {
  // Enroll in a single course
  enrollInCourse: async (userId, courseId) => {
    const response = await axios.post('/api/enrollments/enroll', {
      userId,
      courseId
    });
    return response.data;
  },

  // Bulk enroll in multiple courses
  bulkEnroll: async (userId, courseIds) => {
    const enrollmentPromises = courseIds.map(courseId =>
      enrollmentService.enrollInCourse(userId, courseId)
    );
    return await Promise.all(enrollmentPromises);
  },

  // Get user enrollments
  getUserEnrollments: async (userId) => {
    const response = await axios.get(`/api/enrollments/user/${userId}`);
    return response.data;
  },

  // Check if enrolled
  isEnrolled: async (userId, courseId) => {
    const response = await axios.get('/api/enrollments/check', {
      params: { userId, courseId }
    });
    return response.data;
  },

  // Unenroll from course
  unenroll: async (enrollmentId) => {
    const response = await axios.delete(`/api/enrollments/${enrollmentId}`);
    return response.data;
  },

  // Get enrollment by ID
  getEnrollmentById: async (enrollmentId) => {
    const response = await axios.get(`/api/enrollments/${enrollmentId}`);
    return response.data;
  }
};