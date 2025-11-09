import apiClient from '../../configuration/apiClient';

export interface SceneProduct {
  id?: number;
  sceneId?: number;
  productId: number;
  productName?: string;
  productModelPath?: string;
  colorId?: number;
  colorName?: string;
  colorHexCode?: string;
  positionX?: number;
  positionY?: number;
  positionZ?: number;
  rotationX?: number;
  rotationY?: number;
  rotationZ?: number;
  scaleX?: number;
  scaleY?: number;
  scaleZ?: number;
  customProperties?: string;
}

export interface SceneRoomModel {
  id?: number;
  sceneId?: number;
  verticesData: string;
  roomHeight: number;
  modelType?: string;
  templatePath?: string;
  roomProperties?: string;
}

export interface SceneCovering {
  id?: number;
  sceneId?: number;
  productId: number;
  productName?: string;
  productModelPath?: string;
  surfaceType: string;
  surfaceIdentifier?: string;
  repeatX?: number;
  repeatY?: number;
  materialProperties?: string;
}

export interface Scene {
  id?: number;
  name: string;
  description?: string;
  user: string;
  sceneData?: string;
  cameraPosition?: string;
  lightingSettings?: string;
  backgroundColor?: string;
  isPublic?: boolean;
  createdAt?: string;
  updatedAt?: string;
  sceneProducts?: SceneProduct[];
  roomModel?: SceneRoomModel;
  sceneCoverings?: SceneCovering[];
}

export interface CreateSceneRoomModelRequest {
  verticesData: string;
  roomHeight: number;
  modelType?: string;
  templatePath?: string;
  roomProperties?: string;
}

export interface CreateSceneCoveringRequest {
  productId: number;
  surfaceType: string;
  surfaceIdentifier?: string;
  repeatX?: number;
  repeatY?: number;
  materialProperties?: string;
}

export interface CreateSceneRequest {
  name: string;
  description?: string;
  user?: string;
  sceneData?: string;
  cameraPosition?: string;
  lightingSettings?: string;
  backgroundColor?: string;
  isPublic?: boolean;
  products?: Omit<SceneProduct, 'id' | 'sceneId' | 'productName' | 'productModelPath' | 'colorName' | 'colorHexCode'>[];
  roomModel?: CreateSceneRoomModelRequest;
  coverings?: CreateSceneCoveringRequest[];
}

export interface UpdateSceneRequest {
  name?: string;
  description?: string;
  sceneData?: string;
  cameraPosition?: string;
  lightingSettings?: string;
  backgroundColor?: string;
  isPublic?: boolean;
  products?: Omit<SceneProduct, 'id' | 'sceneId' | 'productName' | 'productModelPath' | 'colorName' | 'colorHexCode'>[];
  roomModel?: CreateSceneRoomModelRequest;
  coverings?: CreateSceneCoveringRequest[];
}

export interface AddProductToSceneRequest {
  productId: number;
  colorId?: number;
  positionX?: number;
  positionY?: number;
  positionZ?: number;
  rotationX?: number;
  rotationY?: number;
  rotationZ?: number;
  scaleX?: number;
  scaleY?: number;
  scaleZ?: number;
  customProperties?: string;
}

class SceneService {
  private readonly BASE_PATH = '/scenes';

  async getAllScenes(): Promise<Scene[]> {
    const response = await apiClient.get<Scene[]>(this.BASE_PATH);
    return response.data;
  }

  async getSceneById(id: number): Promise<Scene> {
    const response = await apiClient.get<Scene>(`${this.BASE_PATH}/${id}`);
    return response.data;
  }

  async getScenesByUser(user: string): Promise<Scene[]> {
    const response = await apiClient.get<Scene[]>(`${this.BASE_PATH}/user/${user}`);
    return response.data;
  }

  async getPublicScenes(): Promise<Scene[]> {
    const response = await apiClient.get<Scene[]>(`${this.BASE_PATH}/public`);
    return response.data;
  }

  async searchScenes(query: string): Promise<Scene[]> {
    const response = await apiClient.get<Scene[]>(`${this.BASE_PATH}/search`, {
      params: { query }
    });
    return response.data;
  }

  async getRecentScenesByUser(user: string, limit: number = 10): Promise<Scene[]> {
    const response = await apiClient.get<Scene[]>(`${this.BASE_PATH}/user/${user}/recent`, {
      params: { limit }
    });
    return response.data;
  }

  async countScenesByUser(user: string): Promise<number> {
    const response = await apiClient.get<number>(`${this.BASE_PATH}/user/${user}/count`);
    return response.data;
  }

  async createScene(sceneData: CreateSceneRequest): Promise<Scene> {
    const response = await apiClient.post<Scene>(this.BASE_PATH, sceneData);
    return response.data;
  }

  async updateScene(id: number, sceneData: UpdateSceneRequest): Promise<void> {
    await apiClient.put<Scene>(`${this.BASE_PATH}/${id}`, sceneData);
  }

  async deleteScene(id: number): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`);
  }

  async getProductsInScene(sceneId: number): Promise<SceneProduct[]> {
    const response = await apiClient.get<SceneProduct[]>(`${this.BASE_PATH}/${sceneId}/products`);
    return response.data;
  }

  async addProductToScene(sceneId: number, productData: AddProductToSceneRequest): Promise<SceneProduct> {
    const response = await apiClient.post<SceneProduct>(`${this.BASE_PATH}/${sceneId}/products`, productData);
    return response.data;
  }

  async updateSceneProduct(sceneId: number, sceneProductId: number, productData: AddProductToSceneRequest): Promise<SceneProduct> {
    const response = await apiClient.put<SceneProduct>(`${this.BASE_PATH}/${sceneId}/products/${sceneProductId}`, productData);
    return response.data;
  }

  async removeProductFromScene(sceneId: number, sceneProductId: number): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${sceneId}/products/${sceneProductId}`);
  }

  async saveCurrentScene(
    name: string, 
    user: string, 
    sceneProducts: SceneProduct[], 
    cameraPosition?: string,
    lightingSettings?: string,
    backgroundColor?: string,
    existingSceneId?: number
  ): Promise<void> {
    const products = sceneProducts.map(product => ({
      productId: product.productId,
      colorId: product.colorId,
      positionX: product.positionX || 0,
      positionY: product.positionY || 0,
      positionZ: product.positionZ || 0,
      rotationX: product.rotationX || 0,
      rotationY: product.rotationY || 0,
      rotationZ: product.rotationZ || 0,
      scaleX: product.scaleX || 1,
      scaleY: product.scaleY || 1,
      scaleZ: product.scaleZ || 1,
      customProperties: product.customProperties
    }));

    if (existingSceneId) {
      this.updateScene(existingSceneId, {
        name,
        products,
        cameraPosition,
        lightingSettings,
        backgroundColor
      });
    } else {
      this.createScene({
        name,
        user,
        products,
        cameraPosition,
        lightingSettings,
        backgroundColor,
        isPublic: false
      });
    }
  }

  async loadScene(sceneId: number): Promise<{
    scene: Scene;
    products: SceneProduct[];
  }> {
    const [scene, products] = await Promise.all([
      this.getSceneById(sceneId),
      this.getProductsInScene(sceneId)
    ]);

    return { scene, products };
  }

  async createOrUpdateRoomModel(sceneId: number, roomModel: CreateSceneRoomModelRequest): Promise<SceneRoomModel> {
    const response = await apiClient.post<SceneRoomModel>(`${this.BASE_PATH}/${sceneId}/room-model`, roomModel);
    return response.data;
  }

  async getRoomModel(sceneId: number): Promise<SceneRoomModel | null> {
    try {
      const response = await apiClient.get<SceneRoomModel>(`${this.BASE_PATH}/${sceneId}/room-model`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  }

  async deleteRoomModel(sceneId: number): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${sceneId}/room-model`);
  }

  async createOrUpdateCovering(sceneId: number, covering: CreateSceneCoveringRequest): Promise<SceneCovering> {
    const response = await apiClient.post<SceneCovering>(`${this.BASE_PATH}/${sceneId}/coverings`, covering);
    return response.data;
  }

  async getCoverings(sceneId: number): Promise<SceneCovering[]> {
    const response = await apiClient.get<SceneCovering[]>(`${this.BASE_PATH}/${sceneId}/coverings`);
    return response.data;
  }

  async deleteCovering(sceneId: number, coveringId: number): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${sceneId}/coverings/${coveringId}`);
  }

  async deleteAllCoverings(sceneId: number): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${sceneId}/coverings`);
  }

  async saveCurrentSceneWithRoomAndCoverings(
    name: string,
    user: string,
    sceneProducts: SceneProduct[],
    roomModel?: { vertices: { x: number; y: number }[]; height: number },
    coverings?: { productId: number; surfaceType: string; surfaceIdentifier?: string; repeatX?: number; repeatY?: number }[],
    cameraPosition?: string,
    lightingSettings?: string,
    backgroundColor?: string,
    existingSceneId?: number
  ): Promise<void> {
    const products = sceneProducts.map(product => ({
      productId: product.productId,
      colorId: product.colorId,
      positionX: product.positionX || 0,
      positionY: product.positionY || 0,
      positionZ: product.positionZ || 0,
      rotationX: product.rotationX || 0,
      rotationY: product.rotationY || 0,
      rotationZ: product.rotationZ || 0,
      scaleX: product.scaleX || 1,
      scaleY: product.scaleY || 1,
      scaleZ: product.scaleZ || 1,
      customProperties: product.customProperties
    }));

    const roomModelData = roomModel ? {
      verticesData: JSON.stringify(roomModel.vertices),
      roomHeight: roomModel.height,
      modelType: 'CUSTOM'
    } : undefined;

    const coveringsData = coverings?.map(covering => ({
      productId: covering.productId,
      surfaceType: covering.surfaceType,
      surfaceIdentifier: covering.surfaceIdentifier,
      repeatX: covering.repeatX || 1,
      repeatY: covering.repeatY || 1
    }));

    if (existingSceneId) {
      await this.updateScene(existingSceneId, {
        name,
        products,
        roomModel: roomModelData,
        coverings: coveringsData,
        cameraPosition,
        lightingSettings,
        backgroundColor
      });
    } else {
      await this.createScene({
        name,
        user,
        products,
        roomModel: roomModelData,
        coverings: coveringsData,
        cameraPosition,
        lightingSettings,
        backgroundColor,
        isPublic: false
      });
    }
  }
}

export const sceneService = new SceneService();
export default sceneService;