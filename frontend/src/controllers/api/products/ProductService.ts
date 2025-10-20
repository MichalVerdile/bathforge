import { Color, Product, ProductFilter } from "../../../types/api";
import { apiClient } from "../../configuration";

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