import React from "react";
import * as THREE from "three";

interface RoomProps {
  width: number;
  depth: number;
}

const WALL_HEIGHT = 2.5;
const WALL_THICKNESS = 0.2;

const wallMaterial = new THREE.MeshStandardMaterial({ color: "#f1f5f9" });
const floorMaterial = new THREE.MeshStandardMaterial({ color: "#7b7e81ff" });

export const Room: React.FC<RoomProps> = ({ width, depth }) => {
  return (
    <group>
      <mesh receiveShadow rotation-x={-Math.PI / 2} material={floorMaterial}>
        <planeGeometry args={[width, depth]} />
      </mesh>

      {/* Back Wall */}
      <mesh
        castShadow
        receiveShadow
        position={[0, WALL_HEIGHT / 2, -depth / 2]}
        material={wallMaterial}
      >
        <boxGeometry
          args={[width + WALL_THICKNESS, WALL_HEIGHT, WALL_THICKNESS]}
        />
      </mesh>

      {/* Front Wall */}
      <mesh
        castShadow
        receiveShadow
        position={[0, WALL_HEIGHT / 2, depth / 2]}
        material={wallMaterial}
      >
        <boxGeometry
          args={[width + WALL_THICKNESS, WALL_HEIGHT, WALL_THICKNESS]}
        />
      </mesh>

      {/* Left Wall */}
      <mesh
        castShadow
        receiveShadow
        position={[-width / 2, WALL_HEIGHT / 2, 0]}
        material={wallMaterial}
      >
        <boxGeometry args={[WALL_THICKNESS, WALL_HEIGHT, depth]} />
      </mesh>

      {/* Right Wall */}
      <mesh
        castShadow
        receiveShadow
        position={[width / 2, WALL_HEIGHT / 2, 0]}
        material={wallMaterial}
      >
        <boxGeometry args={[WALL_THICKNESS, WALL_HEIGHT, depth]} />
      </mesh>
    </group>
  );
};
