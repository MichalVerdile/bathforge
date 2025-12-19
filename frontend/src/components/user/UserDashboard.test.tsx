import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import UserDashboard from './UserDashboard';
import authService from '../../controllers/api/auth/authService';
import userService from '../../controllers/api/user/userService';

// Mock the services
vi.mock('../../controllers/api/auth/authService', () => ({
  default: {
    getCurrentUser: vi.fn(),
    logout: vi.fn(),
    isAuthenticated: vi.fn(() => true),
    isAdmin: vi.fn(() => false),
    getAuthHeader: vi.fn(() => ({ Authorization: 'Bearer mock-token' })),
  },
}));

vi.mock('../../controllers/api/user/userService', () => ({
  default: {
    getUserScenes: vi.fn(),
    getUserQuoteRequests: vi.fn(),
  },
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('UserDashboard', () => {
  const mockUser = {
    email: 'test@example.com',
    userId: 1,
    firstName: 'Test',
    lastName: 'User',
    role: 'USER',
    token: 'mock-token',
  };

  const mockScenes = [
    {
      id: 1,
      name: 'My Bathroom',
      description: 'Test bathroom scene',
      user: 'test@example.com',
      sceneData: '{}',
      cameraPosition: '0,0,0',
      lightingSettings: '{}',
      backgroundColor: '#ffffff',
      isPublic: false,
      createdAt: '2024-01-15',
      updatedAt: '2024-01-15',
    },
  ];

  const mockQuotes = [
    {
      id: 1,
      status: 'PENDING',
      roomDimensions: '3x4x2.5',
      additionalNotes: 'Test notes',
      sceneSnapshot: 'data:image/png;base64,test',
      createdAt: '2024-01-20',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(authService.getCurrentUser).mockReturnValue(mockUser);
    vi.mocked(userService.getUserScenes).mockResolvedValue(mockScenes);
    vi.mocked(userService.getUserQuoteRequests).mockResolvedValue(mockQuotes);
  });

  const renderWithRouter = (component: React.ReactElement) => {
    return render(<BrowserRouter>{component}</BrowserRouter>);
  };

  it('should display loading state initially', () => {
    renderWithRouter(<UserDashboard />);
    
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('should display user data after loading', async () => {
    renderWithRouter(<UserDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/Welcome, Test!/i)).toBeInTheDocument();
      expect(screen.getByText(/test@example.com/i)).toBeInTheDocument();
    });
  });

  it('should display scenes count', async () => {
    renderWithRouter(<UserDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/My Scenes \(1\)/i)).toBeInTheDocument();
    });
  });

  it('should display quote requests count', async () => {
    renderWithRouter(<UserDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/Quote Requests \(1\)/i)).toBeInTheDocument();
    });
  });

  it('should display error message when loading fails', async () => {
    const errorMessage = 'Failed to load your data. Please try again.';
    vi.mocked(userService.getUserScenes).mockRejectedValue(new Error('Network error'));

    renderWithRouter(<UserDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/Failed to load your data/i)).toBeInTheDocument();
    });
  });

  it('should handle backend unavailability gracefully', async () => {
    vi.mocked(userService.getUserScenes).mockRejectedValue(
      new Error('Service unavailable')
    );

    renderWithRouter(<UserDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/Failed to load your data/i)).toBeInTheDocument();
    });

    // User info should still be shown even with error
    expect(screen.getByText(/Welcome, Test!/i)).toBeInTheDocument();
  });

  it('should display empty state when no scenes exist', async () => {
    vi.mocked(userService.getUserScenes).mockResolvedValue([]);

    renderWithRouter(<UserDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/My Scenes \(0\)/i)).toBeInTheDocument();
    });
  });

  it('should call logout when logout button is clicked', async () => {
    vi.mocked(authService.logout).mockResolvedValue(undefined);

    renderWithRouter(<UserDashboard />);

    await waitFor(() => {
      expect(screen.getByText(/Welcome, Test!/i)).toBeInTheDocument();
    });

    const logoutButton = screen.getByRole('button', { name: /logout/i });
    logoutButton.click();

    await waitFor(() => {
      expect(authService.logout).toHaveBeenCalled();
    });
  });
});
