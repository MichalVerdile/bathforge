import React from "react";
import * as THREE from "three";

interface Door3DProps {
  width: number;
  height: number;
  position: THREE.Vector3;
  rotation: number; // Y-axis rotation in radians
  selected?: boolean;
  onClick?: () => void;
  onPointerOver?: () => void;
  onPointerOut?: () => void;
}

const DOOR_DEPTH = 0.05; // Door thickness
const FRAME_WIDTH = 0.05; // Door frame width
const FRAME_DEPTH = 0.08; // Frame depth (slightly more than wall)

// Materials
const doorMaterial = new THREE.MeshStandardMaterial({
  color: "#8B4513", // Brown wood color
  roughness: 0.7,
  metalness: 0.1,
});

const frameMaterial = new THREE.MeshStandardMaterial({
  color: "#654321", // Darker brown for frame
  roughness: 0.6,
  metalness: 0.1,
});

const handleMaterial = new THREE.MeshStandardMaterial({
  color: "#C0C0C0", // Silver
  roughness: 0.3,
  metalness: 0.8,
});

const selectedMaterial = new THREE.MeshStandardMaterial({
  color: "#4A90D9",
  roughness: 0.5,
  metalness: 0.2,
  emissive: "#4A90D9",
  emissiveIntensity: 0.3,
});

export const Door3D: React.FC<Door3DProps> = ({
  width,
  height,
  position,
  rotation,
  selected = false,
  onClick,
  onPointerOver,
  onPointerOut,
}) => {
  const innerWidth = width - FRAME_WIDTH * 2;
  const innerHeight = height - FRAME_WIDTH;

  // Calculate handle position
  const handleHeight = height * 0.45;
  const handleOffset = innerWidth * 0.4;

  return (
    <group
      position={position}
      rotation={[0, rotation, 0]}
      onClick={(e) => {
        e.stopPropagation();
        onClick?.();
      }}
      onPointerOver={(e) => {
        e.stopPropagation();
        onPointerOver?.();
      }}
      onPointerOut={(e) => {
        e.stopPropagation();
        onPointerOut?.();
      }}
    >
      {/* Door frame - left */}
      <mesh
        position={[-width / 2 + FRAME_WIDTH / 2, height / 2, 0]}
        material={selected ? selectedMaterial : frameMaterial}
        receiveShadow
      >
        <boxGeometry args={[FRAME_WIDTH, height, FRAME_DEPTH]} />
      </mesh>

      {/* Door frame - right */}
      <mesh
        position={[width / 2 - FRAME_WIDTH / 2, height / 2, 0]}
        material={selected ? selectedMaterial : frameMaterial}
        receiveShadow
      >
        <boxGeometry args={[FRAME_WIDTH, height, FRAME_DEPTH]} />
      </mesh>

      {/* Door frame - top */}
      <mesh
        position={[0, height - FRAME_WIDTH / 2, 0]}
        material={selected ? selectedMaterial : frameMaterial}
        receiveShadow
      >
        <boxGeometry args={[width, FRAME_WIDTH, FRAME_DEPTH]} />
      </mesh>

      {/* Door panel */}
      <mesh
        position={[0, innerHeight / 2 + 0.01, DOOR_DEPTH / 2]}
        material={selected ? selectedMaterial : doorMaterial}
        receiveShadow
      >
        <boxGeometry args={[innerWidth, innerHeight - 0.02, DOOR_DEPTH]} />
      </mesh>

      {/* Door handle */}
      <mesh
        position={[handleOffset, handleHeight, DOOR_DEPTH + 0.02]}
        material={handleMaterial}
      >
        <cylinderGeometry args={[0.015, 0.015, 0.1, 8]} />
      </mesh>

      {/* Handle base plate */}
      <mesh
        position={[handleOffset, handleHeight, DOOR_DEPTH + 0.005]}
        material={handleMaterial}
      >
        <boxGeometry args={[0.03, 0.08, 0.01]} />
      </mesh>

      {/* Selection highlight outline */}
      {selected && (
        <mesh position={[0, height / 2, 0]}>
          <boxGeometry
            args={[width + 0.02, height + 0.02, FRAME_DEPTH + 0.02]}
          />
          <meshBasicMaterial color="#4A90D9" wireframe />
        </mesh>
      )}
    </group>
  );
};

export default Door3D;
