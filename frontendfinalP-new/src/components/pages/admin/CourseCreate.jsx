import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminService } from '../../../services/adminService';
import { courseService } from '../../../services/courseService';
import '../../../styles/admin/CourseCreate.css';

function CourseCreate() {
  const navigate = useNavigate();
  const { courseId } = useParams();
  const isEditMode = !!courseId;

  const [loading, setLoading] = useState(false);
  const [loadingCourse, setLoadingCourse] = useState(false);
  const [error, setError] = useState(null);

  // Course basic info
  const [courseData, setCourseData] = useState({
    title: '',
    description: '',
    category: '',
    difficulty: 'BEGINNER',
    thumbnailUrl: '',
  });

  // Modules with their lessons
  const [modules, setModules] = useState([]);

  // Load existing course data in edit mode
  useEffect(() => {
    if (isEditMode) {
      loadCourseData();
    }
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
        const loadedModules = course.modules.map(m => ({
          id: m.id,
          serverId: m.id,
          title: m.title || '',
          description: m.description || '',
          orderIndex: m.orderIndex || 1,
          lessons: (m.lessons || []).map(l => ({
            id: l.id,
            serverId: l.id,
            title: l.title || '',
            contentType: l.contentType || 'VIDEO',
            contentUrl: l.contentUrl || '',
            contentText: l.contentText || '',
            orderIndex: l.orderIndex || 1,
          }))
        }));
        setModules(loadedModules);
      }
    } catch (err) {
      console.error('Error loading course:', err);
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
    setModules(prev => [
      ...prev,
      {
        id: Date.now(),
        title: '',
        description: '',
        orderIndex: prev.length + 1,
        lessons: []
      }
    ]);
  };

  const updateModule = (moduleId, field, value) => {
    setModules(prev => prev.map(m =>
      m.id === moduleId ? { ...m, [field]: value } : m
    ));
  };

  const removeModule = (moduleId) => {
    const module = modules.find(m => m.id === moduleId);
    if (isEditMode && module?.serverId) {
      adminService.deleteModule(module.serverId).catch(err => {
        console.error('Error deleting module:', err);
      });
    }
    setModules(prev => {
      const filtered = prev.filter(m => m.id !== moduleId);
      return filtered.map((m, idx) => ({ ...m, orderIndex: idx + 1 }));
    });
  };

  const addLesson = (moduleId) => {
    setModules(prev => prev.map(m => {
      if (m.id === moduleId) {
        return {
          ...m,
          lessons: [
            ...m.lessons,
            {
              id: Date.now(),
              title: '',
              contentType: 'VIDEO',
              contentUrl: '',
              contentText: '',
              orderIndex: m.lessons.length + 1
            }
          ]
        };
      }
      return m;
    }));
  };

  const updateLesson = (moduleId, lessonId, field, value) => {
    setModules(prev => prev.map(m => {
      if (m.id === moduleId) {
        return {
          ...m,
          lessons: m.lessons.map(l =>
            l.id === lessonId ? { ...l, [field]: value } : l
          )
        };
      }
      return m;
    }));
  };

  const removeLesson = (moduleId, lessonId) => {
    const module = modules.find(m => m.id === moduleId);
    const lesson = module?.lessons.find(l => l.id === lessonId);
    if (isEditMode && lesson?.serverId) {
      adminService.deleteLesson(lesson.serverId).catch(err => {
        console.error('Error deleting lesson:', err);
      });
    }
    setModules(prev => prev.map(m => {
      if (m.id === moduleId) {
        const filteredLessons = m.lessons.filter(l => l.id !== lessonId);
        return {
          ...m,
          lessons: filteredLessons.map((l, idx) => ({ ...l, orderIndex: idx + 1 }))
        };
      }
      return m;
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!courseData.title.trim()) {
      setError('Course title is required');
      return;
    }

    if (!courseData.description.trim()) {
      setError('Course description is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      if (isEditMode) {
        // Update existing course
        await adminService.updateCourse(courseId, {
          title: courseData.title,
          description: courseData.description,
          category: courseData.category || 'General',
          difficulty: courseData.difficulty,
          thumbnailUrl: courseData.thumbnailUrl,
        });

        for (const module of modules) {
          if (!module.title.trim()) continue;

          if (module.serverId) {
            await adminService.updateModule(module.serverId, {
              courseId: Number(courseId),
              title: module.title,
              description: module.description,
              orderIndex: module.orderIndex
            });

            for (const lesson of module.lessons) {
              if (!lesson.title.trim()) continue;

              if (lesson.serverId) {
                await adminService.updateLesson(lesson.serverId, {
                  moduleId: module.serverId,
                  title: lesson.title,
                  contentType: lesson.contentType || 'VIDEO',
                  contentUrl: lesson.contentUrl,
                  contentText: lesson.contentText,
                  orderIndex: lesson.orderIndex
                });
              } else {
                await adminService.createLesson({
                  moduleId: module.serverId,
                  title: lesson.title,
                  contentType: lesson.contentType || 'VIDEO',
                  contentUrl: lesson.contentUrl,
                  contentText: lesson.contentText,
                  orderIndex: lesson.orderIndex
                });
              }
            }
          } else {
            const createdModule = await adminService.createModule({
              courseId: Number(courseId),
              title: module.title,
              description: module.description,
              orderIndex: module.orderIndex
            });

            const newModuleId = createdModule.id || createdModule.moduleId;

            for (const lesson of module.lessons) {
              if (!lesson.title.trim()) continue;
              await adminService.createLesson({
                moduleId: newModuleId,
                title: lesson.title,
                contentType: lesson.contentType || 'VIDEO',
                contentUrl: lesson.contentUrl,
                contentText: lesson.contentText,
                orderIndex: lesson.orderIndex
              });
            }
          }
        }
      } else {
        // Create new course
        const createdCourse = await adminService.createCourse({
          title: courseData.title,
          description: courseData.description,
          category: courseData.category || 'General',
          difficulty: courseData.difficulty,
          thumbnailUrl: courseData.thumbnailUrl,
          isPublished: false
        });

        const newCourseId = createdCourse.id || createdCourse.courseId;

        for (const module of modules) {
          if (!module.title.trim()) continue;

          const createdModule = await adminService.createModule({
            courseId: newCourseId,
            title: module.title,
            description: module.description,
            orderIndex: module.orderIndex
          });

          const newModuleId = createdModule.id || createdModule.moduleId;

          for (const lesson of module.lessons) {
            if (!lesson.title.trim()) continue;
            await adminService.createLesson({
              moduleId: newModuleId,
              title: lesson.title,
              contentType: lesson.contentType || 'VIDEO',
              contentUrl: lesson.contentUrl,
              contentText: lesson.contentText,
              orderIndex: lesson.orderIndex
            });
          }
        }
      }

      navigate('/admin/dashboard');
    } catch (err) {
      console.error('Error saving course:', err);
      setError(err.response?.data?.message || 'Failed to save course. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loadingCourse) {
    return (
      <div className="course-create-page">
        <div className="dashboard-loading">
          <div className="spinner"></div>
          <p>Loading course data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="course-create-page">
      <div className="course-create-header">
        <button className="btn-back" onClick={() => navigate('/admin/dashboard')}>
          ← Back to Dashboard
        </button>
        <h1>{isEditMode ? 'Edit Course' : 'Create New Course'}</h1>
      </div>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="course-create-form">
        {/* Course Basic Info */}
        <section className="form-section">
          <h2>Course Information</h2>

          <div className="form-group">
            <label htmlFor="title">Course Title *</label>
            <input
              type="text"
              id="title"
              name="title"
              value={courseData.title}
              onChange={handleCourseChange}
              placeholder="e.g., Introduction to Machine Learning"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description *</label>
            <textarea
              id="description"
              name="description"
              value={courseData.description}
              onChange={handleCourseChange}
              placeholder="Describe what students will learn in this course..."
              rows={4}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="category">Category</label>
              <select
                id="category"
                name="category"
                value={courseData.category}
                onChange={handleCourseChange}
              >
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
              <label htmlFor="difficulty">Difficulty Level</label>
              <select
                id="difficulty"
                name="difficulty"
                value={courseData.difficulty}
                onChange={handleCourseChange}
              >
                <option value="BEGINNER">Beginner</option>
                <option value="INTERMEDIATE">Intermediate</option>
                <option value="ADVANCED">Advanced</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="thumbnailUrl">Thumbnail URL</label>
            <input
              type="url"
              id="thumbnailUrl"
              name="thumbnailUrl"
              value={courseData.thumbnailUrl}
              onChange={handleCourseChange}
              placeholder="https://example.com/image.jpg"
            />
          </div>
        </section>

        {/* Modules Section */}
        <section className="form-section">
          <div className="section-header">
            <h2>Course Modules</h2>
            <button type="button" className="btn-add" onClick={addModule}>
              + Add Module
            </button>
          </div>

          {modules.length === 0 ? (
            <div className="empty-modules">
              <p>No modules added yet. Click "Add Module" to start building your course structure.</p>
            </div>
          ) : (
            <div className="modules-list">
              {modules.map((module, moduleIndex) => (
                <div key={module.id} className="module-card">
                  <div className="module-header">
                    <span className="module-number">Module {moduleIndex + 1}</span>
                    <button
                      type="button"
                      className="btn-remove"
                      onClick={() => removeModule(module.id)}
                    >
                      Remove
                    </button>
                  </div>

                  <div className="form-group">
                    <label>Module Title</label>
                    <input
                      type="text"
                      value={module.title}
                      onChange={(e) => updateModule(module.id, 'title', e.target.value)}
                      placeholder="e.g., Getting Started"
                    />
                  </div>

                  <div className="form-group">
                    <label>Module Description</label>
                    <textarea
                      value={module.description}
                      onChange={(e) => updateModule(module.id, 'description', e.target.value)}
                      placeholder="Briefly describe what this module covers..."
                      rows={2}
                    />
                  </div>

                  {/* Lessons */}
                  <div className="lessons-section">
                    <div className="lessons-header">
                      <h4>Lessons</h4>
                      <button
                        type="button"
                        className="btn-add-small"
                        onClick={() => addLesson(module.id)}
                      >
                        + Add Lesson
                      </button>
                    </div>

                    {module.lessons.length === 0 ? (
                      <p className="no-lessons">No lessons in this module</p>
                    ) : (
                      <div className="lessons-list">
                        {module.lessons.map((lesson, lessonIndex) => (
                          <div key={lesson.id} className="lesson-card">
                            <div className="lesson-header">
                              <span className="lesson-number">Lesson {lessonIndex + 1}</span>
                              <button
                                type="button"
                                className="btn-remove-small"
                                onClick={() => removeLesson(module.id, lesson.id)}
                              >
                                ×
                              </button>
                            </div>

                            <div className="lesson-fields">
                              <div className="form-group">
                                <label>Lesson Title</label>
                                <input
                                  type="text"
                                  value={lesson.title}
                                  onChange={(e) => updateLesson(module.id, lesson.id, 'title', e.target.value)}
                                  placeholder="e.g., Introduction to Variables"
                                />
                              </div>

                              <div className="form-group">
                                <label>Video URL</label>
                                <input
                                  type="url"
                                  value={lesson.contentUrl}
                                  onChange={(e) => updateLesson(module.id, lesson.id, 'contentUrl', e.target.value)}
                                  placeholder="https://youtube.com/embed/..."
                                />
                              </div>

                              <div className="form-group">
                                <label>Content / Notes</label>
                                <textarea
                                  value={lesson.contentText}
                                  onChange={(e) => updateLesson(module.id, lesson.id, 'contentText', e.target.value)}
                                  placeholder="Additional content or notes for this lesson..."
                                  rows={3}
                                />
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Submit Buttons */}
        <div className="form-actions">
          <button
            type="button"
            className="btn-cancel"
            onClick={() => navigate('/admin/dashboard')}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn-submit"
            disabled={loading}
          >
            {loading
              ? (isEditMode ? 'Saving Changes...' : 'Creating Course...')
              : (isEditMode ? 'Save Changes' : 'Create Course')
            }
          </button>
        </div>
      </form>
    </div>
  );
}

export default CourseCreate;
