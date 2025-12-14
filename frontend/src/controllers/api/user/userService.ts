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
}

export default new UserService();
