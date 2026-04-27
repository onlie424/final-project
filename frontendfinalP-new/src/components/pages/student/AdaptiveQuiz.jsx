import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { quizService } from '../../../services/quizService';
import '../../../styles/AdaptiveQuiz.css';

const PHASES = {
  PRE_QUIZ: 'PRE_QUIZ',
  IN_ROUND: 'IN_ROUND',
  ROUND_RESULT: 'ROUND_RESULT',
  QUIZ_COMPLETE: 'QUIZ_COMPLETE',
  ALREADY_PASSED: 'ALREADY_PASSED',
};

export default function AdaptiveQuiz() {
  const { courseId, moduleId, quizId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [phase, setPhase] = useState(PHASES.PRE_QUIZ);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Quiz data
  const [quizTitle, setQuizTitle] = useState('');
  const [attemptId, setAttemptId] = useState(null);
  // Round data
  const [currentDifficulty, setCurrentDifficulty] = useState('EASY');
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});

  // Result data
  const [roundResult, setRoundResult] = useState(null);
  const [allRounds, setAllRounds] = useState([]);
  const [resumedFromPrevious, setResumedFromPrevious] = useState(false);

  // Timer
  const [startTime, setStartTime] = useState(null);

  const getDifficultiesBefore = (difficulty) => {
    const order = ['EASY', 'MEDIUM', 'HARD'];
    const index = order.indexOf(difficulty);
    return order.slice(0, index);
  };

  const handleStartQuiz = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await quizService.startAdaptiveQuiz(quizId, user.userId);
      setQuizTitle(data.quizTitle);
      setAttemptId(data.attemptId);
      setCurrentDifficulty(data.currentDifficulty || 'EASY');
      setQuestions(data.questions || []);
      setAnswers({});
      setStartTime(Date.now());

      // If resumed from a previous attempt, pre-populate passed rounds
      if (data.resumedFromPreviousAttempt && data.currentDifficulty !== 'EASY') {
        setResumedFromPrevious(true);
        const passedDifficulties = getDifficultiesBefore(data.currentDifficulty);
        setAllRounds(passedDifficulties.map((d) => ({ difficulty: d, score: null, previouslyPassed: true })));
      } else {
        setResumedFromPrevious(false);
        setAllRounds([]);
      }

      setPhase(PHASES.IN_ROUND);
    } catch (err) {
      console.error('Error starting quiz:', err);
      const message = err?.response?.data?.message || err?.message || '';
      if (message.includes('QUIZ_ALREADY_PASSED')) {
        setPhase(PHASES.ALREADY_PASSED);
      } else {
        setError('Failed to start quiz. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerSelect = (questionId, optionId) => {
    setAnswers((prev) => ({ ...prev, [questionId]: optionId }));
  };

  const handleShortAnswer = (questionId, text) => {
    setAnswers((prev) => ({ ...prev, [questionId]: text }));
  };

  const handleSubmitRound = async () => {
    if (Object.keys(answers).length === 0) {
      setError('Please answer at least one question before submitting.');
      return;
    }

    const timeTaken = startTime ? Math.round((Date.now() - startTime) / 1000) : 0;
    const timePerQuestion = questions.length > 0 ? Math.round(timeTaken / questions.length) : 0;

    // Include ALL questions — unanswered ones are sent as null so they count as incorrect
    const answerList = questions.map((q) => {
      const answer = answers[q.id];
      return {
        questionId: q.id,
        selectedOptionId: answer !== undefined && typeof answer === 'number' ? answer : null,
        userAnswer: answer !== undefined && typeof answer === 'string' ? answer : null,
        timeSpentSeconds: timePerQuestion,
      };
    });

    try {
      setLoading(true);
      setError(null);
      const result = await quizService.submitRound({
        attemptId,
        difficulty: currentDifficulty,
        answers: answerList,
        timeTakenSeconds: timeTaken,
      });

      setRoundResult(result);
      setAllRounds((prev) => [
        ...prev,
        { difficulty: currentDifficulty, score: result.roundScore, correct: result.correctCount, total: result.totalQuestions },
      ]);

      if (result.quizCompleted) {
        setPhase(PHASES.QUIZ_COMPLETE);
      } else {
        setPhase(PHASES.ROUND_RESULT);
      }
    } catch (err) {
      console.error('Error submitting round:', err);
      setError('Failed to submit answers. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleNextRound = () => {
    if (roundResult?.nextQuestions) {
      setQuestions(roundResult.nextQuestions);
      setCurrentDifficulty(roundResult.nextDifficulty);
      setAnswers({});
      setStartTime(Date.now());
      setPhase(PHASES.IN_ROUND);
    }
  };

  const handleBackToClassroom = () => {
    navigate(`/classroom/${courseId}`);
  };

  const getDifficultyColor = (difficulty) => {
    switch (difficulty?.toUpperCase()) {
      case 'EASY': return '#22c55e';
      case 'MEDIUM': return '#f59e0b';
      case 'HARD': return '#ef4444';
      default: return '#64748b';
    }
  };

  const renderPreQuiz = () => (
    <div className="quiz-pre">
      <div className="quiz-pre-card">
        <div className="quiz-pre-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 3c1.93 0 3.5 1.57 3.5 3.5S13.93 13 12 13s-3.5-1.57-3.5-3.5S10.07 6 12 6zm7 13H5v-.23c0-.62.28-1.2.76-1.58C7.47 15.82 9.64 15 12 15s4.53.82 6.24 2.19c.48.38.76.97.76 1.58V19z" />
          </svg>
        </div>
        <h1>Module Quiz</h1>
        <p className="quiz-pre-desc">
          This adaptive quiz will test your understanding of the module content.
          It starts with easy questions and increases in difficulty as you progress.
        </p>

        <div className="quiz-info-grid">
          <div className="quiz-info-item">
            <span className="qi-label">Format</span>
            <span className="qi-value">Adaptive (3 rounds)</span>
          </div>
          <div className="quiz-info-item">
            <span className="qi-label">Difficulty</span>
            <span className="qi-value">Easy &rarr; Medium &rarr; Hard</span>
          </div>
          <div className="quiz-info-item">
            <span className="qi-label">Pass Threshold</span>
            <span className="qi-value">70% per round</span>
          </div>
        </div>

        <button className="btn-start-quiz" onClick={handleStartQuiz} disabled={loading}>
          {loading ? 'Starting...' : 'Start Quiz'}
        </button>
      </div>
    </div>
  );

  const renderInRound = () => (
    <div className="quiz-round">
      <div className="round-header">
        <div className="round-difficulty" style={{ background: getDifficultyColor(currentDifficulty) }}>
          {currentDifficulty}
        </div>
        <h2>Round {allRounds.length + 1} of 3{resumedFromPrevious && allRounds.some((r) => r.previouslyPassed) ? ' (Resumed)' : ''}</h2>
        <span className="question-count">{questions.length} questions</span>
      </div>

      {resumedFromPrevious && allRounds.some((r) => r.previouslyPassed) && (
        <div className="resume-banner">
          <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
            <path d="M13 3a9 9 0 0 0-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42A8.954 8.954 0 0 0 13 21a9 9 0 0 0 0-18zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z" />
          </svg>
          <span>Resuming from <strong>{currentDifficulty}</strong> round — you passed the earlier rounds in a previous attempt.</span>
        </div>
      )}

      <div className="round-progress">
        <div className="round-dots">
          {['EASY', 'MEDIUM', 'HARD'].map((d) => {
            const prevPassed = allRounds.some((r) => r.difficulty === d && r.previouslyPassed);
            const doneThisAttempt = allRounds.some((r) => r.difficulty === d && !r.previouslyPassed);
            return (
              <div
                key={d}
                className={`round-dot ${d === currentDifficulty ? 'active' : ''} ${doneThisAttempt ? 'done' : ''} ${prevPassed ? 'done prev-passed' : ''}`}
                style={{ '--dot-color': getDifficultyColor(d) }}
              >
                <span className="dot-label">{d}</span>
                {prevPassed && <span className="dot-prev-label">Passed</span>}
              </div>
            );
          })}
        </div>
      </div>

      <div className="questions-list">
        {questions.map((q, index) => (
          <div key={q.id} className="question-card">
            <div className="question-number">Q{index + 1}</div>
            <div className="question-content">
              <p className="question-text">{q.questionText}</p>

              {q.questionType === 'MULTIPLE_CHOICE' && q.options && (
                <div className="options-list">
                  {q.options.map((opt) => (
                    <label
                      key={opt.id}
                      className={`option-item ${answers[q.id] === opt.id ? 'selected' : ''}`}
                    >
                      <input
                        type="radio"
                        name={`question-${q.id}`}
                        value={opt.id}
                        checked={answers[q.id] === opt.id}
                        onChange={() => handleAnswerSelect(q.id, opt.id)}
                      />
                      <span className="option-radio" />
                      <span className="option-text">{opt.optionText}</span>
                    </label>
                  ))}
                </div>
              )}

              {q.questionType === 'TRUE_FALSE' && (
                <div className="options-list tf-options">
                  {['True', 'False'].map((val) => (
                    <label
                      key={val}
                      className={`option-item ${answers[q.id] === val ? 'selected' : ''}`}
                    >
                      <input
                        type="radio"
                        name={`question-${q.id}`}
                        value={val}
                        checked={answers[q.id] === val}
                        onChange={() => handleShortAnswer(q.id, val)}
                      />
                      <span className="option-radio" />
                      <span className="option-text">{val}</span>
                    </label>
                  ))}
                </div>
              )}

              {q.questionType === 'SHORT_ANSWER' && (
                <input
                  type="text"
                  className="short-answer-input"
                  placeholder="Type your answer..."
                  value={answers[q.id] || ''}
                  onChange={(e) => handleShortAnswer(q.id, e.target.value)}
                />
              )}
            </div>
          </div>
        ))}
      </div>

      <div className="round-actions">
        <span className="answered-count">
          {Object.keys(answers).length} of {questions.length} answered
        </span>
        <button
          className="btn-submit-round"
          onClick={handleSubmitRound}
          disabled={loading || Object.keys(answers).length === 0}
        >
          {loading ? 'Submitting...' : 'Submit Answers'}
        </button>
      </div>
    </div>
  );

  const renderRoundResult = () => {
    const passed = roundResult?.escalated;
    return (
      <div className="quiz-result">
        <div className={`result-banner ${passed ? 'passed' : 'failed'}`}>
          <div className="result-icon">
            {passed ? (
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" /></svg>
            ) : (
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z" /></svg>
            )}
          </div>
          <h2>{passed ? 'Round Passed!' : 'Round Not Passed'}</h2>
          <p>
            You scored <strong>{roundResult?.roundScore}%</strong> ({roundResult?.correctCount}/{roundResult?.totalQuestions} correct)
          </p>
        </div>

        {roundResult?.questionResults && (
          <div className="result-details">
            <h3>Question Breakdown</h3>
            {roundResult.questionResults.map((qr, i) => (
              <div key={i} className={`result-question ${qr.isCorrect ? 'correct' : 'incorrect'}`}>
                <div className="rq-status">
                  {qr.isCorrect ? (
                    <svg viewBox="0 0 24 24" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" /></svg>
                  ) : (
                    <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" /></svg>
                  )}
                </div>
                <div className="rq-content">
                  <p className="rq-text">{qr.questionText}</p>
                  <div className="rq-answer-details">
                    {qr.userAnswer && (
                      <p className={`rq-user-answer ${qr.isCorrect ? 'correct' : 'incorrect'}`}>
                        <strong>Your answer:</strong> {qr.userAnswer}
                      </p>
                    )}
                    {!qr.isCorrect && qr.correctAnswer && (
                      <p className="rq-correct-answer">
                        <strong>Correct answer:</strong> {qr.correctAnswer}
                      </p>
                    )}
                  </div>
                  {!qr.isCorrect && qr.explanation && (
                    <div className="rq-explanation-box">
                      <span className="rq-explain-label">Explanation:</span>
                      <p>{qr.explanation}</p>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="result-actions">
          {passed && roundResult?.nextDifficulty ? (
            <button className="btn-next-round" onClick={handleNextRound}>
              Continue to {roundResult.nextDifficulty} Round
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z" /></svg>
            </button>
          ) : (
            <button className="btn-back-classroom" onClick={handleBackToClassroom}>
              Back to Classroom
            </button>
          )}
        </div>
      </div>
    );
  };

  const renderQuizComplete = () => {
    const passed = roundResult?.quizPassed;
    return (
      <div className="quiz-complete">
        <div className={`complete-banner ${passed ? 'passed' : 'failed'}`}>
          <div className="complete-icon">
            {passed ? (
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" /></svg>
            ) : (
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z" /></svg>
            )}
          </div>
          <h1>{passed ? 'Quiz Passed!' : 'Quiz Not Passed'}</h1>
          <p className="complete-subtitle">
            {passed
              ? 'Congratulations! You have demonstrated mastery of this module.'
              : 'Don\'t worry - review the suggested lessons and try again.'}
          </p>
        </div>

        <div className="rounds-summary">
          <h3>Round Summary</h3>
          <div className="rounds-grid">
            {allRounds.map((round, i) => (
              <div key={i} className={`round-summary-card ${round.previouslyPassed ? 'prev-passed' : ''}`}>
                <div className="rs-difficulty" style={{ background: getDifficultyColor(round.difficulty) }}>
                  {round.difficulty}
                </div>
                {round.previouslyPassed ? (
                  <>
                    <div className="rs-score prev">Passed</div>
                    <div className="rs-detail">Previous attempt</div>
                  </>
                ) : (
                  <>
                    <div className="rs-score">{round.score}%</div>
                    <div className="rs-detail">{round.correct}/{round.total} correct</div>
                  </>
                )}
              </div>
            ))}
          </div>
        </div>

        {roundResult?.questionResults && (
          <div className="result-details">
            <h3>Last Round - Question Breakdown</h3>
            {roundResult.questionResults.map((qr, i) => (
              <div key={i} className={`result-question ${qr.isCorrect ? 'correct' : 'incorrect'}`}>
                <div className="rq-status">
                  {qr.isCorrect ? (
                    <svg viewBox="0 0 24 24" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" /></svg>
                  ) : (
                    <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" /></svg>
                  )}
                </div>
                <div className="rq-content">
                  <p className="rq-text">{qr.questionText}</p>
                  <div className="rq-answer-details">
                    {qr.userAnswer && (
                      <p className={`rq-user-answer ${qr.isCorrect ? 'correct' : 'incorrect'}`}>
                        <strong>Your answer:</strong> {qr.userAnswer}
                      </p>
                    )}
                    {!qr.isCorrect && qr.correctAnswer && (
                      <p className="rq-correct-answer">
                        <strong>Correct answer:</strong> {qr.correctAnswer}
                      </p>
                    )}
                  </div>
                  {!qr.isCorrect && qr.explanation && (
                    <div className="rq-explanation-box">
                      <span className="rq-explain-label">Explanation:</span>
                      <p>{qr.explanation}</p>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        {!passed && roundResult?.lessonsToRevisit?.length > 0 && (
          <div className="revisit-lessons">
            <h3>Recommended Lessons to Review</h3>
            <p>Review these lessons to strengthen your understanding before retaking the quiz.</p>
            <div className="revisit-list">
              {roundResult.lessonsToRevisit.map((lesson) => (
                <button
                  key={lesson.id}
                  className="revisit-item"
                  onClick={() => navigate(`/classroom/${courseId}/lesson/${lesson.id}`)}
                >
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zM6 20V4h7v5h5v11H6z" />
                  </svg>
                  <span>{lesson.title}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="complete-actions">
          <button className="btn-back-classroom" onClick={handleBackToClassroom}>
            Back to Classroom
          </button>
          {!passed && (
            <button className="btn-retry" onClick={() => {
              setPhase(PHASES.PRE_QUIZ);
              setAllRounds([]);
              setRoundResult(null);
              setAnswers({});
              setResumedFromPrevious(false);
            }}>
              Retry Quiz
            </button>
          )}
        </div>
      </div>
    );
  };

  const renderAlreadyPassed = () => (
    <div className="quiz-complete">
      <div className="complete-banner passed">
        <div className="complete-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
          </svg>
        </div>
        <h1>Quiz Already Completed</h1>
        <p className="complete-subtitle">
          You have already passed this quiz. Head back to the classroom to continue with the next module.
        </p>
      </div>
      <div className="complete-actions">
        <button className="btn-back-classroom" onClick={handleBackToClassroom}>
          Back to Classroom
        </button>
      </div>
    </div>
  );

  return (
    <div className="adaptive-quiz-container">
      <header className="aq-header">
        <button className="aq-back-btn" onClick={handleBackToClassroom}>
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" />
          </svg>
          Back to Classroom
        </button>
        {quizTitle && <h2 className="aq-title">{quizTitle}</h2>}
        {phase === PHASES.IN_ROUND && (
          <div className="aq-difficulty-badge" style={{ background: getDifficultyColor(currentDifficulty) }}>
            {currentDifficulty}
          </div>
        )}
      </header>

      {error && (
        <div className="aq-error">
          <p>{error}</p>
          <button onClick={() => setError(null)}>Dismiss</button>
        </div>
      )}

      <main className="aq-content">
        {phase === PHASES.PRE_QUIZ && renderPreQuiz()}
        {phase === PHASES.IN_ROUND && renderInRound()}
        {phase === PHASES.ROUND_RESULT && renderRoundResult()}
        {phase === PHASES.QUIZ_COMPLETE && renderQuizComplete()}
        {phase === PHASES.ALREADY_PASSED && renderAlreadyPassed()}
      </main>
    </div>
  );
}
