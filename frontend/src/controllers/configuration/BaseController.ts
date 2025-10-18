import apiClient from './apiClient';

export interface ApiResponse<T = any> {
  data: T;
  status: number;
  message?: string;
}

export interface ApiError {
  message: string;
  status?: number;
  code?: string;
}

/**
 * Base controller class with common API functionality
 */
export class BaseController {
  /**
   * Handle API responses and extract data
   */
  protected handleResponse<T>(response: any): ApiResponse<T> {
    return {
      data: response.data,
      status: response.status,
      message: response.statusText,
    };
  }

  /**
   * Handle API errors and format them consistently
   */
  protected handleError(error: any): ApiError {
    const apiError: ApiError = {
      message: 'An unexpected error occurred',
      status: 500,
    };

    if (error.response) {
      apiError.message = error.response.data?.message || error.response.statusText || 'Server error';
      apiError.status = error.response.status;
      apiError.code = error.response.data?.code;
    } else if (error.request) {
      apiError.message = 'Network error - please check your connection';
      apiError.status = 0;
    } else {
      apiError.message = error.message || 'Unknown error';
    }

    return apiError;
  }

  /**
   * Generic GET request
   */
  protected async get<T>(endpoint: string, params?: any): Promise<ApiResponse<T>> {
    try {
      const response = await apiClient.get(endpoint, { params });
      return this.handleResponse<T>(response);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Generic POST request
   */
  protected async post<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
    try {
      const response = await apiClient.post(endpoint, data);
      return this.handleResponse<T>(response);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Generic PUT request
   */
  protected async put<T>(endpoint: string, data?: any): Promise<ApiResponse<T>> {
    try {
      const response = await apiClient.put(endpoint, data);
      return this.handleResponse<T>(response);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Generic DELETE request
   */
  protected async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    try {
      const response = await apiClient.delete(endpoint);
      return this.handleResponse<T>(response);
    } catch (error) {
      throw this.handleError(error);
    }
  }
}
