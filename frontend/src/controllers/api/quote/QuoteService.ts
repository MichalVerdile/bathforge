import { apiClient } from "../../configuration/apiClient";
import { AxiosResponse } from "axios";

export interface QuoteRequestData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  company?: string;
  sceneId: string;
  roomDimensions?: string;
  products: Array<{
    name: string;
    category: string;
    color?: string;
    position?: string;
  }>;
  coverings: Array<{
    type: string;
    name: string;
    color?: string;
  }>;
  sceneSnapshot?: string;
  additionalNotes?: string;
}

export interface QuoteResponse {
  success: boolean;
  message: string;
  userId?: number;
  userEmail?: string;
  token?: string;
}

class QuoteService {
  async submitQuoteRequest(data: QuoteRequestData): Promise<QuoteResponse> {
    try {
      const response: AxiosResponse<QuoteResponse> = await apiClient.post("/quote/request", data);
      return response.data;
    } catch (error: any) {
      throw new Error(error.response?.data?.message || "Failed to submit quote request");
    }
  }
}

export const quoteService = new QuoteService();
export default quoteService;
