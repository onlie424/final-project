import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminService } from '../../../services/adminService';
import '../../../styles/admin/CourseCreate.css';

function CourseCreate() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
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

  // Handle course field changes
  const handleCourseChange = (e) => {
    const { name, value } = e.target;
    setCourseData(prev => ({ ...prev, [name]: value }));
  };

  // Add a new module
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

  // Update module
  const updateModule = (moduleId, field, value) => {
    setModules(prev => prev.map(m =>
      m.id === moduleId ? { ...m, [field]: value } : m
    ));
  };

  // Remove module
  const removeModule = (moduleId) => {
    setModules(prev => {
      const filtered = prev.filter(m => m.id !== moduleId);
      return filtered.map((m, idx) => ({ ...m, orderIndex: idx + 1 }));
    });
  };

  // Add lesson to a module
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

  // Update lesson
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

  // Remove lesson
  const removeLesson = (moduleId, lessonId) => {
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

  // Submit the course
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

      // 1. Create the course
      const createdCourse = await adminService.createCourse({
        title: courseData.title,
        description: courseData.description,
        category: courseData.category || 'General',
        difficulty: courseData.difficulty,
        thumbnailUrl: courseData.thumbnailUrl,
        isPublished: false
      });
      console.log('Created course response:', createdCourse);

      // Handle both 'id' and 'courseId' field names from backend
      const courseId = createdCourse.id || createdCourse.courseId;
      console.log('Using courseId:', courseId);

      // 2. Create modules and lessons
      for (const module of modules) {
        if (!module.title.trim()) continue;

        const createdModule = await adminService.createModule({
          courseId: courseId,
          title: module.title,
          description: module.description,
          orderIndex: module.orderIndex
        });
        console.log('Created module response:', createdModule);

        // Handle both 'id' and 'moduleId' field names from backend
        const moduleId = createdModule.id || createdModule.moduleId;
        console.log('Using moduleId:', moduleId);

        // Create lessons for this module
        for (const lesson of module.lessons) {
          if (!lesson.title.trim()) continue;

          console.log('Creating lesson with moduleId:', moduleId);
          const createdLesson = await adminService.createLesson({
            moduleId: moduleId,
            title: lesson.title,
            contentType: lesson.contentType || 'VIDEO',
            contentUrl: lesson.contentUrl,
            contentText: lesson.contentText,
            orderIndex: lesson.orderIndex
          });
        }
      }

      // Navigate back to admin dashboard
      navigate('/admin/dashboard');
    } catch (err) {
      console.error('Error creating course:', err);
      setError(err.response?.data?.message || 'Failed to create course. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="course-create-page">
      <div className="course-create-header">
        <button className="btn-back" onClick={() => navigate('/admin/dashboard')}>
          ← Back to Dashboard
        </button>
        <h1>Create New Course</h1>
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
                                <label>Content Type</label>
                                <select
                                  value={lesson.contentType}
                                  onChange={(e) => updateLesson(module.id, lesson.id, 'contentType', e.target.value)}
                                >
                                  <option value="VIDEO">Video</option>
                                  <option value="PDF">PDF</option>
                                  <option value="TEXT">Text / Reading</option>
                                </select>
                              </div>

                              <div className="form-group">
                                <label>{lesson.contentType === 'VIDEO' ? 'Video URL' : lesson.contentType === 'PDF' ? 'PDF URL' : 'Content URL'}</label>
                                <input
                                  type="url"
                                  value={lesson.contentUrl}
                                  onChange={(e) => updateLesson(module.id, lesson.id, 'contentUrl', e.target.value)}
                                  placeholder={lesson.contentType === 'VIDEO' ? 'https://youtube.com/embed/...' : 'https://example.com/file.pdf'}
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
            {loading ? 'Creating Course...' : 'Create Course'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default CourseCreate;
