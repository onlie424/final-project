import { getInitials } from '../../../../utils/helpers';
import '../../../../styles/admin/AdminUsers.css';

function AdminUsers({ users, stats, currentUserId, onUpdateRole, onDelete }) {
  return (
    <div className="admin-card full-width">
      <div className="card-header">
        <h2>All Users ({users.length})</h2>
        <div className="user-stats-mini">
          <span className="stat-mini">
            <span className="stat-label">Admins:</span> {stats.adminCount}
          </span>
          <span className="stat-mini">
            <span className="stat-label">Users:</span> {stats.userCount}
          </span>
        </div>
      </div>

      {users.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">👥</div>
          <h3>No Users Found</h3>
          <p>Users will appear here when they register.</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="admin-table">
            <thead>
              <tr>
                <th>User</th>
                <th>Email</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td>
                    <div className="user-cell">
                      <div className="user-avatar">
                        {getInitials(user.fullName)}
                      </div>
                      <span className="user-name">{user.fullName}</span>
                    </div>
                  </td>
                  <td>{user.email}</td>
                  <td>
                    <span className={`role-badge ${user.role?.toLowerCase()}`}>
                      {user.role}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      {user.role === 'USER' ? (
                        <button
                          className="btn-action promote"
                          onClick={() => onUpdateRole(user.id, 'ADMIN')}
                        >
                          Make Admin
                        </button>
                      ) : (
                        <button
                          className="btn-action demote"
                          onClick={() => onUpdateRole(user.id, 'USER')}
                          disabled={user.id === currentUserId}
                        >
                          Remove Admin
                        </button>
                      )}
                      <button
                        className="btn-action delete"
                        onClick={() => onDelete(user.id)}
                        disabled={user.id === currentUserId}
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

export default AdminUsers;
