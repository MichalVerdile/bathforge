import { apiClient } from "../../configuration";

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