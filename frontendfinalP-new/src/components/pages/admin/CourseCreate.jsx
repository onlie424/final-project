import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminService } from '../../../services/adminService';
import { courseService } from '../../../services/courseService';
import '../../../styles/admin/CourseCreate.css';

const STEPS = [
  { n: 1, label: 'Course Details' },
  { n: 2, label: 'Modules & Lessons' },
  { n: 3, label: 'Review & Save' },
];

function CourseCreate() {
  const navigate = useNavigate();
  const { courseId } = useParams();
  const isEditMode = !!courseId;

  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [loadingCourse, setLoadingCourse] = useState(false);
  const [error, setError] = useState(null);
  const [selectedModuleId, setSelectedModuleId] = useState(null);

  const [courseData, setCourseData] = useState({
    title: '',
    description: '',
    category: '',
    difficulty: 'BEGINNER',
    thumbnailUrl: '',
  });

  const [modules, setModules] = useState([]);

  useEffect(() => {
    if (isEditMode) loadCourseData();
  }, [courseId]);

  const loadCourseData = async () => {
    try {
      setLoadingCourse(true);
      setError(null);
      const course = await courseService.getCourseById(courseId);
      setCourseData({
        title: course.title || '',
        description: course.description || '',
        category: course.category || '',
        difficulty: course.difficulty || 'BEGINNER',
        thumbnailUrl: course.thumbnailUrl || '',
      });
      if (course.modules && course.modules.length > 0) {
        const loaded = course.modules.map(m => ({
          id: m.id, serverId: m.id,
          title: m.title || '', description: m.description || '',
          orderIndex: m.orderIndex || 1,
          lessons: (m.lessons || []).map(l => ({
            id: l.id, serverId: l.id,
            title: l.title || '', contentType: l.contentType || 'VIDEO',
            contentUrl: l.contentUrl || '', contentText: l.contentText || '',
            orderIndex: l.orderIndex || 1,
          }))
        }));
        setModules(loaded);
        setSelectedModuleId(loaded[0]?.id || null);
      }
    } catch (err) {
      setError('Failed to load course data.');
    } finally {
      setLoadingCourse(false);
    }
  };

  const handleCourseChange = (e) => {
    const { name, value } = e.target;
    setCourseData(prev => ({ ...prev, [name]: value }));
  };

  const addModule = () => {
    const newMod = {
      id: Date.now(), title: '', description: '',
      orderIndex: modules.length + 1, lessons: []
    };
    setModules(prev => [...prev, newMod]);
    setSelectedModuleId(newMod.id);
  };

  const updateModule = (moduleId, field, value) => {
    setModules(prev => prev.map(m => m.id === moduleId ? { ...m, [field]: value } : m));
  };

  const removeModule = (moduleId) => {
    const module = modules.find(m => m.id === moduleId);
    if (isEditMode && module?.serverId) {
      adminService.deleteModule(module.serverId).catch(() => {});
    }
    setModules(prev => {
      const filtered = prev.filter(m => m.id !== moduleId);
      const reindexed = filtered.map((m, idx) => ({ ...m, orderIndex: idx + 1 }));
      setSelectedModuleId(reindexed[0]?.id || null);
      return reindexed;
    });
  };

  const addLesson = (moduleId) => {
    setModules(prev => prev.map(m => {
      if (m.id !== moduleId) return m;
      return {
        ...m, lessons: [...m.lessons, {
          id: Date.now(), title: '', contentType: 'VIDEO',
          contentUrl: '', contentText: '',
          orderIndex: m.lessons.length + 1
        }]
      };
    }));
  };

  const updateLesson = (moduleId, lessonId, field, value) => {
    setModules(prev => prev.map(m => {
      if (m.id !== moduleId) return m;
      return { ...m, lessons: m.lessons.map(l => l.id === lessonId ? { ...l, [field]: value } : l) };
    }));
  };

  const removeLesson = (moduleId, lessonId) => {
    const module = modules.find(m => m.id === moduleId);
    const lesson = module?.lessons.find(l => l.id === lessonId);
    if (isEditMode && lesson?.serverId) {
      adminService.deleteLesson(lesson.serverId).catch(() => {});
    }
    setModules(prev => prev.map(m => {
      if (m.id !== moduleId) return m;
      const filtered = m.lessons.filter(l => l.id !== lessonId);
      return { ...m, lessons: filtered.map((l, idx) => ({ ...l, orderIndex: idx + 1 })) };
    }));
  };

  const handleSave = async () => {
    if (!courseData.title.trim() || !courseData.description.trim()) {
      setError('Course title and description are required.');
      setStep(1);
      return;
    }
    try {
      setLoading(true);
      setError(null);

      if (isEditMode) {
        await adminService.updateCourse(courseId, {
          title: courseData.title, description: courseData.description,
          category: courseData.category || 'General',
          difficulty: courseData.difficulty, thumbnailUrl: courseData.thumbnailUrl,
        });
        for (const module of modules) {
          if (!module.title.trim()) continue;
          if (module.serverId) {
            await adminService.updateModule(module.serverId, {
              courseId: Number(courseId), title: module.title,
              description: module.description, orderIndex: module.orderIndex
            });
            for (const lesson of module.lessons) {
              if (!lesson.title.trim()) continue;
              if (lesson.serverId) {
                await adminService.updateLesson(lesson.serverId, {
                  moduleId: module.serverId, title: lesson.title,
                  contentType: lesson.contentType || 'VIDEO',
                  contentUrl: lesson.contentUrl, contentText: lesson.contentText,
                  orderIndex: lesson.orderIndex
                });
              } else {
                await adminService.createLesson({
                  moduleId: module.serverId, title: lesson.title,
                  contentType: lesson.contentType || 'VIDEO',
                  contentUrl: lesson.contentUrl, contentText: lesson.contentText,
                  orderIndex: lesson.orderIndex
                });
              }
            }
          } else {
            const created = await adminService.createModule({
              courseId: Number(courseId), title: module.title,
              description: module.description, orderIndex: module.orderIndex
            });
            const newModuleId = created.id || created.moduleId;
            for (const lesson of module.lessons) {
              if (!lesson.title.trim()) continue;
              await adminService.createLesson({
                moduleId: newModuleId, title: lesson.title,
                contentType: lesson.contentType || 'VIDEO',
                contentUrl: lesson.contentUrl, contentText: lesson.contentText,
                orderIndex: lesson.orderIndex
              });
            }
          }
        }
      } else {
        const createdCourse = await adminService.createCourse({
          title: courseData.title, description: courseData.description,
          category: courseData.category || 'General',
          difficulty: courseData.difficulty, thumbnailUrl: courseData.thumbnailUrl,
          isPublished: false
        });
        const newCourseId = createdCourse.id || createdCourse.courseId;
        for (const module of modules) {
          if (!module.title.trim()) continue;
          const createdModule = await adminService.createModule({
            courseId: newCourseId, title: module.title,
            description: module.description, orderIndex: module.orderIndex
          });
          const newModuleId = createdModule.id || createdModule.moduleId;
          for (const lesson of module.lessons) {
            if (!lesson.title.trim()) continue;
            await adminService.createLesson({
              moduleId: newModuleId, title: lesson.title,
              contentType: lesson.contentType || 'VIDEO',
              contentUrl: lesson.contentUrl, contentText: lesson.contentText,
              orderIndex: lesson.orderIndex
            });
          }
        }
      }
      if (isEditMode) {
        await loadCourseData();
        setStep(1);
        setError(null);
      } else {
        navigate('/admin/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save course. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const selectedModule = modules.find(m => m.id === selectedModuleId);
  const canProceedStep1 = courseData.title.trim() && courseData.description.trim();

  if (loadingCourse) {
    return (
      <div className="course-wizard-page">
        <div className="wizard-loading">
          <div className="spinner" />
          <p>Loading course data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="course-wizard-page">
      {/* Header */}
      <div className="wizard-topbar">
        <button className="btn-back" onClick={() => navigate('/admin/dashboard')}>← Back to Dashboard</button>
        <h1>{isEditMode ? 'Edit Course' : 'Create New Course'}</h1>
      </div>

      {/* Step Indicator */}
      <div className="wizard-stepper">
        {STEPS.map((s, i) => (
          <div key={s.n} className="stepper-item">
            <div className={`stepper-circle ${step === s.n ? 'active' : ''} ${step > s.n ? 'done' : ''}`}>
              {step > s.n ? '✓' : s.n}
            </div>
            <span className={`stepper-label ${step === s.n ? 'active' : ''}`}>{s.label}</span>
            {i < STEPS.length - 1 && <div className={`stepper-line ${step > s.n ? 'done' : ''}`} />}
          </div>
        ))}
      </div>

      {error && <div className="wizard-error">{error}</div>}

      {/* ── Step 1: Course Details ── */}
      {step === 1 && (
        <div className="wizard-card">
          <div className="wizard-card-header">
            <h2>Course Details</h2>
            <p>Start with the basic information about your course</p>
          </div>
          <div className="wizard-card-body">
            <div className="form-group">
              <label>Course Title *</label>
              <input
                type="text" name="title" value={courseData.title}
                onChange={handleCourseChange}
                placeholder="e.g., Introduction to Machine Learning"
              />
            </div>
            <div className="form-group">
              <label>Description *</label>
              <textarea
                name="description" value={courseData.description}
                onChange={handleCourseChange} rows={5}
                placeholder="Describe what students will learn in this course..."
              />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Category</label>
                <select name="category" value={courseData.category} onChange={handleCourseChange}>
                  <option value="">Select a category</option>
                  <option value="Programming">Programming</option>
                  <option value="Data Science">Data Science</option>
                  <option value="Web Development">Web Development</option>
                  <option value="Machine Learning">Machine Learning</option>
                  <option value="Mathematics">Mathematics</option>
                  <option value="Business">Business</option>
                  <option value="Design">Design</option>
                  <option value="Other">Other</option>
                </select>
              </div>
              <div className="form-group">
                <label>Difficulty Level</label>
                <select name="difficulty" value={courseData.difficulty} onChange={handleCourseChange}>
                  <option value="BEGINNER">Beginner</option>
                  <option value="INTERMEDIATE">Intermediate</option>
                  <option value="ADVANCED">Advanced</option>
                </select>
              </div>
            </div>
            <div className="form-group">
              <label>Thumbnail URL</label>
              <input
                type="url" name="thumbnailUrl" value={courseData.thumbnailUrl}
                onChange={handleCourseChange}
                placeholder="https://example.com/image.jpg"
              />
              {courseData.thumbnailUrl && (
                <img src={courseData.thumbnailUrl} alt="Preview" className="thumbnail-preview" />
              )}
            </div>
          </div>
          <div className="wizard-card-footer">
            <button className="btn-ghost" onClick={() => navigate('/admin/dashboard')}>Cancel</button>
            <button
              className="btn-next"
              onClick={() => { setError(null); setStep(2); }}
              disabled={!canProceedStep1}
            >
              Next: Add Modules →
            </button>
          </div>
        </div>
      )}

      {/* ── Step 2: Modules & Lessons ── */}
      {step === 2 && (
        <div className="wizard-card wizard-card--wide">
          <div className="wizard-card-header">
            <h2>Modules & Lessons</h2>
            <p>Organise your course into modules, then add video lessons to each</p>
          </div>
          <div className="builder-layout">
            {/* Left: module list */}
            <aside className="builder-sidebar">
              <div className="builder-sidebar-head">
                <span>Modules <em>({modules.length})</em></span>
                <button type="button" className="btn-add-small" onClick={addModule}>+ Add</button>
              </div>
              {modules.length === 0 ? (
                <p className="sidebar-empty">No modules yet</p>
              ) : (
                modules.map((m, i) => (
                  <button
                    key={m.id}
                    className={`module-sidebar-btn ${selectedModuleId === m.id ? 'active' : ''}`}
                    onClick={() => setSelectedModuleId(m.id)}
                  >
                    <span className="msb-num">{i + 1}</span>
                    <span className="msb-title">{m.title || 'Untitled Module'}</span>
                    <span className="msb-count">{m.lessons.length}</span>
                  </button>
                ))
              )}
            </aside>

            {/* Right: module editor */}
            <div className="builder-editor">
              {!selectedModule ? (
                <div className="editor-empty">
                  <div className="editor-empty-icon">📦</div>
                  <p>Select a module on the left to edit it, or add your first module.</p>
                  <button type="button" className="btn-add" onClick={addModule}>+ Add First Module</button>
                </div>
              ) : (
                <>
                  <div className="editor-module-header">
                    <h3>Module {modules.indexOf(selectedModule) + 1}</h3>
                    <button type="button" className="btn-danger-sm" onClick={() => removeModule(selectedModule.id)}>
                      Remove Module
                    </button>
                  </div>

                  <div className="form-group">
                    <label>Module Title</label>
                    <input
                      type="text" value={selectedModule.title}
                      onChange={e => updateModule(selectedModule.id, 'title', e.target.value)}
                      placeholder="e.g., Getting Started"
                    />
                  </div>
                  <div className="form-group">
                    <label>Module Description</label>
                    <textarea
                      value={selectedModule.description}
                      onChange={e => updateModule(selectedModule.id, 'description', e.target.value)}
                      rows={2}
                      placeholder="Brief overview of this module..."
                    />
                  </div>

                  <div className="lessons-divider">
                    <span>Lessons</span>
                    <button type="button" className="btn-add-small" onClick={() => addLesson(selectedModule.id)}>
                      + Add Lesson
                    </button>
                  </div>

                  {selectedModule.lessons.length === 0 ? (
                    <div className="lessons-empty">
                      <p>No lessons yet — add one above.</p>
                    </div>
                  ) : (
                    <div className="lessons-stack">
                      {selectedModule.lessons.map((lesson, i) => (
                        <div key={lesson.id} className="lesson-block">
                          <div className="lesson-block-header">
                            <span className="lesson-block-num">Lesson {i + 1}</span>
                            <button
                              type="button"
                              className="btn-remove-xs"
                              onClick={() => removeLesson(selectedModule.id, lesson.id)}
                            >×</button>
                          </div>
                          <div className="form-group">
                            <label>Title</label>
                            <input
                              type="text" value={lesson.title}
                              onChange={e => updateLesson(selectedModule.id, lesson.id, 'title', e.target.value)}
                              placeholder="e.g., Introduction to Variables"
                            />
                          </div>
                          <div className="form-group">
                            <label>Video URL</label>
                            <input
                              type="url" value={lesson.contentUrl}
                              onChange={e => updateLesson(selectedModule.id, lesson.id, 'contentUrl', e.target.value)}
                              placeholder="https://youtube.com/embed/..."
                            />
                          </div>
                          <div className="form-group">
                            <label>Notes</label>
                            <textarea
                              value={lesson.contentText}
                              onChange={e => updateLesson(selectedModule.id, lesson.id, 'contentText', e.target.value)}
                              rows={2}
                              placeholder="Additional notes for this lesson..."
                            />
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
          <div className="wizard-card-footer">
            <button className="btn-ghost" onClick={() => setStep(1)}>← Back</button>
            <button className="btn-next" onClick={() => { setError(null); setStep(3); }}>
              Next: Review →
            </button>
          </div>
        </div>
      )}

      {/* ── Step 3: Review & Save ── */}
      {step === 3 && (
        <div className="wizard-card">
          <div className="wizard-card-header">
            <h2>Review & Save</h2>
            <p>Check your course details before saving</p>
          </div>
          <div className="review-body">
            <div className="review-course-row">
              {courseData.thumbnailUrl && (
                <img src={courseData.thumbnailUrl} alt="Thumbnail" className="review-thumb" />
              )}
              <div className="review-course-info">
                <h3>{courseData.title}</h3>
                <p>{courseData.description}</p>
                <div className="review-tags">
                  {courseData.category && <span className="review-tag">{courseData.category}</span>}
                  <span className="review-tag">{courseData.difficulty}</span>
                </div>
              </div>
            </div>

            <div className="review-modules-list">
              <h4>{modules.filter(m => m.title.trim()).length} Module{modules.filter(m => m.title.trim()).length !== 1 ? 's' : ''}</h4>
              {modules.filter(m => m.title.trim()).length === 0 ? (
                <p className="review-empty">No modules added — you can add them after saving.</p>
              ) : (
                modules.filter(m => m.title.trim()).map((m, i) => (
                  <div key={m.id} className="review-module-row">
                    <span className="review-mod-name">{i + 1}. {m.title}</span>
                    <span className="review-mod-count">{m.lessons.filter(l => l.title.trim()).length} lesson{m.lessons.filter(l => l.title.trim()).length !== 1 ? 's' : ''}</span>
                  </div>
                ))
              )}
            </div>
          </div>
          <div className="wizard-card-footer">
            <button className="btn-ghost" onClick={() => setStep(2)}>← Back</button>
            <button className="btn-save" onClick={handleSave} disabled={loading}>
              {loading ? 'Saving...' : (isEditMode ? 'Save Changes' : 'Create Course')}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default CourseCreate;
