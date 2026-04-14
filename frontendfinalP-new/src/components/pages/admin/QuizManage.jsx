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

  const [course, setCourse] = useState(null);
  const [modules, setModules] = useState([]);
  const [selectedModuleId, setSelectedModuleId] = useState(null);

  const [quizzes, setQuizzes] = useState([]);
  const [selectedQuizId, setSelectedQuizId] = useState(null);
  const [quizQuestions, setQuizQuestions] = useState([]);
  const [loadingQuestions, setLoadingQuestions] = useState(false);

  // Panel view: 'questions' | 'create-quiz' | 'add-question'
  const [rightPanel, setRightPanel] = useState('questions');

  const [newQuiz, setNewQuiz] = useState({ title: '', description: '', passingScore: 70 });
  const [newQuestion, setNewQuestion] = useState({
    questionText: '', questionType: 'MULTIPLE_CHOICE',
    correctAnswer: '', explanation: '', points: 1,
    difficultyLevel: 'EASY', lessonId: '',
    options: [
      { optionText: '', isCorrect: false, orderIndex: 1 },
      { optionText: '', isCorrect: false, orderIndex: 2 },
      { optionText: '', isCorrect: false, orderIndex: 3 },
      { optionText: '', isCorrect: false, orderIndex: 4 },
    ],
  });

  useEffect(() => { loadCourseData(); }, [courseId]);

  useEffect(() => {
    if (selectedModuleId) loadQuizzes({ resetSelection: true });
  }, [selectedModuleId]);

  useEffect(() => {
    if (success) {
      const t = setTimeout(() => setSuccess(null), 3000);
      return () => clearTimeout(t);
    }
  }, [success]);

  const loadCourseData = async () => {
    try {
      setLoading(true);
      const data = await courseService.getCourseById(courseId);
      setCourse(data);
      setModules(data.modules || []);
      if (data.modules?.length > 0) setSelectedModuleId(data.modules[0].id);
    } catch {
      setError('Failed to load course data.');
    } finally {
      setLoading(false);
    }
  };

  const loadQuizzes = async ({ resetSelection = false } = {}) => {
    try {
      const data = await quizService.getQuizzesForModule(selectedModuleId);
      setQuizzes(data);
      if (resetSelection) {
        setSelectedQuizId(null);
        setQuizQuestions([]);
        setRightPanel('questions');
      }
    } catch {}
  };

  const loadQuizQuestions = async (quizId) => {
    try {
      setLoadingQuestions(true);
      const data = await quizService.getQuestionsForAdmin(quizId);
      setQuizQuestions(data);
    } catch {
      setQuizQuestions([]);
    } finally {
      setLoadingQuestions(false);
    }
  };

  const handleSelectQuiz = (quizId) => {
    setSelectedQuizId(quizId);
    setRightPanel('questions');
    loadQuizQuestions(quizId);
  };

  const handleDeleteQuestion = async (questionId) => {
    if (!window.confirm('Delete this question?')) return;
    try {
      await quizService.deleteQuestion(questionId);
      setSuccess('Question deleted.');
      await loadQuizQuestions(selectedQuizId);
      await loadQuizzes();
    } catch {
      setError('Failed to delete question.');
    }
  };

  const handleUpdateQuestionLesson = async (questionId, lessonId) => {
    try {
      await quizService.updateQuestionLesson(questionId, lessonId || null);
      setSuccess('Lesson updated.');
      setQuizQuestions(prev =>
        prev.map(q => q.id === questionId ? { ...q, lessonId: lessonId || null } : q)
      );
    } catch {
      setError('Failed to update lesson.');
    }
  };

  const handleCreateQuiz = async (e) => {
    e.preventDefault();
    if (!newQuiz.title.trim()) { setError('Quiz title is required'); return; }
    try {
      setSaving(true); setError(null);
      await quizService.createQuiz({
        moduleId: selectedModuleId, title: newQuiz.title,
        description: newQuiz.description,
        passingScore: newQuiz.passingScore,
      });
      setSuccess('Quiz created!');
      setNewQuiz({ title: '', description: '', passingScore: 70 });
      await loadQuizzes({ resetSelection: true });
    } catch {
      setError('Failed to create quiz.');
    } finally {
      setSaving(false);
    }
  };

  const handleAddQuestion = async (e) => {
    e.preventDefault();
    if (!newQuestion.questionText.trim()) { setError('Question text is required'); return; }
    if (newQuestion.questionType === 'MULTIPLE_CHOICE') {
      if (!newQuestion.options.some(o => o.isCorrect)) { setError('Mark at least one option as correct'); return; }
      if (newQuestion.options.some(o => !o.optionText.trim())) { setError('All options must have text'); return; }
    } else if (!newQuestion.correctAnswer.trim()) {
      setError('Correct answer is required'); return;
    }
    try {
      setSaving(true); setError(null);
      const payload = {
        questionText: newQuestion.questionText, questionType: newQuestion.questionType,
        correctAnswer: newQuestion.correctAnswer, explanation: newQuestion.explanation,
        points: newQuestion.points, difficultyLevel: newQuestion.difficultyLevel,
        lessonId: newQuestion.lessonId || null,
      };
      if (newQuestion.questionType === 'MULTIPLE_CHOICE') payload.options = newQuestion.options;
      await quizService.addQuestion(selectedQuizId, payload);
      setSuccess('Question added!');
      resetQuestionForm();
      setRightPanel('questions');
      await loadQuizzes();
      await loadQuizQuestions(selectedQuizId);
    } catch {
      setError('Failed to add question.');
    } finally {
      setSaving(false);
    }
  };

  const resetQuestionForm = () => {
    setNewQuestion({
      questionText: '', questionType: 'MULTIPLE_CHOICE', correctAnswer: '',
      explanation: '', points: 1, difficultyLevel: 'EASY', lessonId: '',
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
        if (field === 'isCorrect' && value === true) return { ...opt, isCorrect: i === index };
        return i === index ? { ...opt, [field]: value } : opt;
      }),
    }));
  };

  const addOption = () => {
    setNewQuestion(prev => ({
      ...prev,
      options: [...prev.options, { optionText: '', isCorrect: false, orderIndex: prev.options.length + 1 }],
    }));
  };

  const removeOption = (index) => {
    if (newQuestion.options.length <= 2) return;
    setNewQuestion(prev => ({
      ...prev,
      options: prev.options.filter((_, i) => i !== index).map((o, i) => ({ ...o, orderIndex: i + 1 })),
    }));
  };

  const selectedModule = modules.find(m => m.id === selectedModuleId);
  const selectedQuiz = quizzes.find(q => q.id === selectedQuizId);

  if (loading) {
    return (
      <div className="qm-page">
        <div className="qm-loading"><div className="spinner" /><p>Loading...</p></div>
      </div>
    );
  }

  return (
    <div className="qm-page">
      {/* Top bar */}
      <div className="qm-topbar">
        <button className="btn-back" onClick={() => navigate('/admin/dashboard')}>← Back to Dashboard</button>
        <div className="qm-topbar-title">
          <h1>Quiz Manager</h1>
          {course && <span className="qm-course-name">{course.title}</span>}
        </div>
      </div>

      {success && <div className="qm-toast qm-toast--success">{success}</div>}
      {error && <div className="qm-toast qm-toast--error">{error}</div>}

      <div className="qm-layout">
        {/* ── Left sidebar ── */}
        <aside className="qm-sidebar">
          {/* Module list */}
          <div className="qm-sidebar-section">
            <p className="qm-sidebar-label">Modules</p>
            {modules.map(m => (
              <button
                key={m.id}
                className={`qm-module-btn ${selectedModuleId === m.id ? 'active' : ''}`}
                onClick={() => {
                  setSelectedModuleId(m.id);
                  setSelectedQuizId(null);
                  setRightPanel('questions');
                }}
              >
                {m.title}
              </button>
            ))}
          </div>

          {/* Quiz list for selected module */}
          {selectedModuleId && (
            <div className="qm-sidebar-section">
              <div className="qm-sidebar-section-head">
                <p className="qm-sidebar-label">Quizzes</p>
                <button
                  className="btn-add-small"
                  onClick={() => { setSelectedQuizId(null); setRightPanel('create-quiz'); }}
                >+ New</button>
              </div>
              {quizzes.length === 0 ? (
                <p className="qm-sidebar-empty">No quizzes yet</p>
              ) : (
                quizzes.map(q => (
                  <button
                    key={q.id}
                    className={`qm-quiz-btn ${selectedQuizId === q.id ? 'active' : ''}`}
                    onClick={() => handleSelectQuiz(q.id)}
                  >
                    <span className="qmqb-title">{q.title}</span>
                    <span className="qmqb-count">{q.questionCount || 0} Q</span>
                  </button>
                ))
              )}
            </div>
          )}
        </aside>

        {/* ── Right main panel ── */}
        <main className="qm-main">

          {/* No module selected */}
          {!selectedModuleId && (
            <div className="qm-main-empty">
              <p>Select a module from the sidebar to manage its quizzes.</p>
            </div>
          )}

          {/* Create Quiz form */}
          {selectedModuleId && rightPanel === 'create-quiz' && (
            <div className="qm-panel">
              <div className="qm-panel-header">
                <h2>Create New Quiz</h2>
                <p>for {selectedModule?.title}</p>
              </div>
              <form onSubmit={handleCreateQuiz} className="qm-form">
                <div className="form-group">
                  <label>Quiz Title *</label>
                  <input
                    type="text" value={newQuiz.title}
                    onChange={e => setNewQuiz(p => ({ ...p, title: e.target.value }))}
                    placeholder="e.g., Module 1 Assessment"
                  />
                </div>
                <div className="form-group">
                  <label>Description</label>
                  <textarea
                    value={newQuiz.description} rows={2}
                    onChange={e => setNewQuiz(p => ({ ...p, description: e.target.value }))}
                    placeholder="What does this quiz cover?"
                  />
                </div>
                <div className="qm-form-actions">
                  <button type="button" className="btn-ghost" onClick={() => setRightPanel('questions')}>Cancel</button>
                  <button type="submit" className="btn-save" disabled={saving}>{saving ? 'Creating...' : 'Create Quiz'}</button>
                </div>
              </form>
            </div>
          )}

          {/* Questions view */}
          {selectedModuleId && rightPanel === 'questions' && (
            <>
              {!selectedQuizId ? (
                <div className="qm-main-empty">
                  <p>Select a quiz from the sidebar, or create a new one.</p>
                  <button className="btn-add" onClick={() => setRightPanel('create-quiz')}>+ Create Quiz</button>
                </div>
              ) : (
                <div className="qm-panel">
                  <div className="qm-panel-header">
                    <div>
                      <h2>{selectedQuiz?.title}</h2>
                      <p>{selectedQuiz?.description}</p>
                    </div>
                    <div className="qm-quiz-meta">
                      <span className="qm-meta-badge">Pass: 70%</span>
                      <span className="qm-meta-badge">{selectedQuiz?.timeLimitMinutes} min</span>
                      <button className="btn-add" onClick={() => setRightPanel('add-question')}>+ Add Question</button>
                    </div>
                  </div>

                  {loadingQuestions ? (
                    <p className="qm-loading-text">Loading questions...</p>
                  ) : quizQuestions.length === 0 ? (
                    <div className="qm-no-questions">
                      <p>No questions yet. Click "+ Add Question" to get started.</p>
                    </div>
                  ) : (
                    <div className="qm-questions-list">
                      <div className="qm-summary">
                        {quizQuestions.length} question{quizQuestions.length !== 1 ? 's' : ''} &mdash;&nbsp;
                        {quizQuestions.filter(q => q.difficultyLevel === 'EASY').length} Easy,&nbsp;
                        {quizQuestions.filter(q => q.difficultyLevel === 'MEDIUM').length} Medium,&nbsp;
                        {quizQuestions.filter(q => q.difficultyLevel === 'HARD').length} Hard
                      </div>
                      {quizQuestions.map((q, idx) => (
                        <div key={q.id} className="qm-question-card">
                          <div className="qm-q-header">
                            <span className="qm-q-num">Q{idx + 1}</span>
                            <span className={`qm-q-diff ${q.difficultyLevel?.toLowerCase()}`}>{q.difficultyLevel}</span>
                            <span className="qm-q-type">{q.questionType?.replace('_', ' ')}</span>
                            <span className="qm-q-pts">{q.points} pt{q.points !== 1 ? 's' : ''}</span>
                            <button className="qm-delete-btn" onClick={() => handleDeleteQuestion(q.id)}>Delete</button>
                          </div>
                          <div className="qm-q-lesson">
                            <label>Lesson:</label>
                            <select
                              value={q.lessonId || ''}
                              onChange={e => handleUpdateQuestionLesson(q.id, e.target.value ? parseInt(e.target.value) : null)}
                            >
                              <option value="">— None —</option>
                              {selectedModule?.lessons?.map(l => (
                                <option key={l.id} value={l.id}>{l.orderIndex}. {l.title}</option>
                              ))}
                            </select>
                          </div>
                          <p className="qm-q-text">{q.questionText}</p>
                          {q.questionType === 'MULTIPLE_CHOICE' && q.options && (
                            <div className="qm-options">
                              {q.options.map((opt, oi) => (
                                <div key={opt.id} className={`qm-option ${opt.isCorrect ? 'correct' : ''}`}>
                                  <span className="qm-opt-letter">{String.fromCharCode(65 + oi)}</span>
                                  <span className="qm-opt-text">{opt.optionText}</span>
                                  {opt.isCorrect && <span className="qm-correct-badge">Correct</span>}
                                </div>
                              ))}
                            </div>
                          )}
                          {(q.questionType === 'TRUE_FALSE' || q.questionType === 'SHORT_ANSWER') && q.correctAnswer && (
                            <div className="qm-answer-row"><strong>Answer:</strong> {q.correctAnswer}</div>
                          )}
                          {q.explanation && (
                            <div className="qm-explanation"><strong>Explanation:</strong> {q.explanation}</div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </>
          )}

          {/* Add Question form */}
          {selectedModuleId && rightPanel === 'add-question' && selectedQuizId && (
            <div className="qm-panel">
              <div className="qm-panel-header">
                <h2>Add Question</h2>
                <p>to {selectedQuiz?.title}</p>
              </div>
              <form onSubmit={handleAddQuestion} className="qm-form">
                <div className="form-row">
                  <div className="form-group">
                    <label>Difficulty *</label>
                    <select value={newQuestion.difficultyLevel}
                      onChange={e => setNewQuestion(p => ({ ...p, difficultyLevel: e.target.value }))}>
                      <option value="EASY">Easy</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HARD">Hard</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Question Type *</label>
                    <select value={newQuestion.questionType}
                      onChange={e => setNewQuestion(p => ({ ...p, questionType: e.target.value }))}>
                      <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                      <option value="TRUE_FALSE">True / False</option>
                      <option value="SHORT_ANSWER">Short Answer</option>
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label>Related Lesson (Optional)</label>
                  <select value={newQuestion.lessonId}
                    onChange={e => setNewQuestion(p => ({ ...p, lessonId: e.target.value ? parseInt(e.target.value) : '' }))}>
                    <option value="">— No specific lesson —</option>
                    {selectedModule?.lessons?.map(l => (
                      <option key={l.id} value={l.id}>{l.orderIndex}. {l.title}</option>
                    ))}
                  </select>
                  <span className="field-hint">Links this question to a lesson for targeted recommendations</span>
                </div>

                <div className="form-group">
                  <label>Question Text *</label>
                  <textarea value={newQuestion.questionText} rows={3}
                    onChange={e => setNewQuestion(p => ({ ...p, questionText: e.target.value }))}
                    placeholder="Enter your question here..." />
                </div>

                <div className="form-group" style={{ maxWidth: 160 }}>
                  <label>Points</label>
                  <input type="number" value={newQuestion.points} min={1}
                    onChange={e => setNewQuestion(p => ({ ...p, points: parseInt(e.target.value) }))} />
                </div>

                {newQuestion.questionType === 'MULTIPLE_CHOICE' && (
                  <div className="options-section">
                    <div className="options-header">
                      <label>Answer Options *</label>
                      <button type="button" className="btn-add-small" onClick={addOption}>+ Option</button>
                    </div>
                    <div className="options-list">
                      {newQuestion.options.map((opt, i) => (
                        <div key={i} className="option-row">
                          <input type="radio" name="correctOpt" checked={opt.isCorrect}
                            onChange={() => updateOption(i, 'isCorrect', true)} />
                          <input type="text" className="option-input" value={opt.optionText}
                            onChange={e => updateOption(i, 'optionText', e.target.value)}
                            placeholder={`Option ${String.fromCharCode(65 + i)}`} />
                          {newQuestion.options.length > 2 && (
                            <button type="button" className="btn-remove-xs" onClick={() => removeOption(i)}>×</button>
                          )}
                        </div>
                      ))}
                      <p className="option-hint">Select the radio button next to the correct answer</p>
                    </div>
                  </div>
                )}

                {newQuestion.questionType === 'TRUE_FALSE' && (
                  <div className="form-group">
                    <label>Correct Answer *</label>
                    <select value={newQuestion.correctAnswer}
                      onChange={e => setNewQuestion(p => ({ ...p, correctAnswer: e.target.value }))}>
                      <option value="">Select answer</option>
                      <option value="True">True</option>
                      <option value="False">False</option>
                    </select>
                  </div>
                )}

                {newQuestion.questionType === 'SHORT_ANSWER' && (
                  <div className="form-group">
                    <label>Correct Answer *</label>
                    <input type="text" value={newQuestion.correctAnswer}
                      onChange={e => setNewQuestion(p => ({ ...p, correctAnswer: e.target.value }))}
                      placeholder="The correct answer" />
                  </div>
                )}

                <div className="form-group">
                  <label>Explanation (shown when student gets it wrong)</label>
                  <textarea value={newQuestion.explanation} rows={2}
                    onChange={e => setNewQuestion(p => ({ ...p, explanation: e.target.value }))}
                    placeholder="Explain the correct answer..." />
                </div>

                <div className="qm-form-actions">
                  <button type="button" className="btn-ghost" onClick={() => { resetQuestionForm(); setRightPanel('questions'); }}>
                    Cancel
                  </button>
                  <button type="submit" className="btn-save" disabled={saving}>{saving ? 'Adding...' : 'Add Question'}</button>
                </div>
              </form>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default QuizManage;
