import React, { Suspense, useRef } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, Environment, Grid, Html, useProgress } from '@react-three/drei';
import * as THREE from 'three';

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
}

export default function Scene3D({
  children,
  showGrid = true,
  showEnvironment = true,
  cameraPosition = [5, 5, 5],
  backgroundColor = '#0f172a',
  onSceneReady
}: Scene3DProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative' }}>
      <Canvas
        ref={canvasRef}
        shadows
        camera={{
          position: cameraPosition,
          fov: 60,
          near: 0.1,
          far: 1000
        }}
        style={{ background: backgroundColor }}
        onCreated={({ scene }) => {
          scene.castShadow = true;
          scene.receiveShadow = true;
          
          if (onSceneReady) {
            onSceneReady(scene);
          }
        }}
      >
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
            enablePan={true}
            enableZoom={true}
            enableRotate={true}
            minDistance={1}
            maxDistance={50}
            maxPolarAngle={Math.PI / 2.1}
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
        <div>🖱️ Rotate • 🎲 Zoom • 🖐️ Pan</div>
      </div>
    </div>
  );
}