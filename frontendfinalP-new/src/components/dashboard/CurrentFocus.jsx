import { useNavigate } from 'react-router-dom';
import '../../styles/dashboard/CurrentFocus.css';

export default function CurrentFocus({ course, progress = 0 }) {
  const navigate = useNavigate();

  if (!course) {
    return (
      <div className="current-focus">
        <h2 className="cf-title">Current Focus</h2>
        <div className="cf-empty">
          <span className="cf-empty-icon">🎯</span>
          <p>No course in progress yet.</p>
          <button className="cf-btn-browse" onClick={() => navigate('/courses')}>
            Browse Courses
          </button>
        </div>
      </div>
    );
  }

  const clampedProgress = Math.min(100, Math.max(0, Math.round(progress)));

  return (
    <div className="current-focus">
      <h2 className="cf-title">Current Focus</h2>

      <div className="cf-card">
        <div className="cf-thumb-row">
          <div className="cf-thumb">
            {course.thumbnailUrl ? (
              <img src={course.thumbnailUrl} alt={course.title} />
            ) : (
              <span className="cf-thumb-icon">📖</span>
            )}
            <div className="cf-pct-badge">{clampedProgress}%</div>
          </div>
          <div className="cf-details">
            <h3 className="cf-course-title">{course.title}</h3>
            {course.difficulty && (
              <span className={`cf-difficulty cf-diff-${course.difficulty?.toLowerCase()}`}>
                {course.difficulty}
              </span>
            )}
            <p className="cf-meta">
              {course.totalLessons ? `${course.totalLessons} lessons` : ''}
              {course.totalLessons && course.estimatedHours ? ' · ' : ''}
              {course.estimatedHours ? `${course.estimatedHours}h` : ''}
            </p>
          </div>
        </div>

        <div className="cf-progress-row">
          <div className="cf-progress-labels">
            <span>Progress</span>
            <span className="cf-progress-val">{clampedProgress}%</span>
          </div>
          <div className="cf-progress-track">
            <div className="cf-progress-fill" style={{ width: `${clampedProgress}%` }} />
          </div>
        </div>

        <button
          className="cf-btn-continue"
          onClick={() => navigate(`/classroom/${course.id}`)}
        >
          {clampedProgress > 0 ? 'Continue Learning →' : 'Start Learning →'}
        </button>
      </div>
    </div>
  );
}
