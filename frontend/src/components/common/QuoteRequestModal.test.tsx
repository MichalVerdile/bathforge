import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import QuoteRequestModal from './QuoteRequestModal';

describe('QuoteRequestModal Form Validation', () => {
  const mockSceneData = {
    sceneId: 'test-scene-123',
    products: [],
    coverings: [],
  };

  const mockOnClose = vi.fn();
  const mockOnSubmit = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should not render when isOpen is false', () => {
    render(
      <QuoteRequestModal
        isOpen={false}
        onClose={mockOnClose}
        sceneData={mockSceneData}
        onSubmit={mockOnSubmit}
      />
    );

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('should render form fields when open', () => {
    render(
      <QuoteRequestModal
        isOpen={true}
        onClose={mockOnClose}
        sceneData={mockSceneData}
        onSubmit={mockOnSubmit}
      />
    );

    expect(screen.getByLabelText(/first name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/last name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
  });

  it('should validate password length (minimum 8 characters)', async () => {
    render(
      <QuoteRequestModal
        isOpen={true}
        onClose={mockOnClose}
        sceneData={mockSceneData}
        onSubmit={mockOnSubmit}
      />
    );

    const user = userEvent.setup();

    // Fill in required fields
    await user.type(screen.getByLabelText(/first name/i), 'John');
    await user.type(screen.getByLabelText(/last name/i), 'Doe');
    await user.type(screen.getByLabelText(/email/i), 'john@example.com');
    await user.type(screen.getByLabelText(/password/i), 'short'); // Only 5 characters

    const submitButton = screen.getByRole('button', { name: /submit/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument();
    });

    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should accept valid password (8+ characters)', async () => {
    mockOnSubmit.mockResolvedValue(undefined);

    render(
      <QuoteRequestModal
        isOpen={true}
        onClose={mockOnClose}
        sceneData={mockSceneData}
        onSubmit={mockOnSubmit}
      />
    );

    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/first name/i), 'John');
    await user.type(screen.getByLabelText(/last name/i), 'Doe');
    await user.type(screen.getByLabelText(/email/i), 'john@example.com');
    await user.type(screen.getByLabelText(/password/i), 'validPassword123');

    const submitButton = screen.getByRole('button', { name: /submit/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          firstName: 'John',
          lastName: 'Doe',
          email: 'john@example.com',
          password: 'validPassword123',
        })
      );
    });
  });

  it('should clear error when user types', async () => {
    render(
      <QuoteRequestModal
        isOpen={true}
        onClose={mockOnClose}
        sceneData={mockSceneData}
        onSubmit={mockOnSubmit}
      />
    );

    const user = userEvent.setup();

    // Fill in required fields
    await user.type(screen.getByLabelText(/first name/i), 'John');
    await user.type(screen.getByLabelText(/last name/i), 'Doe');
    await user.type(screen.getByLabelText(/email/i), 'john@example.com');
    await user.type(screen.getByLabelText(/password/i), 'short');
    
    const submitButton = screen.getByRole('button', { name: /submit/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument();
    });

    // Type more - error should clear
    await user.type(screen.getByLabelText(/password/i), 'longer');

    expect(screen.queryByText(/password must be at least 8 characters/i)).not.toBeInTheDocument();
  });

  it('should display error message when submission fails', async () => {
    const errorMessage = 'Failed to submit quote request. Please try again.';
    mockOnSubmit.mockRejectedValue(new Error(errorMessage));

    render(
      <QuoteRequestModal
        isOpen={true}
        onClose={mockOnClose}
        sceneData={mockSceneData}
        onSubmit={mockOnSubmit}
      />
    );

    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/first name/i), 'John');
    await user.type(screen.getByLabelText(/last name/i), 'Doe');
    await user.type(screen.getByLabelText(/email/i), 'john@example.com');
    await user.type(screen.getByLabelText(/password/i), 'validPassword123');

    const submitButton = screen.getByRole('button', { name: /submit/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should toggle password visibility', async () => {
    render(
      <QuoteRequestModal
        isOpen={true}
        onClose={mockOnClose}
        sceneData={mockSceneData}
        onSubmit={mockOnSubmit}
      />
    );

    const passwordInput = screen.getByLabelText(/password/i);
    expect(passwordInput).toHaveAttribute('type', 'password');

    const toggleButton = screen.getByText(/Show/i);
    fireEvent.click(toggleButton);

    expect(passwordInput).toHaveAttribute('type', 'text');
  });

  it('should disable submit button while submitting', async () => {
    mockOnSubmit.mockImplementation(
      () => new Promise(resolve => setTimeout(resolve, 1000))
    );

    render(
      <QuoteRequestModal
        isOpen={true}
        onClose={mockOnClose}
        sceneData={mockSceneData}
        onSubmit={mockOnSubmit}
      />
    );

    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/first name/i), 'John');
    await user.type(screen.getByLabelText(/last name/i), 'Doe');
    await user.type(screen.getByLabelText(/email/i), 'john@example.com');
    await user.type(screen.getByLabelText(/password/i), 'validPassword123');

    const submitButton = screen.getByRole('button', { name: /submit/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(submitButton).toBeDisabled();
    });
  });

  it('should include scene data in submission', async () => {
    mockOnSubmit.mockResolvedValue(undefined);

    const sceneDataWithProducts = {
      sceneId: 'test-scene-123',
      roomDimensions: '4m x 3m',
      products: [
        { name: 'Basin 1', category: 'basins', color: 'white' },
      ],
      coverings: [
        { type: 'floor', name: 'Tile 1', color: 'grey' },
      ],
    };

    render(
      <QuoteRequestModal
        isOpen={true}
        onClose={mockOnClose}
        sceneData={sceneDataWithProducts}
        onSubmit={mockOnSubmit}
      />
    );

    const user = userEvent.setup();

    await user.type(screen.getByLabelText(/first name/i), 'John');
    await user.type(screen.getByLabelText(/last name/i), 'Doe');
    await user.type(screen.getByLabelText(/email/i), 'john@example.com');
    await user.type(screen.getByLabelText(/password/i), 'validPassword123');

    const submitButton = screen.getByRole('button', { name: /submit/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          sceneId: 'test-scene-123',
          roomDimensions: '4m x 3m',
          products: expect.arrayContaining([
            expect.objectContaining({ name: 'Basin 1' }),
          ]),
          coverings: expect.arrayContaining([
            expect.objectContaining({ type: 'floor' }),
          ]),
        })
      );
    });
  });
});
