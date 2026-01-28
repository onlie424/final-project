import { useNavigate } from 'react-router-dom';

import { logout, getCurrentUser } from '../../../services/authService';

import '../../../styles/admin/Dashboard.css';

function AdminDashboard() {
  const navigate = useNavigate();
  const user = getCurrentUser();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <nav className="dashboard-nav">
        <h1>Admin Dashboard</h1>
        <button onClick={handleLogout} className="logout-button">
          Logout
        </button>
      </nav>
      
      <div className="dashboard-content">
        <div className="welcome-card">
          <h2>Welcome Admin, {user?.username || 'Administrator'}!</h2>
          <p>You have full administrative access to the system.</p>
        </div>
        
        <div className="info-card">
          <h3>Admin Controls</h3>
          <ul>
            <li>Manage Users</li>
            <li>View Reports</li>
            <li>System Settings</li>
            <li>Analytics Dashboard</li>
          </ul>
        </div>

        <div className="info-card">
          <h3>Account Information</h3>
          <p><strong>fullName:</strong> {user?.fullName}</p>
          <p><strong>Email:</strong> {user?.email}</p>
          <p><strong>Role:</strong> {user?.role}</p>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;