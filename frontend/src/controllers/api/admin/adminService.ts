import apiClient from '../../configuration/apiClient';

export interface UserDTO {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  company?: string;
  phone?: string;
  role: string;
  enabled: boolean;
  createdAt: string;
  sceneCount: number;
  quoteRequestCount: number;
}

export interface UserSceneDTO {
  sceneId: number;
  sceneName: string;
  sceneDescription?: string;
  userId: number;
  userEmail: string;
  userFullName: string;
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface QuoteRequestAdminDTO {
  id: number;
  userId: number;
  userEmail: string;
  userFullName: string;
  userPhone?: string;
  userCompany?: string;
  roomDimensions?: string;
  additionalNotes?: string;
  sceneSnapshot?: string;
  status: string;
  adminResponse?: string;
  documentUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateQuoteRequestDTO {
  status?: string;
  adminResponse?: string;
}

class AdminService {
  // User Management
  async getAllUsers(): Promise<UserDTO[]> {
    const response = await apiClient.get<UserDTO[]>('/admin/users');
    return response.data;
  }

  async getUserById(userId: number): Promise<UserDTO> {
    const response = await apiClient.get<UserDTO>(`/admin/users/${userId}`);
    return response.data;
  }

  // Scene Management
  async getAllScenes(): Promise<UserSceneDTO[]> {
    const response = await apiClient.get<UserSceneDTO[]>('/admin/scenes');
    return response.data;
  }

  async getScenesByUserId(userId: number): Promise<UserSceneDTO[]> {
    const response = await apiClient.get<UserSceneDTO[]>(`/admin/users/${userId}/scenes`);
    return response.data;
  }

  // Quote Request Management
  async getAllQuoteRequests(): Promise<QuoteRequestAdminDTO[]> {
    const response = await apiClient.get<QuoteRequestAdminDTO[]>('/admin/quote-requests');
    return response.data;
  }

  async getQuoteRequestById(requestId: number): Promise<QuoteRequestAdminDTO> {
    const response = await apiClient.get<QuoteRequestAdminDTO>(`/admin/quote-requests/${requestId}`);
    return response.data;
  }

  async updateQuoteRequest(requestId: number, updateData: UpdateQuoteRequestDTO): Promise<QuoteRequestAdminDTO> {
    const response = await apiClient.put<QuoteRequestAdminDTO>(`/admin/quote-requests/${requestId}`, updateData);
    return response.data;
  }

  async uploadDocument(requestId: number, file: File): Promise<QuoteRequestAdminDTO> {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await apiClient.post<QuoteRequestAdminDTO>(
      `/admin/quote-requests/${requestId}/upload`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  }
}

export const adminService = new AdminService();
export default adminService;
