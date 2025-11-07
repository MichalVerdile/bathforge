/**
 * Configuration for intelligent model scaling system
 * Handles mixed units (mm, cm, inches, meters) and normalizes by category
 */

export interface CategoryScalingConfig {
  axis: 'x' | 'y' | 'z' | 'max';
  targetMeters: number;
}

export interface RoomDimensions {
  x: number;
  y: number;
  z: number;
}

export interface ScalingConfig {
  room: RoomDimensions;
  categories: Record<string, CategoryScalingConfig>;
}

export const SCALING_CONFIG: ScalingConfig = {
  room: { x: 2.2, y: 2.50, z: 2.2 },
  categories: {
    accessories: { axis: 'max', targetMeters: 0.14000 },
    accessoires: { axis: 'max', targetMeters: 0.14000 }, // French spelling
    basins: { axis: 'y', targetMeters: 1 },
    bathtubs: { axis: 'x', targetMeters: 1.15 }, // reduced from 1.70000
    fittings: { axis: 'max', targetMeters: 0.15256 },
    fittingsbathtubs: { axis: 'max', targetMeters: 1 }, // fittings_bathtubs
    furniture: { axis: 'x', targetMeters: 1.00000 },
    showers: { axis: 'y', targetMeters: 1.20000 },
    shower: { axis: 'y', targetMeters: 1.20000 },
    towelradiators: { axis: 'max', targetMeters: 1 },
    wcs: { axis: 'x', targetMeters: 1 }
  }
};

/**
 * Detects the unit system used in the model based on raw bounding box dimensions
 * @param maxDimension The largest dimension in the raw bounding box
 * @returns Scale factor to convert to meters
 */
export function detectUnitScale(maxDimension: number): number {
  // Millimeters: 300-6000mm range
  if (maxDimension >= 300 && maxDimension <= 6000) {
    return 0.001;
  }
  
  // Centimeters: 30-300cm range
  if (maxDimension >= 30 && maxDimension < 300) {
    return 0.01;
  }
  
  // Inches (imperial): 12-300 inches range
  if (maxDimension >= 12 && maxDimension < 300) {
    // Check if it looks more like imperial
    // This is a heuristic - if dimensions are suspiciously "round" in inches, treat as inches
    return 0.0254;
  }
  
  // Default to meters
  return 1.0;
}

/**
 * Gets the category name from a model URL
 * @param url The model URL (e.g., /assets/basins/model.glb)
 * @returns Normalized category name or null
 */
export function getCategoryFromUrl(url: string): string | null {
  const match = url.match(/\/assets\/([^\/]+)\//);
  if (!match) return null;
  
  // Normalize category name to match config keys
  const category = match[1].toLowerCase().replace(/[_-]/g, '');
  return category;
}

/**
 * Normalizes a category name to match the config keys
 * @param category Raw category name
 * @returns Normalized category name
 */
export function normalizeCategoryName(category: string): string {
  return category.toLowerCase().replace(/[_\s-]/g, '');
}
