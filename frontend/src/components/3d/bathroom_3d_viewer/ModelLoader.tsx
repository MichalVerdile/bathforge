import React, { useRef, useState, useEffect } from 'react';
import { useGLTF } from '@react-three/drei';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';
import { detectUnitScale } from './modelScalingConfig';

interface ModelLoaderProps {
  url: string;
  position?: [number, number, number];
  rotation?: [number, number, number];
  scale?: [number, number, number] | number;
  castShadow?: boolean;
  receiveShadow?: boolean;
  autoRotate?: boolean;
  applyUnitDetection?: boolean; // New prop to enable 1:1 unit detection for templates
  onLoad?: (model: THREE.Group) => void;
  onError?: (error: Error) => void;
}

export default function ModelLoader({
  url,
  position = [0, 0, 0],
  rotation = [0, 0, 0],
  scale = 1,
  castShadow = true,
  receiveShadow = true,
  autoRotate = false,
  applyUnitDetection = false,
  onLoad,
  onError
}: ModelLoaderProps) {
  const meshRef = useRef<THREE.Group>(null);
  const [error, setError] = useState<string | null>(null);
  const [setIsLoaded] = useState(false);
  const [appliedScale, setAppliedScale] = useState<[number, number, number] | number>(scale);
  
  const gltf = useGLTF(url);

  useFrame((state, delta) => {
    if (meshRef.current && autoRotate) {
      meshRef.current.rotation.y += delta * 0.5;
    }
  });

  useEffect(() => {
    if (!gltf || !gltf.scene) {
      return;
    }

    try {
      const model = gltf.scene.clone();
      
      // Apply unit detection if enabled (for room templates)
      if (applyUnitDetection) {
        const box = new THREE.Box3().setFromObject(model);
        const size = box.getSize(new THREE.Vector3());
        const maxDimension = Math.max(size.x, size.y, size.z);
        const unitScale = detectUnitScale(maxDimension);
        
        console.log(`[Template ${url}] Raw max dimension: ${maxDimension}, detected unit scale: ${unitScale}`);
        
        // Apply the unit scale to the scale prop
        if (Array.isArray(scale)) {
          setAppliedScale([scale[0] * unitScale, scale[1] * unitScale, scale[2] * unitScale]);
        } else {
          setAppliedScale(unitScale * scale);
        }
      }
      
      model.traverse((child: THREE.Object3D) => {
        if (child instanceof THREE.Mesh) {
          child.castShadow = castShadow;
          child.receiveShadow = receiveShadow;
          
          if (child.material) {
            if (Array.isArray(child.material)) {
              child.material.forEach((mat) => {
                if (mat instanceof THREE.MeshStandardMaterial) {
                  mat.needsUpdate = true;
                }
              });
            } else if (child.material instanceof THREE.MeshStandardMaterial) {
              child.material.needsUpdate = true;
            }
          }
        }
      });

      setError(null);
      
      if (onLoad) {
        onLoad(model);
      }
    } catch (err) {
      const errorMessage = `Failed to process model: ${err instanceof Error ? err.message : 'Unknown error'}`;
      setError(errorMessage);
      console.error('Model processing error:', err);
      if (onError) {
        onError(err instanceof Error ? err : new Error(errorMessage));
      }
    }
  }, [gltf, castShadow, receiveShadow, onLoad, onError, applyUnitDetection, scale, url]);

  if (error) {
    return (
      <mesh position={position}>
        <boxGeometry args={[1, 1, 1]} />
        <meshStandardMaterial color="red" transparent opacity={0.5} />
      </mesh>
    );
  }

  if (!gltf || !gltf.scene) {
    return (
      <mesh position={position}>
        <boxGeometry args={[0.5, 0.5, 0.5]} />
        <meshStandardMaterial color="gray" wireframe />
      </mesh>
    );
  }

  return (
    <group ref={meshRef} position={position} rotation={rotation} scale={appliedScale}>
      <primitive object={gltf.scene} />
    </group>
  );
}

export function useFitModel(modelRef: React.RefObject<THREE.Group>) {
  const [bounds, setBounds] = useState<THREE.Box3 | null>(null);
  const [center, setCenter] = useState<THREE.Vector3 | null>(null);
  const [size, setSize] = useState<THREE.Vector3 | null>(null);

  useEffect(() => {
    if (modelRef.current) {
      const box = new THREE.Box3().setFromObject(modelRef.current);
      const center = box.getCenter(new THREE.Vector3());
      const size = box.getSize(new THREE.Vector3());
      
      setBounds(box);
      setCenter(center);
      setSize(size);
      
      modelRef.current.position.copy(center.multiplyScalar(-1));
    }
  }, [modelRef]);

  return { bounds, center, size };
}

export function preloadModel(url: string) {
  useGLTF.preload(url);
}