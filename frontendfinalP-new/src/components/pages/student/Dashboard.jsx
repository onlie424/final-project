import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { courseService } from '../../../services/courseService';
import { enrollmentService } from '../../../services/enrollmentService';
import { quizService } from '../../../services/quizService';
import CurrentFocus from '../../dashboard/CurrentFocus';
import ProgressOverview from '../../dashboard/ProgressOverview';
import ActivityFeed from '../../dashboard/ActivityFeed';
import Recommendations from '../../dashboard/Recommendations';
import { getInitials } from '../../../utils/helpers';
import '../../../styles/Dashboard.css';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [enrolledCourses, setEnrolledCourses] = useState([]);
  const [allCourses, setAllCourses] = useState([]);
  const [enrollingCourseId, setEnrollingCourseId] = useState(null);
  const [dashboardData, setDashboardData] = useState({
    overallProgress: 0,
    coursesCompleted: 0,
    focusCourse: null,
    activities: [],
    masteryGaps: [],
    suggestions: [],
  });

  useEffect(() => {
    fetchDashboardData();
  }, [user]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [allCoursesData, enrollments] = await Promise.all([
        courseService.getAllCourses(),
        user?.userId ? enrollmentService.getUserEnrollments(user.userId) : Promise.resolve([]),
      ]);

      setAllCourses(allCoursesData || []);

      if (!enrollments?.length) {
        setEnrolledCourses([]);
        setLoading(false);
        return;
      }

      const courseDetails = await Promise.all(
        enrollments.map(async (enrollment) => {
          try {
            const course = await courseService.getCourseById(enrollment.courseId, user?.userId);
            const progress = enrollment.completionPercentage ?? enrollment.progress ?? 0;
            return {
              ...course,
              enrollmentId: enrollment.id,
              completionPercentage: Math.round(progress),
              status: enrollment.status || 'IN_PROGRESS',
              lastAccessed: enrollment.lastAccessed || null,
            };
          } catch {
            return null;
          }
        })
      );

      const valid = courseDetails.filter(Boolean);
      setEnrolledCourses(valid);

      // Fetch real recommendation data for all enrolled courses in parallel
      const recResults = valid.length > 0
        ? await Promise.allSettled(
            valid.map((c) => quizService.getRecommendations(c.id, user.userId).catch(() => null))
          )
        : [];

      computeDashboardData(valid, recResults);
    } catch (err) {
      console.error('Error fetching dashboard:', err);
      setError('Failed to load dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  const computeDashboardData = (courses, recResults = []) => {
    if (!courses.length) return;

    const totalProgress = courses.reduce((s, c) => s + (c.completionPercentage || 0), 0);
    const overallProgress = Math.round(totalProgress / courses.length);
    const coursesCompleted = courses.filter(
      (c) => c.status === 'COMPLETED' || c.completionPercentage >= 100
    ).length;

    // Pick the most recently accessed non-completed course as the current focus
    const incomplete = courses.filter(
      (c) => c.status !== 'COMPLETED' && c.completionPercentage < 100
    );
    const withAccess = incomplete.filter((c) => c.lastAccessed != null);
    const focusCourse =
      withAccess.length > 0
        ? withAccess.reduce((latest, c) =>
            new Date(c.lastAccessed) > new Date(latest.lastAccessed) ? c : latest
          )
        : incomplete[0] ?? null;

    const activities = courses.map((course) => {
      if (course.completionPercentage >= 100 || course.status === 'COMPLETED') {
        return {
          id: `done-${course.id}`,
          icon: '🏆',
          title: 'Course Completed',
          description: course.title,
          time: 'Done',
          priority: 'low',
          courseId: course.id,
        };
      }
      if (course.completionPercentage > 0) {
        return {
          id: `prog-${course.id}`,
          icon: '📖',
          title: 'In Progress',
          description: `${course.title} — ${course.completionPercentage}% complete`,
          time: `${course.completionPercentage}%`,
          priority: 'medium',
          courseId: course.id,
        };
      }
      return {
        id: `new-${course.id}`,
        icon: '🚀',
        title: 'Ready to Start',
        description: course.title,
        time: 'New',
        priority: 'high',
        courseId: course.id,
      };
    });

    activities.sort((a, b) => {
      const order = { high: 0, medium: 1, low: 2 };
      return order[a.priority] - order[b.priority];
    });

    // Real recommendation data from quiz attempt history
    const masteryGaps = recResults
      .filter((r) => r.status === 'fulfilled' && r.value)
      .flatMap((r) => r.value.weakLessons || [])
      .sort((a, b) => b.failCount - a.failCount)
      .slice(0, 4)
      .map((w) => ({
        id: w.lessonId,
        topic: w.lessonTitle,
        moduleLabel: w.moduleTitle,
        mastery: w.severity === 'STRONG_REVIEW' ? 15 : w.severity === 'REVIEW' ? 40 : 65,
        status: w.severity === 'STRONG_REVIEW' ? 'needs-review' : 'practice-recommended',
        severity: w.severity,
        quizPassed: w.quizPassed,
        courseId: w.courseId,
      }));

    const suggestions = recResults
      .filter((r) => r.status === 'fulfilled' && r.value?.nextStep)
      .slice(0, 2)
      .map((r) => {
        const ns = r.value.nextStep;
        return {
          id: `next-${ns.moduleId}`,
          text: ns.firstLessonTitle
            ? `Next: "${ns.firstLessonTitle}" in ${ns.moduleTitle}`
            : `Start module: ${ns.moduleTitle}`,
          icon: '▶️',
          courseId: ns.courseId,
        };
      });

    setDashboardData({ overallProgress, coursesCompleted, focusCourse, activities, masteryGaps, suggestions });
  };

  const handleEnrollCourse = async (courseId) => {
    if (!user?.userId) return;
    try {
      setEnrollingCourseId(courseId);
      setError(null);
      await enrollmentService.enrollInCourse(user.userId, courseId);
      await fetchDashboardData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to enroll.');
    } finally {
      setEnrollingCourseId(null);
    }
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="spinner" />
        <p>Loading your dashboard...</p>
      </div>
    );
  }

  const enrolledIds = enrolledCourses.map((c) => c.id);
  const recommendedCourses = allCourses.filter((c) => !enrolledIds.includes(c.id)).slice(0, 4);
  const { overallProgress, coursesCompleted, focusCourse, activities, masteryGaps, suggestions } =
    dashboardData;

  return (
    <div className="dashboard-layout">
      {/* Top Navigation */}
      <nav className="dashboard-topnav">
        <div className="topnav-left">
          <a href="/" className="logo">BrainPath</a>
        </div>
        <div className="topnav-right">
          <div className="user-info" onClick={() => { logout(); navigate('/'); }}>
            <div className="user-avatar">{getInitials(user?.fullName)}</div>
            <span className="user-name">{user?.fullName || 'Student'}</span>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" style={{ opacity: 0.5 }}>
              <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z" />
            </svg>
          </div>
        </div>
      </nav>

      {/* Sidebar */}
      {enrolledCourses.length > 0 && (
        <aside className="dashboard-sidebar">
          <div className="sidebar-header">
            <h2 className="sidebar-title">My Courses</h2>
            <span className="sidebar-count">{enrolledCourses.length}</span>
          </div>
          <div className="sidebar-courses-list">
            {enrolledCourses.map((course) => (
              <div key={course.id} className="sidebar-course-item">
                <div
                  className="sidebar-course-info"
                  onClick={() => navigate(`/courses/${course.id}`)}
                >
                  <div className="sidebar-course-icon">📚</div>
                  <div className="sidebar-course-text">
                    <p className="sidebar-course-name">{course.title}</p>
                    <span className="sidebar-course-meta">{course.completionPercentage}% complete</span>
                  </div>
                </div>
                <div className="sidebar-mini-progress">
                  <div
                    className="sidebar-mini-fill"
                    style={{ width: `${course.completionPercentage || 0}%` }}
                  />
                </div>
                <div className="sidebar-course-actions">
                  <button
                    className="sidebar-btn-learn"
                    onClick={() => navigate(`/classroom/${course.id}`)}
                  >
                    {course.completionPercentage > 0 ? 'Continue' : 'Start'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </aside>
      )}

      {/* Main Content */}
      <main
        className="dashboard-main"
        style={enrolledCourses.length === 0 ? { marginLeft: 0, maxWidth: '100%' } : {}}
      >
        {error && (
          <div className="error-banner">
            <p>{error}</p>
            <button onClick={fetchDashboardData}>Retry</button>
          </div>
        )}

        {/* Welcome + Stats */}
        <div className="db-welcome">
          <div className="db-welcome-left">
            <h1>Welcome back, {user?.fullName?.split(' ')[0] || 'Student'}!</h1>
            <p>Track your progress and keep the momentum going</p>
          </div>
          <div className="db-stats-row">
            <div className="db-stat-card">
              <div className="db-stat-icon">📚</div>
              <div className="db-stat-val">{enrolledCourses.length}</div>
              <div className="db-stat-lbl">Enrolled</div>
            </div>
            <div className="db-stat-card">
              <div className="db-stat-icon">🎓</div>
              <div className="db-stat-val">{coursesCompleted}</div>
              <div className="db-stat-lbl">Completed</div>
            </div>
            <div className="db-stat-card">
              <div className="db-stat-icon">📊</div>
              <div className="db-stat-val">{overallProgress}%</div>
              <div className="db-stat-lbl">Avg Progress</div>
            </div>
          </div>
        </div>

        {/* Progress Overview + Current Focus */}
        {enrolledCourses.length > 0 && (
          <div className="db-two-col">
            <div className="db-card">
              <ProgressOverview
                overallProgress={overallProgress}
                coursesCompleted={coursesCompleted}
                totalCourses={enrolledCourses.length}
                enrolledCourses={enrolledCourses}
              />
            </div>
            <div className="db-card">
              <CurrentFocus
                course={focusCourse}
                progress={focusCourse?.completionPercentage || 0}
              />
            </div>
          </div>
        )}

        {/* Activity Feed + Recommendations */}
        {enrolledCourses.length > 0 && (
          <div className="db-two-col db-feed-row">
            <div className="db-card db-feed-wide">
              <ActivityFeed
                activities={activities}
                onNavigate={(courseId) => navigate(`/classroom/${courseId}`)}
              />
            </div>
            <div className="db-card db-rec-narrow">
              <Recommendations
                masteryGaps={masteryGaps}
                suggestions={suggestions}
                onNavigate={(courseId) => navigate(`/classroom/${courseId}`)}
              />
            </div>
          </div>
        )}

        {/* My Enrolled Courses */}
        {enrolledCourses.length > 0 && (
          <div className="db-card db-section">
            <div className="db-section-header">
              <h2>My Enrolled Courses</h2>
            </div>
            <div className="db-course-grid">
              {enrolledCourses.map((course) => (
                <div key={course.id} className="db-course-card">
                  <div
                    className="db-course-thumb"
                    onClick={() => navigate(`/courses/${course.id}`)}
                  >
                    {course.thumbnailUrl ? (
                      <img src={course.thumbnailUrl} alt={course.title} />
                    ) : (
                      <span>📖</span>
                    )}
                    {(course.completionPercentage >= 100 || course.status === 'COMPLETED') && (
                      <div className="db-completed-badge">✓</div>
                    )}
                  </div>
                  <div className="db-course-body">
                    <h3 onClick={() => navigate(`/courses/${course.id}`)}>{course.title}</h3>
                    <p className="db-course-meta">
                      {course.totalLessons || 0} lessons · {course.difficulty || 'Beginner'}
                    </p>
                    <div className="db-progress-bar">
                      <div
                        className="db-progress-fill"
                        style={{ width: `${course.completionPercentage || 0}%` }}
                      />
                    </div>
                    <div className="db-course-footer">
                      <span className="db-pct-label">{course.completionPercentage || 0}% complete</span>
                      <button onClick={() => navigate(`/classroom/${course.id}`)}>
                        {course.completionPercentage > 0 ? 'Continue →' : 'Start →'}
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Recommended Courses */}
        {recommendedCourses.length > 0 && (
          <div className="db-card db-section">
            <div className="db-section-header">
              <h2>Recommended for You</h2>
              <p>Expand your knowledge</p>
            </div>
            <div className="db-course-grid">
              {recommendedCourses.map((course) => (
                <div key={course.id} className="db-course-card">
                  <div
                    className="db-course-thumb"
                    onClick={() => navigate(`/courses/${course.id}`)}
                  >
                    {course.thumbnailUrl ? (
                      <img src={course.thumbnailUrl} alt={course.title} />
                    ) : (
                      <span>📚</span>
                    )}
                  </div>
                  <div className="db-course-body">
                    <h3 onClick={() => navigate(`/courses/${course.id}`)}>{course.title}</h3>
                    <p className="db-course-meta">
                      {course.totalLessons || 0} lessons · {course.difficulty || 'Beginner'}
                    </p>
                    <button
                      className="db-enroll-btn"
                      onClick={() => handleEnrollCourse(course.id)}
                      disabled={enrollingCourseId === course.id}
                    >
                      {enrollingCourseId === course.id ? 'Enrolling...' : 'Enroll Now'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Empty state */}
        {enrolledCourses.length === 0 && allCourses.length > 0 && (
          <div className="db-card db-section">
            <div className="db-section-header">
              <h2>Available Courses</h2>
              <p>Start your learning journey today</p>
            </div>
            <div className="db-course-grid">
              {allCourses.slice(0, 6).map((course) => (
                <div key={course.id} className="db-course-card">
                  <div
                    className="db-course-thumb"
                    onClick={() => navigate(`/courses/${course.id}`)}
                  >
                    {course.thumbnailUrl ? (
                      <img src={course.thumbnailUrl} alt={course.title} />
                    ) : (
                      <span>📚</span>
                    )}
                  </div>
                  <div className="db-course-body">
                    <h3 onClick={() => navigate(`/courses/${course.id}`)}>{course.title}</h3>
                    <p className="db-course-meta">
                      {course.totalLessons || 0} lessons · {course.difficulty || 'Beginner'}
                    </p>
                    <button
                      className="db-enroll-btn"
                      onClick={() => handleEnrollCourse(course.id)}
                      disabled={enrollingCourseId === course.id}
                    >
                      {enrollingCourseId === course.id ? 'Enrolling...' : 'Enroll Now'}
                    </button>
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
