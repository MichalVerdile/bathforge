import React, { Suspense } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, Environment } from '@react-three/drei';
import { Room } from '../custom_room/Room';
import { RoomOpenings } from '../custom_room/DoorWindowTypes';

interface Template3DPreviewProps {
  template: {
    id: number;
    name: string;
    preview: string;
    roomData?: {
      height: number;
      vertices: Array<{ x: number; y: number }>;
      openings?: RoomOpenings;
    };
  };
}

const Template3DPreview: React.FC<Template3DPreviewProps> = ({ template }) => {
  const [isLoading, setIsLoading] = React.useState(true);

  React.useEffect(() => {
    const timer = setTimeout(() => {
      setIsLoading(false);
    }, 500);

    return () => clearTimeout(timer);
  }, []);

  // Calculate camera position based on room size
  const roomData = template.roomData;
  const cameraDistance = roomData
    ? Math.max(
        ...roomData.vertices.map((v) => Math.max(Math.abs(v.x), Math.abs(v.y)))
      ) * 0.015 + 3
    : 8;

  return (
    <div className="template-3d-preview">
      <div className="model-preview">
        <Canvas
          camera={{ position: [cameraDistance, cameraDistance, cameraDistance], fov: 60 }}
          style={{ width: '100%', height: '100%' }}
          shadows
        >
          <Suspense fallback={null}>
            <ambientLight intensity={0.6} />
            <directionalLight
              position={[10, 10, 5]}
              intensity={0.8}
              castShadow
              shadow-mapSize-width={2048}
              shadow-mapSize-height={2048}
            />
            <pointLight position={[-10, -10, -10]} intensity={0.4} />

            <Environment preset="apartment" />

            {/* Render room with doors and windows */}
            {roomData && (
              <Room
                vertices={roomData.vertices}
                height={roomData.height / 100} // Convert cm to meters
                viewMode="3D"
                openings={roomData.openings}
                isInteractive={false}
              />
            )}

            <OrbitControls
              enablePan={true}
              enableZoom={true}
              enableRotate={true}
              minDistance={2}
              maxDistance={25}
              maxPolarAngle={Math.PI / 2}
              autoRotate={true}
              autoRotateSpeed={0.5}
            />
          </Suspense>
        </Canvas>

        {isLoading && (
          <div className="loading-overlay">
            <div className="loading-spinner"></div>
            <span>Loading 3D Preview...</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default Template3DPreview;