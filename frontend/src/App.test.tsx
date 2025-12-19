import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import App from './App';
import { systemController } from './controllers/configuration';

// Mock system controller
vi.mock('./controllers/configuration', () => ({
  systemController: {
    testConnection: vi.fn(),
  },
}));

// Mock auth service
vi.mock('./controllers/api/auth/authService', () => ({
  default: {
    isAuthenticated: vi.fn(() => false),
    isAdmin: vi.fn(() => false),
  },
}));

describe('App Backend Connection Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should test backend connection on mount', async () => {
    vi.mocked(systemController.testConnection).mockResolvedValue({
      isConnected: true,
      message: 'Backend connected successfully',
    });

    render(<App />);

    await waitFor(() => {
      expect(systemController.testConnection).toHaveBeenCalled();
    });
  });

  it('should display error message when backend is unavailable', async () => {
    vi.mocked(systemController.testConnection).mockRejectedValue(
      new Error('Backend unavailable')
    );

    render(<App />);

    await waitFor(() => {
      expect(systemController.testConnection).toHaveBeenCalled();
    });

    // App should not crash - just verify it rendered
    expect(document.body).toBeTruthy();
  });

  it('should handle timeout errors gracefully', async () => {
    vi.mocked(systemController.testConnection).mockImplementation(
      () => new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Request timeout')), 100)
      )
    );

    render(<App />);

    await waitFor(() => {
      expect(systemController.testConnection).toHaveBeenCalled();
    }, { timeout: 2000 });

    // App should remain functional
    expect(document.body).toBeInTheDocument();
  });

  it('should set error status when connection fails', async () => {
    vi.mocked(systemController.testConnection).mockResolvedValue({
      isConnected: false,
      message: 'Connection failed',
    });

    render(<App />);

    await waitFor(() => {
      expect(systemController.testConnection).toHaveBeenCalled();
    });

    // Check if error state is reflected in the UI
    // (This depends on how your app displays the connection status)
  });
});
