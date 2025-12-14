import apiClient from '../../configuration/apiClient';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  userId: number;
  firstName: string;
  lastName: string;
}

export interface AuthUser {
  email: string;
  userId: number;
  firstName: string;
  lastName: string;
  token: string;
}

class AuthService {
  private static TOKEN_KEY = 'bathforge_auth_token';
  private static USER_KEY = 'bathforge_user';

  async login(email: string, password: string): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/auth/login', { email, password });
    
    // Store token and user info
    if (response.data.token) {
      localStorage.setItem(AuthService.TOKEN_KEY, response.data.token);
      localStorage.setItem(AuthService.USER_KEY, JSON.stringify({
        email: response.data.email,
        userId: response.data.userId,
        firstName: response.data.firstName,
        lastName: response.data.lastName
      }));
    }
    
    return response.data;
  }

  async validateToken(): Promise<LoginResponse | null> {
    const token = this.getToken();
    if (!token) return null;

    try {
      const response = await apiClient.get<LoginResponse>('/auth/validate', {
        headers: { Authorization: `Bearer ${token}` }
      });
      return response.data;
    } catch (error) {
      this.logout();
      return null;
    }
  }

  logout(): void {
    localStorage.removeItem(AuthService.TOKEN_KEY);
    localStorage.removeItem(AuthService.USER_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(AuthService.TOKEN_KEY);
  }

  getCurrentUser(): AuthUser | null {
    const userStr = localStorage.getItem(AuthService.USER_KEY);
    const token = this.getToken();
    
    if (userStr && token) {
      return { ...JSON.parse(userStr), token };
    }
    return null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getAuthHeader(): { Authorization: string } | {} {
    const token = this.getToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }
}

export default new AuthService();
