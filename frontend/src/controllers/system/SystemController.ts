import { BaseController, ApiResponse } from '../configuration/BaseController';

export interface HealthStatus {
  status: string;
  timestamp: string;
  message?: string;
}

/**
 * Controller for general system endpoints
 */
export class SystemController extends BaseController {
  /**
   * Get welcome message from API
   */
  async getWelcomeMessage(): Promise<ApiResponse<string>> {
    return this.get<string>('/');
  }

  /**
   * Check API health status
   */
  async checkHealth(): Promise<ApiResponse<string>> {
    return this.get<string>('/health');
  }

  /**
   * Test API connectivity
   */
  async testConnection(): Promise<{ isConnected: boolean; message: string }> {
    try {
      const response = await this.getWelcomeMessage();
      return {
        isConnected: true,
        message: `Backend connected: ${response.data}`,
      };
    } catch (error: any) {
      return {
        isConnected: false,
        message: error.message || 'Backend connection failed. Make sure the Spring Boot application is running.',
      };
    }
  }
}

// Export singleton instance
export const systemController = new SystemController();
