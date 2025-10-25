import React, { useRef, useState, useEffect } from 'react';
import { ThreeEvent, useFrame, useThree, useLoader } from '@react-three/fiber';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader';
import { DRACOLoader } from 'three/examples/jsm/loaders/DRACOLoader';
import * as THREE from 'three';

interface DraggableModelProps {
  id: string;
  url: string;
  position?: [number, number, number];
  rotation?: [number, number, number];
  scale?: [number, number, number];
  selected?: boolean;
  castShadow?: boolean;
  receiveShadow?: boolean;
  color?: string;
  /**
   * Drag sensitivity multiplier. 1 = direct mapping; < 1 slows movement; > 1 speeds up.
   * Defaults to 0.5 for smoother control.
   */
  dragSensitivity?: number;
  /** Hex color for subtle selection glow */
  highlightColor?: string;
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
  position = [0, 0, 0],
  rotation = [0, 0, 0],
  scale = [1, 1, 1],
  selected = false,
  castShadow = true,
  receiveShadow = true,
  color,
  dragSensitivity = 1.1,
  // Use a standard 6-digit hex; 8-digit (with alpha) is not supported by THREE.Color
  highlightColor = '#ffffffff',
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
      // Configure DRACO loader to support compressed GLBs
      const dracoLoader = new DRACOLoader();
      // Use a reliable CDN for decoders; alternatively, copy decoders into /public/draco/ and use that path
      dracoLoader.setDecoderPath('https://www.gstatic.com/draco/versioned/decoders/1.5.6/');
      dracoLoader.setDecoderConfig({ type: 'js' }); // wasm also works if preferred
      loader.setDRACOLoader(dracoLoader);
    }
  );
  const { camera, gl } = useThree();
  
  // Local state
  const [currentPosition, setCurrentPosition] = useState<[number, number, number]>(position);
  const [currentRotation, setCurrentRotation] = useState<[number, number, number]>(rotation);
  const [isLoaded, setIsLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState<{
    position: [number, number, number];
    mouse: THREE.Vector2;
    client: { x: number; y: number };
  } | null>(null);
  const [processedModel, setProcessedModel] = useState<THREE.Group | null>(null);
  const [highlightModel, setHighlightModel] = useState<THREE.Group | null>(null);

  // Handle click events
  const handleClick = (event: ThreeEvent<MouseEvent>) => {
    event.stopPropagation();
    if (onClick && !isDragging) {
      onClick();
    }
  };

  // Cursor helpers
  const setCursor = (cursor: string) => {
    try {
      (gl.domElement as HTMLCanvasElement).style.cursor = cursor;
    } catch {}
  };

  const handlePointerOver = (event: ThreeEvent<PointerEvent>) => {
    event.stopPropagation();
    if (!isDragging) setCursor('grab');
  };

  const handlePointerOut = (event: ThreeEvent<PointerEvent>) => {
    event.stopPropagation();
    if (!isDragging) setCursor('default');
  };

  // Handle pointer down (start drag)
  const handlePointerDown = (event: ThreeEvent<PointerEvent>) => {
    event.stopPropagation();
    // Ensure selection happens even if user starts dragging immediately
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

    // Set pointer capture
    (event.target as any).setPointerCapture?.(event.pointerId);
  };

  // Handle pointer move (drag)
  const handlePointerMove = (event: ThreeEvent<PointerEvent>) => {
    if (!isDragging || !dragStart || !meshRef.current) return;
    
    event.stopPropagation();

    const currentMouse = new THREE.Vector2(
      (event.clientX / gl.domElement.clientWidth) * 2 - 1,
      -(event.clientY / gl.domElement.clientHeight) * 2 + 1
    );

    // Modifier keys for movement modes
    const shift = (event as any).shiftKey;
    const alt = (event as any).altKey;

    // If SHIFT is held: vertical movement along world Y
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

    // If ALT is held: move along camera forward/back (depth)
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

    // Default: movement on the ground plane (X/Z), Y stays constant
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

  // Handle pointer up (end drag)
  const handlePointerUp = (event: ThreeEvent<PointerEvent>) => {
    if (isDragging) {
      event.stopPropagation();
      setIsDragging(false);
      if (onDragEnd) onDragEnd(id);
      setDragStart(null);
      setCursor('grab');
      
      // Release pointer capture
      (event.target as any).releasePointerCapture?.(event.pointerId);
    }
  };

  // Update position when prop changes
  useEffect(() => {
    setCurrentPosition(position);
  }, [position]);

  // Update rotation when prop changes
  useEffect(() => {
    setCurrentRotation(rotation);
  }, [rotation]);

  // Process the GLTF model into a normalized, centered, tinted clone we can render
  useEffect(() => {
    if (!gltf?.scene) {
      setError('Failed to load model');
      console.warn(`Model not loaded yet for: ${url}`);
      return;
    }

    try {
      const model = gltf.scene.clone(true);

      // Compute bounding box & apply scale first, then recalc and place on ground
      let box = new THREE.Box3().setFromObject(model);
      let size = box.getSize(new THREE.Vector3());
      let maxDimension = Math.max(size.x, size.y, size.z);

      const TARGET_MAX = 1.5; // scene units
      const scaleFactor = maxDimension > 0 ? Math.min(1, TARGET_MAX / maxDimension) : 1;
      if (scaleFactor !== 1) model.scale.multiplyScalar(scaleFactor);

      // Recalculate bounds after scaling
      model.updateMatrixWorld(true);
      box = new THREE.Box3().setFromObject(model);
      const center = box.getCenter(new THREE.Vector3());

      // Center on X/Z and drop to ground (y=0)
      model.position.x -= center.x;
      model.position.z -= center.z;
      model.position.y -= box.min.y;

      // Configure materials, color tint and shadow properties
      model.traverse((child: THREE.Object3D) => {
        if (child instanceof THREE.Mesh) {
          child.castShadow = castShadow;
          child.receiveShadow = receiveShadow;
          child.frustumCulled = false; // avoid accidental culling from bad bounds

          // Ensure geometry has valid normals and bounds
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
                } catch {}
              }
              // Clamp roughness/metalness to sane values
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
      // Build a subtle highlight clone (very slightly scaled, transparent basic material)
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
        glow.position.set(model.position.x*1.02, model.position.y*1.02, model.position.z*1.02);
        glow.renderOrder = -1;
        setHighlightModel(glow);
      } catch {
        setHighlightModel(null);
      }
      setIsLoaded(true);
      setError(null);

      if (onLoad) onLoad(model);
      console.log(`Successfully processed model: ${url} (scaled: ${scaleFactor.toFixed(2)})`);
    } catch (err) {
      const errorMessage = `Failed to process model: ${err instanceof Error ? err.message : 'Unknown error'}`;
      setError(errorMessage);
      console.error('Model processing error:', err);
      if (onError) onError(err instanceof Error ? err : new Error(errorMessage));
    }
  }, [gltf, castShadow, receiveShadow, onLoad, onError, color, url, highlightColor]);

  // Error display
  if (error) {
    console.error(`Model error for ${url}:`, error);
    return (
      <group position={currentPosition}>
        <mesh>
          <boxGeometry args={[1, 1, 1]} />
          <meshStandardMaterial color="red" />
        </mesh>
        {/* Add text to show the error */}
        <mesh position={[0, 1.5, 0]}>
          <boxGeometry args={[2, 0.3, 0.1]} />
          <meshBasicMaterial color="white" />
        </mesh>
      </group>
    );
  }

  // Loading display
  if (!gltf?.scene) {
    console.log(`Loading model: ${url}`);
    return (
      <group position={currentPosition}>
        <mesh>
          <boxGeometry args={[0.8, 0.8, 0.8]} />
          <meshStandardMaterial color="#94a3b8" wireframe />
        </mesh>
        {/* Add a spinning indicator */}
        <mesh rotation={[0, Date.now() * 0.001, 0]}>
          <boxGeometry args={[1.2, 0.1, 0.1]} />
          <meshBasicMaterial color="#10b981" />
        </mesh>
      </group>
    );
  }

  console.log(`Rendering model: ${url}`, gltf.scene);

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
      {/* Selection outline removed per user request */}
    </group>
  );
}

// Note: Preloading can be added by hosting DRACO decoders locally and using GLTFLoader.preload-equivalent patterns if needed.