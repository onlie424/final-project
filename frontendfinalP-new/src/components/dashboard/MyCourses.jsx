import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/dashboard/MyCourses.css';

export default function MyCourses({ courses }) {
  const navigate = useNavigate();

  const handleCourseClick = (courseId) => {
    navigate(`/courses/${courseId}`);
  };

  const handleResumeCourse = (courseId, e) => {
    e.stopPropagation();
    navigate(`/courses/${courseId}`);
  };

  return (
    <div className="my-courses">
      <div className="courses-header">
        <h2 className="section-title">📚 My Courses</h2>
        <button
          className="btn-browse"
          onClick={() => navigate('/courses')}
        >
          + Browse More Courses
        </button>
      </div>

      {courses.length === 0 ? (
        <div className="no-courses">
          <span className="no-courses-icon">📚</span>
          <h3>No Courses Yet</h3>
          <p>Start your learning journey by enrolling in a course!</p>
          <button
            className="btn-primary"
            onClick={() => navigate('/courses')}
          >
            Explore Courses
          </button>
        </div>
      ) : (
        <div className="courses-grid">
          {courses.map((course) => {
            const progress = course.completionPercentage || 0;
            const isStarted = progress > 0;

            return (
              <div
                key={course.id}
                className="course-card-dashboard"
                onClick={() => handleCourseClick(course.id)}
              >
                <div className="course-image">
                  <img
                    src={course.thumbnailUrl || 'https://via.placeholder.com/400x200'}
                    alt={course.title}
                  />
                  <div className="course-overlay">
                    <button
                      className="btn-course-action"
                      onClick={(e) => handleResumeCourse(course.id, e)}
                    >
                      {isStarted ? 'Resume' : 'Start'} →
                    </button>
                  </div>
                </div>

                <div className="course-body">
                  <div className="course-category">{course.category}</div>
                  <h3 className="course-title">{course.title}</h3>

                  <div className="course-meta">
                    <span className="meta-badge">
                      <span className="meta-icon">📖</span>
                      {course.totalLessons || 0} lessons
                    </span>
                    <span className="meta-badge">
                      <span className="meta-icon">⏱️</span>
                      {course.estimatedHours || 0}h
                    </span>
                  </div>

                  <div className="course-progress">
                    <div className="progress-info">
                      <span className="progress-label">Progress</span>
                      <span className="progress-percentage">{progress}%</span>
                    </div>
                    <div className="progress-bar-track">
                      <div
                        className="progress-bar-fill"
                        style={{ width: `${progress}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
