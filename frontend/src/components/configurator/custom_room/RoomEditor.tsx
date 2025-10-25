import React, {
  useEffect,
  useState,
  useImperativeHandle,
  forwardRef,
} from "react";
import { Canvas, useThree } from "@react-three/fiber";
import * as THREE from "three";
import { Room } from "./Room";
import { Simple2DEditor } from "./Simple2DEditor";
import {
  OrthographicCamera,
  PerspectiveCamera,
  OrbitControls,
} from "@react-three/drei";

interface Vertex {
  x: number;
  y: number;
}

const DynamicCamera: React.FC<{
  viewMode: "2D" | "3D";
  width: number;
  depth: number;
  height: number;
  isDragging: boolean;
}> = ({ viewMode, width, depth, height, isDragging }) => {
  const { camera } = useThree();
  const [controlsRef, setControlsRef] = useState<any>(null);

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

  // Re-enable controls after drag completes
  useEffect(() => {
    if (!isDragging && controlsRef) {
      controlsRef.enabled = true;
    }
  }, [isDragging, controlsRef]);

  return (
    <>
      {viewMode === "2D" ? (
        <OrthographicCamera makeDefault />
      ) : (
        <PerspectiveCamera makeDefault fov={60} />
      )}

      {viewMode === "2D" ? null : ( // No camera controls in 2D mode - panning disabled
        <OrbitControls
          ref={setControlsRef}
          target={[0, 1.25, 0]}
          enabled={!isDragging}
        />
      )}
    </>
  );
};

interface RoomEditorProps {
  viewMode: "2D" | "3D";
  width: number;
  depth: number;
  height: number;
  onWidthChange: (width: number) => void;
  onDepthChange: (depth: number) => void;
  onHeightChange: (height: number) => void;
}

export interface RoomEditorRef {
  reset: () => void;
}

export const RoomEditor = forwardRef<RoomEditorRef, RoomEditorProps>(
  (
    {
      viewMode,
      width,
      depth,
      height,
      onWidthChange,
      onDepthChange,
      onHeightChange,
    },
    ref
  ) => {
    const [isDragging, setIsDragging] = useState(false);
    // Calculate initial canvas size and default square
    const getInitialVertices = (): Vertex[] => {
      const canvasSize = Math.min(
        Math.min(window.innerHeight - 200, window.innerWidth - 40),
        800
      );
      const margin = canvasSize * 0.3;
      const squareSize = canvasSize * 0.4;

      return [
        { x: margin, y: margin },
        { x: margin + squareSize, y: margin },
        { x: margin + squareSize, y: margin + squareSize },
        { x: margin, y: margin + squareSize },
      ];
    };

    const [vertices, setVertices] = useState<Vertex[]>(getInitialVertices());

    const handleDragStart = () => setIsDragging(true);
    const handleDragEnd = () => setIsDragging(false);

    // Reset function to restore default vertices
    const resetVertices = () => {
      setVertices(getInitialVertices());
    };

    // Expose reset function to parent
    useImperativeHandle(ref, () => ({
      reset: resetVertices,
    }));

    // If in 2D mode, render the Simple2DEditor
    if (viewMode === "2D") {
      return <Simple2DEditor vertices={vertices} setVertices={setVertices} />;
    }

    // If in 3D mode, render the 3D canvas
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

        <Room
          width={width}
          depth={depth}
          height={height}
          viewMode={viewMode}
          onWidthChange={onWidthChange}
          onDepthChange={onDepthChange}
          onHeightChange={onHeightChange}
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
        />
        <DynamicCamera
          viewMode={viewMode}
          width={width}
          depth={depth}
          height={height}
          isDragging={isDragging}
        />
      </Canvas>
    );
  }
);
