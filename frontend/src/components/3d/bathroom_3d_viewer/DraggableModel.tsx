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

  useEffect(() => {
    if (!gltf?.scene) {
      setError('Failed to load model');
      console.warn(`Model not loaded yet for: ${url}`);
      return;
    }

    try {
      const model = gltf.scene.clone(true);

      let box = new THREE.Box3().setFromObject(model);
      let size = box.getSize(new THREE.Vector3());
      let maxDimension = Math.max(size.x, size.y, size.z);

      const unitScale = detectUnitScale(maxDimension);
      model.scale.multiplyScalar(unitScale);

      const categoryName = category
        ? normalizeCategoryName(category)
        : getCategoryFromUrl(url);

      const categoryConfig = categoryName
        ? SCALING_CONFIG.categories[categoryName]
        : null;

      if (categoryConfig) {
        model.updateMatrixWorld(true);
        box = new THREE.Box3().setFromObject(model);
        size = box.getSize(new THREE.Vector3());

        let currentDimension: number;
        if (categoryConfig.axis === 'max') {
          currentDimension = Math.max(size.x, size.y, size.z);
        } else {
          currentDimension = size[categoryConfig.axis];
        }

        if (currentDimension > 0) {
          const categoryScale = categoryConfig.targetMeters;
          model.scale.multiplyScalar(categoryScale);
        }

        model.updateMatrixWorld(true);
        box = new THREE.Box3().setFromObject(model);
        size = box.getSize(new THREE.Vector3());

        const roomExceedX = size.x / SCALING_CONFIG.room.x;
        const roomExceedY = size.y / SCALING_CONFIG.room.y;
        const roomExceedZ = size.z / SCALING_CONFIG.room.z;
        const maxExceed = Math.max(roomExceedX, roomExceedY, roomExceedZ);

        if (maxExceed > 1) {
          const roomCapScale = 1 / maxExceed;
          model.scale.multiplyScalar(roomCapScale);
        }

      } else {
        model.updateMatrixWorld(true);
        box = new THREE.Box3().setFromObject(model);
        size = box.getSize(new THREE.Vector3());
        maxDimension = Math.max(size.x, size.y, size.z);

        const TARGET_MAX = 1.5;
        const scaleFactor = maxDimension > 0 ? Math.min(1, TARGET_MAX / maxDimension) : 1;
        if (scaleFactor !== 1) model.scale.multiplyScalar(scaleFactor);
      }

      model.updateMatrixWorld(true);
      box = new THREE.Box3().setFromObject(model);
      const center = box.getCenter(new THREE.Vector3());

      model.position.x -= center.x;
      model.position.z -= center.z;
      model.position.y -= box.min.y;

      model.traverse((child: THREE.Object3D) => {
        if (child instanceof THREE.Mesh) {
          child.castShadow = castShadow;
          child.receiveShadow = receiveShadow;
          child.frustumCulled = false;

          if (child.geometry) {
            child.geometry.computeVertexNormals();
            child.geometry.computeBoundingSphere();
          }

          const applyColor = (mat: THREE.Material) => {
            if (mat instanceof THREE.MeshStandardMaterial) {
              mat.needsUpdate = true;
              mat.wireframe = false;
              mat.transparent = false;
              mat.opacity = 1;
              mat.depthWrite = true;
              mat.side = THREE.DoubleSide;
              
              if (color) {
                try {
                  mat.color.set(color);
                  
                  // Adjust material properties for better detail visibility
                  // For very dark colors (like black), use slightly higher roughness
                  const colorObj = new THREE.Color(color);
                  const luminance = 0.299 * colorObj.r + 0.587 * colorObj.g + 0.114 * colorObj.b;
                  
                  if (luminance < 0.1) {
                    // Very dark color (black)
                    mat.roughness = 0.4;  // Less shiny to show more detail
                    mat.metalness = 0.05; // Slight metalness for highlights
                  } else if (luminance > 0.9) {
                    // Very light color (white)
                    mat.roughness = 0.5;  // Medium roughness to show depth
                    mat.metalness = 0.0;  // No metalness for white
                  } else {
                    // Regular colors
                    mat.roughness = mat.roughness !== undefined ? Math.min(1, Math.max(0, mat.roughness)) : 0.5;
                    mat.metalness = mat.metalness !== undefined ? Math.min(1, Math.max(0, mat.metalness)) : 0.1;
                  }
                } catch { }
              } else {
                // No color override - use material defaults with slight adjustments
                if (typeof mat.roughness === 'number') mat.roughness = Math.min(1, Math.max(0, mat.roughness ?? 0.5));
                if (typeof mat.metalness === 'number') mat.metalness = Math.min(1, Math.max(0, mat.metalness ?? 0.1));
              }
            }
          };

          if (Array.isArray(child.material)) {
            child.material.forEach(applyColor);
          } else if (child.material) {
            applyColor(child.material);
          }
        }
      });

      setProcessedModel(model);
      try {
        const glow = model.clone(true);
        glow.traverse((child: THREE.Object3D) => {
          if (child instanceof THREE.Mesh) {
            child.castShadow = false;
            child.receiveShadow = false;
            const mat = new THREE.MeshBasicMaterial({
              color: new THREE.Color(highlightColor),
              transparent: true,
              opacity: 0.8,
              depthWrite: false,
              side: THREE.BackSide
            });
            child.material = mat;
          }
        });

        glow.scale.multiplyScalar(1.02);
        glow.position.set(model.position.x * 1.02, model.position.y * 1.02, model.position.z * 1.02);
        glow.renderOrder = -1;
        setHighlightModel(glow);
      } catch {
        setHighlightModel(null);
      }
      setError(null);

      if (onLoad) onLoad(model);
    } catch (err) {
      const errorMessage = `Failed to process model: ${err instanceof Error ? err.message : 'Unknown error'}`;
      setError(errorMessage);
      console.error('Model processing error:', err);
      if (onError) onError(err instanceof Error ? err : new Error(errorMessage));
    }
  }, [gltf, castShadow, receiveShadow, onLoad, onError, color, url, highlightColor, category]);

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
