import React from 'react';
import '../../styles/dashboard/ActivityFeed.css';

export default function ActivityFeed({ userId }) {
  // Mock activities - you'll fetch real data from backend
  const activities = [
    {
      id: 1,
      type: 'quiz-due',
      icon: '📌',
      title: 'Quiz Due: Python Basics',
      description: 'Tomorrow, 2:00 PM',
      time: 'Due soon',
      priority: 'high',
    },
    {
      id: 2,
      type: 'new-lesson',
      icon: '📺',
      title: 'New Lesson Available',
      description: 'Object-Oriented Programming',
      time: '2 hours ago',
      priority: 'medium',
    },
    {
      id: 3,
      type: 'achievement',
      icon: '🏆',
      title: 'Achievement Unlocked',
      description: '7-Day Streak! Keep it up!',
      time: 'Today',
      priority: 'low',
    },
    {
      id: 4,
      type: 'lesson-complete',
      icon: '✅',
      title: 'Lesson Completed',
      description: 'Functions and Parameters',
      time: '1 day ago',
      priority: 'low',
    },
    {
      id: 5,
      type: 'recommendation',
      icon: '💡',
      title: 'Recommended for You',
      description: 'Advanced Functions module based on your progress',
      time: '2 days ago',
      priority: 'medium',
    },
  ];

  const getPriorityClass = (priority) => {
    switch (priority) {
      case 'high':
        return 'priority-high';
      case 'medium':
        return 'priority-medium';
      default:
        return 'priority-low';
    }
  };

  return (
    <div className="activity-feed">
      <div className="feed-header">
        <h2 className="section-title">📅 Activity Feed & Upcoming</h2>
        <button className="btn-view-all">View All</button>
      </div>

      <div className="activities-list">
        {activities.map((activity) => (
          <div 
            key={activity.id} 
            className={`activity-item ${getPriorityClass(activity.priority)}`}
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
            </div>
          </div>
        ))}
      </div>

      {activities.length === 0 && (
        <div className="no-activities">
          <p>No recent activities</p>
          <span className="empty-icon">📭</span>
        </div>
      )}
    </div>
  );
}