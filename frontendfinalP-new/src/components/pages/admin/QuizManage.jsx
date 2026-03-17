import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { courseService } from '../../../services/courseService';
import { quizService } from '../../../services/quizService';
import '../../../styles/admin/QuizManage.css';

function QuizManage() {
  const navigate = useNavigate();
  const { courseId } = useParams();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Course and module data
  const [course, setCourse] = useState(null);
  const [modules, setModules] = useState([]);
  const [selectedModuleId, setSelectedModuleId] = useState(null);

  // Quizzes for the selected module
  const [quizzes, setQuizzes] = useState([]);
  const [selectedQuizId, setSelectedQuizId] = useState(null);
  const [quizQuestions, setQuizQuestions] = useState([]);
  const [loadingQuestions, setLoadingQuestions] = useState(false);

  // Create quiz form
  const [showCreateQuiz, setShowCreateQuiz] = useState(false);
  const [newQuiz, setNewQuiz] = useState({
    title: '',
    description: '',
    passingScore: 70,
    timeLimitMinutes: 30,
  });

  // Add question form
  const [showAddQuestion, setShowAddQuestion] = useState(false);
  const [newQuestion, setNewQuestion] = useState({
    questionText: '',
    questionType: 'MULTIPLE_CHOICE',
    correctAnswer: '',
    explanation: '',
    points: 1,
    difficultyLevel: 'EASY',
    lessonId: '',
    options: [
      { optionText: '', isCorrect: false, orderIndex: 1 },
      { optionText: '', isCorrect: false, orderIndex: 2 },
      { optionText: '', isCorrect: false, orderIndex: 3 },
      { optionText: '', isCorrect: false, orderIndex: 4 },
    ],
  });

  // Load course data
  useEffect(() => {
    loadCourseData();
  }, [courseId]);

  // Load quizzes when module changes
  useEffect(() => {
    if (selectedModuleId) {
      loadQuizzes();
    }
  }, [selectedModuleId]);

  const loadCourseData = async () => {
    try {
      setLoading(true);
      const courseData = await courseService.getCourseById(courseId);
      setCourse(courseData);
      setModules(courseData.modules || []);

      // Auto-select first module
      if (courseData.modules && courseData.modules.length > 0) {
        setSelectedModuleId(courseData.modules[0].id);
      }
    } catch (err) {
      console.error('Error loading course:', err);
      setError('Failed to load course data.');
    } finally {
      setLoading(false);
    }
  };

  const loadQuizzes = async () => {
    try {
      const data = await quizService.getQuizzesForModule(selectedModuleId);
      setQuizzes(data);
      setSelectedQuizId(null);
      setQuizQuestions([]);
    } catch (err) {
      console.error('Error loading quizzes:', err);
    }
  };

  const loadQuizQuestions = async (quizId) => {
    try {
      setLoadingQuestions(true);
      const data = await quizService.getQuestionsForAdmin(quizId);
      setQuizQuestions(data);
    } catch (err) {
      console.error('Error loading questions:', err);
      setQuizQuestions([]);
    } finally {
      setLoadingQuestions(false);
    }
  };

  const handleDeleteQuestion = async (questionId) => {
    if (!window.confirm('Are you sure you want to delete this question?')) return;
    try {
      await quizService.deleteQuestion(questionId);
      setSuccess('Question deleted.');
      await loadQuizQuestions(selectedQuizId);
      await loadQuizzes();
    } catch (err) {
      console.error('Error deleting question:', err);
      setError('Failed to delete question.');
    }
  };

  const handleUpdateQuestionLesson = async (questionId, lessonId) => {
    try {
      await quizService.updateQuestionLesson(questionId, lessonId || null);
      setSuccess('Lesson updated.');
      await loadQuizQuestions(selectedQuizId);
    } catch (err) {
      console.error('Error updating question lesson:', err);
      setError('Failed to update lesson.');
    }
  };

  const handleCreateQuiz = async (e) => {
    e.preventDefault();
    if (!newQuiz.title.trim()) {
      setError('Quiz title is required');
      return;
    }

    try {
      setSaving(true);
      setError(null);
      await quizService.createQuiz({
        moduleId: selectedModuleId,
        title: newQuiz.title,
        description: newQuiz.description,
        passingScore: newQuiz.passingScore,
        timeLimitMinutes: newQuiz.timeLimitMinutes,
      });

      setSuccess('Quiz created successfully!');
      setShowCreateQuiz(false);
      setNewQuiz({ title: '', description: '', passingScore: 70, timeLimitMinutes: 30 });
      await loadQuizzes();
    } catch (err) {
      console.error('Error creating quiz:', err);
      setError('Failed to create quiz.');
    } finally {
      setSaving(false);
    }
  };

  const handleAddQuestion = async (e) => {
    e.preventDefault();
    if (!newQuestion.questionText.trim()) {
      setError('Question text is required');
      return;
    }

    if (newQuestion.questionType === 'MULTIPLE_CHOICE') {
      const hasCorrectOption = newQuestion.options.some(opt => opt.isCorrect);
      if (!hasCorrectOption) {
        setError('Please mark at least one option as correct');
        return;
      }
      const hasEmptyOption = newQuestion.options.some(opt => !opt.optionText.trim());
      if (hasEmptyOption) {
        setError('All options must have text');
        return;
      }
    } else if (!newQuestion.correctAnswer.trim()) {
      setError('Correct answer is required');
      return;
    }

    try {
      setSaving(true);
      setError(null);

      const questionData = {
        questionText: newQuestion.questionText,
        questionType: newQuestion.questionType,
        correctAnswer: newQuestion.correctAnswer,
        explanation: newQuestion.explanation,
        points: newQuestion.points,
        difficultyLevel: newQuestion.difficultyLevel,
        lessonId: newQuestion.lessonId || null,
      };

      if (newQuestion.questionType === 'MULTIPLE_CHOICE') {
        questionData.options = newQuestion.options;
      }

      await quizService.addQuestion(selectedQuizId, questionData);

      setSuccess('Question added successfully!');
      resetQuestionForm();
      await loadQuizzes(); // Refresh question count
      await loadQuizQuestions(selectedQuizId); // Refresh questions list
    } catch (err) {
      console.error('Error adding question:', err);
      setError('Failed to add question.');
    } finally {
      setSaving(false);
    }
  };

  const resetQuestionForm = () => {
    setNewQuestion({
      questionText: '',
      questionType: 'MULTIPLE_CHOICE',
      correctAnswer: '',
      explanation: '',
      points: 1,
      difficultyLevel: 'EASY',
      lessonId: '',
      options: [
        { optionText: '', isCorrect: false, orderIndex: 1 },
        { optionText: '', isCorrect: false, orderIndex: 2 },
        { optionText: '', isCorrect: false, orderIndex: 3 },
        { optionText: '', isCorrect: false, orderIndex: 4 },
      ],
    });
  };

  const updateOption = (index, field, value) => {
    setNewQuestion(prev => ({
      ...prev,
      options: prev.options.map((opt, i) => {
        if (i === index) {
          return { ...opt, [field]: value };
        }
        // If setting isCorrect to true, unset others (single correct answer)
        if (field === 'isCorrect' && value === true) {
          return { ...opt, isCorrect: i === index };
        }
        return opt;
      }),
    }));
  };

  const addOption = () => {
    setNewQuestion(prev => ({
      ...prev,
      options: [
        ...prev.options,
        { optionText: '', isCorrect: false, orderIndex: prev.options.length + 1 },
      ],
    }));
  };

  const removeOption = (index) => {
    if (newQuestion.options.length <= 2) return;
    setNewQuestion(prev => ({
      ...prev,
      options: prev.options
        .filter((_, i) => i !== index)
        .map((opt, i) => ({ ...opt, orderIndex: i + 1 })),
    }));
  };

  // Clear messages after 3 seconds
  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [success]);

  if (loading) {
    return (
      <div className="quiz-manage-page">
        <div className="dashboard-loading">
          <div className="spinner"></div>
          <p>Loading course data...</p>
        </div>
      </div>
    );
  }

  const selectedModule = modules.find(m => m.id === selectedModuleId);

  return (
    <div className="quiz-manage-page">
      <div className="quiz-manage-header">
        <button className="btn-back" onClick={() => navigate('/admin/dashboard')}>
          ← Back to Dashboard
        </button>
        <h1>Manage Quizzes</h1>
        <p className="course-title-subtitle">{course?.title}</p>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {/* Module Selector */}
      <section className="form-section">
        <h2>Select Module</h2>
        <div className="module-selector">
          {modules.map(module => (
            <button
              key={module.id}
              className={`module-tab ${selectedModuleId === module.id ? 'active' : ''}`}
              onClick={() => {
                setSelectedModuleId(module.id);
                setSelectedQuizId(null);
                setShowCreateQuiz(false);
                setShowAddQuestion(false);
              }}
            >
              <span className="module-tab-title">{module.title}</span>
              <span className="module-tab-lessons">{module.lessons?.length || 0} lessons</span>
            </button>
          ))}
        </div>
      </section>

      {selectedModule && (
        <>
          {/* Quizzes List */}
          <section className="form-section">
            <div className="section-header">
              <h2>Quizzes for: {selectedModule.title}</h2>
              <button
                type="button"
                className="btn-add"
                onClick={() => {
                  setShowCreateQuiz(true);
                  setShowAddQuestion(false);
                }}
              >
                + Create Quiz
              </button>
            </div>

            {quizzes.length === 0 ? (
              <div className="empty-modules">
                <p>No quizzes for this module yet. Click "Create Quiz" to add one.</p>
              </div>
            ) : (
              <div className="quizzes-list">
                {quizzes.map(quiz => (
                  <div
                    key={quiz.id}
                    className={`quiz-card ${selectedQuizId === quiz.id ? 'selected' : ''}`}
                    onClick={() => {
                      setSelectedQuizId(quiz.id);
                      setShowCreateQuiz(false);
                      setShowAddQuestion(false);
                      loadQuizQuestions(quiz.id);
                    }}
                  >
                    <div className="quiz-card-info">
                      <h3>{quiz.title}</h3>
                      <p>{quiz.description}</p>
                    </div>
                    <div className="quiz-card-stats">
                      <span className="stat">{quiz.questionCount || 0} questions</span>
                      <span className="stat">Pass: {quiz.passingScore}%</span>
                      <span className="stat">{quiz.timeLimitMinutes} min</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Create Quiz Form */}
          {showCreateQuiz && (
            <section className="form-section">
              <h2>Create New Quiz</h2>
              <form onSubmit={handleCreateQuiz}>
                <div className="form-group">
                  <label>Quiz Title *</label>
                  <input
                    type="text"
                    value={newQuiz.title}
                    onChange={(e) => setNewQuiz(prev => ({ ...prev, title: e.target.value }))}
                    placeholder="e.g., Module 1 Quiz - HTML Basics"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Description</label>
                  <textarea
                    value={newQuiz.description}
                    onChange={(e) => setNewQuiz(prev => ({ ...prev, description: e.target.value }))}
                    placeholder="What does this quiz cover?"
                    rows={2}
                  />
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Passing Score (%)</label>
                    <input
                      type="number"
                      value={newQuiz.passingScore}
                      onChange={(e) => setNewQuiz(prev => ({ ...prev, passingScore: parseInt(e.target.value) }))}
                      min={0}
                      max={100}
                    />
                  </div>
                  <div className="form-group">
                    <label>Time Limit (minutes)</label>
                    <input
                      type="number"
                      value={newQuiz.timeLimitMinutes}
                      onChange={(e) => setNewQuiz(prev => ({ ...prev, timeLimitMinutes: parseInt(e.target.value) }))}
                      min={1}
                    />
                  </div>
                </div>
                <div className="form-actions">
                  <button type="button" className="btn-cancel" onClick={() => setShowCreateQuiz(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn-submit" disabled={saving}>
                    {saving ? 'Creating...' : 'Create Quiz'}
                  </button>
                </div>
              </form>
            </section>
          )}

          {/* Selected Quiz - Add Questions */}
          {selectedQuizId && !showCreateQuiz && (
            <section className="form-section">
              <div className="section-header">
                <h2>Questions for: {quizzes.find(q => q.id === selectedQuizId)?.title}</h2>
                <button
                  type="button"
                  className="btn-add"
                  onClick={() => setShowAddQuestion(true)}
                >
                  + Add Question
                </button>
              </div>

              {/* Existing Questions Review */}
              {!showAddQuestion && (
                <>
                  {loadingQuestions ? (
                    <p style={{ color: '#64748b', padding: '1rem 0' }}>Loading questions...</p>
                  ) : quizQuestions.length === 0 ? (
                    <div className="empty-modules">
                      <p>No questions yet. Click "+ Add Question" to start adding questions with different difficulty levels (EASY, MEDIUM, HARD).</p>
                    </div>
                  ) : (
                    <div className="existing-questions">
                      <p className="questions-summary">
                        {quizQuestions.length} question{quizQuestions.length !== 1 ? 's' : ''} &mdash;
                        {' '}{quizQuestions.filter(q => q.difficultyLevel === 'EASY').length} Easy,
                        {' '}{quizQuestions.filter(q => q.difficultyLevel === 'MEDIUM').length} Medium,
                        {' '}{quizQuestions.filter(q => q.difficultyLevel === 'HARD').length} Hard
                      </p>
                      {quizQuestions.map((q, idx) => (
                        <div key={q.id} className="review-question-card">
                          <div className="rq-header">
                            <span className="rq-number">Q{idx + 1}</span>
                            <span className={`rq-difficulty ${q.difficultyLevel?.toLowerCase()}`}>
                              {q.difficultyLevel}
                            </span>
                            <span className="rq-type">{q.questionType?.replace('_', ' ')}</span>
                            <span className="rq-points">{q.points} pt{q.points !== 1 ? 's' : ''}</span>
                            <button
                              className="rq-delete-btn"
                              onClick={() => handleDeleteQuestion(q.id)}
                              title="Delete question"
                            >
                              Delete
                            </button>
                          </div>
                          <div className="rq-lesson-select">
                            <label className="rq-lesson-label">Lesson:</label>
                            <select
                              className="rq-lesson-dropdown"
                              value={q.lessonId || ''}
                              onChange={(e) => handleUpdateQuestionLesson(q.id, e.target.value ? parseInt(e.target.value) : null)}
                            >
                              <option value="">-- None --</option>
                              {selectedModule?.lessons?.map(lesson => (
                                <option key={lesson.id} value={lesson.id}>
                                  {lesson.orderIndex}. {lesson.title}
                                </option>
                              ))}
                            </select>
                          </div>
                          <p className="rq-text">{q.questionText}</p>

                          {q.questionType === 'MULTIPLE_CHOICE' && q.options && (
                            <div className="rq-options">
                              {q.options.map((opt, oi) => (
                                <div
                                  key={opt.id}
                                  className={`rq-option ${opt.isCorrect ? 'correct' : ''}`}
                                >
                                  <span className="rq-option-letter">{String.fromCharCode(65 + oi)}</span>
                                  <span className="rq-option-text">{opt.optionText}</span>
                                  {opt.isCorrect && <span className="rq-correct-badge">Correct</span>}
                                </div>
                              ))}
                            </div>
                          )}

                          {(q.questionType === 'TRUE_FALSE' || q.questionType === 'SHORT_ANSWER') && q.correctAnswer && (
                            <div className="rq-correct-answer">
                              <strong>Correct Answer:</strong> {q.correctAnswer}
                            </div>
                          )}

                          {q.explanation && (
                            <div className="rq-explanation">
                              <strong>Explanation:</strong> {q.explanation}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </>
              )}

              {showAddQuestion && (
                <form onSubmit={handleAddQuestion} className="add-question-form">
                  <div className="form-row">
                    <div className="form-group">
                      <label>Difficulty Level *</label>
                      <select
                        value={newQuestion.difficultyLevel}
                        onChange={(e) => setNewQuestion(prev => ({ ...prev, difficultyLevel: e.target.value }))}
                      >
                        <option value="EASY">Easy</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HARD">Hard</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label>Question Type *</label>
                      <select
                        value={newQuestion.questionType}
                        onChange={(e) => setNewQuestion(prev => ({ ...prev, questionType: e.target.value }))}
                      >
                        <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                        <option value="TRUE_FALSE">True / False</option>
                        <option value="SHORT_ANSWER">Short Answer</option>
                      </select>
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Related Lesson (Optional)</label>
                    <select
                      value={newQuestion.lessonId}
                      onChange={(e) => setNewQuestion(prev => ({
                        ...prev,
                        lessonId: e.target.value ? parseInt(e.target.value) : ''
                      }))}
                    >
                      <option value="">-- No specific lesson --</option>
                      {selectedModule?.lessons?.map(lesson => (
                        <option key={lesson.id} value={lesson.id}>
                          {lesson.orderIndex}. {lesson.title}
                        </option>
                      ))}
                    </select>
                    <span className="field-hint">Links this question to a lesson for targeted study recommendations</span>
                  </div>

                  <div className="form-group">
                    <label>Question Text *</label>
                    <textarea
                      value={newQuestion.questionText}
                      onChange={(e) => setNewQuestion(prev => ({ ...prev, questionText: e.target.value }))}
                      placeholder="Enter your question here..."
                      rows={3}
                      required
                    />
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Points</label>
                      <input
                        type="number"
                        value={newQuestion.points}
                        onChange={(e) => setNewQuestion(prev => ({ ...prev, points: parseInt(e.target.value) }))}
                        min={1}
                      />
                    </div>
                  </div>

                  {/* Multiple Choice Options */}
                  {newQuestion.questionType === 'MULTIPLE_CHOICE' && (
                    <div className="options-section">
                      <div className="options-header">
                        <label>Answer Options *</label>
                        <button type="button" className="btn-add-small" onClick={addOption}>
                          + Add Option
                        </button>
                      </div>
                      <div className="options-list">
                        {newQuestion.options.map((option, index) => (
                          <div key={index} className="option-row">
                            <input
                              type="radio"
                              name="correctOption"
                              checked={option.isCorrect}
                              onChange={() => updateOption(index, 'isCorrect', true)}
                              title="Mark as correct answer"
                            />
                            <input
                              type="text"
                              value={option.optionText}
                              onChange={(e) => updateOption(index, 'optionText', e.target.value)}
                              placeholder={`Option ${String.fromCharCode(65 + index)}`}
                              className="option-input"
                            />
                            {newQuestion.options.length > 2 && (
                              <button
                                type="button"
                                className="btn-remove-small"
                                onClick={() => removeOption(index)}
                              >
                                x
                              </button>
                            )}
                          </div>
                        ))}
                        <p className="option-hint">Select the radio button next to the correct answer</p>
                      </div>
                    </div>
                  )}

                  {/* True/False */}
                  {newQuestion.questionType === 'TRUE_FALSE' && (
                    <div className="form-group">
                      <label>Correct Answer *</label>
                      <select
                        value={newQuestion.correctAnswer}
                        onChange={(e) => setNewQuestion(prev => ({ ...prev, correctAnswer: e.target.value }))}
                      >
                        <option value="">Select correct answer</option>
                        <option value="True">True</option>
                        <option value="False">False</option>
                      </select>
                    </div>
                  )}

                  {/* Short Answer */}
                  {newQuestion.questionType === 'SHORT_ANSWER' && (
                    <div className="form-group">
                      <label>Correct Answer *</label>
                      <input
                        type="text"
                        value={newQuestion.correctAnswer}
                        onChange={(e) => setNewQuestion(prev => ({ ...prev, correctAnswer: e.target.value }))}
                        placeholder="Enter the correct answer"
                      />
                    </div>
                  )}

                  <div className="form-group">
                    <label>Explanation (shown when student gets it wrong)</label>
                    <textarea
                      value={newQuestion.explanation}
                      onChange={(e) => setNewQuestion(prev => ({ ...prev, explanation: e.target.value }))}
                      placeholder="Explain why this is the correct answer..."
                      rows={2}
                    />
                  </div>

                  <div className="form-actions">
                    <button type="button" className="btn-cancel" onClick={() => setShowAddQuestion(false)}>
                      Cancel
                    </button>
                    <button type="submit" className="btn-submit" disabled={saving}>
                      {saving ? 'Adding...' : 'Add Question'}
                    </button>
                  </div>
                </form>
              )}
            </section>
          )}
        </>
      )}
    </div>
  );
}

export default QuizManage;
