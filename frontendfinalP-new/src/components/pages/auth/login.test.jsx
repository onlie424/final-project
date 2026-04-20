import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Login from './login';

// --- Mock react-router-dom navigate ---
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return { ...actual, useNavigate: () => mockNavigate };
});

// --- Mock AuthContext ---
const mockLogin = vi.fn();
vi.mock('../../../context/AuthContext', () => ({
  useAuth: () => ({ login: mockLogin }),
}));

// Wrap in MemoryRouter so Link components don't crash
const renderLogin = () =>
  render(
    <MemoryRouter>
      <Login />
    </MemoryRouter>
  );

beforeEach(() => {
  vi.clearAllMocks();
});

// =========================================================
// Rendering
// =========================================================

describe('Login — rendering', () => {
  it('renders the heading', () => {
    renderLogin();
    expect(screen.getByText('Welcome Back')).toBeInTheDocument();
  });

  it('renders email input', () => {
    renderLogin();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
  });

  it('renders password input', () => {
    renderLogin();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  it('renders the login button', () => {
    renderLogin();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('renders the register link', () => {
    renderLogin();
    expect(screen.getByRole('link', { name: /register here/i })).toBeInTheDocument();
  });

  it('does not show error message on initial render', () => {
    renderLogin();
    expect(screen.queryByRole('div', { className: 'error-message' })).not.toBeInTheDocument();
  });
});

// =========================================================
// Form input
// =========================================================

describe('Login — form input', () => {
  it('updates email field when typed into', () => {
    renderLogin();
    const emailInput = screen.getByLabelText(/email/i);
    fireEvent.change(emailInput, { target: { name: 'email', value: 'test@test.com' } });
    expect(emailInput.value).toBe('test@test.com');
  });

  it('updates password field when typed into', () => {
    renderLogin();
    const passwordInput = screen.getByLabelText(/password/i);
    fireEvent.change(passwordInput, { target: { name: 'password', value: 'secret123' } });
    expect(passwordInput.value).toBe('secret123');
  });
});

// =========================================================
// Submission — success
// =========================================================

describe('Login — successful submission', () => {
  it('navigates to user dashboard when role is USER', async () => {
    mockLogin.mockResolvedValueOnce({ role: 'USER' });
    renderLogin();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { name: 'email', value: 'user@test.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { name: 'password', value: 'password123' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/user/dashboard');
    });
  });

  it('navigates to admin dashboard when role is ADMIN', async () => {
    mockLogin.mockResolvedValueOnce({ role: 'ADMIN' });
    renderLogin();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { name: 'email', value: 'admin@test.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { name: 'password', value: 'adminpass' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/admin/dashboard');
    });
  });

  it('calls login with the entered email and password', async () => {
    mockLogin.mockResolvedValueOnce({ role: 'USER' });
    renderLogin();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { name: 'email', value: 'user@test.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { name: 'password', value: 'mypassword' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('user@test.com', 'mypassword');
    });
  });
});

// =========================================================
// Submission — loading state
// =========================================================

describe('Login — loading state', () => {
  it('disables the button while logging in', async () => {
    // login never resolves so we can inspect loading state
    mockLogin.mockReturnValue(new Promise(() => {}));
    renderLogin();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { name: 'email', value: 'user@test.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { name: 'password', value: 'pass' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /logging in/i })).toBeDisabled();
    });
  });
});

// =========================================================
// Submission — error state
// =========================================================

describe('Login — error state', () => {
  it('shows error message when login fails', async () => {
    mockLogin.mockRejectedValueOnce('Invalid credentials');
    renderLogin();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { name: 'email', value: 'bad@test.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { name: 'password', value: 'wrongpass' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
    });
  });

  it('clears error when user starts typing after a failed login', async () => {
    mockLogin.mockRejectedValueOnce('Invalid credentials');
    renderLogin();

    fireEvent.change(screen.getByLabelText(/email/i), { target: { name: 'email', value: 'bad@test.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { name: 'password', value: 'wrong' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
    });

    // Typing in any field should clear the error
    fireEvent.change(screen.getByLabelText(/email/i), { target: { name: 'email', value: 'new@test.com' } });
    expect(screen.queryByText('Invalid credentials')).not.toBeInTheDocument();
  });
});
