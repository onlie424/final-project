import React, { createContext, useState, useContext, useEffect } from 'react';

import * as authService from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in on mount
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const data = await authService.login(email, password);
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
    return data;
  };

  const register = async (fullName, email, password) => {
    const data = await authService.register(fullName, email, password);
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
    return data;
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const isAdminUser = () => {
    return authService.isAdmin();
  };

  const value = {
    user,
    login,
    register,
    logout,
    isAdmin: isAdminUser,
    isAuthenticated: authService.isAuthenticated,
    loading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};