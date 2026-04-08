import DonutChart from './DonutChart';
import '../../styles/dashboard/ProgressOverview.css';

export default function ProgressOverview({
  overallProgress = 0,
  coursesCompleted = 0,
  totalCourses = 0,
  enrolledCourses = [],
}) {
  const getGrade = (pct) => {
    if (pct >= 90) return 'A';
    if (pct >= 75) return 'B';
    if (pct >= 60) return 'C';
    if (pct >= 40) return 'D';
    return 'F';
  };

  // Mastery = average completion only across courses the user has actually started
  // (courses at 0% are excluded so an unattempted enrolment doesn't drag the grade down)
  const activeCourses = enrolledCourses.filter((c) => c.completionPercentage > 0);
  const masteryPct =
    activeCourses.length > 0
      ? Math.round(
          activeCourses.reduce((s, c) => s + c.completionPercentage, 0) / activeCourses.length
        )
      : 0;

  const grade = activeCourses.length > 0 ? getGrade(masteryPct) : '—';

  return (
    <div className="progress-overview">
      <h2 className="po-title">Progress Overview</h2>

      <div className="po-chart-row">
        <div className="po-donut-wrap">
          <DonutChart percentage={masteryPct} size={160} strokeWidth={16} />
        </div>
        <div className="po-summary">
          <div className="po-grade-block">
            <div className="po-grade-label">Mastery</div>
            <div className="po-grade-value">{grade}</div>
          </div>
          <div className="po-stat-mini">
            <span className="po-stat-num">{coursesCompleted}</span>
            <span className="po-stat-lbl">of {totalCourses} completed</span>
          </div>
          <div className="po-stat-mini">
            <span className="po-stat-num">{masteryPct}%</span>
            <span className="po-stat-lbl">active course avg</span>
          </div>
        </div>
      </div>

      {enrolledCourses.length > 0 && (
        <div className="po-courses">
          <h3 className="po-courses-title">Course Breakdown</h3>
          <div className="po-courses-list">
            {enrolledCourses.slice(0, 4).map((course) => (
              <div key={course.id} className="po-course-row">
                <div className="po-course-name">{course.title}</div>
                <div className="po-course-bar-wrap">
                  <div
                    className="po-course-bar-fill"
                    style={{ width: `${course.completionPercentage || 0}%` }}
                  />
                </div>
                <span className="po-course-pct">{course.completionPercentage || 0}%</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
