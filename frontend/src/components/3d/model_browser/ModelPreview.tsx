import React, { useRef, useEffect, Suspense } from 'react';
import { Canvas } from '@react-three/fiber';
import { useGLTF, OrbitControls } from '@react-three/drei';
import * as THREE from 'three';

interface ModelPreviewProps {
  url: string;
  fallbackIcon?: string;
  className?: string;
}

interface PreviewModelProps {
  url: string;
}

function PreviewModel({ url }: PreviewModelProps) {
  const meshRef = useRef<THREE.Group>(null);
  const gltf = useGLTF(url);

  useEffect(() => {
    if (!gltf || !gltf.scene || !meshRef.current) return;

    const model = gltf.scene.clone();
    
    meshRef.current.clear();
    
    model.traverse((child: THREE.Object3D) => {
      if (child instanceof THREE.Mesh) {
        child.castShadow = false;
        child.receiveShadow = false;
        
        if (child.material) {
          if (Array.isArray(child.material)) {
            child.material.forEach(mat => {
              if (mat instanceof THREE.MeshStandardMaterial) {
                mat.metalness = 0.1;
                mat.roughness = 0.8;
              }
            });
          } else if (child.material instanceof THREE.MeshStandardMaterial) {
            child.material.metalness = 0.1;
            child.material.roughness = 0.8;
          }
        }
      }
    });

    const box = new THREE.Box3().setFromObject(model);
    const center = box.getCenter(new THREE.Vector3());
    const size = box.getSize(new THREE.Vector3());
    
    model.position.sub(center);
    
    const maxDim = Math.max(size.x, size.y, size.z);
    const scale = maxDim > 0 ? 2 / maxDim : 1;
    model.scale.setScalar(scale);
    
    meshRef.current.add(model);
  }, [gltf]);

  return (
    <group ref={meshRef} />
  );
}

function LoadingSpinner() {
  return (
    <div 
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: '100%',
        height: '100%',
        background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)',
        color: '#94a3b8',
        fontSize: '12px'
      }}
    >
      <div style={{ animation: 'spin 1s linear infinite' }}>⟳</div>
    </div>
  );
}

function ErrorFallback({ icon }: { icon?: string }) {
  return (
    <div 
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: '100%',
        height: '100%',
        background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)',
        color: '#94a3b8',
        fontSize: '24px'
      }}
    >
      {icon || '📦'}
    </div>
  );
}

export default function ModelPreview({ url, fallbackIcon, className = '' }: ModelPreviewProps) {
  if (!url) {
    console.log('No URL provided, showing fallback');
    return <ErrorFallback icon={fallbackIcon} />;
  }

  return (
    <div className={`model-preview ${className}`}>
      <Canvas
        camera={{ position: [3, 3, 3], fov: 50 }}
        style={{ width: '100%', height: '100%' }}
        gl={{ 
          antialias: true,
          alpha: true,
          powerPreference: "low-power"
        }}
        onCreated={(state) => {
          console.log('Canvas created for URL:', url);
        }}
      >
        <ambientLight intensity={0.6} />
        <directionalLight 
          position={[5, 5, 5]} 
          intensity={0.8}
          castShadow={false}
        />
        <pointLight position={[-5, 5, 5]} intensity={0.4} />
        
        <Suspense fallback={null}>
          <PreviewModel url={url} />
        </Suspense>
        
        <OrbitControls 
          enableZoom={false}
          enablePan={false}
          autoRotate={true}
          autoRotateSpeed={2}
          dampingFactor={0.1}
          rotateSpeed={0.5}
        />
      </Canvas>
      
      <Suspense fallback={<LoadingSpinner />}>
        <div />
      </Suspense>
    </div>
  );
}

const style = document.createElement('style');
style.textContent = `
  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
`;
if (!document.head.querySelector('style[data-model-preview]')) {
  style.setAttribute('data-model-preview', 'true');
  document.head.appendChild(style);
}