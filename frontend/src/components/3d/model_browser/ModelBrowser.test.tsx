import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ModelBrowser from './ModelBrowser';
import { ProductService } from '../../../controllers/api/products/ProductService';
import { CategoryService } from '../../../controllers/api/products/CategoryService';
import ColorService from '../../../controllers/api/products/ColorService';

// Mock the services
vi.mock('../../../controllers/api/products/CategoryService', () => ({
  CategoryService: {
    getAll: vi.fn(),
  },
}));

vi.mock('../../../controllers/api/products/ProductService', () => ({
  ProductService: {
    getAll: vi.fn(),
    getByCategory: vi.fn(),
  },
}));

vi.mock('../../../controllers/api/products/ColorService', () => ({
  default: {
    getByCategory: vi.fn(),
  },
}));

describe('ModelBrowser Catalog Filtering', () => {
  const mockCategories = [
    { id: 1, name: 'basins', description: 'Basins' },
    { id: 2, name: 'bathtubs', description: 'Bathtubs' },
    { id: 3, name: 'shower', description: 'Showers' },
  ];

  const mockBasinProducts = [
    {
      id: 1,
      name: 'Modern Basin',
      categoryId: 1,
      categoryName: 'basins',
      priceRange: 'MEDIUM',
      mountingType: 'WALL',
      modelPath: 'assets/basins/basin1.glb',
    },
    {
      id: 2,
      name: 'Classic Basin',
      categoryId: 1,
      categoryName: 'basins',
      priceRange: 'LOW',
      mountingType: 'FLOOR',
      modelPath: 'assets/basins/basin2.glb',
    },
  ];

  const mockColors = [
    { id: 1, name: 'White', hexCode: '#FFFFFF', categoryId: 1 },
    { id: 2, name: 'Black', hexCode: '#000000', categoryId: 1 },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(CategoryService.getAll).mockResolvedValue(mockCategories);
    vi.mocked(ProductService.getAll).mockResolvedValue(mockBasinProducts);
    vi.mocked(ColorService.getByCategory).mockResolvedValue(mockColors);
  });

  it('should render ModelBrowser without crashing', async () => {
    render(<ModelBrowser onSelectModel={vi.fn()} selectedCategory="" />);

    // Component should render and load data
    await waitFor(() => {
      expect(CategoryService.getAll).toHaveBeenCalled();
    }, { timeout: 3000 });
  });

  it('should handle errors gracefully', async () => {
    vi.mocked(CategoryService.getAll).mockRejectedValue(
      new Error('Failed to load categories')
    );

    // Should not crash even with API errors
    render(<ModelBrowser onSelectModel={vi.fn()} selectedCategory="" />);
    
    await waitFor(() => {
      expect(CategoryService.getAll).toHaveBeenCalled();
    }, { timeout: 3000 });
  });
});
