import { useNavigate } from 'react-router-dom';

import logo from '../images/logo.jpg';

import '../styles/Welcome.css';

function WelcomePage() {
  const navigate = useNavigate();

  const handleLogin = () => {
    navigate('/login');
  };

  const handleRegister = () => {
    navigate('/register');
  };

  return (
    <div className="welcome-container">
      <div className="welcome-content">
        <div className="logo-container">
        <img src={logo} alt="Logo" className="logo" />
          
        </div>

        <h1 className="welcome-title">Welcome to Brainpath</h1>
        <p className="welcome-subtitle">
        Your journey to customized learning starts here. Please log in or register to access your courses and personalized dashboard.
        </p>

        <div className="welcome-buttons">
          <button onClick={handleLogin} className="welcome-btn login-btn">
            Login
          </button>
          <button onClick={handleRegister} className="welcome-btn register-btn">
            Get Started
          </button>
        </div>

        <div className="welcome-features">
          <div className="feature-item">
            <div className="feature-icon">✓</div>
            <h3>Secure & Safe</h3>
            <p>Your data is protected with top-tier security</p>
          </div>
          <div className="feature-item">
            <div className="feature-icon">⚡</div>
            <h3>Fast & Reliable</h3>
            <p>Lightning-fast performance you can count on</p>
          </div>
          <div className="feature-item">
            <div className="feature-icon">🎯</div>
            <h3>Easy to Use</h3>
            <p>Intuitive interface designed for everyone</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default WelcomePage;