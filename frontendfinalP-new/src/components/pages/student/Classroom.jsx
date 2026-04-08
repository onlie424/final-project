import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { courseService } from '../../../services/courseService';
import { quizService } from '../../../services/quizService';
import '../../../styles/Classroom.css';

// Convert YouTube URLs to embeddable format
const getEmbedUrl = (url) => {
  if (!url) return null;
  if (url.includes('/embed/')) return url;
  const watchMatch = url.match(/(?:youtube\.com\/watch\?v=)([^&\s]+)/);
  if (watchMatch) return `https://www.youtube.com/embed/${watchMatch[1]}`;
  const shortMatch = url.match(/(?:youtu\.be\/)([^?\s]+)/);
  if (shortMatch) return `https://www.youtube.com/embed/${shortMatch[1]}`;
  return url;
};

export default function Classroom() {
  const { courseId, lessonId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [course, setCourse] = useState(null);
  const [currentLesson, setCurrentLesson] = useState(null);
  const [expandedModules, setExpandedModules] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [moduleQuizzes, setModuleQuizzes] = useState({});

  // Quiz-based progress state
  const [passedQuizzes, setPassedQuizzes] = useState(new Set());
  const [moduleLockStatus, setModuleLockStatus] = useState({});

  // Compute total quizzes across the entire course
  const totalQuizzes = Object.values(moduleQuizzes).reduce(
    (sum, quizzes) => sum + quizzes.length,
    0
  );

  // Progress = quizzes fully passed / total quizzes
  const progressPercentage =
    totalQuizzes > 0 ? Math.round((passedQuizzes.size / totalQuizzes) * 100) : 0;

  // How many modules are "completed" (all their quizzes passed)
  const completedModulesCount = course?.modules?.filter((mod) => {
    const quizzes = moduleQuizzes[mod.id] || [];
    return quizzes.length > 0 && quizzes.every((q) => passedQuizzes.has(q.id));
  }).length || 0;

  // Is a given module locked?
  const isModuleLocked = (moduleId) => {
    const lock = moduleLockStatus[moduleId];
    return lock?.isLocked === true;
  };

  // Is a given module fully completed (all quizzes passed)?
  const isModuleCompleted = (moduleId) => {
    const quizzes = moduleQuizzes[moduleId] || [];
    return quizzes.length > 0 && quizzes.every((q) => passedQuizzes.has(q.id));
  };

  useEffect(() => {
    fetchCourseData();
  }, [courseId]);

  // Re-check quiz/lock state whenever the user navigates back to the classroom
  // (e.g. returning from AdaptiveQuiz after passing)
  useEffect(() => {
    if (Object.keys(moduleQuizzes).length > 0) {
      refreshQuizState(null, moduleQuizzes);
    }
  }, [location.pathname]);

  useEffect(() => {
    if (course && lessonId) {
      const lesson = findLessonById(lessonId);
      setCurrentLesson(lesson);
    } else if (course && !lessonId) {
      const firstLesson = getFirstLesson();
      if (firstLesson) {
        navigate(`/classroom/${courseId}/lesson/${firstLesson.id}`, { replace: true });
      }
    }
  }, [course, lessonId]);

  const refreshQuizState = useCallback(
    async (_courseData, quizMap) => {
      if (!user?.userId) return;

      try {
        // Fetch module lock status
        const lockStatus = await quizService.getModuleLockStatus(courseId, user.userId);
        // lockStatus is an array of { moduleId, isLocked, lockReason }
        const lockMap = {};
        (lockStatus || []).forEach((item) => {
          lockMap[item.moduleId] = { isLocked: item.isLocked, lockReason: item.lockReason };
        });
        setModuleLockStatus(lockMap);

        // Check which quizzes the user has passed
        const allQuizIds = Object.values(quizMap).flat().map((q) => q.id);
        const passedSet = new Set();
        await Promise.all(
          allQuizIds.map(async (qId) => {
            try {
              const passed = await quizService.hasPassedQuiz(qId, user.userId);
              if (passed) passedSet.add(qId);
            } catch (_) {
              // ignore per-quiz errors
            }
          })
        );
        setPassedQuizzes(passedSet);

        // Sync enrollment completion in the backend — fixes any user whose
        // completion percentage was not recorded due to the previous bug
        if (passedSet.size > 0) {
          try {
            await quizService.syncCompletion(courseId, user.userId);
          } catch (_) {
            // non-critical — UI progress is already correct from passedSet
          }
        }
      } catch (err) {
        console.error('Error refreshing quiz state:', err);
      }
    },
    [courseId, user?.userId]
  );

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

      // Fetch quizzes for each module
      const quizMap = {};
      if (courseData.modules?.length > 0) {
        for (const mod of courseData.modules) {
          try {
            const quizzes = await quizService.getQuizzesForModule(mod.id);
            if (quizzes?.length > 0) {
              quizMap[mod.id] = quizzes;
            }
          } catch (_) {
            // Module may have no quizzes
          }
        }
      }
      setModuleQuizzes(quizMap);

      // Now fetch lock + passed state
      await refreshQuizState(courseData, quizMap);
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
      if (lesson) return { ...lesson, moduleName: module.title, moduleId: module.id };
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

  const handleLessonClick = (lesson, moduleId) => {
    if (isModuleLocked(moduleId)) return;
    navigate(`/classroom/${courseId}/lesson/${lesson.id}`);
  };

  const navigateToLesson = (lesson) => {
    if (!lesson) return;
    if (isModuleLocked(lesson.moduleId)) return;
    navigate(`/classroom/${courseId}/lesson/${lesson.id}`);
  };

  const getLessonIcon = (type) => {
    return (
      <svg className="lesson-type-icon" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z" />
      </svg>
    );
  };

  const renderLessonContent = () => {
    if (!currentLesson) {
      return (
        <div className="no-content">
          <p>Select a lesson from the sidebar to begin learning.</p>
        </div>
      );
    }

    // Show locked overlay if this lesson's module is locked
    if (isModuleLocked(currentLesson.moduleId)) {
      return (
        <div className="content-locked">
          <div className="locked-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
            </svg>
          </div>
          <h2>Module Locked</h2>
          <p>Complete the quiz for the previous module to unlock this content.</p>
        </div>
      );
    }

    const renderLessonContext = () => {
      const lessonContentText = currentLesson.contentText;
      return (
        <div className="lesson-context">
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

          {lessonContentText && (
            <div className="context-section">
              <h3>Lesson Content</h3>
              <div className="prose">
                {lessonContentText.includes('<') ? (
                  <div dangerouslySetInnerHTML={{ __html: lessonContentText }} />
                ) : (
                  <p style={{ whiteSpace: 'pre-wrap' }}>{lessonContentText}</p>
                )}
              </div>
            </div>
          )}

          <div className="context-section">
            <h3>About This Lesson</h3>
            <p>
              {currentLesson.description ||
                `This lesson covers ${currentLesson.title}.`}
            </p>
          </div>

          {currentLesson.content && (
            <div className="context-section">
              <h3>Lesson Notes</h3>
              <div className="prose" dangerouslySetInnerHTML={{ __html: currentLesson.content }} />
            </div>
          )}

          {currentLesson.summary && (
            <div className="context-section">
              <h3>Summary</h3>
              <p>{currentLesson.summary}</p>
            </div>
          )}

          {currentLesson.keyPoints?.length > 0 && (
            <div className="context-section">
              <h3>Key Points</h3>
              <ul className="key-points-list">
                {currentLesson.keyPoints.map((point, index) => (
                  <li key={index}>{point}</li>
                ))}
              </ul>
            </div>
          )}

          {currentLesson.resources?.length > 0 && (
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

  const previousLesson = getPreviousLesson();
  const nextLesson = getNextLesson();
  const nextLessonLocked = nextLesson ? isModuleLocked(nextLesson.moduleId) : false;

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
              {completedModulesCount} / {course.modules?.length || 0} modules completed
            </p>
          </div>

          <div className="modules-list">
            {course.modules?.map((module, moduleIndex) => {
              const locked = isModuleLocked(module.id);
              const completed = isModuleCompleted(module.id);

              return (
                <div
                  key={module.id}
                  className={`module-item ${locked ? 'locked' : ''} ${completed ? 'completed' : ''}`}
                >
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
                    <div className="module-status-icons">
                      {completed ? (
                        <svg className="module-completed-icon" viewBox="0 0 24 24" fill="currentColor">
                          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                        </svg>
                      ) : locked ? (
                        <svg className="lock-icon" viewBox="0 0 24 24" fill="currentColor">
                          <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
                        </svg>
                      ) : (
                        <svg
                          className={`chevron ${expandedModules[module.id] ? 'rotated' : ''}`}
                          viewBox="0 0 24 24"
                          fill="currentColor"
                        >
                          <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z" />
                        </svg>
                      )}
                      {!completed && !locked && (
                        <span style={{ display: 'none' }} />
                      )}
                    </div>
                  </button>

                  {expandedModules[module.id] && (
                    <div className="lessons-list">
                      {module.lessons?.map((lesson, lessonIndex) => {
                        const isActive =
                          currentLesson?.id === lesson.id ||
                          parseInt(lessonId) === lesson.id;

                        return (
                          <button
                            key={lesson.id}
                            className={`lesson-item ${isActive ? 'active' : ''} ${locked ? 'locked-lesson' : ''}`}
                            onClick={() => handleLessonClick(lesson, module.id)}
                            disabled={locked}
                          >
                            <div className="lesson-status">
                              {locked ? (
                                <svg className="lock-icon-small" viewBox="0 0 24 24" fill="currentColor">
                                  <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
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
                      {moduleQuizzes[module.id]?.map((quiz) => {
                        const quizPassed = passedQuizzes.has(quiz.id);

                        if (quizPassed) {
                          return (
                            <div key={`quiz-${quiz.id}`} className="lesson-item quiz-item quiz-passed">
                              <div className="lesson-status">
                                <svg className="check-icon" viewBox="0 0 24 24" fill="currentColor">
                                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                                </svg>
                              </div>
                              <div className="lesson-info">
                                <span className="lesson-title">{quiz.title || 'Module Quiz'}</span>
                                <span className="quiz-complete-label">Completed</span>
                              </div>
                            </div>
                          );
                        }

                        if (locked) {
                          return (
                            <div key={`quiz-${quiz.id}`} className="lesson-item quiz-item locked-lesson">
                              <div className="lesson-status">
                                <svg className="lock-icon-small" viewBox="0 0 24 24" fill="currentColor">
                                  <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
                                </svg>
                              </div>
                              <div className="lesson-info">
                                <span className="lesson-title">{quiz.title || 'Module Quiz'}</span>
                                <span className="lesson-meta quiz-meta">Locked</span>
                              </div>
                            </div>
                          );
                        }

                        return (
                          <button
                            key={`quiz-${quiz.id}`}
                            className="lesson-item quiz-item"
                            onClick={() =>
                              navigate(
                                `/classroom/${courseId}/module/${module.id}/quiz/${quiz.id}`
                              )
                            }
                          >
                            <div className="lesson-status">
                              <svg className="quiz-icon" viewBox="0 0 24 24" fill="currentColor">
                                <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 3c1.93 0 3.5 1.57 3.5 3.5S13.93 13 12 13s-3.5-1.57-3.5-3.5S10.07 6 12 6zm7 13H5v-.23c0-.62.28-1.2.76-1.58C7.47 15.82 9.64 15 12 15s4.53.82 6.24 2.19c.48.38.76.97.76 1.58V19z" />
                              </svg>
                            </div>
                            <div className="lesson-info">
                              <span className="lesson-title">{quiz.title || 'Module Quiz'}</span>
                              <span className="lesson-meta quiz-meta">
                                <svg viewBox="0 0 24 24" fill="currentColor" style={{ width: 14, height: 14 }}>
                                  <path d="M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z" />
                                </svg>
                                Adaptive Quiz
                              </span>
                            </div>
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            })}
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
              disabled={!nextLesson || nextLessonLocked}
            >
              <span>
                <small>Next</small>
                <strong>
                  {nextLessonLocked
                    ? 'Complete quiz to unlock'
                    : nextLesson?.title || 'Course Complete!'}
                </strong>
              </span>
              {nextLessonLocked ? (
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
                </svg>
              ) : (
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z" />
                </svg>
              )}
            </button>
          </div>
        </main>
      </div>
    </div>
  );
}
