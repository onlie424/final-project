import '../../styles/dashboard/Recommendation.css';

export default function Recommendations({ masteryGaps = [], suggestions = [], onNavigate }) {
  const getStatusLabel = (mastery) => {
    if (mastery < 25) return { label: 'Needs Review', cls: 'status-red' };
    if (mastery < 50) return { label: 'Needs Practice', cls: 'status-orange' };
    return { label: 'Making Progress', cls: 'status-green' };
  };

  const hasContent = masteryGaps.length > 0 || suggestions.length > 0;

  if (!hasContent) {
    return (
      <div className="recommendations">
        <h2 className="rec-title">Recommendations</h2>
        <div className="rec-empty">
          <span>✨</span>
          <p>Enroll in courses to get personalised recommendations.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="recommendations">
      <h2 className="rec-title">Recommendations</h2>

      {masteryGaps.length > 0 && (
        <div className="rec-section">
          <h3 className="rec-sub-title">Needs Attention</h3>
          <div className="rec-gaps-list">
            {masteryGaps.map((gap) => {
              const { label, cls } = getStatusLabel(gap.mastery);
              return (
                <div key={gap.id} className="rec-gap-item">
                  <div className="rec-gap-info">
                    <span className="rec-gap-name">{gap.topic}</span>
                    {gap.moduleLabel && (
                      <span className="rec-gap-module">{gap.moduleLabel}</span>
                    )}
                    <span className={`rec-gap-status ${cls}`}>
                      {gap.severity === 'STRONG_REVIEW' ? 'Strongly ' : ''}{label}
                    </span>
                  </div>
                  <div className="rec-gap-bar-wrap">
                    <div
                      className={`rec-gap-bar-fill ${gap.mastery < 25 ? 'fill-red' : 'fill-orange'}`}
                      style={{ width: `${gap.mastery}%` }}
                    />
                  </div>
                  <div className="rec-gap-footer">
                    <span className="rec-gap-pct">{gap.mastery}% progress</span>
                    {gap.courseId && (
                      <button
                        className="rec-btn-go"
                        onClick={() => onNavigate?.(gap.courseId)}
                      >
                        Go to Course →
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {suggestions.length > 0 && (
        <div className="rec-section">
          <h3 className="rec-sub-title">Suggested Next Steps</h3>
          <div className="rec-suggestions">
            {suggestions.map((s) => (
              <div
                key={s.id}
                className={`rec-suggestion-item ${s.courseId ? 'clickable' : ''}`}
                onClick={() => s.courseId && onNavigate?.(s.courseId)}
                role={s.courseId ? 'button' : undefined}
              >
                <span className="rec-sug-icon">{s.icon}</span>
                <span className="rec-sug-text">{s.text}</span>
                {s.courseId && (
                  <svg className="rec-sug-arrow" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z" />
                  </svg>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
