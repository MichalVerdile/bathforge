import * as THREE from "three";

interface Vertex2D {
  x: number;
  y: number;
}

interface Vertex3D {
  x: number;
  z: number;
}

/**
 * Convert 2D room vertices to 3D coordinates
 */
export function convertRoomTo3D(vertices: Vertex2D[]): Vertex3D[] {
  if (!vertices || vertices.length === 0) return [];

  const sum = vertices.reduce(
    (acc, v) => ({ x: acc.x + v.x, y: acc.y + v.y }),
    { x: 0, y: 0 }
  );
  const centroid = {
    x: sum.x / vertices.length,
    y: sum.y / vertices.length,
  };

  return vertices.map((v) => ({
    x: (v.x - centroid.x) * 0.01,
    z: (v.y - centroid.y) * 0.01,
  }));
}

/**
 * Point-in-polygon test using ray-casting algorithm
 */
export function isPointInPolygon(
  x: number,
  z: number,
  polygon: Vertex3D[]
): boolean {
  let inside = false;
  for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
    const xi = polygon[i].x;
    const zi = polygon[i].z;
    const xj = polygon[j].x;
    const zj = polygon[j].z;

    const intersect =
      zi > z !== zj > z && x < ((xj - xi) * (z - zi)) / (zj - zi) + xi;
    if (intersect) inside = !inside;
  }
  return inside;
}

export function shrinkPolygon(polygon: Vertex3D[], offset: number): Vertex3D[] {
  if (polygon.length < 3) return polygon;

  // Calculate centroid
  const centroid = {
    x: polygon.reduce((sum, p) => sum + p.x, 0) / polygon.length,
    z: polygon.reduce((sum, p) => sum + p.z, 0) / polygon.length,
  };

  // Calculate average distance from centroid to vertices
  const avgDistance =
    polygon.reduce((sum, p) => {
      const dx = p.x - centroid.x;
      const dz = p.z - centroid.z;
      return sum + Math.sqrt(dx * dx + dz * dz);
    }, 0) / polygon.length;

  // Scale factor to shrink by offset amount
  const scaleFactor = Math.max(0.1, (avgDistance - offset) / avgDistance);

  // Move each vertex toward centroid
  return polygon.map((p) => ({
    x: centroid.x + (p.x - centroid.x) * scaleFactor,
    z: centroid.z + (p.z - centroid.z) * scaleFactor,
  }));
}

/**
 * Check if a position is valid within room bounds
 */
export function isPositionValid(
  testPosition: THREE.Vector3,
  model: THREE.Group,
  rotation: [number, number, number],
  scale: [number, number, number],
  shrunkPolygon: Vertex3D[] | null,
  roomHeight: number,
  collisionBuffer: number
): boolean {
  if (!model) return true;

  // Fallback to simple bounds if no polygon
  if (!shrunkPolygon) {
    const ROOM_HALF_SIZE = 1.1;
    return (
      testPosition.x >= -(ROOM_HALF_SIZE - collisionBuffer) &&
      testPosition.x <= ROOM_HALF_SIZE - collisionBuffer &&
      testPosition.z >= -(ROOM_HALF_SIZE - collisionBuffer) &&
      testPosition.z <= ROOM_HALF_SIZE - collisionBuffer &&
      testPosition.y >= 0 &&
      testPosition.y <= roomHeight
    );
  }

  // Create temporary group to get bounding box at test position
  const tempGroup = new THREE.Group();
  tempGroup.position.copy(testPosition);
  tempGroup.rotation.set(...rotation);
  tempGroup.scale.set(...scale);
  tempGroup.add(model);
  tempGroup.updateMatrixWorld(true);

  // Get object bounding box
  const objectBox = new THREE.Box3().setFromObject(tempGroup);

  // Check Y bounds
  if (objectBox.min.y < 0 || objectBox.max.y > roomHeight) {
    tempGroup.remove(model);
    return false;
  }

  // Test 5 points: 4 corners + center
  const testPoints: Vertex3D[] = [
    { x: objectBox.min.x, z: objectBox.min.z },
    { x: objectBox.max.x, z: objectBox.min.z },
    { x: objectBox.min.x, z: objectBox.max.z },
    { x: objectBox.max.x, z: objectBox.max.z },
    {
      x: (objectBox.min.x + objectBox.max.x) / 2,
      z: (objectBox.min.z + objectBox.max.z) / 2,
    },
  ];

  // All test points must be inside the polygon
  for (const point of testPoints) {
    if (!isPointInPolygon(point.x, point.z, shrunkPolygon)) {
      tempGroup.remove(model);
      return false;
    }
  }

  tempGroup.remove(model);
  return true;
}

/**
 * Find valid position with wall sliding behavior
 */
export function findValidPosition(
  desiredPosition: THREE.Vector3,
  currentPos: THREE.Vector3,
  model: THREE.Group,
  rotation: [number, number, number],
  scale: [number, number, number],
  shrunkPolygon: Vertex3D[] | null,
  roomHeight: number,
  collisionBuffer: number
): THREE.Vector3 {
  // If desired position is valid, use it
  if (
    isPositionValid(
      desiredPosition,
      model,
      rotation,
      scale,
      shrunkPolygon,
      roomHeight,
      collisionBuffer
    )
  ) {
    return desiredPosition;
  }

  // Try sliding along walls by testing X and Z movement separately
  const tryXOnly = new THREE.Vector3(
    desiredPosition.x,
    currentPos.y,
    currentPos.z
  );
  if (
    isPositionValid(
      tryXOnly,
      model,
      rotation,
      scale,
      shrunkPolygon,
      roomHeight,
      collisionBuffer
    )
  ) {
    return tryXOnly;
  }

  const tryZOnly = new THREE.Vector3(
    currentPos.x,
    currentPos.y,
    desiredPosition.z
  );
  if (
    isPositionValid(
      tryZOnly,
      model,
      rotation,
      scale,
      shrunkPolygon,
      roomHeight,
      collisionBuffer
    )
  ) {
    return tryZOnly;
  }

  // If no valid movement, stay at current position
  return currentPos;
}
