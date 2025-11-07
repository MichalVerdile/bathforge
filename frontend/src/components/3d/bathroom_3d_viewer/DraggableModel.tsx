import React, { useRef, useState, useEffect } from 'react';
import { ThreeEvent, useFrame, useThree, useLoader } from '@react-three/fiber';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader';
import { DRACOLoader } from 'three/examples/jsm/loaders/DRACOLoader';
import * as THREE from 'three';
import { 
  SCALING_CONFIG, 
  detectUnitScale, 
  getCategoryFromUrl, 
  normalizeCategoryName 
} from './modelScalingConfig';

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
  onClick?: () => void;
  onPositionChange?: (position: [number, number, number]) => void;
  onRotationChange?: (rotation: [number, number, number]) => void;
  onLoad?: (model: THREE.Group) => void;
  onError?: (error: Error) => void;
  onDragStart?: (id: string) => void;
  onDragEnd?: (id: string) => void;
}

export default function DraggableModel({
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
  highlightColor = 'white',
  disableInteractions = false,
  onClick,
  onPositionChange,
  onRotationChange,
  onLoad,
  onError,
  onDragStart,
  onDragEnd
}: DraggableModelProps) {
  const meshRef = useRef<THREE.Group>(null);
  const gltf = useLoader(
    GLTFLoader,
    url,
    (loader: GLTFLoader) => {
      const dracoLoader = new DRACOLoader();
      dracoLoader.setDecoderPath('https://www.gstatic.com/draco/versioned/decoders/1.5.6/');
      dracoLoader.setDecoderConfig({ type: 'js' });
      loader.setDRACOLoader(dracoLoader);
    }
  );
  const { camera, gl } = useThree();

  const [currentPosition, setCurrentPosition] = useState<[number, number, number]>(position);
  const [currentRotation, setCurrentRotation] = useState<[number, number, number]>(rotation);
  const [error, setError] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState<{
    position: [number, number, number];
    mouse: THREE.Vector2;
    client: { x: number; y: number };
  } | null>(null);
  const [processedModel, setProcessedModel] = useState<THREE.Group | null>(null);
  const [highlightModel, setHighlightModel] = useState<THREE.Group | null>(null);

  const handleClick = (event: ThreeEvent<MouseEvent>) => {
    if (disableInteractions) return;
    event.stopPropagation();
    if (onClick && !isDragging) {
      onClick();
    }
  };

  const setCursor = (cursor: string) => {
    try {
      (gl.domElement as HTMLCanvasElement).style.cursor = cursor;
    } catch { }
  };

  const handlePointerOver = (event: ThreeEvent<PointerEvent>) => {
    if (disableInteractions) return;
    event.stopPropagation();
    if (!isDragging) setCursor('grab');
  };

  const handlePointerOut = (event: ThreeEvent<PointerEvent>) => {
    if (disableInteractions) return;
    event.stopPropagation();
    if (!isDragging) setCursor('default');
  };

  const handlePointerDown = (event: ThreeEvent<PointerEvent>) => {
    if (disableInteractions) return;
    event.stopPropagation();
    if (onClick) onClick();

    setIsDragging(true);
    if (onDragStart) onDragStart(id);
    setCursor('grabbing');
    setDragStart({
      position: [...currentPosition] as [number, number, number],
      mouse: new THREE.Vector2(
        (event.clientX / gl.domElement.clientWidth) * 2 - 1,
        -(event.clientY / gl.domElement.clientHeight) * 2 + 1
      ),
      client: { x: event.clientX, y: event.clientY }
    });

    (event.target as any).setPointerCapture?.(event.pointerId);
  };

  const handlePointerMove = (event: ThreeEvent<PointerEvent>) => {
    if (disableInteractions || !isDragging || !dragStart || !meshRef.current) return;

    event.stopPropagation();

    const currentMouse = new THREE.Vector2(
      (event.clientX / gl.domElement.clientWidth) * 2 - 1,
      -(event.clientY / gl.domElement.clientHeight) * 2 + 1
    );

    const shift = (event as any).shiftKey;
    const alt = (event as any).altKey;

    if (shift) {
      const pixelDeltaY = event.clientY - dragStart.client.y;
      const distance = camera.position.distanceTo(meshRef.current.position);
      const worldDeltaY = -pixelDeltaY * (distance * 0.002) * dragSensitivity;

      const newPosition: [number, number, number] = [
        dragStart.position[0],
        Math.max(0, dragStart.position[1] + worldDeltaY),
        dragStart.position[2]
      ];

      setCurrentPosition(newPosition);
      if (onPositionChange) onPositionChange(newPosition);
      return;
    }

    if (alt) {
      const pixelDeltaY = event.clientY - dragStart.client.y;
      const distance = camera.position.distanceTo(meshRef.current.position);
      const worldDelta = -pixelDeltaY * (distance * 0.002) * dragSensitivity;
      const dir = new THREE.Vector3();
      camera.getWorldDirection(dir);
      const move = dir.multiplyScalar(worldDelta);

      const newPosition: [number, number, number] = [
        dragStart.position[0] + move.x,
        Math.max(0, dragStart.position[1] + move.y),
        dragStart.position[2] + move.z
      ];

      setCurrentPosition(newPosition);
      if (onPositionChange) onPositionChange(newPosition);
      return;
    }

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

      const newPosition: [number, number, number] = [
        dragStart.position[0] + deltaX,
        dragStart.position[1],
        dragStart.position[2] + deltaZ
      ];
      setCurrentPosition(newPosition);
      if (onPositionChange) onPositionChange(newPosition);
    }
  };

  const handlePointerUp = (event: ThreeEvent<PointerEvent>) => {
    if (disableInteractions) return;
    if (isDragging) {
      event.stopPropagation();
      setIsDragging(false);
      if (onDragEnd) onDragEnd(id);
      setDragStart(null);
      setCursor('grab');

      (event.target as any).releasePointerCapture?.(event.pointerId);
    }
  };

  useEffect(() => {
    setCurrentPosition(position);
  }, [position]);

  useEffect(() => {
    setCurrentRotation(rotation);
  }, [rotation]);

  useEffect(() => {
    if (!gltf?.scene) {
      setError('Failed to load model');
      console.warn(`Model not loaded yet for: ${url}`);
      return;
    }

    try {
      const model = gltf.scene.clone(true);

      // Step 1: Get raw bounding box to detect unit system
      let box = new THREE.Box3().setFromObject(model);
      let size = box.getSize(new THREE.Vector3());
      let maxDimension = Math.max(size.x, size.y, size.z);

      // Step 2: Detect and apply unit conversion (mm/cm/inches -> meters)
      const unitScale = detectUnitScale(maxDimension);
      model.scale.multiplyScalar(unitScale);
      
      // Step 3: Get category configuration
      const categoryName = category 
        ? normalizeCategoryName(category)
        : getCategoryFromUrl(url);
      
      const categoryConfig = categoryName 
        ? SCALING_CONFIG.categories[categoryName]
        : null;

      if (categoryConfig) {
        // Step 4: Recalculate bounding box after unit conversion
        model.updateMatrixWorld(true);
        box = new THREE.Box3().setFromObject(model);
        size = box.getSize(new THREE.Vector3());

        // Step 5: Apply category normalization
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

        // Step 6: Check room dimensions and cap if needed (never upscale)
        model.updateMatrixWorld(true);
        box = new THREE.Box3().setFromObject(model);
        size = box.getSize(new THREE.Vector3());

        const roomExceedX = size.x / SCALING_CONFIG.room.x;
        const roomExceedY = size.y / SCALING_CONFIG.room.y;
        const roomExceedZ = size.z / SCALING_CONFIG.room.z;
        const maxExceed = Math.max(roomExceedX, roomExceedY, roomExceedZ);

        if (maxExceed > 1) {
          // Model exceeds room, shrink to fit
          const roomCapScale = 1 / maxExceed;
          model.scale.multiplyScalar(roomCapScale);
        }
        
      } else {
        // Fallback: simple scaling if no category config found
        model.updateMatrixWorld(true);
        box = new THREE.Box3().setFromObject(model);
        size = box.getSize(new THREE.Vector3());
        maxDimension = Math.max(size.x, size.y, size.z);

        const TARGET_MAX = 1.5;
        const scaleFactor = maxDimension > 0 ? Math.min(1, TARGET_MAX / maxDimension) : 1;
        if (scaleFactor !== 1) model.scale.multiplyScalar(scaleFactor);
      }

      // Final update and centering
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
                } catch { }
              }
              
              if (typeof mat.roughness === 'number') mat.roughness = Math.min(1, Math.max(0, mat.roughness ?? 0.6));
              if (typeof mat.metalness === 'number') mat.metalness = Math.min(1, Math.max(0, mat.metalness ?? 0.1));
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
      {selected && highlightModel && (
        <primitive object={highlightModel} />
      )}
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
