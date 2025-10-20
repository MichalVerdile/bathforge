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