import React, { useRef, useEffect } from "react";
import * as THREE from "three";
import { useFrame, useThree } from "@react-three/fiber";
import { Door3D } from "./Door3D";
import { Window3D } from "./Window3D";
import { RoomOpenings } from "./DoorWindowTypes";

interface Vertex {
  x: number;
  y: number;
}

interface RoomProps {
  vertices: Vertex[];
  height: number;
  viewMode: "2D" | "3D";
  openings?: RoomOpenings;
  selectedOpeningId?: string | null;
  onOpeningClick?: (id: string, type: "door" | "window") => void;
  onOpeningHover?: (id: string | null) => void;
  isInteractive?: boolean;
}

const WALL_THICKNESS = 0.075;
const FLOOR_THICKNESS = 0.075;

// Store original raycast functions to restore later
const originalRaycastMap = new WeakMap<THREE.Object3D, typeof THREE.Object3D.prototype.raycast>();
const noopRaycast = () => {};

// Helper to disable/enable raycasting on an object and its children
const setRaycastEnabled = (object: THREE.Object3D, enabled: boolean) => {
  if (enabled) {
    const original = originalRaycastMap.get(object);
    if (original) {
      object.raycast = original;
    }
  } else {
    if (!originalRaycastMap.has(object)) {
      originalRaycastMap.set(object, object.raycast.bind(object));
    }
    object.raycast = noopRaycast;
  }
  object.children.forEach(child => setRaycastEnabled(child, enabled));
};

// Enhanced photorealistic materials for bathroom
const wallMaterial = new THREE.MeshStandardMaterial({ 
  color: "#f1f5f9",
  roughness: 0.6,
  metalness: 0.0,
  envMapIntensity: 0.8,
});

const floorMaterial = new THREE.MeshPhysicalMaterial({ 
  color: "#7b7e81ff",
  roughness: 0.4,
  metalness: 0.0,
  envMapIntensity: 1.0,
  clearcoat: 0.2,
  clearcoatRoughness: 0.4,
});

const cornerMaterial = new THREE.MeshStandardMaterial({ 
  color: "#f1f5f9",
  roughness: 0.6,
  metalness: 0.0,
  envMapIntensity: 0.8,
});

const convert2DTo3D = (
  vertex: Vertex,
  centerOffset: { x: number; y: number }
): THREE.Vector3 => {
  return new THREE.Vector3(
    (vertex.x - centerOffset.x) * 0.01,
    0,
    (vertex.y - centerOffset.y) * 0.01
  );
};

const calculateCentroid = (vertices: Vertex[]): { x: number; y: number } => {
  if (vertices.length === 0) return { x: 0, y: 0 };

  const sum = vertices.reduce(
    (acc, v) => ({ x: acc.x + v.x, y: acc.y + v.y }),
    { x: 0, y: 0 }
  );
  return { x: sum.x / vertices.length, y: sum.y / vertices.length };
};

// Helper function to calculate wall position and rotation in 3D
const calculateWallTransform = (
  v1: Vertex,
  v2: Vertex,
  centroid: { x: number; y: number }
): { position: THREE.Vector3; rotation: number; length: number } => {
  const v1_3D = new THREE.Vector3(
    (v1.x - centroid.x) * 0.01,
    0,
    (v1.y - centroid.y) * 0.01
  );
  const v2_3D = new THREE.Vector3(
    (v2.x - centroid.x) * 0.01,
    0,
    (v2.y - centroid.y) * 0.01
  );

  const dx = v2_3D.x - v1_3D.x;
  const dz = v2_3D.z - v1_3D.z;
  const length = Math.sqrt(dx * dx + dz * dz);
  const angle = Math.atan2(-dz, dx);

  const midpoint = new THREE.Vector3(
    (v1_3D.x + v2_3D.x) / 2,
    0,
    (v1_3D.z + v2_3D.z) / 2
  );

  return { position: midpoint, rotation: angle, length };
};

// Calculate position for door/window on a wall
const calculateOpeningPosition = (
  wallIndex: number,
  positionAlongWall: number,
  elevation: number,
  vertices: Vertex[],
  centroid: { x: number; y: number }
): { position: THREE.Vector3; rotation: number } => {
  const v1 = vertices[wallIndex];
  const v2 = vertices[(wallIndex + 1) % vertices.length];
  const wallTransform = calculateWallTransform(v1, v2, centroid);

  // Calculate offset from center along the wall
  const offsetFromCenter = (positionAlongWall - 0.5) * wallTransform.length;

  // Apply the offset in the wall's direction
  const wallDir = new THREE.Vector3(
    Math.cos(wallTransform.rotation),
    0,
    -Math.sin(wallTransform.rotation)
  );

  const position = new THREE.Vector3(
    wallTransform.position.x + wallDir.x * offsetFromCenter,
    elevation,
    wallTransform.position.z + wallDir.z * offsetFromCenter
  );

  return { position, rotation: wallTransform.rotation };
};

export const Room: React.FC<RoomProps> = ({
  vertices,
  height,
  viewMode,
  openings,
  selectedOpeningId,
  onOpeningClick,
  onOpeningHover,
  isInteractive = false,
}) => {
  const groupRef = useRef<THREE.Group>(null);
  const { camera } = useThree();
  const wallRefs = useRef<(THREE.Mesh | null)[]>([]);
  const cornerRefs = useRef<(THREE.Mesh | null)[]>([]);
  const doorRefs = useRef<(THREE.Group | null)[]>([]);
  const windowRefs = useRef<(THREE.Group | null)[]>([]);

  useEffect(() => {
    if (!vertices || vertices.length === 0) {
      return;
    }
    wallRefs.current = wallRefs.current.slice(0, vertices.length);
    cornerRefs.current = cornerRefs.current.slice(0, vertices.length);
  }, [vertices.length, height, viewMode]);

  useFrame(() => {
    if (
      viewMode !== "3D" ||
      !vertices ||
      vertices.length < 3 ||
      wallRefs.current.length === 0
    )
      return;

    const hiddenWalls = new Set<number>();

    wallRefs.current.forEach((wallRef, index) => {
      if (!wallRef || index >= vertices.length) return;

      const nextIndex = (index + 1) % vertices.length;
      const v1_3D = convert2DTo3D(vertices[index], centroid);
      const v2_3D = convert2DTo3D(vertices[nextIndex], centroid);

      const wallDir = new THREE.Vector3(
        v2_3D.x - v1_3D.x,
        0,
        v2_3D.z - v1_3D.z
      ).normalize();
      const normal = new THREE.Vector3(-wallDir.z, 0, wallDir.x);

      const midpoint = new THREE.Vector3(
        (v1_3D.x + v2_3D.x) / 2,
        height / 2,
        (v1_3D.z + v2_3D.z) / 2
      );

      const cameraPos = new THREE.Vector3();
      camera.getWorldPosition(cameraPos);
      const toWall = new THREE.Vector3()
        .subVectors(midpoint, cameraPos)
        .normalize();

      const dotProduct = normal.dot(toWall);
      const isHidden = dotProduct > 0;
      wallRef.visible = !isHidden;
      setRaycastEnabled(wallRef, !isHidden);
      if (isHidden) {
        hiddenWalls.add(index);
      }
    });

    cornerRefs.current.forEach((cornerRef, index) => {
      if (!cornerRef || index >= vertices.length) return;

      const prevIndex = (index - 1 + vertices.length) % vertices.length;
      const isHidden = hiddenWalls.has(prevIndex) || hiddenWalls.has(index);

      cornerRef.visible = !isHidden;
      setRaycastEnabled(cornerRef, !isHidden);
    });

    // Hide doors on hidden walls and disable raycasting
    openings?.doors.forEach((door, index) => {
      const doorRef = doorRefs.current[index];
      if (doorRef) {
        const isHidden = hiddenWalls.has(door.wallIndex);
        doorRef.visible = !isHidden;
        setRaycastEnabled(doorRef, !isHidden);
      }
    });

    // Hide windows on hidden walls and disable raycasting
    openings?.windows.forEach((window, index) => {
      const windowRef = windowRefs.current[index];
      if (windowRef) {
        const isHidden = hiddenWalls.has(window.wallIndex);
        windowRef.visible = !isHidden;
        setRaycastEnabled(windowRef, !isHidden);
      }
    });
  });

  if (!vertices || vertices.length < 3) {
    return null;
  }

  const centroid = calculateCentroid(vertices);

  const floor3DVerts = vertices.map((v) => {
    const pos3D = convert2DTo3D(v, centroid);
    return new THREE.Vector2(pos3D.x, -pos3D.z);
  });

  const floorShape = new THREE.Shape(floor3DVerts);
  const floorGeometry = new THREE.ExtrudeGeometry(floorShape, {
    depth: FLOOR_THICKNESS,
    bevelEnabled: false,
  });

  return (
    <group ref={groupRef}>
      <mesh
        name="floor"
        receiveShadow
        position-y={0}
        rotation-x={-Math.PI / 2}
        material={floorMaterial}
        geometry={floorGeometry}
      />

      {vertices.map((vertex, index) => {
        const nextIndex = (index + 1) % vertices.length;
        const nextVertex = vertices[nextIndex];

        const v1_3D = convert2DTo3D(vertex, centroid);
        const v2_3D = convert2DTo3D(nextVertex, centroid);
        const dx = v2_3D.x - v1_3D.x;
        const dz = v2_3D.z - v1_3D.z;
        const length = Math.sqrt(dx * dx + dz * dz);

        const midpoint = new THREE.Vector3(
          (v1_3D.x + v2_3D.x) / 2,
          height / 2,
          (v1_3D.z + v2_3D.z) / 2
        );

        const angle = Math.atan2(-dz, dx);

        return (
          <mesh
            key={`wall-${index}`}
            name={`wall-${index}`}
            ref={(el) => (wallRefs.current[index] = el)}
            castShadow={false}
            receiveShadow
            position={midpoint}
            rotation-y={angle}
            material={wallMaterial}
          >
            <boxGeometry args={[length, height, WALL_THICKNESS]} />
          </mesh>
        );
      })}

      {vertices.map((vertex, index) => {
        const v_3D = convert2DTo3D(vertex, centroid);
        return (
          <mesh
            key={`corner-${index}`}
            ref={(el) => (cornerRefs.current[index] = el)}
            castShadow={false}
            receiveShadow
            position={[v_3D.x, height / 2, v_3D.z]}
            material={cornerMaterial}
          >
            <cylinderGeometry
              args={[WALL_THICKNESS / 2, WALL_THICKNESS / 2, height, 12]}
            />
          </mesh>
        );
      })}

      {/* Render doors */}
      {openings?.doors.map((door, index) => {
        const { position, rotation } = calculateOpeningPosition(
          door.wallIndex,
          door.position,
          0, // Door starts at floor level
          vertices,
          centroid
        );

        return (
          <group key={door.id} ref={(el) => (doorRefs.current[index] = el)}>
            <Door3D
              width={door.width}
              height={door.height}
              position={position}
              rotation={rotation}
              selected={selectedOpeningId === door.id}
              onClick={
                isInteractive
                  ? () => onOpeningClick?.(door.id, "door")
                  : undefined
              }
              onPointerOver={
                isInteractive ? () => onOpeningHover?.(door.id) : undefined
              }
              onPointerOut={
                isInteractive ? () => onOpeningHover?.(null) : undefined
              }
            />
          </group>
        );
      })}

      {/* Render windows */}
      {openings?.windows.map((window, index) => {
        const { position, rotation } = calculateOpeningPosition(
          window.wallIndex,
          window.position,
          window.elevation + window.height / 2, // Center of window
          vertices,
          centroid
        );

        return (
          <group key={window.id} ref={(el) => (windowRefs.current[index] = el)}>
            <Window3D
              width={window.width}
              height={window.height}
              position={position}
              rotation={rotation}
              selected={selectedOpeningId === window.id}
              onClick={
                isInteractive
                  ? () => onOpeningClick?.(window.id, "window")
                  : undefined
              }
              onPointerOver={
                isInteractive ? () => onOpeningHover?.(window.id) : undefined
              }
              onPointerOut={
                isInteractive ? () => onOpeningHover?.(null) : undefined
              }
            />
          </group>
        );
      })}
    </group>
  );
};
