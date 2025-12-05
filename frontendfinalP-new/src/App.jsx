import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/login';
import Register from './pages/registration';
import AdminDashboard from './pages/AdminDashboard';
import UserDashboard from './pages/UserDashboard';
// Import the necessary functions, including the new getUserRole
import { isAuthenticated, isAdmin, isUser, getUserRole } from './services/authService'; 
import WelcomePage from './pages/WelcomePage';

// --- (Your Protected Route Components remain unchanged) ---
function ProtectedRoute({ children }) {
  return isAuthenticated() ? children : <Navigate to="/login" />;
}

function AdminRoute({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/login" />;
  }
  return isAdmin() ? children : <Navigate to="/user/dashboard" />;
}

function UserRoute({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/login" />;
  }
  return isUser() ? children : <Navigate to="/admin/dashboard" />;
}

// --- Main App Component ---

function App() {
  
  // New function to handle the logic for the root path
  const handleRootPath = () => {
    if (isAuthenticated()) {
      const role = getUserRole(); // Get role from authService
      
      // Redirect authenticated users to their specific dashboard
      if (role === 'ADMIN') {
        return <Navigate to="/admin/dashboard" replace />;
      }
      if (role === 'USER') {
        return <Navigate to="/user/dashboard" replace />;
      }
      // Fallback redirect if role is missing but authenticated
      return <Navigate to="/user/dashboard" replace />; 
    }
    
    // If NOT authenticated, render the Welcome Page
    return <WelcomePage />;
  };

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
        
        {/* User Dashboard Route */}
        <Route 
          path="/user/dashboard" 
          element={
            <UserRoute>
              <UserDashboard />
            </UserRoute>
          } 
        />
        
        {/* CORRECTION: Root Path (/) logic */}
        <Route path="/" element={handleRootPath()} />
        
        {/* Optional: Add a catch-all route for better user experience */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;