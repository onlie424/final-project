import React from 'react';
import { useNavigate } from 'react-router-dom';

import '../../styles/dashboard/CurrentFocus.css';

export default function CurrentFocus({ course }) {
  const navigate = useNavigate();

  if (!course) {
    return (
      <div className="current-focus">
        <h2 className="section-title">📚 Current Focus</h2>
        <div className="no-course">
          <p>You haven't enrolled in any courses yet.</p>
          <button 
            className="btn-primary"
            onClick={() => navigate('/courses')}
          >
            Browse Courses
          </button>
        </div>
      </div>
    );
  }

  // Calculate progress (mock for now - you'll implement this with real data)
  const currentModule = course.modules?.[0];
  const currentLesson = currentModule?.lessons?.[0];
  const progress = 65; // Mock progress
  const timeRemaining = "2 weeks";

  const handleContinue = () => {
    if (currentLesson) {
      navigate(`/lessons/${currentLesson.id}`);
    }
  };

  return (
    <div className="current-focus">
      <h2 className="section-title">📚 Current Focus</h2>
      
      <div className="focus-card">
        <div className="course-info">
          <div className="course-thumbnail">
            <img 
              src={course.thumbnailUrl || 'https://via.placeholder.com/100x60'} 
              alt={course.title}
            />
          </div>
          <div className="course-details">
            <h3 className="course-title">{course.title}</h3>
            <p className="course-subtitle">
              {currentModule?.title || 'Getting Started'}
            </p>
            <p className="lesson-info">
              {currentLesson?.title || 'First lesson'}
            </p>
          </div>
        </div>

        <div className="progress-section">
          <div className="progress-header">
            <span className="progress-label">Progress</span>
            <span className="progress-value">{progress}%</span>
          </div>
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: `${progress}%` }}
            ></div>
          </div>
          <p className="time-estimate">
            <span className="time-icon">⏱️</span>
            Est. completion: {timeRemaining}
          </p>
        </div>

        <button 
          className="btn-continue"
          onClick={handleContinue}
        >
          Continue Learning →
        </button>

        <div className="focus-meta">
          <div className="meta-item">
            <span className="meta-icon">📖</span>
            <span>{course.totalLessons || 0} lessons</span>
          </div>
          <div className="meta-item">
            <span className="meta-icon">⏰</span>
            <span>{course.estimatedHours || 0}h total</span>
          </div>
          <div className="meta-item">
            <span className="meta-icon">📊</span>
            <span className={`difficulty-badge ${course.difficulty?.toLowerCase()}`}>
              {course.difficulty}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}