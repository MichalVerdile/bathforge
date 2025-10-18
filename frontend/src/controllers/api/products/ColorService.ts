import { Color } from "three";
import { apiClient } from "../../configuration";

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