import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { logout, getCurrentUser } from '../../../services/authService';
//import CurrentFocus from './dashboard/CurrentFocus';
//import ProgressOverview from './dashboard/ProgressOverview';
// import PersonalizedRecommendations from './dashboard/PersonalizedRecommendations';
// import ActivityFeed from './dashboard/ActivityFeed';
import '../styles/Dashboard.css';

function UserDashboard() {
  const navigate = useNavigate();
  const user = getCurrentUser();
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const userId = user?.id || '1';
      
      const response = await fetch(`http://localhost:8080/api/dashboard/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch dashboard data: ${response.status}`);
      }

      const data = await response.json();
      setDashboardData(data);
      setLoading(false);
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
      setError(err.message);
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleRefresh = () => {
    setLoading(true);
    setError(null);
    fetchDashboardData();
  };

  if (loading) {
    return (
      <div className="dashboard-container">
        <nav className="dashboard-nav">
          <div className="nav-left">
            <h1>Learning Dashboard</h1>
          </div>
          <div className="nav-right">
            <span className="user-greeting">Hello, {user?.username || 'User'}</span>
            <button onClick={handleLogout} className="logout-button">
              Logout
            </button>
          </div>
        </nav>
        <div className="dashboard-loading">
          <div className="spinner"></div>
          <p>Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-container">
        <nav className="dashboard-nav">
          <div className="nav-left">
            <h1>Learning Dashboard</h1>
          </div>
          <div className="nav-right">
            <span className="user-greeting">Hello, {user?.username || 'User'}</span>
            <button onClick={handleLogout} className="logout-button">
              Logout
            </button>
          </div>
        </nav>
        <div className="dashboard-error">
          <h2>Unable to Load Dashboard</h2>
          <p>{error}</p>
          <button onClick={handleRefresh} className="retry-btn">
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <nav className="dashboard-nav">
        <div className="nav-left">
          <h1>Learning Dashboard</h1>
        </div>
        <div className="nav-right">
          <button onClick={handleRefresh} className="refresh-btn" title="Refresh dashboard">
            🔄
          </button>
          <div className="user-info">
            <span className="user-greeting">{user?.fullName || user?.username || 'User'}</span>
            <span className="user-role">{user?.role || 'Student'}</span>
          </div>
          <button onClick={handleLogout} className="logout-button">
            Logout
          </button>
        </div>
      </nav>

      <div className="dashboard-main">
        <div className="welcome-section">
          <h2>Welcome back, {user?.fullName || user?.username}! 👋</h2>
          <p>Here's your learning progress overview</p>
        </div>

        <div className="dashboard-grid">
          <div className="dashboard-left">
            <CurrentFocus data={dashboardData?.currentFocus} />
            <PersonalizedRecommendations data={dashboardData?.recommendations} />
          </div>

          <div className="dashboard-right">
            <ProgressOverview data={dashboardData?.progressOverview} />
            <ActivityFeed activities={dashboardData?.activities} />
          </div>
        </div>
      </div>
    </div>
  );
}

export default UserDashboard;