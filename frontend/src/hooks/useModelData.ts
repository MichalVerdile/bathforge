import { useState, useEffect, useCallback } from 'react';
import { Product, ModelItem, ModelCategory, ProductFilter } from '../types/api';
import { CategoryService } from '../controllers/api/products/CategoryService';
import { ProductService } from '../controllers/api/products/ProductService';

export interface UseModelDataResult {
  categories: ModelCategory[];
  allProducts: Product[];
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
  filterProducts: (filters: ProductFilter) => Promise<Product[]>;
}

export function useModelData(): UseModelDataResult {
  const [categories, setCategories] = useState<ModelCategory[]>([]);
  const [allProducts, setAllProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const convertProductToModelItem = (product: Product): ModelItem => ({
    id: product.id,
    name: product.name,
    url: product.modelPath.startsWith('/') ? product.modelPath : `/${product.modelPath}`,
    category: product.categoryName,
    categoryId: product.categoryId,
    priceRange: product.priceRange,
    mountingType: product.mountingType,
    availableColors: product.availableColors || [],
    thumbnail: undefined
  });

  const formatCategoryDisplayName = (categoryName: string): string => {
    const displayNames: { [key: string]: string } = {
      'accessoires': 'Accessories',
      'furniture': 'Furniture',
      'basins': 'Basins & Sinks',
      'wcs': 'WCs & Toilets',
      'bathtubs': 'Bathtubs',
      'shower': 'Shower',
      'fittings': 'Fittings',
      'fittings_bathtubs': 'Bathtub Fittings',
      'towel_radiators': 'Towel Radiators',
      'coverings': 'Wall & Floor Coverings'
    };
    
    return displayNames[categoryName] || categoryName.charAt(0).toUpperCase() + categoryName.slice(1);
  };

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const [categoriesData, productsData] = await Promise.all([
        CategoryService.getAll(),
        ProductService.getAll()
      ]);

      setAllProducts(productsData);

      const categoryMap = new Map<number, ModelCategory>();

      categoriesData.forEach(category => {
        categoryMap.set(category.id, {
          id: category.id,
          name: category.name,
          displayName: formatCategoryDisplayName(category.name),
          description: category.description,
          models: []
        });
      });

      productsData.forEach(product => {
        const category = categoryMap.get(product.categoryId);
        if (category) {
          const modelItem = convertProductToModelItem(product);
          category.models.push(modelItem);
        }
      });

      const categoriesWithModels = Array.from(categoryMap.values())
        .filter(category => category.models.length > 0)
        .sort((a, b) => a.displayName.localeCompare(b.displayName));

      setCategories(categoriesWithModels);
    } catch (err) {
      console.error('Failed to load model data:', err);
      setError(err instanceof Error ? err.message : 'Failed to load data from the backend');
    } finally {
      setLoading(false);
    }
  }, []);

  const filterProducts = useCallback(async (filters: ProductFilter): Promise<Product[]> => {
    try {
      if (filters.searchTerm) {
        return await ProductService.search(filters.searchTerm);
      } else {
        return await ProductService.getWithFilters(filters);
      }
    } catch (err) {
      console.error('Failed to filter products:', err);
      throw err;
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  return {
    categories,
    allProducts,
    loading,
    error,
    refresh: loadData,
    filterProducts
  };
}

export function useProductsByCategory(categoryId?: number) {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadProducts = useCallback(async () => {
    if (!categoryId) {
      setProducts([]);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await ProductService.getByCategoryId(categoryId);
      setProducts(data);
    } catch (err) {
      console.error('Failed to load products by category:', err);
      setError(err instanceof Error ? err.message : 'Failed to load products');
    } finally {
      setLoading(false);
    }
  }, [categoryId]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  return { products, loading, error, refresh: loadProducts };
}