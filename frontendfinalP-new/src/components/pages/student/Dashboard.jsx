import React, { useState, useEffect } from 'react';

import { useAuth } from '../../../context/AuthContext';

import { courseService } from '../../../services/courseService';

import CurrentFocus from '../../dashboard/CurrentFocus';
import ProgressOverview from '../../dashboard/ProgressOverview';
import Recommendations from '../../dashboard/Recommendations';
import ActivityFeed from '../../dashboard/ActivityFeed';
import MyCourses from '../../dashboard/MyCourses';

import '../../../styles/Dashboard.css';

export default function Dashboard() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [enrolledCourses, setEnrolledCourses] = useState([]);
  const [currentCourse, setCurrentCourse] = useState(null);
  const [stats, setStats] = useState({
    totalCoursesEnrolled: 0,
    completedLessons: 0,
    totalLessons: 0,
    averageScore: 0,
    streak: 7, // Mock data for now
    level: 2,
  });

  useEffect(() => {
    fetchDashboardData();
  }, [user]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // Fetch enrolled courses (you'll need to implement this endpoint)
      // For now, fetch all courses as demo
      const courses = await courseService.getAllCourses();
      setEnrolledCourses(courses);
      
      // Set current course (first enrolled course for demo)
      if (courses.length > 0) {
        const courseDetail = await courseService.getCourseById(courses[0].id, user?.userId);
        setCurrentCourse(courseDetail);
      }
      
      // Calculate stats
      setStats({
        ...stats,
        totalCoursesEnrolled: courses.length,
        // Add more calculations as you implement enrollment/progress features
      });
      
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="spinner"></div>
        <p>Loading your dashboard...</p>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      {/* Hero Section */}
      <div className="dashboard-hero">
        <div className="hero-content">
          <h1>Welcome back, {user?.fullName || 'Student'}! 🎓</h1>
          <p>Continue your learning journey and achieve your goals</p>
        </div>
        <div className="hero-stats">
          <div className="stat-badge">
            <span className="stat-icon">🔥</span>
            <div>
              <div className="stat-value">{stats.streak}</div>
              <div className="stat-label">Day Streak</div>
            </div>
          </div>
          <div className="stat-badge">
            <span className="stat-icon">⭐</span>
            <div>
              <div className="stat-value">Level {stats.level}</div>
              <div className="stat-label">Your Level</div>
            </div>
          </div>
          <div className="stat-badge">
            <span className="stat-icon">📚</span>
            <div>
              <div className="stat-value">{stats.totalCoursesEnrolled}</div>
              <div className="stat-label">Courses</div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Grid Layout */}
      <div className="dashboard-grid">
        {/* Left Column - Current Focus */}
        <div className="dashboard-left">
          <CurrentFocus course={currentCourse} />
        </div>

        {/* Right Column - Progress Overview */}
        <div className="dashboard-right">
          <ProgressOverview stats={stats} />
        </div>
      </div>

      {/* Recommendations Section */}
      <div className="dashboard-section">
        <Recommendations userId={user?.userId} />
      </div>

      {/* Activity Feed */}
      <div className="dashboard-section">
        <ActivityFeed userId={user?.userId} />
      </div>

      {/* My Courses */}
      <div className="dashboard-section">
        <MyCourses courses={enrolledCourses} />
      </div>
    </div>
  );
}