import { useNavigate } from 'react-router-dom';
import '../../../../styles/admin/AdminOverview.css';

function AdminOverview({ stats, courses, setActiveTab, onRefresh }) {
  const navigate = useNavigate();

  return (
    <>
      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon courses">📚</div>
          <div className="stat-info">
            <h3>{stats.totalCourses}</h3>
            <p>Total Courses</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon published">✅</div>
          <div className="stat-info">
            <h3>{stats.publishedCourses}</h3>
            <p>Published</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon draft">📝</div>
          <div className="stat-info">
            <h3>{stats.draftCourses}</h3>
            <p>Drafts</p>
          </div>
        </div>
        
        <div className="stat-card">
          <div className="stat-icon users">👥</div>
          <div className="stat-info">
            <h3>{stats.totalUsers}</h3>
            <p>Total Users</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon admins">🛡️</div>
          <div className="stat-info">
            <h3>{stats.adminCount}</h3>
            <p>Admins</p>
          </div>
        </div>
      </div>

      {/* Recent Courses */}
      <div className="admin-card full-width">
        <div className="card-header">
          <h2>Recent Courses</h2>
          <button className="btn-primary-small" onClick={() => setActiveTab('courses')}>
            View All
          </button>
        </div>
        {courses.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📚</div>
            <h3>No Courses Yet</h3>
            <p>Create your first course to get started!</p>
            <button className="btn-primary" onClick={() => navigate('/admin/courses/create')}>
              Create Course
            </button>
          </div>
        ) : (
          <div className="table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Course Name</th>
                  <th>Category</th>
                  <th>Status</th>

                </tr>
              </thead>
              <tbody>
                {courses.slice(0, 5).map(course => (
                  <tr key={course.id}>
                    <td>
                      <div className="course-cell">
                        <div className="course-thumbnail">
                          {course.thumbnailUrl ? (
                            <img src={course.thumbnailUrl} alt={course.title} />
                          ) : (
                            <span>📖</span>
                          )}
                        </div>
                        <span className="course-name">{course.title}</span>
                      </div>
                    </td>
                    <td>
                      <span className="category-badge">{course.category || 'Uncategorized'}</span>
                    </td>
                    <td>
                      <span className={`status-badge ${course.isPublished ? 'published' : 'draft'}`}>
                        {course.isPublished ? 'Published' : 'Draft'}
                      </span>
                    </td>
                    
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      
    </>
  );
}

export default AdminOverview;
