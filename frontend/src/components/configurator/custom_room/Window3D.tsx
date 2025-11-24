import React from "react";
import * as THREE from "three";

interface Window3DProps {
  width: number;
  height: number;
  position: THREE.Vector3;
  rotation: number; // Y-axis rotation in radians
  selected?: boolean;
  onClick?: () => void;
  onPointerOver?: () => void;
  onPointerOut?: () => void;
}

const FRAME_WIDTH = 0.04; // Window frame width
const FRAME_DEPTH = 0.12; // Frame depth (slightly more than wall thickness of 0.075)
const GLASS_DEPTH = 0.076; // Glass thickness
const DIVIDER_WIDTH = 0.02; // Width of window dividers

// Materials
const frameMaterial = new THREE.MeshStandardMaterial({
  color: "#FFFFFF", // White frame
  roughness: 0.4,
  metalness: 0.1,
});

const glassMaterial = new THREE.MeshStandardMaterial({
  color: "#B5E3FA", // Lighter blue glass
  roughness: 0.1,
  metalness: 0.2,
});

const selectedFrameMaterial = new THREE.MeshStandardMaterial({
  color: "#4A90D9",
  roughness: 0.5,
  metalness: 0.2,
  emissive: "#4A90D9",
  emissiveIntensity: 0.3,
});

export const Window3D: React.FC<Window3DProps> = ({
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
  const innerHeight = height - FRAME_WIDTH * 2;
  const currentFrameMaterial = selected ? selectedFrameMaterial : frameMaterial;

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
      {/* Window frame - left */}
      <mesh
        position={[-width / 2 + FRAME_WIDTH / 2, 0, 0]}
        material={currentFrameMaterial}
        receiveShadow
      >
        <boxGeometry args={[FRAME_WIDTH, height, FRAME_DEPTH]} />
      </mesh>

      {/* Window frame - right */}
      <mesh
        position={[width / 2 - FRAME_WIDTH / 2, 0, 0]}
        material={currentFrameMaterial}
        receiveShadow
      >
        <boxGeometry args={[FRAME_WIDTH, height, FRAME_DEPTH]} />
      </mesh>

      {/* Window frame - top */}
      <mesh
        position={[0, height / 2 - FRAME_WIDTH / 2, 0]}
        material={currentFrameMaterial}
        receiveShadow
      >
        <boxGeometry args={[width, FRAME_WIDTH, FRAME_DEPTH]} />
      </mesh>

      {/* Window frame - bottom */}
      <mesh
        position={[0, -height / 2 + FRAME_WIDTH / 2, 0]}
        material={currentFrameMaterial}
        receiveShadow
      >
        <boxGeometry args={[width, FRAME_WIDTH, FRAME_DEPTH]} />
      </mesh>

      {/* Horizontal divider (middle) */}
      <mesh position={[0, 0, 0]} material={currentFrameMaterial} receiveShadow>
        <boxGeometry args={[innerWidth, DIVIDER_WIDTH, FRAME_DEPTH * 0.8]} />
      </mesh>

      {/* Vertical divider (middle) */}
      <mesh position={[0, 0, 0]} material={currentFrameMaterial} receiveShadow>
        <boxGeometry args={[DIVIDER_WIDTH, innerHeight, FRAME_DEPTH * 0.8]} />
      </mesh>

      {/* Glass panes (4 panes) */}
      {/* Top-left pane */}
      <mesh
        position={[-innerWidth / 4, innerHeight / 4, 0]}
        material={glassMaterial}
      >
        <boxGeometry
          args={[
            innerWidth / 2 - DIVIDER_WIDTH / 2 - 0.01,
            innerHeight / 2 - DIVIDER_WIDTH / 2 - 0.01,
            GLASS_DEPTH,
          ]}
        />
      </mesh>

      {/* Top-right pane */}
      <mesh
        position={[innerWidth / 4, innerHeight / 4, 0]}
        material={glassMaterial}
      >
        <boxGeometry
          args={[
            innerWidth / 2 - DIVIDER_WIDTH / 2 - 0.01,
            innerHeight / 2 - DIVIDER_WIDTH / 2 - 0.01,
            GLASS_DEPTH,
          ]}
        />
      </mesh>

      {/* Bottom-left pane */}
      <mesh
        position={[-innerWidth / 4, -innerHeight / 4, 0]}
        material={glassMaterial}
      >
        <boxGeometry
          args={[
            innerWidth / 2 - DIVIDER_WIDTH / 2 - 0.01,
            innerHeight / 2 - DIVIDER_WIDTH / 2 - 0.01,
            GLASS_DEPTH,
          ]}
        />
      </mesh>

      {/* Bottom-right pane */}
      <mesh
        position={[innerWidth / 4, -innerHeight / 4, 0]}
        material={glassMaterial}
      >
        <boxGeometry
          args={[
            innerWidth / 2 - DIVIDER_WIDTH / 2 - 0.01,
            innerHeight / 2 - DIVIDER_WIDTH / 2 - 0.01,
            GLASS_DEPTH,
          ]}
        />
      </mesh>

      {/* Window sill */}
      {/* <mesh
        position={[0, -height / 2 - 0.02, FRAME_DEPTH / 2 + 0.02]}
        material={currentFrameMaterial}
        receiveShadow
      >
        <boxGeometry args={[width + 0.04, 0.03, 0.08]} />
      </mesh> */}

      {/* Selection highlight outline */}
      {selected && (
        <mesh position={[0, 0, 0]}>
          <boxGeometry
            args={[width + 0.02, height + 0.02, FRAME_DEPTH + 0.02]}
          />
          <meshBasicMaterial color="#4A90D9" wireframe />
        </mesh>
      )}
    </group>
  );
};

export default Window3D;
