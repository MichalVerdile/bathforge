import React, { Suspense, useRef, useEffect } from 'react';
import { Canvas, useThree, useFrame } from '@react-three/fiber';
import { OrbitControls, Environment, Grid, Html, useProgress } from '@react-three/drei';
import * as THREE from 'three';
import { detectMeshType } from '../bathroom_3d_viewer/WallFloorSelector';

export type ViewType = '2D' | '3D-Person' | '3D-Free';

interface WASDControllerProps {
  enabled: boolean;
  controlsRef: React.RefObject<any>;
}

function WASDController({ enabled, controlsRef }: WASDControllerProps) {
  const { camera, scene } = useThree();
  const keysPressed = useRef<{ [key: string]: boolean }>({});
  const moveSpeed = 0.05;
  const collisionDistance = 0.3; // Distance to check for walls
  const raycaster = useRef(new THREE.Raycaster());

  useEffect(() => {
    if (!enabled) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      const key = e.key.toLowerCase();
      if (['w', 'a', 's', 'd'].includes(key)) {
        keysPressed.current[key] = true;
      }
    };

    const handleKeyUp = (e: KeyboardEvent) => {
      const key = e.key.toLowerCase();
      if (['w', 'a', 's', 'd'].includes(key)) {
        keysPressed.current[key] = false;
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
    };
  }, [enabled]);

  useFrame(() => {
    if (!enabled || !controlsRef.current) return;

    // First, check if camera is too close to any walls in all directions and push back if needed
    const pushBackDirections = [
      new THREE.Vector3(1, 0, 0),   // Right
      new THREE.Vector3(-1, 0, 0),  // Left
      new THREE.Vector3(0, 0, 1),   // Forward
      new THREE.Vector3(0, 0, -1),  // Back
      new THREE.Vector3(1, 0, 1).normalize(),   // Diagonal
      new THREE.Vector3(-1, 0, 1).normalize(),  // Diagonal
      new THREE.Vector3(1, 0, -1).normalize(),  // Diagonal
      new THREE.Vector3(-1, 0, -1).normalize(), // Diagonal
    ];

    for (const dir of pushBackDirections) {
      raycaster.current.set(camera.position, dir);
      raycaster.current.far = collisionDistance * 0.8; // Slightly shorter distance for push-back
      
      const intersects = raycaster.current.intersectObjects(scene.children, true);
      
      for (const intersect of intersects) {
        if (intersect.object instanceof THREE.Mesh) {
          const meshType = detectMeshType(intersect.object);
          if (meshType === 'wall') {
            // Push camera away from wall
            const pushDirection = dir.clone().multiplyScalar(-1);
            const pushAmount = (collisionDistance * 0.8 - intersect.distance) * 0.5;
            const pushVector = pushDirection.multiplyScalar(pushAmount);
            
            camera.position.add(pushVector);
            controlsRef.current.target.add(pushVector);
            controlsRef.current.update();
            break;
          }
        }
      }
    }

    const direction = new THREE.Vector3();
    const sideDirection = new THREE.Vector3();

    // Get camera's forward direction (ignoring Y component for horizontal movement)
    camera.getWorldDirection(direction);
    direction.y = 0;
    direction.normalize();

    // Get camera's right direction
    sideDirection.crossVectors(camera.up, direction).normalize();

    const moveVector = new THREE.Vector3();

    if (keysPressed.current['w']) {
      moveVector.addScaledVector(direction, moveSpeed);
    }
    if (keysPressed.current['s']) {
      moveVector.addScaledVector(direction, -moveSpeed);
    }
    if (keysPressed.current['a']) {
      moveVector.addScaledVector(sideDirection, moveSpeed);
    }
    if (keysPressed.current['d']) {
      moveVector.addScaledVector(sideDirection, -moveSpeed);
    }

    // Check for wall collision before moving
    if (moveVector.length() > 0) {
      // Check collision in the movement direction
      const normalizedMove = moveVector.clone().normalize();
      raycaster.current.set(camera.position, normalizedMove);
      raycaster.current.far = collisionDistance;
      
      const intersects = raycaster.current.intersectObjects(scene.children, true);
      
      let hasWallCollision = false;
      for (const intersect of intersects) {
        if (intersect.object instanceof THREE.Mesh) {
          const meshType = detectMeshType(intersect.object);
          if (meshType === 'wall') {
            hasWallCollision = true;
            break;
          }
        }
      }
      
      // Only move if there's no wall collision
      if (!hasWallCollision) {
        camera.position.add(moveVector);
        controlsRef.current.target.add(moveVector);
        controlsRef.current.update();
      }
    }
  });

  return null;
}

interface CameraControllerProps {
  viewType: ViewType;
  customPosition: [number, number, number];
  controlsRef: React.RefObject<any>;
}

function CameraController({ viewType, customPosition, controlsRef }: CameraControllerProps) {
  const { camera } = useThree();
  const previousViewType = useRef<ViewType | null>(null);
  
  useEffect(() => {
    // Only update camera when view type actually changes
    if (previousViewType.current === viewType) return;
    
    let targetPosition: [number, number, number];
    
    if (viewType === '2D') {
      targetPosition = [0, 6, 0];
      camera.rotation.set(-Math.PI / 2, 0, 0);
      camera.up.set(0, 0, -1);
      camera.position.set(...targetPosition);
    } else if (viewType === '3D-Person') {
      targetPosition = [0, 1.8, 1];
      camera.up.set(0, 1, 0);
      camera.rotation.set(0, 0, 0);
      camera.position.set(...targetPosition);
      if (controlsRef.current) {
        controlsRef.current.target.set(0, 1.8, 0);
        controlsRef.current.update();
      }
    } else {
      // Free view - only set initial position when switching TO this view
      targetPosition = customPosition;
      camera.up.set(0, 1, 0);
      camera.rotation.set(0, 0, 0);
      camera.position.set(...targetPosition);
      if (controlsRef.current) {
        controlsRef.current.target.set(0, 0, 0);
        controlsRef.current.update();
      }
    }
    
    camera.updateProjectionMatrix();
    previousViewType.current = viewType;
  }, [viewType, camera, customPosition, controlsRef]);
  
  return null;
}

function Loader() {
  const { progress } = useProgress();
  return (
    <Html center>
      <div style={{
        color: '#f1f5f9',
        fontSize: '14px',
        fontFamily: 'Segoe UI, Tahoma, Geneva, Verdana, sans-serif',
        textAlign: 'center',
        background: 'rgba(30, 41, 59, 0.95)',
        backdropFilter: 'blur(10px)',
        padding: '16px 24px',
        borderRadius: '12px',
        border: '1px solid #334155',
        boxShadow: '0 4px 20px rgba(0, 0, 0, 0.3)',
        minWidth: '120px'
      }}>
        <div style={{ marginBottom: '8px', fontSize: '16px' }}>⚙️</div>
        <div style={{ fontWeight: '600', marginBottom: '4px' }}>Loading 3D Model</div>
        <div style={{ fontSize: '12px', color: '#94a3b8' }}>{progress.toFixed(0)}% complete</div>
      </div>
    </Html>
  );
}

function Lights() {
  return (
    <>
      <ambientLight intensity={0.4} />
      
      <directionalLight
        position={[10, 10, 5]}
        intensity={1}
        castShadow
        shadow-mapSize-width={2048}
        shadow-mapSize-height={2048}
        shadow-camera-far={50}
        shadow-camera-left={-10}
        shadow-camera-right={10}
        shadow-camera-top={10}
        shadow-camera-bottom={-10}
      />
      
      <directionalLight
        position={[-5, 5, -5]}
        intensity={0.3}
      />
      
      <spotLight
        position={[0, 15, 0]}
        intensity={0.5}
        angle={0.6}
        penumbra={0.5}
        castShadow
      />
    </>
  );
}

function Ground() {
  return (
    <mesh 
      rotation={[-Math.PI / 2, 0, 0]} 
      position={[0, -0.1, 0]} 
      receiveShadow
    >
      <planeGeometry args={[50, 50]} />
      <shadowMaterial opacity={0.3} />
    </mesh>
  );
}

interface Scene3DProps {
  children?: React.ReactNode;
  showGrid?: boolean;
  showEnvironment?: boolean;
  cameraPosition?: [number, number, number];
  backgroundColor?: string;
  onSceneReady?: (scene: THREE.Scene) => void;
  onCameraReady?: (camera: THREE.Camera) => void;
  controlsEnabled?: boolean;
  onBackgroundClick?: () => void;
  viewType?: ViewType;
}

export default function Scene3D({
  children,
  showGrid = true,
  showEnvironment = true,
  cameraPosition = [5, 5, 5],
  backgroundColor = '#0f172a',
  onSceneReady,
  onCameraReady,
  controlsEnabled = true,
  onBackgroundClick,
  viewType = '2D',
}: Scene3DProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const controlsRef = useRef<any>(null);

  // Determine camera position based on view type
  const getCameraPosition = (): [number, number, number] => {
    if (viewType === '2D') {
      return [0, 6, 0]; // Top-down view
    } else if (viewType === '3D-Person') {
      return [0, 1.8, 0]; // Eye-level view, slightly back
    } else {
      return cameraPosition; // Free view with custom position
    }
  };

  // Determine if controls should be enabled based on view type
  const areControlsEnabled = controlsEnabled;
  
  // Determine control settings based on view type
  const getControlSettings = () => {
    if (viewType === '2D') {
      return {
        enablePan: false,
        enableZoom: true,
        enableRotate: false,
        minDistance: 1,
        maxDistance: 50,
        maxPolarAngle: Math.PI,
      };
    } else if (viewType === '3D-Person') {
      return {
        enablePan: false,
        enableZoom: false,
        enableRotate: true,
        minDistance: 1,
        maxDistance: 50,
        maxPolarAngle: Math.PI / 2.1,
      };
    } else {
      // 3D-Free: full freedom
      return {
        enablePan: true,
        enableZoom: true,
        enableRotate: true,
        minDistance: 0.5,
        maxDistance: 100,
        maxPolarAngle: Math.PI, // Allow full rotation
      };
    }
  };

  const controlSettings = getControlSettings();

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative' }}>
      <Canvas
        ref={canvasRef}
        shadows
        camera={{
          position: getCameraPosition(),
          fov: viewType === '2D' ? 50 : 60,
          near: 0.1,
          far: 1000
        }}
        style={{ background: backgroundColor }}
        onCreated={({ scene, camera }) => {
          scene.castShadow = true;
          scene.receiveShadow = true;
          
          if (onSceneReady) {
            onSceneReady(scene);
          }
          
          if (onCameraReady) {
            onCameraReady(camera);
          }
        }}
        onPointerMissed={() => onBackgroundClick?.()}
      >
        <CameraController viewType={viewType} customPosition={cameraPosition} controlsRef={controlsRef} />
        <WASDController enabled={viewType === '3D-Person'} controlsRef={controlsRef} />
        
        <Suspense fallback={<Loader />}>
          <Lights />
          
          {showEnvironment && (
            <Environment preset="apartment" background={false} />
          )}
          
          {showGrid && (
            <Grid infiniteGrid />
          )}
          
          <Ground />
          
          <OrbitControls
            ref={controlsRef}
            enabled={areControlsEnabled}
            enablePan={controlSettings.enablePan}
            enableZoom={controlSettings.enableZoom}
            enableRotate={controlSettings.enableRotate}
            minDistance={controlSettings.minDistance}
            maxDistance={controlSettings.maxDistance}
            maxPolarAngle={controlSettings.maxPolarAngle}
          />
          
          {children}
        </Suspense>
      </Canvas>
      
      <div style={{
        position: 'absolute',
        bottom: '10px',
        left: '10px',
        background: 'rgba(30, 41, 59, 0.95)',
        backdropFilter: 'blur(10px)',
        padding: '8px 12px',
        borderRadius: '8px',
        fontSize: '11px',
        color: '#94a3b8',
        pointerEvents: 'none',
        border: '1px solid #334155',
        fontFamily: 'Segoe UI, Tahoma, Geneva, Verdana, sans-serif'
      }}>
        {viewType === '2D' ? (
          <div>📐 2D Top View - Controls Disabled</div>
        ) : viewType === '3D-Person' ? (
          <div>👤 Person View • 🖱️ Click & drag to look around • WASD to move</div>
        ) : (
          <div>🌐 Free View • 🖱️ Rotate • 🎲 Zoom • 🖐️ Pan (Full Freedom)</div>
        )}
      </div>
    </div>
  );
}