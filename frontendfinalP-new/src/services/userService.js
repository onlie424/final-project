import axios from '../api/axiosConfig';

export const userService = {
  // Get all users (admin only)
  getAllUsers: async () => {
    const response = await axios.get('/api/admin/users');
    return response.data;
  },

  // Get user by ID
  getUserById: async (userId) => {
    const response = await axios.get(`/api/admin/users/${userId}`);
    return response.data;
  },

  // Get user stats
  getUserStats: async () => {
    const response = await axios.get('/api/admin/users/stats');
    return response.data;
  },

  // Delete user
  deleteUser: async (userId) => {
    const response = await axios.delete(`/api/admin/users/${userId}`);
    return response.data;
  },

  // Update user role
  updateUserRole: async (userId, role) => {
    const response = await axios.put(`/api/admin/users/${userId}/role`, null, {
      params: { role }
    });
    return response.data;
  }
};
