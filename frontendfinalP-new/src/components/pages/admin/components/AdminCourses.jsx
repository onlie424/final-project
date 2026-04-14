import { useNavigate } from 'react-router-dom';
import { truncateText } from '../../../../utils/helpers';
import '../../../../styles/admin/AdminCourses.css';

function AdminCourses({ courses, onPublish, onUnpublish, onDelete }) {
  const navigate = useNavigate();

  return (
    <div className="admin-card full-width">
      <div className="card-header">
        <h2>All Courses ({courses.length})</h2>
        <button className="btn-primary-small" onClick={() => navigate('/admin/courses/create')}>
          + Add Course
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
                <th>Difficulty</th>
                
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {courses.map(course => (
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
                      <div className="course-info">
                        <span className="course-name">{course.title}</span>
                        <span className="course-desc">
                          {truncateText(course.description, 50)}
                        </span>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className="category-badge">{course.category || 'Uncategorized'}</span>
                  </td>
                  <td>
                    <span className={`difficulty-badge ${course.difficulty?.toLowerCase()}`}>
                      {course.difficulty || 'N/A'}
                    </span>
                  </td>
                  
                  <td>
                    <span className={`status-badge ${course.isPublished ? 'published' : 'draft'}`}>
                      {course.isPublished ? 'Published' : 'Draft'}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="btn-action edit"
                        onClick={() => navigate(`/admin/courses/${course.id}/edit`)}
                      >
                        Edit
                      </button>
                      <button
                        className="btn-action quizzes"
                        onClick={() => navigate(`/admin/courses/${course.id}/quizzes`)}
                      >
                        Quizzes
                      </button>
                      {course.isPublished ? (
                        <button
                          className="btn-action unpublish"
                          onClick={() => onUnpublish(course.id)}
                        >
                          Unpublish
                        </button>
                      ) : (
                        <button
                          className="btn-action publish"
                          onClick={() => onPublish(course.id)}
                        >
                          Publish
                        </button>
                      )}
                      <button
                        className="btn-action delete"
                        onClick={() => onDelete(course.id)}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default AdminCourses;
