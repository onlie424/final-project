import '../../styles/dashboard/ActivityFeed.css';

const PRIORITY_CLASS = { high: 'priority-high', medium: 'priority-medium', low: 'priority-low' };

export default function ActivityFeed({ activities = [], onNavigate }) {
  if (!activities.length) {
    return (
      <div className="activity-feed">
        <div className="feed-header">
          <h2 className="af-title">Activity & Status</h2>
        </div>
        <div className="no-activities">
          <span className="empty-icon">📭</span>
          <p>No activity yet — enroll in a course to get started!</p>
        </div>
      </div>
    );
  }

  return (
    <div className="activity-feed">
      <div className="feed-header">
        <h2 className="af-title">Activity & Status</h2>
        <span className="af-count">{activities.length} courses</span>
      </div>

      <div className="activities-list">
        {activities.map((activity) => (
          <div
            key={activity.id}
            className={`activity-item ${PRIORITY_CLASS[activity.priority] || 'priority-low'} ${activity.courseId ? 'clickable' : ''}`}
            onClick={() => activity.courseId && onNavigate?.(activity.courseId)}
            role={activity.courseId ? 'button' : undefined}
          >
            <div className="activity-icon-wrapper">
              <span className="activity-icon">{activity.icon}</span>
            </div>
            <div className="activity-content">
              <h4 className="activity-title">{activity.title}</h4>
              <p className="activity-description">{activity.description}</p>
            </div>
            <div className="activity-time">
              <span className="time-text">{activity.time}</span>
              {activity.courseId && (
                <svg className="af-arrow" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z" />
                </svg>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
