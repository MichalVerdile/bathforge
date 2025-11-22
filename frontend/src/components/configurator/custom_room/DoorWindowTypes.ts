// Types for doors and windows in rooms

export interface Vertex {
  x: number;
  y: number;
}

export interface DoorData {
  id: string;
  wallIndex: number; // Which wall the door is on (0-indexed)
  position: number; // Position along the wall (0-1, where 0 is start and 1 is end)
  width: number; // Width in meters
  height: number; // Height in meters
}

export interface WindowData {
  id: string;
  wallIndex: number; // Which wall the window is on (0-indexed)
  position: number; // Position along the wall (0-1)
  width: number; // Width in meters
  height: number; // Height in meters
  elevation: number; // Height from floor to bottom of window in meters
}

export interface RoomOpenings {
  doors: DoorData[];
  windows: WindowData[];
}

// Default dimensions
export const DEFAULT_DOOR_WIDTH = 0.9; // 90cm
export const DEFAULT_DOOR_HEIGHT = 1.9; // 190cm
export const DEFAULT_WINDOW_WIDTH = 1.0; // 100cm
export const DEFAULT_WINDOW_HEIGHT = 1.0; // 100cm
export const DEFAULT_WINDOW_ELEVATION = 0.9; // 90cm from floor

// Helper function to generate unique IDs
export const generateOpeningId = (type: 'door' | 'window'): string => {
  return `${type}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
};

// Helper to find the longest wall for door placement
export const findLongestWallIndex = (vertices: Vertex[]): number => {
  let longestIndex = 0;
  let maxLength = 0;

  for (let i = 0; i < vertices.length; i++) {
    const nextIndex = (i + 1) % vertices.length;
    const dx = vertices[nextIndex].x - vertices[i].x;
    const dy = vertices[nextIndex].y - vertices[i].y;
    const length = Math.sqrt(dx * dx + dy * dy);

    if (length > maxLength) {
      maxLength = length;
      longestIndex = i;
    }
  }

  return longestIndex;
};

// Helper to find second longest wall for window placement
export const findSecondLongestWallIndex = (vertices: Vertex[], excludeIndex: number): number => {
  let longestIndex = 0;
  let maxLength = 0;

  for (let i = 0; i < vertices.length; i++) {
    if (i === excludeIndex) continue;

    const nextIndex = (i + 1) % vertices.length;
    const dx = vertices[nextIndex].x - vertices[i].x;
    const dy = vertices[nextIndex].y - vertices[i].y;
    const length = Math.sqrt(dx * dx + dy * dy);

    if (length > maxLength) {
      maxLength = length;
      longestIndex = i;
    }
  }

  return longestIndex;
};

// Calculate wall length in meters
export const getWallLengthMeters = (vertices: Vertex[], wallIndex: number): number => {
  const nextIndex = (wallIndex + 1) % vertices.length;
  const dx = (vertices[nextIndex].x - vertices[wallIndex].x) * 0.01; // Convert to meters
  const dy = (vertices[nextIndex].y - vertices[wallIndex].y) * 0.01;
  return Math.sqrt(dx * dx + dy * dy);
};

// Create default openings for a room
export const createDefaultOpenings = (vertices: Vertex[], roomHeight: number): RoomOpenings => {
  const doorWallIndex = findLongestWallIndex(vertices);
  const windowWallIndex = findSecondLongestWallIndex(vertices, doorWallIndex);

  const doorWallLength = getWallLengthMeters(vertices, doorWallIndex);
  const windowWallLength = getWallLengthMeters(vertices, windowWallIndex);

  // Ensure door fits on wall
  const doorWidth = Math.min(DEFAULT_DOOR_WIDTH, doorWallLength * 0.8);
  // Ensure window fits on wall
  const windowWidth = Math.min(DEFAULT_WINDOW_WIDTH, windowWallLength * 0.6);
  // Ensure window height fits in room
  const maxWindowHeight = roomHeight - DEFAULT_WINDOW_ELEVATION - 0.1;
  const windowHeight = Math.min(DEFAULT_WINDOW_HEIGHT, maxWindowHeight);

  return {
    doors: [
      {
        id: generateOpeningId('door'),
        wallIndex: doorWallIndex,
        position: 0.5, // Center of wall
        width: doorWidth,
        height: Math.min(DEFAULT_DOOR_HEIGHT, roomHeight - 0.1),
      },
    ],
    windows: [
      {
        id: generateOpeningId('window'),
        wallIndex: windowWallIndex,
        position: 0.5, // Center of wall
        width: windowWidth,
        height: windowHeight,
        elevation: DEFAULT_WINDOW_ELEVATION,
      },
    ],
  };
};
