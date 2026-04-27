import axios from '../api/axiosConfig';

const API_URL = '/api/auth';

// Register a new user
export const register = async (fullName, email, password) => {
  try {
    const response = await axios.post(`${API_URL}/register`, {
      fullName,
      email,
      password
    });

    if (response.data && response.data.token) {
      const userData = {
        token: response.data.token,
        userId: response.data.userId || response.data.id,
        email: response.data.email,
        fullName: response.data.fullName,
        role: response.data.role,
      };
      localStorage.setItem('user', JSON.stringify(userData));
    }

    return response.data;
  } catch (error) {
    throw error.response?.data?.message || 'Registration failed';
  }
};

// Login user
export const login = async (email, password) => {
  try {
    const trimmedEmail = email.trim();
    const trimmedPassword = password.trim();

    const response = await axios.post(`${API_URL}/login`, {
      email: trimmedEmail,
      password: trimmedPassword
    });

    if (response.data && response.data.token) {
      const userData = {
        token: response.data.token,
        userId: response.data.userId || response.data.id,
        email: response.data.email,
        fullName: response.data.fullName,
        role: response.data.role,
      };
      localStorage.setItem('user', JSON.stringify(userData));
    }

    return response.data;
  } catch (error) {
    throw error.response?.data?.message || 'Login failed';
  }
};

// Logout user
export const logout = () => {
  localStorage.removeItem('user');
};

// Get current user
export const getCurrentUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

// Check if user is authenticated
export const isAuthenticated = () => {
  return !!getCurrentUser();
};

// Get user role
export const getUserRole = () => {
  const user = getCurrentUser();
  return user?.role || null;
};

// Check if user is admin
export const isAdmin = () => {
  return getUserRole() === 'ADMIN';
};

// Check if user is regular user
export const isUser = () => {
  return getUserRole() === 'USER';
};
