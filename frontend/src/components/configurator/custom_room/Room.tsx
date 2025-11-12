import React, { useRef, useEffect } from "react";
import * as THREE from "three";
import { useFrame, useThree } from "@react-three/fiber";

interface Vertex {
  x: number;
  y: number;
}

interface RoomProps {
  vertices: Vertex[];
  height: number;
  viewMode: "2D" | "3D";
}

const WALL_THICKNESS = 0.075;
const FLOOR_THICKNESS = 0.075;

const wallMaterial = new THREE.MeshStandardMaterial({ color: "#f1f5f9" });
const floorMaterial = new THREE.MeshStandardMaterial({ color: "#7b7e81ff" });
const cornerMaterial = new THREE.MeshStandardMaterial({ color: "#f1f5f9" });

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

export const Room: React.FC<RoomProps> = ({ vertices, height, viewMode }) => {
  const groupRef = useRef<THREE.Group>(null);
  const { camera } = useThree();
  const wallRefs = useRef<(THREE.Mesh | null)[]>([]);
  const cornerRefs = useRef<(THREE.Mesh | null)[]>([]);

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
      if (dotProduct > 0) {
        wallRef.visible = false;
        hiddenWalls.add(index);
      } else {
        wallRef.visible = true;
      }
    });

    cornerRefs.current.forEach((cornerRef, index) => {
      if (!cornerRef || index >= vertices.length) return;

      const prevIndex = (index - 1 + vertices.length) % vertices.length;
      const isHidden = hiddenWalls.has(prevIndex) || hiddenWalls.has(index);

      if (isHidden) {
        cornerRef.visible = false;
      } else {
        cornerRef.visible = true;
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
    </group>
  );
};
