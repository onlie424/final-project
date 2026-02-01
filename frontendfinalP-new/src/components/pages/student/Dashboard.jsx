import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { courseService } from '../../../services/courseService';
import { enrollmentService } from '../../../services/enrollmentService';
import '../../../styles/Dashboard.css';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [enrolledCourses, setEnrolledCourses] = useState([]);
  const [allCourses, setAllCourses] = useState([]);
  const [currentCourse, setCurrentCourse] = useState(null);
  const [stats, setStats] = useState({
    totalCoursesEnrolled: 0,
    completedLessons: 0,
    totalLessons: 0,
    streak: 7,
  });

  useEffect(() => {
    fetchDashboardData();
  }, [user]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch all available courses
      const coursesData = await courseService.getAllCourses();
      setAllCourses(coursesData);

      // Fetch enrolled courses if user has userId
      if (user?.userId) {
        try {
          const enrollments = await enrollmentService.getUserEnrollments(user.userId);

          // Get course details for each enrollment
          const enrolledCoursesData = await Promise.all(
            enrollments.map(async (enrollment) => {
              try {
                const course = await courseService.getCourseById(enrollment.courseId, user.userId);
                return { ...course, enrollmentId: enrollment.id, progress: enrollment.progress || 0 };
              } catch {
                return null;
              }
            })
          );

          const validCourses = enrolledCoursesData.filter(c => c !== null);
          setEnrolledCourses(validCourses);

          if (validCourses.length > 0) {
            setCurrentCourse(validCourses[0]);
          }

          // Calculate stats from real data
          const totalLessons = validCourses.reduce((sum, c) => sum + (c.totalLessons || 0), 0);
          setStats({
            totalCoursesEnrolled: validCourses.length,
            completedLessons: 0, // This would come from progress tracking
            totalLessons: totalLessons,
            streak: 7,
          });
        } catch (enrollError) {
          console.log('No enrollments found or error:', enrollError);
          setEnrolledCourses([]);
        }
      }

    } catch (err) {
      console.error('Error fetching dashboard data:', err);
      setError('Failed to load dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  const handleBrowseCourses = () => {
    navigate('/enroll');
  };

  const handleCourseClick = (courseId) => {
    navigate(`/courses/${courseId}`);
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="spinner"></div>
        <p>Loading your dashboard...</p>
      </div>
    );
  }

  // Get recommended courses (courses not enrolled in)
  const enrolledIds = enrolledCourses.map(c => c.id);
  const recommendedCourses = allCourses.filter(c => !enrolledIds.includes(c.id)).slice(0, 4);

  return (
    <div className="dashboard-layout">
      {/* Top Navigation */}
      <nav className="dashboard-topnav">
        <div className="topnav-left">
          <a href="/" className="logo">BrainPath</a>
          <div className="search-bar">
            <span>🔍</span>
            <input type="text" placeholder="Search courses..." />
          </div>
        </div>
        <div className="topnav-right">
          <div className="user-info" onClick={handleLogout}>
            <div className="user-avatar">
              {getInitials(user?.fullName)}
            </div>
            <span className="user-name">{user?.fullName || 'Student'}</span>
            <span>▼</span>
          </div>
        </div>
      </nav>

      {/* Sidebar - Show only if user has enrolled courses */}
      {currentCourse && (
        <aside className="dashboard-sidebar">
          <div className="sidebar-header">
            <div className="course-icon">📚</div>
            <h2 className="course-title">{currentCourse.title}</h2>
            <div className="course-breadcrumb">
              <span>{currentCourse.category || 'COURSE'}</span>
              <span>›</span>
              <span>{currentCourse.difficulty || 'BEGINNER'}</span>
            </div>
          </div>

          <div className="lesson-navigator">
            <div className="current-lesson">
              <div className="current-lesson-label">
                {currentCourse.totalLessons || 0} Lessons
              </div>
              <div className="current-lesson-title">
                {currentCourse.estimatedHours || 0}h estimated
              </div>
            </div>
          </div>

          <div className="lesson-list">
            <div className="lesson-item active">
              <div className="lesson-icon video">▶</div>
              <div className="lesson-info">
                <p className="lesson-title">Continue Learning</p>
                <span className="lesson-meta">Start where you left off</span>
              </div>
            </div>
            <div className="lesson-item" onClick={() => handleCourseClick(currentCourse.id)}>
              <div className="lesson-icon article">📄</div>
              <div className="lesson-info">
                <p className="lesson-title">View Course Details</p>
                <span className="lesson-meta">See all modules and lessons</span>
              </div>
            </div>
          </div>
        </aside>
      )}

      {/* Main Content */}
      <main className="dashboard-main" style={!currentCourse ? { marginLeft: 0, maxWidth: '100%' } : {}}>
        {/* Error Message */}
        {error && (
          <div className="error-banner">
            <p>{error}</p>
            <button onClick={fetchDashboardData}>Retry</button>
          </div>
        )}

        {/* Welcome Section */}
        <div className="welcome-section">
          <div className="welcome-header">
            <div className="welcome-text">
              <h1>Welcome back, {user?.fullName || 'Student'}!</h1>
              <p>Continue your learning journey and achieve your goals</p>
            </div>
            <div className="streak-badge">
              <span>🔥</span>
              <span>{stats.streak} day streak</span>
            </div>
          </div>

          <div className="progress-cards">
            <div className="progress-card">
              <div className="progress-card-icon">📚</div>
              <div className="progress-card-value">{stats.totalCoursesEnrolled}</div>
              <div className="progress-card-label">Courses Enrolled</div>
            </div>
            <div className="progress-card">
              <div className="progress-card-icon">📖</div>
              <div className="progress-card-value">{stats.totalLessons}</div>
              <div className="progress-card-label">Total Lessons</div>
            </div>
            <div className="progress-card">
              <div className="progress-card-icon">⭐</div>
              <div className="progress-card-value">Level 2</div>
              <div className="progress-card-label">Your Level</div>
            </div>
          </div>
        </div>

        {/* My Courses Section */}
        <div className="content-section">
          <div className="section-header">
            <h2 className="section-title">My Enrolled Courses</h2>
            <span className="section-action" onClick={handleBrowseCourses}>
              Browse more courses →
            </span>
          </div>

          {enrolledCourses.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">📚</div>
              <h3>No Enrolled Courses Yet</h3>
              <p>Explore our course catalog and start learning today!</p>
              <button className="btn-primary" onClick={handleBrowseCourses}>
                Browse Courses
              </button>
            </div>
          ) : (
            <div className="course-cards">
              {enrolledCourses.map((course) => (
                <div
                  key={course.id}
                  className="course-card"
                  onClick={() => handleCourseClick(course.id)}
                >
                  <div className="course-card-image">
                    {course.thumbnailUrl ? (
                      <img src={course.thumbnailUrl} alt={course.title} />
                    ) : (
                      '📖'
                    )}
                  </div>
                  <div className="course-card-body">
                    <h3 className="course-card-title">{course.title}</h3>
                    <p className="course-card-meta">
                      {course.totalLessons || 0} lessons • {course.estimatedHours || 0}h • {course.difficulty || 'Beginner'}
                    </p>
                    <div className="course-progress-bar">
                      <div
                        className="course-progress-fill"
                        style={{ width: `${course.progress || 0}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recommended Courses Section */}
        {recommendedCourses.length > 0 && (
          <div className="content-section">
            <div className="section-header">
              <h2 className="section-title">Recommended for You</h2>
              <span className="section-action" onClick={handleBrowseCourses}>
                See all →
              </span>
            </div>

            <div className="course-cards">
              {recommendedCourses.map((course) => (
                <div
                  key={course.id}
                  className="course-card"
                  onClick={handleBrowseCourses}
                >
                  <div className="course-card-image" style={{
                    background: course.thumbnailUrl ? 'none' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                  }}>
                    {course.thumbnailUrl ? (
                      <img src={course.thumbnailUrl} alt={course.title} />
                    ) : (
                      '📚'
                    )}
                  </div>
                  <div className="course-card-body">
                    <h3 className="course-card-title">{course.title}</h3>
                    <p className="course-card-meta">
                      {course.totalLessons || 0} lessons • {course.estimatedHours || 0}h • {course.difficulty || 'Beginner'}
                    </p>
                    <div className="course-progress-bar">
                      <div className="course-progress-fill" style={{ width: '0%' }}></div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* All Available Courses */}
        {allCourses.length > 0 && enrolledCourses.length === 0 && (
          <div className="content-section">
            <div className="section-header">
              <h2 className="section-title">Available Courses</h2>
              <span className="section-action" onClick={handleBrowseCourses}>
                Enroll now →
              </span>
            </div>

            <div className="course-cards">
              {allCourses.slice(0, 4).map((course) => (
                <div
                  key={course.id}
                  className="course-card"
                  onClick={handleBrowseCourses}
                >
                  <div className="course-card-image" style={{
                    background: course.thumbnailUrl ? 'none' : 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
                  }}>
                    {course.thumbnailUrl ? (
                      <img src={course.thumbnailUrl} alt={course.title} />
                    ) : (
                      '📚'
                    )}
                  </div>
                  <div className="course-card-body">
                    <h3 className="course-card-title">{course.title}</h3>
                    <p className="course-card-meta">
                      {course.totalLessons || 0} lessons • {course.estimatedHours || 0}h • {course.difficulty || 'Beginner'}
                    </p>
                    <span className="enroll-badge">Click to Enroll</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
