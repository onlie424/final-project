import React from 'react';

import DonutChart from './DonutChart';

import '../../styles/dashboard/ProgressOverview.css';

export default function ProgressOverview({ stats }) {
  // Calculate overall progress (mock calculation)
  const overallProgress = 65; // You'll calculate this from real data
  
  // Calculate grade based on progress
  const getGrade = (progress) => {
    if (progress >= 95) return 'A+';
    if (progress >= 90) return 'A';
    if (progress >= 85) return 'B+';
    if (progress >= 80) return 'B';
    if (progress >= 75) return 'C+';
    if (progress >= 70) return 'C';
    return 'D';
  };

  const grade = getGrade(overallProgress);

  // Mock data for weekly study time
  const weeklyData = [
    { day: 'Mon', hours: 2 },
    { day: 'Tue', hours: 1.5 },
    { day: 'Wed', hours: 3 },
    { day: 'Thu', hours: 2.5 },
    { day: 'Fri', hours: 1 },
    { day: 'Sat', hours: 4 },
    { day: 'Sun', hours: 2 },
  ];

  const maxHours = Math.max(...weeklyData.map(d => d.hours));

  return (
    <div className="progress-overview">
      <h2 className="section-title">📊 Progress Overview</h2>

      {/* Donut Chart */}
      <div className="chart-container">
        <DonutChart percentage={overallProgress} size={200} strokeWidth={20} />
        <div className="grade-display">
          <div className="grade-label">Mastery Score</div>
          <div className="grade-value">{grade}</div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="stats-grid">
        <div className="stat-box">
          <div className="stat-icon">📚</div>
          <div className="stat-content">
            <div className="stat-number">{stats.completedLessons || 0}</div>
            <div className="stat-text">Lessons Completed</div>
          </div>
        </div>

        <div className="stat-box">
          <div className="stat-icon">⏰</div>
          <div className="stat-content">
            <div className="stat-number">12h 30m</div>
            <div className="stat-text">Time Spent</div>
          </div>
        </div>

        <div className="stat-box">
          <div className="stat-icon">🎯</div>
          <div className="stat-content">
            <div className="stat-number">85%</div>
            <div className="stat-text">Avg. Quiz Score</div>
          </div>
        </div>

        <div className="stat-box">
          <div className="stat-icon">📅</div>
          <div className="stat-content">
            <div className="stat-number">Nov 15</div>
            <div className="stat-text">Predicted Finish</div>
          </div>
        </div>
      </div>

      {/* Weekly Activity */}
      <div className="weekly-activity">
        <h3 className="activity-title">Weekly Study Time</h3>
        <div className="activity-chart">
          {weeklyData.map((day, index) => (
            <div key={index} className="activity-bar-container">
              <div className="activity-bar-wrapper">
                <div 
                  className="activity-bar"
                  style={{ 
                    height: `${(day.hours / maxHours) * 100}%`,
                    background: day.hours >= 2 ? '#48bb78' : '#ed8936'
                  }}
                ></div>
              </div>
              <div className="activity-label">{day.day}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}