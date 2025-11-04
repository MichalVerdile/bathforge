export interface Category {
  id: number;
  name: string;
  description: string;
}

export interface Color {
  id: number;
  name: string;
  hexCode: string;
  categoryId: number;
  categoryName: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  priceRange: 'LOW' | 'MEDIUM' | 'HIGH';
  modelPath: string;
  thumbnail?: string; // Optional thumbnail field added
  mountingType: 'FLOOR' | 'WALL' | 'FREESTANDING';
  categoryId: number;
  categoryName: string;
  availableColors: Color[];
}

export interface ProductFilter {
  categoryId?: number;
  priceRange?: 'LOW' | 'MEDIUM' | 'HIGH';
  mountingType?: 'FLOOR' | 'WALL' | 'FREESTANDING';
  searchTerm?: string;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: string;
}

export interface ModelItem {
  id: number;
  name: string;
  url: string;
  category: string;
  categoryId: number;
  priceRange: 'LOW' | 'MEDIUM' | 'HIGH';
  mountingType: 'FLOOR' | 'WALL' | 'FREESTANDING';
  availableColors: Color[];
  thumbnail?: string;
}

export interface ModelCategory {
  id: number;
  name: string;
  displayName: string;
  description: string;
  models: ModelItem[];
}

// Scene Management Types
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
}