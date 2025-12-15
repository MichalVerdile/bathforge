import apiClient from "../../configuration/apiClient";
import authService from "../auth/authService";

export interface SceneData {
  id: number;
  name: string;
  description: string;
  user: string;
  sceneData: string;
  cameraPosition: string;
  lightingSettings: string;
  backgroundColor: string;
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface QuoteRequestHistory {
  id: number;
  roomDimensions: string;
  additionalNotes: string;
  sceneSnapshot: string;
  createdAt: string;
  status: string;
}

export interface QuoteRequestMessage {
  id: number;
  message: string;
  senderType: string; // 'ADMIN' or 'SYSTEM'
  createdAt: string;
}

export interface QuoteRequestDetail {
  id: number;
  status: string;
  roomDimensions: string;
  additionalNotes: string;
  sceneSnapshot: string;
  createdAt: string;
  updatedAt: string;
  documentUrl: string;
  messages: QuoteRequestMessage[];
}

class UserService {
  async getUserScenes(): Promise<SceneData[]> {
    const response = await apiClient.get<SceneData[]>('/user/scenes', {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async getUserQuoteRequests(): Promise<QuoteRequestHistory[]> {
    const response = await apiClient.get<QuoteRequestHistory[]>('/user/quote-requests', {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }

  async getQuoteRequestDetail(requestId: number): Promise<QuoteRequestDetail> {
    const response = await apiClient.get<QuoteRequestDetail>(`/user/quote-requests/${requestId}`, {
      headers: authService.getAuthHeader()
    });
    return response.data;
  }
}

export default new UserService();
