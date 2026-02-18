import axios from '../api/axiosConfig';

const API_URL = '/api/auth';

// Register a new user
export const register = async (fullName, email, password) => {
  try {
    console.log('Registering user...', { fullName, email });
    const response = await axios.post(`${API_URL}/register`, {
      fullName,
      email,
      password
    });
    
    console.log('Registration response:', response);
    console.log('Registration response data:', response.data);
    console.log('Token:', response.data.token);
    console.log('Role:', response.data.role);
    console.log('Role type:', typeof response.data.role);
    
    if (response.data && response.data.token) {
      const userData = {
        token: response.data.token,
        userId: response.data.userId || response.data.id,
        email: response.data.email,
        fullName: response.data.fullName,
        role: response.data.role,
        loginStreak: response.data.loginStreak || 1
      };

      console.log('Saving to localStorage:', userData);
      localStorage.setItem('user', JSON.stringify(userData));

      // Verify it was saved
      const saved = localStorage.getItem('user');
      console.log('Verified saved data:', saved);
    } else {
      console.error('Invalid response structure:', response.data);
    }

    return response.data;
  } catch (error) {
    console.error('Registration error:', error);
    console.error('Error response:', error.response);
    throw error.response?.data?.message || 'Registration failed';
  }
};

// Login user
export const login = async (email, password) => {
  try {
    // Trim whitespace from inputs
    const trimmedEmail = email.trim();
    const trimmedPassword = password.trim();
    
    console.log('Logging in user...');
    console.log('Original email:', email);
    console.log('Trimmed email:', trimmedEmail);
    console.log('Password length:', password.length);
    console.log('Trimmed password length:', trimmedPassword.length);
    
    const response = await axios.post(`${API_URL}/login`, {
      email: trimmedEmail,
      password: trimmedPassword
    });
    
    console.log('Login response:', response);
    console.log('Login response data:', response.data);
    console.log('Token:', response.data.token);
    console.log('Role:', response.data.role);
    console.log('Role type:', typeof response.data.role);
    
    if (response.data && response.data.token) {
      const userData = {
        token: response.data.token,
        userId: response.data.userId || response.data.id,
        email: response.data.email,
        fullName: response.data.fullName,
        role: response.data.role,
        loginStreak: response.data.loginStreak || 0
      };

      console.log('Saving to localStorage:', userData);
      localStorage.setItem('user', JSON.stringify(userData));

      // Verify it was saved
      const saved = localStorage.getItem('user');
      console.log('Verified saved data:', saved);
    } else {
      console.error('Invalid response structure:', response.data);
    }

    return response.data;
  } catch (error) {
    console.error('Login error:', error);
    console.error('Error response:', error.response);
    throw error.response?.data?.message || 'Login failed';
  }
};

// Logout user
export const logout = () => {
  console.log('Logging out user');
  localStorage.removeItem('user');
};

// Get current user
export const getCurrentUser = () => {
  const user = localStorage.getItem('user');
  console.log('Getting current user from localStorage:', user);
  return user ? JSON.parse(user) : null;
};

// Check if user is authenticated
export const isAuthenticated = () => {
  const authenticated = !!getCurrentUser();
  console.log('Is authenticated:', authenticated);
  return authenticated;
};

// Get user role
export const getUserRole = () => {
  const user = getCurrentUser();
  const role = user?.role || null;
  console.log('User role:', role);
  return role;
};

// Check if user is admin
export const isAdmin = () => {
  return getUserRole() === 'ADMIN';
};

// Check if user is regular user
export const isUser = () => {
  return getUserRole() === 'USER';
};