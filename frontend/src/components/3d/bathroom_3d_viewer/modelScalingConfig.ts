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
    accessoires: { axis: 'max', targetMeters: 0.14000 },
    basins: { axis: 'y', targetMeters: 1 },
    bathtubs: { axis: 'x', targetMeters: 1.15 },
    fittings: { axis: 'max', targetMeters: 0.15256 },
    fittingsbathtubs: { axis: 'max', targetMeters: 1 },
    furniture: { axis: 'x', targetMeters: 1.00000 },
    showers: { axis: 'y', targetMeters: 1.20000 },
    shower: { axis: 'y', targetMeters: 1.20000 },
    towelradiators: { axis: 'max', targetMeters: 1 },
    wcs: { axis: 'x', targetMeters: 1 }
  }
};

export function detectUnitScale(maxDimension: number): number {
  if (maxDimension >= 300 && maxDimension <= 6000) {
    return 0.001;
  }

  if (maxDimension >= 30 && maxDimension < 300) {
    return 0.01;
  }

  if (maxDimension >= 12 && maxDimension < 300) {
    return 0.0254;
  }

  return 1.0;
}

export function getCategoryFromUrl(url: string): string | null {
  const match = url.match(/\/assets\/([^\/]+)\//);
  if (!match) return null;

  const category = match[1].toLowerCase().replace(/[_-]/g, '');
  return category;
}

export function normalizeCategoryName(category: string): string {
  return category.toLowerCase().replace(/[_\s-]/g, '');
}
