import React, { Suspense, useRef } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, Environment, Grid, Html, useProgress } from '@react-three/drei';
import * as THREE from 'three';

function Loader() {
  const { progress } = useProgress();
  return (
    <Html center>
      <div style={{
        color: 'white',
        fontSize: '14px',
        fontFamily: 'Arial, sans-serif',
        textAlign: 'center',
        background: 'rgba(0,0,0,0.8)',
        padding: '10px 20px',
        borderRadius: '5px'
      }}>
        Loading: {progress.toFixed(0)}%
      </div>
    </Html>
  );
}

function Lights() {
  return (
    <>
      {/* Ambient light for overall illumination */}
      <ambientLight intensity={0.4} />
      
      {/* Main directional light */}
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
      
      {/* Fill light from the opposite side */}
      <directionalLight
        position={[-5, 5, -5]}
        intensity={0.3}
      />
      
      {/* Spot light for highlights */}
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
  backgroundColor = '#f0f0f0',
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
          {/* Lighting */}
          <Lights />
          
          {/* Environment for reflections and ambient lighting */}
          {showEnvironment && (
            <Environment preset="apartment" background={false} />
          )}
          
          {/* Grid helper */}
          {showGrid && (
            <Grid infiniteGrid />
          )}
          
          {/* Ground plane for shadows */}
          <Ground />
          
          {/* Camera controls */}
          <OrbitControls
            enablePan={true}
            enableZoom={true}
            enableRotate={true}
            minDistance={1}
            maxDistance={50}
            maxPolarAngle={Math.PI / 2.1}
          />
          
          {/* Children */}
          {children}
        </Suspense>
      </Canvas>
      
      {/* UI Controls overlay */}
      <div style={{
        position: 'absolute',
        top: '10px',
        left: '10px',
        background: 'rgba(255, 255, 255, 0.9)',
        padding: '10px',
        borderRadius: '5px',
        fontSize: '12px',
        color: '#333',
        pointerEvents: 'none'
      }}>
        <div>Mouse: Rotate | Wheel: Zoom | Right-click: Pan</div>
      </div>
    </div>
  );
}