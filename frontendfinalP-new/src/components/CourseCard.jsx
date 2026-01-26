import React from 'react';
import { Link } from 'react-router-dom';
import '../styles/CourseCard.css';

export default function CourseCard({ course }) {
  return (
    <Link to={`/courses/${course.id}`} className="course-card">
      <div className="course-card-image">
        <img 
          src={course.thumbnailUrl || 'https://via.placeholder.com/400x250'} 
          alt={course.title}
        />
        <span className={`difficulty-badge ${course.difficulty?.toLowerCase()}`}>
          {course.difficulty}
        </span>
      </div>

      <div className="course-card-content">
        <div className="course-card-category">{course.category}</div>
        
        <h3 className="course-card-title">{course.title}</h3>
        
        <p className="course-card-description">
          {course.description?.substring(0, 120)}
          {course.description?.length > 120 ? '...' : ''}
        </p>

        <div className="course-card-footer">
          <div className="course-card-info">
            <span>📚 {course.totalLessons || 0} lessons</span>
            <span>⏱️ {course.estimatedHours}h</span>
          </div>
        </div>
      </div>
    </Link>
  );
}