import React, { useState, useRef, useCallback } from "react";
import * as THREE from "three";
import { Html } from "@react-three/drei";

// Room component with drag functionality

interface RoomProps {
  width: number;
  depth: number;
  height: number;
  viewMode: "2D" | "3D";
  onWidthChange: (width: number) => void;
  onDepthChange: (depth: number) => void;
  onHeightChange: (height: number) => void;
  onDragStart: () => void;
  onDragEnd: () => void;
}

const WALL_THICKNESS = 0.2;

const wallMaterial = new THREE.MeshStandardMaterial({ color: "#f1f5f9" });
const wallHoverMaterial = new THREE.MeshStandardMaterial({ color: "#cbd5e1" });
const floorMaterial = new THREE.MeshStandardMaterial({ color: "#7b7e81ff" });

// Dimension constraints
const MIN_DIMENSION = 2; // meters
const MAX_DIMENSION = 10; // meters
const MIN_HEIGHT = 2; // meters
const MAX_HEIGHT = 4; // meters

export const Room: React.FC<RoomProps> = ({
  width,
  depth,
  height,
  viewMode,
  onWidthChange,
  onDepthChange,
  onHeightChange,
  onDragStart,
  onDragEnd,
}) => {
  const [hoveredWall, setHoveredWall] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState<THREE.Vector2 | null>(null);
  const [dragWall, setDragWall] = useState<string | null>(null);
  const [dragStartDimensions, setDragStartDimensions] = useState<{
    width: number;
    depth: number;
    height: number;
  } | null>(null);
  const [roomPosition, setRoomPosition] = useState<THREE.Vector3>(
    new THREE.Vector3(0, 0, 0)
  );

  const groupRef = useRef<THREE.Group>(null);

  const constrainDimension = (value: number, min: number, max: number) => {
    return Math.max(min, Math.min(max, value));
  };

  const handlePointerEnter = useCallback(
    (wall: string) => {
      if (viewMode === "2D") {
        setHoveredWall(wall);
        // Set cursor based on wall direction
        if (wall === "north" || wall === "south") {
          document.body.style.cursor = "ns-resize";
        } else if (wall === "east" || wall === "west") {
          document.body.style.cursor = "ew-resize";
        }
      } else if (viewMode === "3D") {
        // In 3D mode, show height adjustment cursor
        document.body.style.cursor = "ns-resize";
      }
    },
    [viewMode]
  );

  const handlePointerLeave = useCallback(() => {
    if (!isDragging) {
      setHoveredWall(null);
      document.body.style.cursor = "default";
    }
  }, [isDragging]);

  const handlePointerDown = useCallback(
    (event: any, wall: string) => {
      event.stopPropagation();
      setIsDragging(true);
      setDragWall(wall);
      setDragStart(new THREE.Vector2(event.clientX, event.clientY));
      setDragStartDimensions({ width, depth, height });
      onDragStart(); // Notify parent that dragging has started
    },
    [width, depth, height, onDragStart]
  );

  const handlePointerMove = useCallback(
    (event: any) => {
      if (!isDragging || !dragStart || !dragStartDimensions) return;

      const deltaX = (event.clientX - dragStart.x) * 0.01; // Scale factor for sensitivity
      const deltaY = (event.clientY - dragStart.y) * 0.01;

      if (viewMode === "2D") {
        // In 2D mode, move the room position to make it feel like the wall is moving
        if (dragWall === "east") {
          const newWidth = constrainDimension(
            dragStartDimensions.width + deltaX,
            MIN_DIMENSION,
            MAX_DIMENSION
          );
          onWidthChange(newWidth);
          // Move room left to keep the dragged wall in place
          setRoomPosition(new THREE.Vector3(-deltaX * 0.5, 0, 0));
        } else if (dragWall === "west") {
          const newWidth = constrainDimension(
            dragStartDimensions.width - deltaX,
            MIN_DIMENSION,
            MAX_DIMENSION
          );
          onWidthChange(newWidth);
          // Move room right to keep the dragged wall in place
          setRoomPosition(new THREE.Vector3(deltaX * 0.5, 0, 0));
        } else if (dragWall === "north") {
          const newDepth = constrainDimension(
            dragStartDimensions.depth - deltaY, // Fixed: was + deltaY
            MIN_DIMENSION,
            MAX_DIMENSION
          );
          onDepthChange(newDepth);
          // Move room down to keep the dragged wall in place
          setRoomPosition(new THREE.Vector3(0, 0, deltaY * 0.5));
        } else if (dragWall === "south") {
          const newDepth = constrainDimension(
            dragStartDimensions.depth + deltaY, // Fixed: was - deltaY
            MIN_DIMENSION,
            MAX_DIMENSION
          );
          onDepthChange(newDepth);
          // Move room up to keep the dragged wall in place
          setRoomPosition(new THREE.Vector3(0, 0, -deltaY * 0.5));
        }
      } else if (viewMode === "3D") {
        const newHeight = constrainDimension(
          dragStartDimensions.height - deltaY,
          MIN_HEIGHT,
          MAX_HEIGHT
        );
        onHeightChange(newHeight);
      }
    },
    [
      isDragging,
      dragStart,
      dragStartDimensions,
      dragWall,
      viewMode,
      onWidthChange,
      onDepthChange,
      onHeightChange,
    ]
  );

  const handlePointerUp = useCallback(() => {
    setIsDragging(false);
    setDragWall(null);
    setDragStart(null);
    setDragStartDimensions(null);
    setHoveredWall(null);
    document.body.style.cursor = "default";

    // Center the room after dragging completes
    if (viewMode === "2D") {
      setRoomPosition(new THREE.Vector3(0, 0, 0));
    }

    onDragEnd(); // Notify parent that dragging has ended
  }, [onDragEnd, viewMode]);

  // Add global pointer move and up listeners
  React.useEffect(() => {
    if (isDragging) {
      const handleGlobalPointerMove = (event: PointerEvent) =>
        handlePointerMove(event);
      const handleGlobalPointerUp = () => handlePointerUp();

      document.addEventListener("pointermove", handleGlobalPointerMove);
      document.addEventListener("pointerup", handleGlobalPointerUp);

      return () => {
        document.removeEventListener("pointermove", handleGlobalPointerMove);
        document.removeEventListener("pointerup", handleGlobalPointerUp);
      };
    }
  }, [isDragging, handlePointerMove, handlePointerUp]);
  return (
    <group ref={groupRef} position={roomPosition}>
      <mesh receiveShadow rotation-x={-Math.PI / 2} material={floorMaterial}>
        <planeGeometry args={[width, depth]} />
      </mesh>

      {/* Back Wall (North) */}
      <mesh
        castShadow
        receiveShadow
        position={[0, height / 2, -depth / 2]}
        material={
          viewMode === "2D" && hoveredWall === "north"
            ? wallHoverMaterial
            : wallMaterial
        }
        onPointerEnter={() => handlePointerEnter("north")}
        onPointerLeave={handlePointerLeave}
        onPointerDown={(e) => handlePointerDown(e, "north")}
      >
        <boxGeometry args={[width + WALL_THICKNESS, height, WALL_THICKNESS]} />
      </mesh>

      {/* Front Wall (South) */}
      <mesh
        castShadow
        receiveShadow
        position={[0, height / 2, depth / 2]}
        material={
          viewMode === "2D" && hoveredWall === "south"
            ? wallHoverMaterial
            : wallMaterial
        }
        onPointerEnter={() => handlePointerEnter("south")}
        onPointerLeave={handlePointerLeave}
        onPointerDown={(e) => handlePointerDown(e, "south")}
      >
        <boxGeometry args={[width + WALL_THICKNESS, height, WALL_THICKNESS]} />
      </mesh>

      {/* Left Wall (West) */}
      <mesh
        castShadow
        receiveShadow
        position={[-width / 2, height / 2, 0]}
        material={
          viewMode === "2D" && hoveredWall === "west"
            ? wallHoverMaterial
            : wallMaterial
        }
        onPointerEnter={() => handlePointerEnter("west")}
        onPointerLeave={handlePointerLeave}
        onPointerDown={(e) => handlePointerDown(e, "west")}
      >
        <boxGeometry args={[WALL_THICKNESS, height, depth]} />
      </mesh>

      {/* Right Wall (East) */}
      <mesh
        castShadow
        receiveShadow
        position={[width / 2, height / 2, 0]}
        material={
          viewMode === "2D" && hoveredWall === "east"
            ? wallHoverMaterial
            : wallMaterial
        }
        onPointerEnter={() => handlePointerEnter("east")}
        onPointerLeave={handlePointerLeave}
        onPointerDown={(e) => handlePointerDown(e, "east")}
      >
        <boxGeometry args={[WALL_THICKNESS, height, depth]} />
      </mesh>

      {/* Dimension Labels - Only show in 2D mode */}
      {viewMode === "2D" && (
        <>
          {/* Width labels (top and bottom) */}
          <Html position={[0, height + 0.5, -depth / 2 - 0.3]} center>
            <div className="dimension-label">{Math.round(width * 100)} cm</div>
          </Html>
          <Html position={[0, height + 0.5, depth / 2 + 0.3]} center>
            <div className="dimension-label">{Math.round(width * 100)} cm</div>
          </Html>

          {/* Depth labels (left and right) */}
          <Html position={[-width / 2 - 0.3, height + 0.5, 0]} center>
            <div className="dimension-label">{Math.round(depth * 100)} cm</div>
          </Html>
          <Html position={[width / 2 + 0.3, height + 0.5, 0]} center>
            <div className="dimension-label">{Math.round(depth * 100)} cm</div>
          </Html>
        </>
      )}

      {/* Height label - Only show in 3D mode */}
      {viewMode === "3D" && (
        <Html position={[width / 2 + 0.5, height / 2, 0]} center>
          <div className="dimension-label">{Math.round(height * 100)} cm</div>
        </Html>
      )}
    </group>
  );
};
