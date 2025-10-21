import React, { useEffect } from "react";
import { Canvas, useThree } from "@react-three/fiber";
import * as THREE from "three";
import { Room } from "./Room";
import {
  OrthographicCamera,
  PerspectiveCamera,
  MapControls,
  OrbitControls,
} from "@react-three/drei";

const DynamicCamera: React.FC<RoomEditorProps> = ({
  viewMode,
  width,
  depth,
}) => {
  const { camera } = useThree();

  useEffect(() => {
    if (viewMode === "2D") {
      camera.position.set(0, 20, 0);
      camera.rotation.set(-Math.PI / 2, 0, 0);

      if ("zoom" in camera) {
        const maxDim = Math.max(width, depth);
        (camera as THREE.OrthographicCamera).zoom = 100 / (maxDim * 0.3);
        camera.updateProjectionMatrix();
      }
    } else {
      camera.position.set(width, 10, depth * 1.5);
      camera.rotation.set(0, 0, 0);

      if ("zoom" in camera) {
        (camera as THREE.PerspectiveCamera).zoom = 1;
        camera.updateProjectionMatrix();
      }
    }
  }, [viewMode, width, depth, camera]);

  return (
    <>
      {viewMode === "2D" ? (
        <OrthographicCamera makeDefault />
      ) : (
        <PerspectiveCamera makeDefault fov={60} />
      )}

      {viewMode === "2D" ? (
        <MapControls enableRotate={false} screenSpacePanning={true} />
      ) : (
        <OrbitControls target={[0, 1.25, 0]} />
      )}
    </>
  );
};

interface RoomEditorProps {
  viewMode: "2D" | "3D";
  width: number;
  depth: number;
}

export const RoomEditor: React.FC<RoomEditorProps> = ({
  viewMode,
  width,
  depth,
}) => {
  return (
    <Canvas shadows camera={undefined}>
      <color attach="background" args={["#0f172a"]} />

      <ambientLight intensity={0.5} />
      <directionalLight
        position={[10, 15, 10]}
        intensity={1}
        castShadow
        shadow-mapSize-width={2048}
        shadow-mapSize-height={2048}
      />

      <Room width={width} depth={depth} />
      <DynamicCamera viewMode={viewMode} width={width} depth={depth} />
    </Canvas>
  );
};
