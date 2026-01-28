import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { useAuth } from '../context/AuthContext';

import '../styles/NavBar.css';

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          🎓 EduPlatform
        </Link>

        <div className="navbar-menu">
          <Link to="/courses" className="navbar-link">
            Courses
          </Link>

          {user ? (
            <>
              {isAdmin() ? (
                <>
                  <Link to="/admin" className="navbar-link">
                    Admin Dashboard
                  </Link>
                  <Link to="/admin/courses/create" className="navbar-link">
                    Create Course
                  </Link>
                </>
              ) : (
                <>
                  <Link to="/dashboard" className="navbar-link">
                    My Dashboard
                  </Link>
                </>
              )}

              <button onClick={handleLogout} className="navbar-btn logout">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="navbar-btn">
                Login
              </Link>
              <Link to="/register" className="navbar-btn primary">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}