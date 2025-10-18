import apiClient from '../configuration/apiClient';
import { Category, Product, Color, ProductFilter } from '../../types/api';

export class CategoryService {
  private static readonly BASE_PATH = '/categories';

  static async getAll(): Promise<Category[]> {
    const response = await apiClient.get<Category[]>(this.BASE_PATH);
    return response.data;
  }

  static async getById(id: number): Promise<Category> {
    const response = await apiClient.get<Category>(`${this.BASE_PATH}/${id}`);
    return response.data;
  }

  static async getByName(name: string): Promise<Category> {
    const response = await apiClient.get<Category>(`${this.BASE_PATH}/name/${name}`);
    return response.data;
  }

  static async create(category: Omit<Category, 'id'>): Promise<Category> {
    const response = await apiClient.post<Category>(this.BASE_PATH, category);
    return response.data;
  }

  static async update(id: number, category: Omit<Category, 'id'>): Promise<Category> {
    const response = await apiClient.put<Category>(`${this.BASE_PATH}/${id}`, category);
    return response.data;
  }

  static async delete(id: number): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`);
  }

  static async exists(name: string): Promise<boolean> {
    const response = await apiClient.get<boolean>(`${this.BASE_PATH}/exists/${name}`);
    return response.data;
  }
}

export class ProductService {
  private static readonly BASE_PATH = '/products';

  static async getAll(): Promise<Product[]> {
    const response = await apiClient.get<Product[]>(this.BASE_PATH);
    return response.data;
  }

  static async getById(id: number): Promise<Product> {
    const response = await apiClient.get<Product>(`${this.BASE_PATH}/${id}`);
    return response.data;
  }

  static async getByCategoryId(categoryId: number): Promise<Product[]> {
    const response = await apiClient.get<Product[]>(`${this.BASE_PATH}/category/${categoryId}`);
    return response.data;
  }

  static async getByCategoryName(categoryName: string): Promise<Product[]> {
    const response = await apiClient.get<Product[]>(`${this.BASE_PATH}/category/name/${categoryName}`);
    return response.data;
  }

  static async getByPriceRange(priceRange: 'LOW' | 'MEDIUM' | 'HIGH'): Promise<Product[]> {
    const response = await apiClient.get<Product[]>(`${this.BASE_PATH}/price/${priceRange}`);
    return response.data;
  }

  static async getByMountingType(mountingType: 'FLOOR' | 'WALL' | 'FREESTANDING'): Promise<Product[]> {
    const response = await apiClient.get<Product[]>(`${this.BASE_PATH}/mounting/${mountingType}`);
    return response.data;
  }

  static async search(name: string): Promise<Product[]> {
    const response = await apiClient.get<Product[]>(`${this.BASE_PATH}/search`, {
      params: { name }
    });
    return response.data;
  }

  static async getWithFilters(filters: ProductFilter): Promise<Product[]> {
    const params = new URLSearchParams();
    
    if (filters.categoryId) params.append('categoryId', filters.categoryId.toString());
    if (filters.priceRange) params.append('priceRange', filters.priceRange);
    if (filters.mountingType) params.append('mountingType', filters.mountingType);

    const response = await apiClient.get<Product[]>(`${this.BASE_PATH}/filter?${params.toString()}`);
    return response.data;
  }

  static async create(product: Omit<Product, 'id' | 'categoryName' | 'availableColors'>): Promise<Product> {
    const response = await apiClient.post<Product>(this.BASE_PATH, product);
    return response.data;
  }

  static async update(id: number, product: Omit<Product, 'id' | 'categoryName' | 'availableColors'>): Promise<Product> {
    const response = await apiClient.put<Product>(`${this.BASE_PATH}/${id}`, product);
    return response.data;
  }

  static async delete(id: number): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`);
  }

  static async addColor(productId: number, colorId: number): Promise<void> {
    await apiClient.post(`${this.BASE_PATH}/${productId}/colors/${colorId}`);
  }

  static async removeColor(productId: number, colorId: number): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${productId}/colors/${colorId}`);
  }

  static async getColors(productId: number): Promise<Color[]> {
    const response = await apiClient.get<Color[]>(`${this.BASE_PATH}/${productId}/colors`);
    return response.data;
  }
}

export class ColorService {
  private static readonly BASE_PATH = '/colors';

  static async getAll(): Promise<Color[]> {
    const response = await apiClient.get<Color[]>(this.BASE_PATH);
    return response.data;
  }

  static async getById(id: number): Promise<Color> {
    const response = await apiClient.get<Color>(`${this.BASE_PATH}/${id}`);
    return response.data;
  }

  static async getByCategoryId(categoryId: number): Promise<Color[]> {
    const response = await apiClient.get<Color[]>(`${this.BASE_PATH}/category/${categoryId}`);
    return response.data;
  }

  static async getByCategoryName(categoryName: string): Promise<Color[]> {
    const response = await apiClient.get<Color[]>(`${this.BASE_PATH}/category/name/${categoryName}`);
    return response.data;
  }

  static async create(color: Omit<Color, 'id' | 'categoryName'>): Promise<Color> {
    const response = await apiClient.post<Color>(this.BASE_PATH, color);
    return response.data;
  }

  static async update(id: number, color: Omit<Color, 'id' | 'categoryName'>): Promise<Color> {
    const response = await apiClient.put<Color>(`${this.BASE_PATH}/${id}`, color);
    return response.data;
  }

  static async delete(id: number): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`);
  }
}

export class AdminService {
  private static readonly BASE_PATH = '/admin';

  static async scanAssets(): Promise<{ message: string; status: string }> {
    const response = await apiClient.post<{ message: string; status: string }>(`${this.BASE_PATH}/scan-assets`);
    return response.data;
  }

  static async healthCheck(): Promise<{ status: string; message: string }> {
    const response = await apiClient.get<{ status: string; message: string }>(`${this.BASE_PATH}/health`);
    return response.data;
  }
}