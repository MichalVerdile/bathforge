import { apiClient } from "../../configuration/apiClient";
import type { AIPreferences } from "../../../components/configurator/ai_designer/AIDesigner";

export interface Vertex {
  x: number;
  y: number;
}

export interface RoomConfiguration {
  vertices: Vertex[];
  height: number;
}

export interface AIDesignRequest {
  style: string;
  colorPalettes: string[];
  features: string[];
  priceRange?: string;
  roomConfiguration?: RoomConfiguration;
  additionalRequirements?: string;
}

export interface ProductRecommendation {
  productId: number;
  productName: string;
  category: string;
  reason: string;
  confidenceScore?: number;
  position?: string;
  color?: string;
}

export interface AIDesignResponse {
  designId: string;
  generatedPrompt: string;
  description: string;
  style: string;
  colorPalettes: string[];
  features: string[];
  productRecommendations: ProductRecommendation[];
  sceneConfiguration: string;
  generatedAt: string;
  status: "PENDING" | "GENERATED" | "FAILED";
}

class AIDesignerController {
  private readonly baseUrl = "/ai/design";

  /**
   * Generate AI bathroom design based on user preferences
   */
  async generateDesign(preferences: AIPreferences): Promise<AIDesignResponse> {
    try {
      // Convert frontend preferences to backend format
      const request: AIDesignRequest = {
        style: preferences.style || "",
        colorPalettes: preferences.colors || [],
        features: preferences.features || [],
        priceRange: preferences.priceRange,
        roomConfiguration: preferences.room
          ? {
              vertices: preferences.room.vertices,
              height: preferences.room.height,
            }
          : undefined,
        additionalRequirements: "",
      };

      const response = await apiClient.post<AIDesignResponse>(
        `${this.baseUrl}/generate`,
        request
      );

      return response.data;
    } catch (error: any) {
      console.error("Error generating AI design:", error);

      if (error.response?.data) {
        throw new Error(
          error.response.data.description || "Failed to generate design"
        );
      }

      throw new Error("Failed to connect to AI design service");
    }
  }

  /**
   * Get available style options from backend
   */
  async getAvailableStyles(): Promise<string[]> {
    try {
      const response = await apiClient.get<string[]>(`${this.baseUrl}/styles`);
      return response.data;
    } catch (error) {
      console.error("Error fetching available styles:", error);
      // Return default styles as fallback
      return [
        "modern",
        "traditional",
        "minimalist",
        "luxury",
        "industrial",
        "scandinavian",
      ];
    }
  }

  /**
   * Get available color palette options from backend
   */
  async getAvailableColorPalettes(): Promise<string[]> {
    try {
      const response = await apiClient.get<string[]>(
        `${this.baseUrl}/color-palettes`
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching available color palettes:", error);
      // Return default color palettes as fallback
      return [
        "spa-serenity",
        "modern-monochrome",
        "natural-warmth",
        "urban-chic",
        "luxe-dark",
        "sage-stone",
      ];
    }
  }

  /**
   * Get available feature options from backend
   */
  async getAvailableFeatures(): Promise<string[]> {
    try {
      const response = await apiClient.get<string[]>(
        `${this.baseUrl}/features`
      );
      return response.data;
    } catch (error) {
      console.error("Error fetching available features:", error);
      // Return default features as fallback
      return ["bathtub", "shower", "sink", "toilet", "storage", "mirror"];
    }
  }

  /**
   * Check if AI service is available
   */
  async healthCheck(): Promise<boolean> {
    try {
      await apiClient.get(`${this.baseUrl}/health`);
      return true;
    } catch (error) {
      console.error("AI service health check failed:", error);
      return false;
    }
  }
}

export const aiDesignerController = new AIDesignerController();
export default aiDesignerController;
