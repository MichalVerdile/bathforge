import React, {
  useEffect,
  useState,
  useImperativeHandle,
  forwardRef,
  useMemo,
} from "react";
import { Canvas, useThree, useFrame } from "@react-three/fiber";
import * as THREE from "three";
import { Room } from "./Room";
import { TwoDEditor, calculateCanvasSize } from "./2DEditor";
import {
  OrthographicCamera,
  PerspectiveCamera,
  OrbitControls,
} from "@react-three/drei";
import {
  RoomOpenings,
  createDefaultOpenings,
} from "./DoorWindowTypes";

interface Vertex {
  x: number;
  y: number;
}

const DynamicCamera: React.FC<{
  viewMode: "2D" | "3D";
  vertices: Vertex[];
  height: number;
}> = ({ viewMode, vertices, height }) => {
  const { camera } = useThree();
  const [controlsRef, setControlsRef] = useState<any>(null);
  const [hasSet3DInitialPosition, setHasSet3DInitialPosition] = useState(false);

  // Calculate bounding box from vertices
  const bounds = useMemo(() => {
    if (vertices.length === 0) return { width: 8, depth: 8 };

    let minX = Infinity,
      maxX = -Infinity;
    let minY = Infinity,
      maxY = -Infinity;

    vertices.forEach((v) => {
      minX = Math.min(minX, v.x);
      maxX = Math.max(maxX, v.x);
      minY = Math.min(minY, v.y);
      maxY = Math.max(maxY, v.y);
    });

    // Convert to meters (1 pixel = 0.01 meters)
    return {
      width: (maxX - minX) * 0.01,
      depth: (maxY - minY) * 0.01,
    };
  }, [vertices]);

  // Set initial camera position when switching to 3D mode
  useEffect(() => {
    if (viewMode === "2D") {
      camera.position.set(0, 20, 0);
      camera.rotation.set(-Math.PI / 2, 0, 0);
      setHasSet3DInitialPosition(false);

      if ("zoom" in camera) {
        const maxDim = Math.max(bounds.width, bounds.depth);
        (camera as THREE.OrthographicCamera).zoom = 100 / (maxDim * 0.3);
        camera.updateProjectionMatrix();
      }
    } else if (controlsRef && !hasSet3DInitialPosition) {
      // Set initial 3D position only once when controls are ready
      const maxDim = Math.max(bounds.width, bounds.depth);
      const desiredPosition = new THREE.Vector3(
        maxDim * 1.5,
        height * 1.5,
        maxDim * 1.5
      );

      // Set camera position
      camera.position.copy(desiredPosition);
      camera.lookAt(0, height / 2, 0);

      // Mark as set
      setHasSet3DInitialPosition(true);
    }
  }, [viewMode, bounds, camera, controlsRef, height, hasSet3DInitialPosition]);

  // Update camera controls for damping effect
  useFrame(() => {
    if (controlsRef && viewMode === "3D") {
      controlsRef.update();
    }
  });

  return (
    <>
      {viewMode === "2D" ? (
        <OrthographicCamera makeDefault />
      ) : (
        <PerspectiveCamera makeDefault fov={60} />
      )}

      {viewMode === "3D" && (
        <OrbitControls
          ref={setControlsRef}
          target={[0, 1.25, 0]}
          enableDamping={true}
          dampingFactor={0.05}
          minPolarAngle={0}
          maxPolarAngle={Math.PI / 1.8}
          minDistance={Math.max(bounds.width, bounds.depth) * 1.5}
          maxDistance={Math.max(bounds.width, bounds.depth) * 6}
        />
      )}
    </>
  );
};

interface RoomEditorProps {
  viewMode: "2D" | "3D";
  height: number;
}

export interface RoomEditorRef {
  reset: () => void;
  getRoomData: () => { vertices: Vertex[]; height: number; openings: RoomOpenings };
}

export const RoomEditor = forwardRef<RoomEditorRef, RoomEditorProps>(
  ({ viewMode, height }, ref) => {
    const getInitialVertices = (): Vertex[] => {
      const canvas = calculateCanvasSize();
      const squareSize = 200; // Fixed 200x200 square

      // Center the square on the canvas
      const centerX = canvas.width / 2;
      const centerY = canvas.height / 2;
      const halfSize = squareSize / 2;

      return [
        { x: centerX - halfSize, y: centerY - halfSize },
        { x: centerX + halfSize, y: centerY - halfSize },
        { x: centerX + halfSize, y: centerY + halfSize },
        { x: centerX - halfSize, y: centerY + halfSize },
      ];
    };

    const [vertices, setVertices] = useState<Vertex[]>(getInitialVertices());

    // Normalize vertices to start at origin for consistent door/window placement
    const normalizedVertices = useMemo(() => {
      if (vertices.length === 0) return [];
      const minX = Math.min(...vertices.map((v) => v.x));
      const minY = Math.min(...vertices.map((v) => v.y));
      return vertices.map((v) => ({ x: v.x - minX, y: v.y - minY }));
    }, [vertices]);

    // Generate default openings based on current room shape
    const openings = useMemo(() => {
      if (normalizedVertices.length < 3) return { doors: [], windows: [] };
      return createDefaultOpenings(normalizedVertices, height);
    }, [normalizedVertices, height]);

    const resetVertices = () => {
      setVertices(getInitialVertices());
    };

    useImperativeHandle(ref, () => ({
      reset: resetVertices,
      getRoomData: () => ({ vertices: normalizedVertices, height, openings }),
    }));

    if (viewMode === "2D") {
      return <TwoDEditor vertices={vertices} setVertices={setVertices} />;
    }

    return (
      <Canvas shadows camera={undefined}>
        <color attach="background" args={["#0f172a"]} />

        <ambientLight intensity={0.5} />
        <directionalLight
          position={[5, 8, 5]}
          intensity={1}
          castShadow
          shadow-mapSize-width={2048}
          shadow-mapSize-height={2048}
          shadow-bias={-0.0001}
        />

        <Room
          vertices={normalizedVertices}
          height={height}
          viewMode={viewMode}
          openings={openings}
          isInteractive={false}
        />
        <DynamicCamera
          viewMode={viewMode}
          vertices={vertices}
          height={height}
        />
      </Canvas>
    );
  }
);
