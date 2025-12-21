import { useNavigate } from 'react-router-dom';
import { logout, getCurrentUser } from '../services/authService';
import '../styles/Dashboard.css';

function UserDashboard() {
  const navigate = useNavigate();
  const user = getCurrentUser();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <nav className="dashboard-nav">
        <h1>User Dashboard</h1>
        <button onClick={handleLogout} className="logout-button">
          Logout
        </button>
      </nav>
      
      <div className="dashboard-content">
        <div className="welcome-card">
          <h2>Welcome, {user?.username || 'User'}!</h2>
          <p>You have successfully logged in to your account.</p>
        </div>
        
        <div className="info-card">
          <h3>Your Features</h3>
          <ul>
            <li>View Profile</li>
            <li>Edit Settings</li>
            <li>View History</li>
            <li>Access Content</li>
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

export default UserDashboard;