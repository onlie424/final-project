import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Register from './registration';

// --- Mock react-router-dom navigate ---
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return { ...actual, useNavigate: () => mockNavigate };
});

// --- Mock AuthContext ---
const mockRegister = vi.fn();
vi.mock('../../../context/AuthContext', () => ({
  useAuth: () => ({ register: mockRegister }),
}));

const renderRegister = () =>
  render(
    <MemoryRouter>
      <Register />
    </MemoryRouter>
  );

// Helper to fill the form
const fillForm = ({ fullName = 'John Doe', email = 'john@test.com', password = 'password123', confirmPassword = 'password123' } = {}) => {
  fireEvent.change(screen.getByLabelText(/fullname/i), { target: { name: 'fullName', value: fullName } });
  fireEvent.change(screen.getByLabelText(/email/i),    { target: { name: 'email',    value: email } });
  fireEvent.change(screen.getByLabelText(/^password$/i), { target: { name: 'password', value: password } });
  fireEvent.change(screen.getByLabelText(/confirm password/i), { target: { name: 'confirmPassword', value: confirmPassword } });
};

beforeEach(() => {
  vi.clearAllMocks();
});

// =========================================================
// Rendering
// =========================================================

describe('Register — rendering', () => {
  it('renders the heading', () => {
    renderRegister();
    expect(screen.getByText('Create Account')).toBeInTheDocument();
  });

  it('renders full name input', () => {
    renderRegister();
    expect(screen.getByLabelText(/fullname/i)).toBeInTheDocument();
  });

  it('renders email input', () => {
    renderRegister();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
  });

  it('renders password input', () => {
    renderRegister();
    expect(screen.getByLabelText(/^password$/i)).toBeInTheDocument();
  });

  it('renders confirm password input', () => {
    renderRegister();
    expect(screen.getByLabelText(/confirm password/i)).toBeInTheDocument();
  });

  it('renders the register button', () => {
    renderRegister();
    expect(screen.getByRole('button', { name: /^register$/i })).toBeInTheDocument();
  });

  it('renders the login link', () => {
    renderRegister();
    expect(screen.getByRole('link', { name: /login here/i })).toBeInTheDocument();
  });
});

// =========================================================
// Form input
// =========================================================

describe('Register — form input', () => {
  it('updates full name field when typed into', () => {
    renderRegister();
    const input = screen.getByLabelText(/fullname/i);
    fireEvent.change(input, { target: { name: 'fullName', value: 'Jane Smith' } });
    expect(input.value).toBe('Jane Smith');
  });

  it('updates email field when typed into', () => {
    renderRegister();
    const input = screen.getByLabelText(/email/i);
    fireEvent.change(input, { target: { name: 'email', value: 'jane@test.com' } });
    expect(input.value).toBe('jane@test.com');
  });
});

// =========================================================
// Validation — password rules
// =========================================================

describe('Register — validation', () => {
  it('shows error when passwords do not match', async () => {
    renderRegister();
    fillForm({ password: 'password123', confirmPassword: 'different123' });
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(screen.getByText('Passwords do not match')).toBeInTheDocument();
    });
  });

  it('does not call register when passwords do not match', async () => {
    renderRegister();
    fillForm({ password: 'password123', confirmPassword: 'different123' });
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(mockRegister).not.toHaveBeenCalled();
    });
  });

  it('shows error when password is shorter than 6 characters', async () => {
    renderRegister();
    fillForm({ password: 'abc', confirmPassword: 'abc' });
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(screen.getByText('Password must be at least 6 characters long')).toBeInTheDocument();
    });
  });

  it('does not call register when password is too short', async () => {
    renderRegister();
    fillForm({ password: 'abc', confirmPassword: 'abc' });
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(mockRegister).not.toHaveBeenCalled();
    });
  });

  it('clears error when user types after a validation error', async () => {
    renderRegister();
    fillForm({ password: 'abc', confirmPassword: 'abc' });
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(screen.getByText('Password must be at least 6 characters long')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText(/email/i), { target: { name: 'email', value: 'new@test.com' } });
    expect(screen.queryByText('Password must be at least 6 characters long')).not.toBeInTheDocument();
  });
});

// =========================================================
// Submission — success
// =========================================================

describe('Register — successful submission', () => {
  it('navigates to user dashboard when role is USER', async () => {
    mockRegister.mockResolvedValueOnce({ role: 'USER' });
    renderRegister();
    fillForm();
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/user/dashboard');
    });
  });

  it('navigates to admin dashboard when role is ADMIN', async () => {
    mockRegister.mockResolvedValueOnce({ role: 'ADMIN' });
    renderRegister();
    fillForm();
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/admin/dashboard');
    });
  });

  it('calls register with fullName, email, and password', async () => {
    mockRegister.mockResolvedValueOnce({ role: 'USER' });
    renderRegister();
    fillForm({ fullName: 'John Doe', email: 'john@test.com', password: 'securepass', confirmPassword: 'securepass' });
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(mockRegister).toHaveBeenCalledWith('John Doe', 'john@test.com', 'securepass');
    });
  });
});

// =========================================================
// Submission — loading state
// =========================================================

describe('Register — loading state', () => {
  it('disables the button and shows "Creating Account..." while submitting', async () => {
    mockRegister.mockReturnValue(new Promise(() => {})); // never resolves
    renderRegister();
    fillForm();
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /creating account/i })).toBeDisabled();
    });
  });
});

// =========================================================
// Submission — error state
// =========================================================

describe('Register — error state', () => {
  it('shows error message when registration fails', async () => {
    mockRegister.mockRejectedValueOnce('Email already in use');
    renderRegister();
    fillForm();
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(screen.getByText('Email already in use')).toBeInTheDocument();
    });
  });

  it('re-enables the button after a failed registration', async () => {
    mockRegister.mockRejectedValueOnce('Email already in use');
    renderRegister();
    fillForm();
    fireEvent.click(screen.getByRole('button', { name: /^register$/i }));

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /^register$/i })).not.toBeDisabled();
    });
  });
});
