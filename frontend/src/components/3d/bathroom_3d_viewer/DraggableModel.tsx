import React, {
  useRef,
  useState,
  useEffect,
  useCallback,
  memo,
  useMemo,
} from "react";
import { ThreeEvent, useThree, useLoader } from "@react-three/fiber";
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";
import { DRACOLoader } from "three/examples/jsm/loaders/DRACOLoader";
import * as THREE from "three";
import { useModelProcessor } from "./useModelProcessor";
import {
  convertRoomTo3D,
  shrinkPolygon,
  findValidPosition as findValidPositionUtil,
} from "./collisionUtils";

interface Vertex {
  x: number;
  y: number;
}

interface DraggableModelProps {
  id: string;
  url: string;
  category?: string;
  position?: [number, number, number];
  rotation?: [number, number, number];
  scale?: [number, number, number];
  selected?: boolean;
  castShadow?: boolean;
  receiveShadow?: boolean;
  color?: string;
  dragSensitivity?: number;
  highlightColor?: string;
  disableInteractions?: boolean;
  roomVertices?: Vertex[];
  roomHeight?: number;
  onClick?: () => void;
  onPositionChange?: (position: [number, number, number]) => void;
  onRotationChange?: (rotation: [number, number, number]) => void;
  onLoad?: (model: THREE.Group) => void;
  onError?: (error: Error) => void;
  onDragStart?: (id: string) => void;
  onDragEnd?: (id: string) => void;
}

function DraggableModel({
  id,
  url,
  category,
  position = [0, 0, 0],
  rotation = [0, 0, 0],
  scale = [1, 1, 1],
  selected = false,
  castShadow = true,
  receiveShadow = true,
  color,
  dragSensitivity = 1.1,
  highlightColor = "white",
  disableInteractions = false,
  roomVertices,
  roomHeight = 2.5,
  onClick,
  onPositionChange,
  onRotationChange,
  onLoad,
  onError,
  onDragStart,
  onDragEnd,
}: DraggableModelProps) {
  const meshRef = useRef<THREE.Group>(null);
  const dragPositionRef = useRef<[number, number, number]>(position);

  // Load GLTF model
  const gltf = useLoader(GLTFLoader, url, (loader: GLTFLoader) => {
    const dracoLoader = new DRACOLoader();
    dracoLoader.setDecoderPath(
      "https://www.gstatic.com/draco/versioned/decoders/1.5.6/"
    );
    dracoLoader.setDecoderConfig({ type: "js" });
    loader.setDRACOLoader(dracoLoader);
  });

  const { camera, gl } = useThree();

  // Process model using custom hook
  const { processedModel, highlightModel, error } = useModelProcessor({
    gltf,
    url,
    category,
    color,
    castShadow,
    receiveShadow,
    highlightColor,
    onLoad,
    onError,
  });

  const [currentPosition, setCurrentPosition] =
    useState<[number, number, number]>(position);
  const [currentRotation, setCurrentRotation] =
    useState<[number, number, number]>(rotation);
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState<{
    position: [number, number, number];
    mouse: THREE.Vector2;
  } | null>(null);

  const COLLISION_BUFFER = 0.04;

  // Convert room vertices to 3D and shrink for collision
  const roomPolygon3D = useMemo(
    () => (roomVertices ? convertRoomTo3D(roomVertices) : null),
    [roomVertices]
  );

  const shrunkPolygon = useMemo(
    () =>
      roomPolygon3D ? shrinkPolygon(roomPolygon3D, COLLISION_BUFFER) : null,
    [roomPolygon3D]
  );

  // Cursor management
  const setCursor = useCallback(
    (cursor: string) => {
      try {
        (gl.domElement as HTMLCanvasElement).style.cursor = cursor;
      } catch {}
    },
    [gl]
  );

  // Click handler
  const handleClick = useCallback(
    (event: ThreeEvent<MouseEvent>) => {
      if (disableInteractions) return;
      event.stopPropagation();
      if (onClick && !isDragging) {
        onClick();
      }
    },
    [disableInteractions, onClick, isDragging]
  );

  // Pointer handlers
  const handlePointerOver = useCallback(
    (event: ThreeEvent<PointerEvent>) => {
      if (disableInteractions) return;
      event.stopPropagation();
      if (!isDragging) setCursor("grab");
    },
    [disableInteractions, isDragging, setCursor]
  );

  const handlePointerOut = useCallback(
    (event: ThreeEvent<PointerEvent>) => {
      if (disableInteractions) return;
      event.stopPropagation();
      if (!isDragging) setCursor("default");
    },
    [disableInteractions, isDragging, setCursor]
  );

  const handlePointerDown = useCallback(
    (event: ThreeEvent<PointerEvent>) => {
      if (disableInteractions) return;
      event.stopPropagation();
      if (onClick) onClick();

      dragPositionRef.current = currentPosition;
      setIsDragging(true);
      if (onDragStart) onDragStart(id);
      setCursor("none");

      setDragStart({
        position: [...currentPosition] as [number, number, number],
        mouse: new THREE.Vector2(
          (event.clientX / gl.domElement.clientWidth) * 2 - 1,
          -(event.clientY / gl.domElement.clientHeight) * 2 + 1
        ),
      });

      (event.target as any).setPointerCapture?.(event.pointerId);
    },
    [
      disableInteractions,
      onClick,
      currentPosition,
      onDragStart,
      id,
      gl,
      setCursor,
    ]
  );

  const handlePointerMove = useCallback(
    (event: ThreeEvent<PointerEvent>) => {
      if (
        disableInteractions ||
        !isDragging ||
        !dragStart ||
        !meshRef.current ||
        !processedModel
      )
        return;

      event.stopPropagation();

      const currentMouse = new THREE.Vector2(
        (event.clientX / gl.domElement.clientWidth) * 2 - 1,
        -(event.clientY / gl.domElement.clientHeight) * 2 + 1
      );

      // Raycast to ground plane
      const raycaster = new THREE.Raycaster();
      raycaster.setFromCamera(currentMouse, camera);
      const groundPlane = new THREE.Plane(new THREE.Vector3(0, 1, 0), 0);
      const intersection = new THREE.Vector3();
      raycaster.ray.intersectPlane(groundPlane, intersection);

      const startRaycaster = new THREE.Raycaster();
      startRaycaster.setFromCamera(dragStart.mouse, camera);
      const startIntersection = new THREE.Vector3();
      startRaycaster.ray.intersectPlane(groundPlane, startIntersection);

      if (intersection && startIntersection) {
        const deltaX = (intersection.x - startIntersection.x) * dragSensitivity;
        const deltaZ = (intersection.z - startIntersection.z) * dragSensitivity;

        const currentPos = new THREE.Vector3(...dragPositionRef.current);
        const desiredPosition = new THREE.Vector3(
          dragStart.position[0] + deltaX,
          dragStart.position[1],
          dragStart.position[2] + deltaZ
        );

        // Find valid position with wall sliding
        const validPosition = findValidPositionUtil(
          desiredPosition,
          currentPos,
          processedModel,
          currentRotation,
          scale,
          shrunkPolygon,
          roomHeight,
          COLLISION_BUFFER
        );

        const newPosition: [number, number, number] = [
          validPosition.x,
          validPosition.y,
          validPosition.z,
        ];

        meshRef.current.position.set(...newPosition);
        dragPositionRef.current = newPosition;
      }
    },
    [
      disableInteractions,
      isDragging,
      dragStart,
      gl,
      camera,
      dragSensitivity,
      processedModel,
      currentRotation,
      scale,
      shrunkPolygon,
      roomHeight,
    ]
  );

  const handlePointerUp = useCallback(
    (event: ThreeEvent<PointerEvent>) => {
      if (disableInteractions) return;
      if (isDragging) {
        event.stopPropagation();

        const finalPosition = dragPositionRef.current;

        setCurrentPosition(finalPosition);

        if (
          onPositionChange &&
          (finalPosition[0] !== currentPosition[0] ||
            finalPosition[1] !== currentPosition[1] ||
            finalPosition[2] !== currentPosition[2])
        ) {
          onPositionChange(finalPosition);
        }

        setIsDragging(false);
        if (onDragEnd) onDragEnd(id);
        setDragStart(null);
        setCursor("grab");

        (event.target as any).releasePointerCapture?.(event.pointerId);
      }
    },
    [
      disableInteractions,
      isDragging,
      currentPosition,
      onPositionChange,
      onDragEnd,
      id,
      setCursor,
    ]
  );

  // Sync position from props
  useEffect(() => {
    setCurrentPosition(position);
    dragPositionRef.current = position;
  }, [position]);

  // Sync rotation from props
  useEffect(() => {
    if (
      rotation[0] !== currentRotation[0] ||
      rotation[1] !== currentRotation[1] ||
      rotation[2] !== currentRotation[2]
    ) {
      setCurrentRotation(rotation);
    }
  }, [rotation, currentRotation]);

  // Model processing is handled by the useModelProcessor hook
  
  if (error) {
    console.error(`Model error for ${url}:`, error);
    return (
      <group position={currentPosition}>
        <mesh>
          <boxGeometry args={[1, 1, 1]} />
          <meshStandardMaterial color="red" />
        </mesh>
        <mesh position={[0, 1.5, 0]}>
          <boxGeometry args={[2, 0.3, 0.1]} />
          <meshBasicMaterial color="white" />
        </mesh>
      </group>
    );
  }

  // Loading state
  if (!gltf?.scene) {
    return (
      <group position={currentPosition}>
        <mesh>
          <boxGeometry args={[0.8, 0.8, 0.8]} />
          <meshStandardMaterial color="#94a3b8" wireframe />
        </mesh>
        <mesh rotation={[0, Date.now() * 0.001, 0]}>
          <boxGeometry args={[1.2, 0.1, 0.1]} />
          <meshBasicMaterial color="#10b981" />
        </mesh>
      </group>
    );
  }

  return (
    <group
      ref={meshRef}
      position={currentPosition}
      rotation={currentRotation}
      scale={scale}
      onClick={handleClick}
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={handlePointerUp}
      onPointerOver={handlePointerOver}
      onPointerOut={handlePointerOut}
    >
      {selected && highlightModel && <primitive object={highlightModel} />}
      {processedModel && (
        <primitive
          object={processedModel}
          castShadow={castShadow}
          receiveShadow={receiveShadow}
        />
      )}
    </group>
  );
}

export default memo(DraggableModel);
