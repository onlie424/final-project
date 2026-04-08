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

  const handleStartLearning = () => {
    navigate(`/classroom/${courseId}`);
  };

  if (loading) {
    return (
      <div className="course-detail-loading">
        <div className="spinner"></div>
        <p>Loading course details...</p>
      </div>
    );
  }

  if (error && !course) {
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

  const totalLessons = course.modules
    ? course.modules.reduce((sum, m) => sum + (m.lessons?.length || 0), 0)
    : (course.totalLessons || 0);

  return (
    <div className="course-detail-page">
      {/* Top Bar */}
      <header className="course-detail-topbar">
        <button className="back-btn" onClick={handleBack}>
          ← Back to Dashboard
        </button>
        <span className="topbar-title">Course Details</span>
      </header>

      {error && (
        <div className="course-detail-alert">
          <p>{error}</p>
        </div>
      )}

      <div className="course-detail-content">
        {/* Course Info Card */}
        <div className="course-info-card">
          <div className="course-info-main">
            <div className="course-badges">
              <span className="badge badge-category">{course.category || 'General'}</span>
              <span className={`badge badge-difficulty ${(course.difficulty || 'beginner').toLowerCase()}`}>
                {course.difficulty || 'Beginner'}
              </span>
            </div>

            <h1 className="course-detail-title">{course.title}</h1>
            <p className="course-detail-description">{course.description}</p>

            <div className="course-stats">
              <div className="stat-item">
                <span className="stat-value">{totalLessons}</span>
                <span className="stat-label">Lessons</span>
              </div>
              <div className="stat-divider"></div>
              <div className="stat-item">
                <span className="stat-value">{course.modules?.length || 0}</span>
                <span className="stat-label">Modules</span>
              </div>
              <div className="stat-divider"></div>
              <div className="stat-item">
                <span className="stat-value">{course.estimatedHours || 0}h</span>
                <span className="stat-label">Estimated</span>
              </div>
            </div>

            <div className="course-actions">
              {isEnrolled ? (
                <>
                  <span className="enrolled-status">Enrolled</span>
                  <button className="btn-start-learning" onClick={handleStartLearning}>
                    Go to Classroom
                  </button>
                </>
              ) : (
                <button
                  className="btn-enroll"
                  onClick={handleEnroll}
                  disabled={enrolling}
                >
                  {enrolling ? 'Enrolling...' : 'Enroll in this Course'}
                </button>
              )}
            </div>
          </div>

          {course.thumbnailUrl && (
            <div className="course-info-image">
              <img src={course.thumbnailUrl} alt={course.title} />
            </div>
          )}
        </div>

        {/* Modules & Lessons */}
        {course.modules && course.modules.length > 0 && (
          <div className="course-modules-section">
            <h2 className="section-heading">Course Content</h2>
            <p className="section-subheading">
              {course.modules.length} modules &middot; {totalLessons} lessons
            </p>

            <div className="modules-accordion">
              {course.modules.map((module, index) => (
                <div key={module.id || index} className="module-accordion-item">
                  <div className="module-accordion-header">
                    <div className="module-accordion-left">
                      <span className="module-index">{index + 1}</span>
                      <div>
                        <h3 className="module-acc-title">{module.title}</h3>
                        {module.description && (
                          <p className="module-acc-desc">{module.description}</p>
                        )}
                      </div>
                    </div>
                    <span className="module-lesson-count">
                      {module.lessons?.length || 0} lessons
                    </span>
                  </div>

                  {module.lessons && module.lessons.length > 0 && (
                    <div className="module-lessons-list">
                      {module.lessons.map((lesson, lIndex) => (
                        <div key={lesson.id || lIndex} className="lesson-list-item">
                          <span className="lesson-type-icon">
                            {'▶'}
                          </span>
                          <span className="lesson-list-title">{lesson.title}</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
