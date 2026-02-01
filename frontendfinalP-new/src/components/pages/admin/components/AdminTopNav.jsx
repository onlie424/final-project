import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../../context/AuthContext';
import { getInitials } from '../../../../utils/helpers';
import '../../../../styles/admin/AdminTopNav.css';

function AdminTopNav() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="admin-topnav">
      <div className="topnav-left">
        <a href="/" className="admin-logo">
          <span className="logo-icon">🎓</span>
          <span className="logo-text">BrainPath Admin</span>
        </a>
      </div>
      <div className="topnav-right">
        <div className="admin-user-info">
          <div className="admin-avatar">
            {getInitials(user?.fullName)}
          </div>
          <div className="admin-user-details">
            <span className="admin-user-name">{user?.fullName || 'Administrator'}</span>
            <span className="admin-user-role">Administrator</span>
          </div>
          <button className="btn-logout" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}

export default AdminTopNav;
