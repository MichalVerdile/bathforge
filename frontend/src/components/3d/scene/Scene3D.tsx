import React, { Suspense, useRef, useEffect } from 'react';
import { Canvas, useThree, useFrame } from '@react-three/fiber';
import { OrbitControls, Environment, Grid, Html, useProgress, ContactShadows } from '@react-three/drei';
import { FaSpinner } from 'react-icons/fa';
import * as THREE from 'three';
import { detectMeshType } from '../bathroom_3d_viewer/WallFloorSelector';
import SceneWatermark from './SceneWatermark';

export type ViewType = '2D' | '3D-Person' | '3D-Free';

interface WASDControllerProps {
  enabled: boolean;
  controlsRef: React.RefObject<any>;
}

function WASDController({ enabled, controlsRef }: WASDControllerProps) {
  const { camera, scene } = useThree();
  const keysPressed = useRef<{ [key: string]: boolean }>({});
  const moveSpeed = 0.05;
  const collisionDistance = 0.3;
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

    const pushBackDirections = [
      new THREE.Vector3(1, 0, 0),
      new THREE.Vector3(-1, 0, 0),
      new THREE.Vector3(0, 0, 1),
      new THREE.Vector3(0, 0, -1),
      new THREE.Vector3(1, 0, 1).normalize(),
      new THREE.Vector3(-1, 0, 1).normalize(),
      new THREE.Vector3(1, 0, -1).normalize(),
      new THREE.Vector3(-1, 0, -1).normalize(),
    ];

    for (const dir of pushBackDirections) {
      raycaster.current.set(camera.position, dir);
      raycaster.current.far = collisionDistance * 0.8;

      const intersects = raycaster.current.intersectObjects(scene.children, true);

      for (const intersect of intersects) {
        if (intersect.object instanceof THREE.Mesh) {
          const meshType = detectMeshType(intersect.object);
          if (meshType === 'wall') {
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

    camera.getWorldDirection(direction);
    direction.y = 0;
    direction.normalize();

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

    if (moveVector.length() > 0) {
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
    if (previousViewType.current === viewType) return;

    let targetPosition: [number, number, number];

    if (viewType === '2D') {
      targetPosition = [0, 6, 0];
      camera.rotation.set(-Math.PI / 2, 0, 0);
      camera.up.set(0, 0, -1);
      camera.position.set(...targetPosition);
      if (controlsRef.current) {
        controlsRef.current.target.set(0, 0, 0);
        controlsRef.current.update();
      }
    } else if (viewType === '3D-Person') {
      targetPosition = [0, 1.5, 1];
      camera.up.set(0, 1, 0);
      camera.rotation.set(0, 0, 0);
      camera.position.set(...targetPosition);
      if (controlsRef.current) {
        controlsRef.current.target.set(0, 1.5, 0);
        controlsRef.current.update();
      }
    } else {
      targetPosition = customPosition;
      camera.up.set(0, 1, 0);
      camera.rotation.set(0, 0, 0);
      camera.position.set(...targetPosition);
      if (controlsRef.current) {
        controlsRef.current.target.set(0, 1.25, 0);
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
        <div style={{ marginBottom: '8px', fontSize: '16px', display: 'flex', justifyContent: 'center' }}>
          <FaSpinner size={16} style={{ animation: 'spin 1s linear infinite' }} />
        </div>
        <div style={{ fontWeight: '600', marginBottom: '4px' }}>Loading 3D Model</div>
        <div style={{ fontSize: '12px', color: '#94a3b8' }}>{progress.toFixed(0)}% complete</div>
      </div>
    </Html>
  );
}

function Lights() {
  return (
    <>
      {/* Increased ambient light for better visibility of dark materials */}
      <ambientLight intensity={0.8} color="#ffffff" />

      {/* Main directional light simulating sun/window light with enhanced shadows */}
      <directionalLight
        position={[10, 15, 5]}
        intensity={1.8}
        castShadow
        shadow-mapSize-width={4096}
        shadow-mapSize-height={4096}
        shadow-camera-far={50}
        shadow-camera-left={-15}
        shadow-camera-right={15}
        shadow-camera-top={15}
        shadow-camera-bottom={-15}
        shadow-bias={-0.0001}
        color="#ffffff"
      />

      {/* Secondary directional light for fill - essential for dark materials */}
      <directionalLight
        position={[-10, 10, -5]}
        intensity={1.2}
        color="#f0f4ff"
      />

      {/* Ceiling light simulation - warm white, higher intensity */}
      <spotLight
        position={[0, 4, 0]}
        intensity={3.5}
        angle={Math.PI / 2.5}
        penumbra={0.8}
        distance={12}
        decay={2}
        castShadow
        shadow-mapSize-width={2048}
        shadow-mapSize-height={2048}
        shadow-bias={-0.0001}
        color="#fff8f0"
      />

      {/* Additional ceiling lights for even coverage */}
      <spotLight
        position={[2, 3.5, 2]}
        intensity={2.5}
        angle={Math.PI / 3}
        penumbra={0.9}
        distance={10}
        decay={2}
        color="#ffffff"
      />
      
      <spotLight
        position={[-2, 3.5, -2]}
        intensity={2.5}
        angle={Math.PI / 3}
        penumbra={0.9}
        distance={10}
        decay={2}
        color="#ffffff"
      />

      {/* Fill light from window - cooler tone, higher intensity */}
      <rectAreaLight
        position={[4, 2.5, 0]}
        width={3}
        height={4}
        intensity={4.5}
        color="#e6f2ff"
      />

      {/* Bounce light for realism - simulates light reflecting off surfaces */}
      <pointLight
        position={[0, 0.5, 0]}
        intensity={1.5}
        distance={10}
        decay={2}
        color="#ffe5d0"
      />
      
      {/* Multiple accent lights for revealing details in dark materials */}
      <pointLight
        position={[-3, 2, 3]}
        intensity={2.5}
        distance={8}
        decay={2}
        color="#ffffff"
      />
      
      <pointLight
        position={[3, 2, -3]}
        intensity={2.5}
        distance={8}
        decay={2}
        color="#ffffff"
      />
      
      <pointLight
        position={[-3, 2, -3]}
        intensity={2.0}
        distance={8}
        decay={2}
        color="#f5f5ff"
      />
      
      <pointLight
        position={[3, 2, 3]}
        intensity={2.0}
        distance={8}
        decay={2}
        color="#fff5f5"
      />

      {/* Ground-level fill lights to illuminate from below */}
      <pointLight
        position={[0, 0.2, 2]}
        intensity={1.2}
        distance={6}
        decay={2}
        color="#f0f0f0"
      />
      
      <pointLight
        position={[0, 0.2, -2]}
        intensity={1.2}
        distance={6}
        decay={2}
        color="#f0f0f0"
      />

      {/* Hemisphere light for natural sky/ground color variation - increased */}
      <hemisphereLight
        color="#ffffff"
        groundColor="#a0a0a0"
        intensity={0.8}
      />
    </>
  );
}

function Ground() {
  return (
    <>
      {/* Contact shadows for realistic object grounding */}
      <ContactShadows
        position={[0, -0.01, 0]}
        opacity={0.35}
        scale={50}
        blur={2.0}
        far={10}
        resolution={1024}
        color="#000000"
      />
      
      {/* Shadow receiving plane */}
      <mesh
        rotation={[-Math.PI / 2, 0, 0]}
        position={[0, -0.02, 0]}
        receiveShadow
      >
        <planeGeometry args={[100, 100]} />
        <shadowMaterial opacity={0.25} transparent />
      </mesh>
    </>
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
  onRendererReady?: (renderer: THREE.WebGLRenderer) => void;
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
  onRendererReady,
  controlsEnabled = true,
  onBackgroundClick,
  viewType = '2D',
}: Scene3DProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const controlsRef = useRef<any>(null);

  const getCameraPosition = (): [number, number, number] => {
    if (viewType === '2D') {
      return [0, 6, 0];
    } else if (viewType === '3D-Person') {
      return [0, 1.5, 0];
    } else {
      return cameraPosition;
    }
  };

  const areControlsEnabled = controlsEnabled;

  const getControlSettings = () => {
    if (viewType === '2D') {
      return {
        enablePan: false,
        enableZoom: true,
        enableRotate: false,
        enableDamping: false,
        minDistance: 1,
        maxDistance: 50,
        maxPolarAngle: Math.PI,
        minPolarAngle: 0,
      };
    } else if (viewType === '3D-Person') {
      return {
        enablePan: false,
        enableZoom: false,
        enableRotate: true,
        enableDamping: false,
        minDistance: 1,
        maxDistance: 50,
        maxPolarAngle: Math.PI / 2.1,
        minPolarAngle: 0,
      };
    } else {
      return {
        enablePan: true,
        enableZoom: true,
        enableRotate: true,
        enableDamping: true,
        dampingFactor: 0.05,
        minDistance: 0.5,
        maxDistance: 100,
        maxPolarAngle: Math.PI / 1.8,
        minPolarAngle: 0,
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
        onCreated={({ scene, camera, gl }) => {
          // Enable shadows
          scene.castShadow = true;
          scene.receiveShadow = true;

          // Configure renderer for photorealism
          gl.toneMapping = THREE.ACESFilmicToneMapping;
          gl.toneMappingExposure = 1.2;
          gl.shadowMap.enabled = true;
          gl.shadowMap.type = THREE.PCFSoftShadowMap;
          gl.shadowMap.autoUpdate = true;
          
          // Output color space for accurate colors (Three.js r152+)
          gl.outputColorSpace = THREE.SRGBColorSpace;
          
          // Pixel ratio for crisp rendering
          gl.setPixelRatio(Math.min(window.devicePixelRatio, 2));

          if (onSceneReady) {
            onSceneReady(scene);
          }

          if (onCameraReady) {
            onCameraReady(camera);
          }

          if (onRendererReady) {
            onRendererReady(gl);
          }
        }}
        onPointerMissed={() => onBackgroundClick?.()}
      >
        <CameraController viewType={viewType} customPosition={cameraPosition} controlsRef={controlsRef} />
        <WASDController enabled={viewType === '3D-Person'} controlsRef={controlsRef} />

        <Suspense fallback={<Loader />}>
          <Lights />

          {showEnvironment && (
            <Environment
              preset="apartment"
              background={false}
              environmentIntensity={1.5}
            />
          )}

          {showGrid && (
            <Grid infiniteGrid fadeDistance={30} fadeStrength={5} />
          )}

          <Ground />
          
          {/* Subtle watermark throughout the scene */}
          <SceneWatermark />

          <OrbitControls
            ref={controlsRef}
            enabled={areControlsEnabled}
            enablePan={controlSettings.enablePan}
            enableZoom={controlSettings.enableZoom}
            enableRotate={controlSettings.enableRotate}
            enableDamping={controlSettings.enableDamping}
            dampingFactor={controlSettings.dampingFactor}
            minDistance={controlSettings.minDistance}
            maxDistance={controlSettings.maxDistance}
            maxPolarAngle={controlSettings.maxPolarAngle}
            minPolarAngle={controlSettings.minPolarAngle}
          />

          {children}
        </Suspense>
      </Canvas>
    </div>
  );
}