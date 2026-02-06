import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { courseService } from '../../../services/courseService';
import { enrollmentService } from '../../../services/enrollmentService';
import '../../../styles/CourseDetail.css';

export default function CourseDetail() {
  const { courseId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEnrolled, setIsEnrolled] = useState(false);
  const [enrolling, setEnrolling] = useState(false);

  useEffect(() => {
    fetchCourseDetails();
  }, [courseId, user]);

  const fetchCourseDetails = async () => {
    try {
      setLoading(true);
      setError(null);

      const courseData = await courseService.getCourseById(courseId, user?.userId);
      setCourse(courseData);

      // Check if user is enrolled
      if (user?.userId) {
        try {
          const enrollmentStatus = await enrollmentService.isEnrolled(user.userId, courseId);
          setIsEnrolled(enrollmentStatus.enrolled || enrollmentStatus);
        } catch {
          setIsEnrolled(false);
        }
      }
    } catch (err) {
      console.error('Error fetching course:', err);
      setError('Failed to load course details.');
    } finally {
      setLoading(false);
    }
  };

  const handleEnroll = async () => {
    if (!user?.userId) {
      setError('Please log in to enroll');
      return;
    }

    try {
      setEnrolling(true);
      await enrollmentService.enrollInCourse(user.userId, courseId);
      setIsEnrolled(true);
    } catch (err) {
      console.error('Error enrolling:', err);
      setError('Failed to enroll in course.');
    } finally {
      setEnrolling(false);
    }
  };

  const handleBack = () => {
    navigate('/user/dashboard');
  };

  if (loading) {
    return (
      <div className="course-detail-loading">
        <div className="spinner"></div>
        <p>Loading course details...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="course-detail-error">
        <p>{error}</p>
        <button onClick={handleBack}>Back to Dashboard</button>
      </div>
    );
  }

  if (!course) {
    return (
      <div className="course-detail-error">
        <p>Course not found.</p>
        <button onClick={handleBack}>Back to Dashboard</button>
      </div>
    );
  }

  return (
    <div className="course-detail-page">
      {/* Header */}
      <header className="course-detail-header">
        <button className="back-btn" onClick={handleBack}>
          Back to Dashboard
        </button>
      </header>

      {/* Course Hero Section */}
      <div className="course-hero">
        <div className="course-hero-content">
          <div className="course-category">{course.category || 'Course'}</div>
          <h1 className="course-title">{course.title}</h1>
          <p className="course-description">{course.description}</p>

          <div className="course-meta">
            <span className="meta-item">
              <strong>{course.totalLessons || 0}</strong> Lessons
            </span>
            <span className="meta-item">
              <strong>{course.estimatedHours || 0}</strong> Hours
            </span>
            <span className="meta-item">
              <strong>{course.difficulty || 'Beginner'}</strong>
            </span>
          </div>

          {isEnrolled ? (
            <div className="enrolled-badge">You are enrolled in this course</div>
          ) : (
            <button
              className="enroll-btn"
              onClick={handleEnroll}
              disabled={enrolling}
            >
              {enrolling ? 'Enrolling...' : 'Enroll Now'}
            </button>
          )}
        </div>

        <div className="course-hero-image">
          {course.thumbnailUrl ? (
            <img src={course.thumbnailUrl} alt={course.title} />
          ) : (
            <div className="placeholder-image">Course</div>
          )}
        </div>
      </div>

      {/* Course Content */}
      <div className="course-content">
        {/* About Section */}
        <section className="content-section">
          <h2>About this Course</h2>
          <p>{course.description || 'No description available.'}</p>
        </section>

        {/* What You'll Learn */}
        {course.learningObjectives && course.learningObjectives.length > 0 && (
          <section className="content-section">
            <h2>What You'll Learn</h2>
            <ul className="objectives-list">
              {course.learningObjectives.map((objective, index) => (
                <li key={index}>{objective}</li>
              ))}
            </ul>
          </section>
        )}

        {/* Modules Section */}
        {course.modules && course.modules.length > 0 && (
          <section className="content-section">
            <h2>Course Modules</h2>
            <div className="modules-list">
              {course.modules.map((module, index) => (
                <div key={module.id || index} className="module-card">
                  <div className="module-header">
                    <span className="module-number">Module {index + 1}</span>
                    <h3 className="module-title">{module.title}</h3>
                  </div>
                  <p className="module-description">{module.description}</p>
                  {module.lessons && (
                    <div className="module-lessons">
                      <span>{module.lessons.length} lessons</span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Instructor Section */}
        {course.instructor && (
          <section className="content-section">
            <h2>Instructor</h2>
            <div className="instructor-card">
              <div className="instructor-avatar">
                {course.instructor.name?.charAt(0) || 'I'}
              </div>
              <div className="instructor-info">
                <h3>{course.instructor.name}</h3>
                <p>{course.instructor.bio}</p>
              </div>
            </div>
          </section>
        )}
      </div>
    </div>
  );
}
