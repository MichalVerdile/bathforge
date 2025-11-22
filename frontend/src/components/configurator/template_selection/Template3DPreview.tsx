import React, { Suspense } from "react";
import { Canvas } from "@react-three/fiber";
import { OrbitControls, useGLTF, Environment } from "@react-three/drei";

interface Template {
  id: number;
  name: string;
  preview: string;
  roomData: {
    width: number;
    height: number;
    depth: number;
    fixtures: Array<{
      type: "bathtub" | "sink" | "toilet" | "shower" | "window" | "door";
      position: { x: number; y: number; z: number };
      rotation: { x: number; y: number; z: number };
      scale: { x: number; y: number; z: number };
    }>;
  };
}

interface Template3DPreviewProps {
  template: Template;
}

const RoomModel: React.FC<{ modelPath: string }> = ({ modelPath }) => {
  const { scene } = useGLTF(modelPath);
  return <primitive object={scene} scale={[1, 1, 1]} position={[0, -1, 0]} />;
};

const Template3DPreview: React.FC<Template3DPreviewProps> = ({ template }) => {
  const [isLoading, setIsLoading] = React.useState(true);

  React.useEffect(() => {
    const timer = setTimeout(() => {
      setIsLoading(false);
    }, 2000);

    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="template-3d-preview">
      <div className="model-preview">
        <Canvas
          camera={{ position: [4, 4, 4], fov: 60 }}
          style={{ width: "100%", height: "100%" }}
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

            <RoomModel modelPath={template.preview} />

            <OrbitControls
              enablePan={true}
              enableZoom={true}
              enableRotate={true}
              minDistance={2}
              maxDistance={15}
              maxPolarAngle={Math.PI / 2}
              autoRotate={false}
              autoRotateSpeed={0.5}
            />
          </Suspense>
        </Canvas>

        {isLoading && (
          <div className="loading-overlay">
            <div className="loading-spinner"></div>
            <span>Loading 3D Model...</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default Template3DPreview;
