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
    <div className="welcome-page">
      {/* Navigation */}
      <nav className="welcome-nav">
        <div className="welcome-nav-logo">
          <img src={logo} alt="BrainPath" className="nav-logo-img" />
          <span className="nav-logo-text">BrainPath</span>
        </div>
        <div className="welcome-nav-actions">
          <button onClick={handleLogin} className="nav-login-btn">Log In</button>
          <button onClick={handleRegister} className="nav-signup-btn">Sign Up Free</button>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="welcome-hero">
        <div className="hero-bg-shapes">
          <div className="shape shape-1"></div>
          <div className="shape shape-2"></div>
          <div className="shape shape-3"></div>
        </div>

        <div className="hero-content">
          <div className="hero-text">
            <span className="hero-badge">AI-Powered Learning Platform</span>
            <h1 className="hero-title">
              Learn Smarter,<br />
              <span className="hero-title-accent">Not Harder</span>
            </h1>
            <p className="hero-subtitle">
              BrainPath adapts to how you learn. Get personalized course recommendations,
              track your progress, and master new skills at your own pace.
            </p>
            <div className="hero-buttons">
              <button onClick={handleRegister} className="hero-btn-primary">
                Start Learning for Free
              </button>
              <button onClick={handleLogin} className="hero-btn-secondary">
                I already have an account
              </button>
            </div>

            <div className="hero-stats">
              <div className="hero-stat">
                <span className="hero-stat-value">500+</span>
                <span className="hero-stat-label">Lessons</span>
              </div>
              <div className="hero-stat-divider"></div>
              <div className="hero-stat">
                <span className="hero-stat-value">AI</span>
                <span className="hero-stat-label">Powered</span>
              </div>
              <div className="hero-stat-divider"></div>
              <div className="hero-stat">
                <span className="hero-stat-value">24/7</span>
                <span className="hero-stat-label">Access</span>
              </div>
            </div>
          </div>

          <div className="hero-visual">
            <div className="hero-card hero-card-main">
              <div className="hero-card-header">
                <div className="hero-card-dot dot-red"></div>
                <div className="hero-card-dot dot-yellow"></div>
                <div className="hero-card-dot dot-green"></div>
              </div>
              <div className="hero-card-body">
                <div className="hero-card-line line-short"></div>
                <div className="hero-card-line line-long"></div>
                <div className="hero-card-line line-medium"></div>
                <div className="hero-card-progress">
                  <div className="hero-card-progress-fill"></div>
                </div>
                <div className="hero-card-line line-short"></div>
                <div className="hero-card-line line-long"></div>
              </div>
            </div>
            <div className="hero-card hero-card-float">
              <span className="float-icon">🎯</span>
              <span className="float-text">Quiz Complete!</span>
              <span className="float-score">95%</span>
            </div>
            <div className="hero-card hero-card-streak">
              <span className="streak-fire">🔥</span>
              <span className="streak-text">7 Day Streak</span>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="welcome-features-section">
        <h2 className="features-title">Why students choose BrainPath</h2>
        <p className="features-subtitle">Built for learners who want real results</p>

        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-card-icon icon-courses">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
                <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
              </svg>
            </div>
            <h3>Structured Courses</h3>
            <p>Courses organized into modules and lessons so you always know what to study next.</p>
          </div>

          <div className="feature-card">
            <div className="feature-card-icon icon-progress">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="18" y1="20" x2="18" y2="10"></line>
                <line x1="12" y1="20" x2="12" y2="4"></line>
                <line x1="6" y1="20" x2="6" y2="14"></line>
              </svg>
            </div>
            <h3>Track Your Progress</h3>
            <p>See how far you've come with detailed progress tracking and streak counters.</p>
          </div>

          <div className="feature-card">
            <div className="feature-card-icon icon-quiz">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
            </div>
            <h3>Quizzes & Assessments</h3>
            <p>Test your knowledge with quizzes after each lesson to reinforce what you've learned.</p>
          </div>

          <div className="feature-card">
            <div className="feature-card-icon icon-ai">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
              </svg>
            </div>
            <h3>Smart Recommendations</h3>
            <p>Our AI suggests what to learn next based on your strengths and areas for improvement.</p>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="welcome-cta">
        <h2>Ready to start your learning journey?</h2>
        <p>Join BrainPath today and start building the skills that matter.</p>
        <button onClick={handleRegister} className="cta-btn">
          Create Your Free Account
        </button>
      </section>

      {/* Footer */}
      <footer className="welcome-footer">
        <span>BrainPath &copy; 2025. Built for learners, by learners.</span>
      </footer>
    </div>
  );
}

export default WelcomePage;
