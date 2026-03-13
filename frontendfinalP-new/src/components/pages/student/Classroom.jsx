import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { courseService } from '../../../services/courseService';
import { enrollmentService } from '../../../services/enrollmentService';
import { quizService } from '../../../services/quizService';
import '../../../styles/Classroom.css';

// Convert YouTube URLs to embeddable format
const getEmbedUrl = (url) => {
  if (!url) return null;
  // Already an embed URL
  if (url.includes('/embed/')) return url;
  // https://www.youtube.com/watch?v=VIDEO_ID
  const watchMatch = url.match(/(?:youtube\.com\/watch\?v=)([^&\s]+)/);
  if (watchMatch) return `https://www.youtube.com/embed/${watchMatch[1]}`;
  // https://youtu.be/VIDEO_ID
  const shortMatch = url.match(/(?:youtu\.be\/)([^?\s]+)/);
  if (shortMatch) return `https://www.youtube.com/embed/${shortMatch[1]}`;
  // Return as-is for other URLs
  return url;
};

export default function Classroom() {
  const { courseId, lessonId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [course, setCourse] = useState(null);
  const [currentLesson, setCurrentLesson] = useState(null);
  const [expandedModules, setExpandedModules] = useState({});
  const [completedLessons, setCompletedLessons] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [markingComplete, setMarkingComplete] = useState(false);
  const [moduleQuizzes, setModuleQuizzes] = useState({});

  // Calculate progress
  const totalLessons = course?.modules?.reduce(
    (sum, module) => sum + (module.lessons?.length || 0),
    0
  ) || 0;
  const progressPercentage = totalLessons > 0
    ? Math.round((completedLessons.size / totalLessons) * 100)
    : 0;

  useEffect(() => {
    fetchCourseData();
  }, [courseId]);

  useEffect(() => {
    if (course && lessonId) {
      const lesson = findLessonById(lessonId);
      setCurrentLesson(lesson);
    } else if (course && !lessonId) {
      // Navigate to first lesson if no lesson specified
      const firstLesson = getFirstLesson();
      if (firstLesson) {
        navigate(`/classroom/${courseId}/lesson/${firstLesson.id}`, { replace: true });
      }
    }
  }, [course, lessonId]);

  const fetchCourseData = async () => {
    try {
      setLoading(true);
      setError(null);

      const courseData = await courseService.getCourseById(courseId, user?.userId);
      setCourse(courseData);

      // Expand first module by default
      if (courseData.modules?.length > 0) {
        setExpandedModules({ [courseData.modules[0].id]: true });
      }

      // Load completed lessons from progress (if available)
      if (courseData.completedLessonIds) {
        setCompletedLessons(new Set(courseData.completedLessonIds));
      }

      // Fetch quizzes for each module
      if (courseData.modules?.length > 0) {
        const quizMap = {};
        for (const mod of courseData.modules) {
          try {
            const quizzes = await quizService.getQuizzesForModule(mod.id);
            if (quizzes?.length > 0) {
              quizMap[mod.id] = quizzes;
            }
          } catch (e) {
            // Module may have no quizzes, that's fine
          }
        }
        setModuleQuizzes(quizMap);
      }
    } catch (err) {
      console.error('Error fetching course:', err);
      setError('Failed to load course content.');
    } finally {
      setLoading(false);
    }
  };

  const findLessonById = (id) => {
    if (!course?.modules) return null;
    for (const module of course.modules) {
      const lesson = module.lessons?.find((l) => l.id === parseInt(id) || l.id === id);
      if (lesson) return { ...lesson, moduleName: module.title };
    }
    return null;
  };

  const getFirstLesson = () => {
    if (!course?.modules?.length) return null;
    for (const module of course.modules) {
      if (module.lessons?.length) return module.lessons[0];
    }
    return null;
  };

  const getAllLessons = () => {
    if (!course?.modules) return [];
    return course.modules.flatMap((module) =>
      (module.lessons || []).map((lesson) => ({
        ...lesson,
        moduleId: module.id,
        moduleName: module.title,
      }))
    );
  };

  const getCurrentLessonIndex = () => {
    const allLessons = getAllLessons();
    return allLessons.findIndex(
      (l) => l.id === parseInt(lessonId) || l.id === lessonId
    );
  };

  const getPreviousLesson = () => {
    const allLessons = getAllLessons();
    const currentIndex = getCurrentLessonIndex();
    return currentIndex > 0 ? allLessons[currentIndex - 1] : null;
  };

  const getNextLesson = () => {
    const allLessons = getAllLessons();
    const currentIndex = getCurrentLessonIndex();
    return currentIndex < allLessons.length - 1 ? allLessons[currentIndex + 1] : null;
  };

  const toggleModule = (moduleId) => {
    setExpandedModules((prev) => ({
      ...prev,
      [moduleId]: !prev[moduleId],
    }));
  };

  const handleLessonClick = (lesson) => {
    navigate(`/classroom/${courseId}/lesson/${lesson.id}`);
  };

  const handleMarkComplete = async () => {
    if (!currentLesson || !user?.userId) return;

    try {
      setMarkingComplete(true);

      // Call API to mark lesson as complete (if endpoint exists)
      // await progressService.markLessonComplete(user.userId, courseId, currentLesson.id);

      // Update local state
      setCompletedLessons((prev) => new Set([...prev, currentLesson.id]));

      // Auto-navigate to next lesson after short delay
      setTimeout(() => {
        const nextLesson = getNextLesson();
        if (nextLesson) {
          navigate(`/classroom/${courseId}/lesson/${nextLesson.id}`);
        }
      }, 500);
    } catch (err) {
      console.error('Error marking lesson complete:', err);
    } finally {
      setMarkingComplete(false);
    }
  };

  const navigateToLesson = (lesson) => {
    if (lesson) {
      navigate(`/classroom/${courseId}/lesson/${lesson.id}`);
    }
  };

  const getLessonIcon = (type) => {
    switch (type?.toLowerCase()) {
      case 'video':
        return (
          <svg className="lesson-type-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z" />
          </svg>
        );
      case 'pdf':
      case 'slides':
        return (
          <svg className="lesson-type-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z" />
          </svg>
        );
      case 'text':
      case 'reading':
      default:
        return (
          <svg className="lesson-type-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zM6 20V4h7v5h5v11H6z" />
          </svg>
        );
    }
  };

  const renderLessonContent = () => {
    if (!currentLesson) {
      return (
        <div className="no-content">
          <p>Select a lesson from the sidebar to begin learning.</p>
        </div>
      );
    }

    const contentType = currentLesson.contentType?.toLowerCase() || 'text';

    // Get the lesson content text from backend LessonDTO
    const getLessonContentText = () => {
      return currentLesson.contentText;
    };

    // Lesson context section (shown below video/PDF content)
    const renderLessonContext = () => {
      const lessonContentText = getLessonContentText();

      return (
      <div className="lesson-context">
        {/* Always show lesson info section */}
        <div className="context-section lesson-info-section">
          <h3>Lesson Information</h3>
          <div className="lesson-info-grid">
            <div className="info-item">
              <span className="info-label">Lesson</span>
              <span className="info-value">{currentLesson.title}</span>
            </div>
            {currentLesson.duration && (
              <div className="info-item">
                <span className="info-label">Duration</span>
                <span className="info-value">{currentLesson.duration}</span>
              </div>
            )}
            {currentLesson.contentType && (
              <div className="info-item">
                <span className="info-label">Type</span>
                <span className="info-value" style={{ textTransform: 'capitalize' }}>
                  {currentLesson.contentType}
                </span>
              </div>
            )}
            {currentLesson.moduleName && (
              <div className="info-item">
                <span className="info-label">Module</span>
                <span className="info-value">{currentLesson.moduleName}</span>
              </div>
            )}
          </div>
        </div>

        {/* Lesson Content Text from backend */}
        {lessonContentText && (
          <div className="context-section">
            <h3>Lesson Content</h3>
            <div className="prose">
              {/* Check if content is HTML or plain text */}
              {lessonContentText.includes('<') ? (
                <div dangerouslySetInnerHTML={{ __html: lessonContentText }} />
              ) : (
                <p style={{ whiteSpace: 'pre-wrap' }}>{lessonContentText}</p>
              )}
            </div>
          </div>
        )}

        {/* About section - show description or default text */}
        <div className="context-section">
          <h3>About This Lesson</h3>
          <p>
            {currentLesson.description ||
              `This lesson covers ${currentLesson.title}. Watch the video above and make sure to complete the lesson by clicking "Mark as Complete" when you're done.`}
          </p>
        </div>

        {/* Optional: Lesson Notes */}
        {currentLesson.content && (
          <div className="context-section">
            <h3>Lesson Notes</h3>
            <div className="prose" dangerouslySetInnerHTML={{ __html: currentLesson.content }} />
          </div>
        )}

        {/* Optional: Summary */}
        {currentLesson.summary && (
          <div className="context-section">
            <h3>Summary</h3>
            <p>{currentLesson.summary}</p>
          </div>
        )}

        {/* Optional: Key Points */}
        {currentLesson.keyPoints && currentLesson.keyPoints.length > 0 && (
          <div className="context-section">
            <h3>Key Points</h3>
            <ul className="key-points-list">
              {currentLesson.keyPoints.map((point, index) => (
                <li key={index}>{point}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Optional: Resources */}
        {currentLesson.resources && currentLesson.resources.length > 0 && (
          <div className="context-section">
            <h3>Additional Resources</h3>
            <ul className="resources-list">
              {currentLesson.resources.map((resource, index) => (
                <li key={index}>
                  <a href={resource.url} target="_blank" rel="noopener noreferrer">
                    {resource.title || resource.url}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    );
    };

    switch (contentType) {
      case 'video':
        return (
          <>
            <div className="video-container">
              {getEmbedUrl(currentLesson.contentUrl) ? (
                <iframe
                  src={getEmbedUrl(currentLesson.contentUrl)}
                  title={currentLesson.title}
                  frameBorder="0"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowFullScreen
                />
              ) : (
                <div className="video-placeholder">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z" />
                  </svg>
                  <p>Video content will appear here</p>
                </div>
              )}
            </div>
            {renderLessonContext()}
          </>
        );

      case 'pdf':
      case 'slides':
        return (
          <>
            <div className="pdf-container">
              {currentLesson.contentUrl ? (
                <iframe
                  src={currentLesson.contentUrl}
                  title={currentLesson.title}
                  frameBorder="0"
                />
              ) : (
                <div className="pdf-placeholder">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z" />
                  </svg>
                  <p>PDF/Slides content will appear here</p>
                </div>
              )}
            </div>
            {renderLessonContext()}
          </>
        );

      case 'text':
      case 'reading':
      default:
        return (
          <div className="text-content prose">
            {currentLesson.content ? (
              <div dangerouslySetInnerHTML={{ __html: currentLesson.content }} />
            ) : (
              <div className="text-placeholder">
                <h3>{currentLesson.title}</h3>
                <p>{currentLesson.description || 'Lesson content will appear here.'}</p>
                {currentLesson.summary && (
                  <>
                    <h4>Summary</h4>
                    <p>{currentLesson.summary}</p>
                  </>
                )}
              </div>
            )}
          </div>
        );
    }
  };

  if (loading) {
    return (
      <div className="classroom-loading">
        <div className="spinner"></div>
        <p>Loading classroom...</p>
      </div>
    );
  }

  if (error || !course) {
    return (
      <div className="classroom-error">
        <p>{error || 'Course not found.'}</p>
        <button onClick={() => navigate('/user/dashboard')}>Back to Dashboard</button>
      </div>
    );
  }

  const isLessonCompleted = (lessonId) => completedLessons.has(lessonId);
  const previousLesson = getPreviousLesson();
  const nextLesson = getNextLesson();

  return (
    <div className="classroom-container">
      {/* Progress Bar */}
      <div className="progress-bar-container">
        <div className="progress-bar-fill" style={{ width: `${progressPercentage}%` }} />
        <span className="progress-text">{progressPercentage}% Complete</span>
      </div>

      {/* Top Navigation */}
      <header className="classroom-header">
        <nav className="breadcrumbs">
          <Link to="/user/dashboard">Home</Link>
          <span className="separator">/</span>
          <Link to={`/courses/${courseId}`}>{course.title}</Link>
          {currentLesson && (
            <>
              <span className="separator">/</span>
              <span className="current">{currentLesson.title}</span>
            </>
          )}
        </nav>
        <button className="exit-btn" onClick={() => navigate('/user/dashboard')}>
          Exit Classroom
        </button>
      </header>

      <div className="classroom-layout">
        {/* Sidebar */}
        <aside className="curriculum-sidebar">
          <div className="sidebar-header">
            <h2>Course Content</h2>
            <p className="lesson-count">
              {completedLessons.size} / {totalLessons} lessons completed
            </p>
          </div>

          <div className="modules-list">
            {course.modules?.map((module, moduleIndex) => (
              <div key={module.id} className="module-item">
                <button
                  className={`module-header ${expandedModules[module.id] ? 'expanded' : ''}`}
                  onClick={() => toggleModule(module.id)}
                >
                  <div className="module-info">
                    <span className="module-number">Module {moduleIndex + 1}</span>
                    <h3 className="module-title">{module.title}</h3>
                    <span className="module-meta">
                      {module.lessons?.length || 0} lessons
                    </span>
                  </div>
                  <svg
                    className={`chevron ${expandedModules[module.id] ? 'rotated' : ''}`}
                    viewBox="0 0 24 24"
                    fill="currentColor"
                  >
                    <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z" />
                  </svg>
                </button>

                {expandedModules[module.id] && (
                  <div className="lessons-list">
                    {module.lessons?.map((lesson, lessonIndex) => {
                      const isActive =
                        currentLesson?.id === lesson.id ||
                        parseInt(lessonId) === lesson.id;
                      const isCompleted = isLessonCompleted(lesson.id);

                      return (
                        <button
                          key={lesson.id}
                          className={`lesson-item ${isActive ? 'active' : ''} ${isCompleted ? 'completed' : ''}`}
                          onClick={() => handleLessonClick(lesson)}
                        >
                          <div className="lesson-status">
                            {isCompleted ? (
                              <svg className="check-icon" viewBox="0 0 24 24" fill="currentColor">
                                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                              </svg>
                            ) : (
                              <span className="lesson-number">{lessonIndex + 1}</span>
                            )}
                          </div>
                          <div className="lesson-info">
                            <span className="lesson-title">{lesson.title}</span>
                            <span className="lesson-meta">
                              {getLessonIcon(lesson.contentType)}
                              {lesson.duration || '5 min'}
                            </span>
                          </div>
                        </button>
                      );
                    })}

                    {/* Module Quiz Button */}
                    {moduleQuizzes[module.id]?.map((quiz) => (
                      <button
                        key={`quiz-${quiz.id}`}
                        className="lesson-item quiz-item"
                        onClick={() => navigate(`/classroom/${courseId}/module/${module.id}/quiz/${quiz.id}`)}
                      >
                        <div className="lesson-status">
                          <svg className="quiz-icon" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 3c1.93 0 3.5 1.57 3.5 3.5S13.93 13 12 13s-3.5-1.57-3.5-3.5S10.07 6 12 6zm7 13H5v-.23c0-.62.28-1.2.76-1.58C7.47 15.82 9.64 15 12 15s4.53.82 6.24 2.19c.48.38.76.97.76 1.58V19z" />
                          </svg>
                        </div>
                        <div className="lesson-info">
                          <span className="lesson-title">{quiz.title || 'Module Quiz'}</span>
                          <span className="lesson-meta quiz-meta">
                            <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14" style={{width: 14, height: 14}}>
                              <path d="M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z" />
                            </svg>
                            Adaptive Quiz
                          </span>
                        </div>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </aside>

        {/* Main Content */}
        <main className="content-stage">
          {currentLesson && (
            <div className="content-header">
              <div className="lesson-header-info">
                <span className="lesson-type-badge">
                  {getLessonIcon(currentLesson.contentType)}
                  {currentLesson.contentType || 'Lesson'}
                </span>
                <h1 className="lesson-title">{currentLesson.title}</h1>
              </div>
              <button
                className={`mark-complete-btn ${isLessonCompleted(currentLesson.id) ? 'completed' : ''}`}
                onClick={handleMarkComplete}
                disabled={markingComplete || isLessonCompleted(currentLesson.id)}
              >
                {isLessonCompleted(currentLesson.id) ? (
                  <>
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
                    </svg>
                    Completed
                  </>
                ) : markingComplete ? (
                  'Saving...'
                ) : (
                  'Mark as Complete'
                )}
              </button>
            </div>
          )}

          <div className="content-body">{renderLessonContent()}</div>

          {/* Footer Navigation */}
          <div className="content-footer">
            <button
              className="nav-btn prev"
              onClick={() => navigateToLesson(previousLesson)}
              disabled={!previousLesson}
            >
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z" />
              </svg>
              <span>
                <small>Previous</small>
                <strong>{previousLesson?.title || 'No previous lesson'}</strong>
              </span>
            </button>

            <button
              className="nav-btn next"
              onClick={() => navigateToLesson(nextLesson)}
              disabled={!nextLesson}
            >
              <span>
                <small>Next</small>
                <strong>{nextLesson?.title || 'Course Complete!'}</strong>
              </span>
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z" />
              </svg>
            </button>
          </div>
        </main>
      </div>
    </div>
  );
}
