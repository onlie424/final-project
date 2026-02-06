import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { adminService } from '../../../services/adminService';
import { userService } from '../../../services/userService';

// Import admin components
import {
  AdminSidebar,
  AdminTopNav,
  AdminOverview,
  AdminCourses,
  AdminUsers,
  AdminAnalytics,
  AdminSettings
} from './components';

import '../../../styles/admin/Dashboard.css';

function AdminDashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [courses, setCourses] = useState([]);
  const [users, setUsers] = useState([]);
  const [stats, setStats] = useState({
    totalCourses: 0,
    publishedCourses: 0,
    draftCourses: 0,
    totalLessons: 0,
    totalUsers: 0,
    adminCount: 0,
    userCount: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  useEffect(() => {
    if (activeTab === 'users') {
      fetchUsers();
    }
  }, [activeTab]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      const coursesData = await adminService.getAllCoursesAdmin();
      setCourses(coursesData);

      const userStats = await userService.getUserStats();

      const publishedCount = coursesData.filter(c => c.isPublished).length;
      const draftCount = coursesData.filter(c => !c.isPublished).length;
      const totalLessonsCount = coursesData.reduce((sum, c) => sum + (c.totalLessons || 0), 0);

      setStats({
        totalCourses: coursesData.length,
        publishedCourses: publishedCount,
        draftCourses: draftCount,
        totalLessons: totalLessonsCount,
        totalUsers: userStats.totalUsers || 0,
        adminCount: userStats.adminCount || 0,
        userCount: userStats.userCount || 0,
      });

    } catch (err) {
      console.error('Error fetching admin data:', err);
      setError('Failed to load dashboard data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    try {
      const usersData = await userService.getAllUsers();
      setUsers(usersData);
    } catch (err) {
      console.error('Error fetching users:', err);
      setError('Failed to load users. Please try again.');
    }
  };

  // Course handlers
  const handlePublishCourse = async (courseId) => {
    try {
      await adminService.publishCourse(courseId);
      fetchDashboardData();
    } catch (err) {
      console.error('Error publishing course:', err);
    }
  };

  const handleUnpublishCourse = async (courseId) => {
    try {
      await adminService.unpublishCourse(courseId);
      fetchDashboardData();
    } catch (err) {
      console.error('Error unpublishing course:', err);
    }
  };

  const handleDeleteCourse = async (courseId) => {
    if (window.confirm('Are you sure you want to delete this course? This will also remove all enrollments, modules, and lessons.')) {
      try {
        await adminService.deleteCourse(courseId);
        fetchDashboardData();
      } catch (err) {
        console.error('Error deleting course:', err);
        setError(err.response?.data?.message || 'Failed to delete course. Please try again.');
      }
    }
  };

  // User handlers
  const handleDeleteUser = async (userId) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        await userService.deleteUser(userId);
        fetchUsers();
        fetchDashboardData();
      } catch (err) {
        console.error('Error deleting user:', err);
      }
    }
  };

  const handleUpdateUserRole = async (userId, newRole) => {
    try {
      await userService.updateUserRole(userId, newRole);
      fetchUsers();
      fetchDashboardData();
    } catch (err) {
      console.error('Error updating user role:', err);
    }
  };

  // Render content based on active tab
  const renderContent = () => {
    switch (activeTab) {
      case 'overview':
        return (
          <AdminOverview
            stats={stats}
            courses={courses}
            setActiveTab={setActiveTab}
            onRefresh={fetchDashboardData}
          />
        );
      case 'courses':
        return (
          <AdminCourses
            courses={courses}
            onPublish={handlePublishCourse}
            onUnpublish={handleUnpublishCourse}
            onDelete={handleDeleteCourse}
          />
        );
      case 'users':
        return (
          <AdminUsers
            users={users}
            stats={stats}
            currentUserId={user?.userId}
            onUpdateRole={handleUpdateUserRole}
            onDelete={handleDeleteUser}
          />
        );
      case 'analytics':
        return <AdminAnalytics />;
      case 'settings':
        return <AdminSettings />;
      default:
        return (
          <AdminOverview
            stats={stats}
            courses={courses}
            setActiveTab={setActiveTab}
            onRefresh={fetchDashboardData}
          />
        );
    }
  };

  if (loading) {
    return (
      <div className="admin-loading">
        <div className="spinner"></div>
        <p>Loading admin dashboard...</p>
      </div>
    );
  }

  return (
    <div className="admin-layout">
      <AdminTopNav />
      <AdminSidebar activeTab={activeTab} setActiveTab={setActiveTab} />

      <main className="admin-main">
        {/* Welcome Header */}
        <div className="admin-header">
          <div className="header-content">
            <h1>Welcome back, {user?.fullName || 'Administrator'}!</h1>
            <p>Here's what's happening with your platform today.</p>
          </div>
          <button className="btn-primary" onClick={() => navigate('/admin/courses/create')}>
            + Create New Course
          </button>
        </div>

        {/* Error Message */}
        {error && (
          <div className="error-banner">
            <p>{error}</p>
            <button onClick={fetchDashboardData}>Retry</button>
          </div>
        )}

        {renderContent()}
      </main>
    </div>
  );
}

export default AdminDashboard;
