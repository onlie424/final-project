import { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

import { isAuthenticated, isAdmin, isUser, getUserRole, getCurrentUser } from './services/authService';

import Login from './components/pages/auth/login';
import Register from './components/pages/auth/registration';
import AdminDashboard from './components/pages/admin/Dashboard';
import CourseCreate from './components/pages/admin/CourseCreate';
import UserDashboard from './components/pages/student/Dashboard';
import CourseDetail from './components/pages/student/CourseDetail';
import Classroom from './components/pages/student/Classroom';
import WelcomePage from './components/WelcomePage';

// Protected Route Component
function ProtectedRoute({ children }) {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const checkAuth = () => {
      const auth = isAuthenticated();
      console.log('ProtectedRoute - isAuthenticated:', auth);
      console.log('ProtectedRoute - getCurrentUser:', getCurrentUser());
      setAuthenticated(auth);
      setLoading(false);
    };
    
    checkAuth();
  }, []);
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  return authenticated ? children : <Navigate to="/login" replace />;
}

// Admin Route Component
function AdminRoute({ children }) {
  const [isAdminUser, setIsAdminUser] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const checkAuth = () => {
      const auth = isAuthenticated();
      const adminStatus = isAdmin();
      console.log('AdminRoute - isAuthenticated:', auth);
      console.log('AdminRoute - isAdmin:', adminStatus);
      console.log('AdminRoute - getUserRole:', getUserRole());
      setAuthenticated(auth);
      setIsAdminUser(adminStatus);
      setLoading(false);
    };
    
    checkAuth();
  }, []);
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  if (!authenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return isAdminUser ? children : <Navigate to="/user/dashboard" replace />;
}

// User Route Component
function UserRoute({ children }) {
  const [isRegularUser, setIsRegularUser] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const checkAuth = () => {
      const auth = isAuthenticated();
      const userStatus = isUser();
      console.log('UserRoute - isAuthenticated:', auth);
      console.log('UserRoute - isUser:', userStatus);
      console.log('UserRoute - getUserRole:', getUserRole());
      setAuthenticated(auth);
      setIsRegularUser(userStatus);
      setLoading(false);
    };
    
    checkAuth();
  }, []);
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  if (!authenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return isRegularUser ? children : <Navigate to="/admin/dashboard" replace />;
}

// Root Path Handler Component
function RootPathHandler() {
  const [destination, setDestination] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const determineDestination = () => {
      console.log('RootPathHandler - Checking authentication...');
      
      if (isAuthenticated()) {
        const role = getUserRole();
        console.log('RootPathHandler - User authenticated, role:', role);
        
        if (role === 'ADMIN') {
          setDestination('/admin/dashboard');
        } else if (role === 'USER') {
          setDestination('/user/dashboard');
        } else {
          setDestination('/user/dashboard'); // Fallback
        }
      } else {
        console.log('RootPathHandler - User not authenticated, showing welcome page');
        setDestination('welcome');
      }
      
      setLoading(false);
    };
    
    determineDestination();
  }, []);
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  if (destination === 'welcome') {
    return <WelcomePage />;
  }
  
  return <Navigate to={destination} replace />;
}

// Main App Component
function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Admin Dashboard Route */}
        <Route
          path="/admin/dashboard"
          element={
            <AdminRoute>
              <AdminDashboard />
            </AdminRoute>
          }
        />

        {/* Admin Create Course Route */}
        <Route
          path="/admin/courses/create"
          element={
            <AdminRoute>
              <CourseCreate />
            </AdminRoute>
          }
        />

        {/* Admin Edit Course Route */}
        <Route
          path="/admin/courses/:courseId/edit"
          element={
            <AdminRoute>
              <CourseCreate />
            </AdminRoute>
          }
        />
        
        {/* User Dashboard Route */}
        <Route
          path="/user/dashboard"
          element={
            <UserRoute>
              <UserDashboard />
            </UserRoute>
          }
        />

        {/* Course Detail Route */}
        <Route
          path="/courses/:courseId"
          element={
            <ProtectedRoute>
              <CourseDetail />
            </ProtectedRoute>
          }
        />

        {/* Classroom Routes */}
        <Route
          path="/classroom/:courseId"
          element={
            <ProtectedRoute>
              <Classroom />
            </ProtectedRoute>
          }
        />
        <Route
          path="/classroom/:courseId/lesson/:lessonId"
          element={
            <ProtectedRoute>
              <Classroom />
            </ProtectedRoute>
          }
        />

        {/* Root Path */}
        <Route path="/" element={<RootPathHandler />} />
        
        {/* Catch-all route */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;