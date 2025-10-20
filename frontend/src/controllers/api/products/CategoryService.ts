import { Category } from "../../../types/api";
import { apiClient } from "../../configuration";

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