import { useEffect, useState } from 'react';
import { useThree } from '@react-three/fiber';
import * as THREE from 'three';

interface WallFloorSelectorProps {
  enabled: boolean;
  onSelect: (mesh: THREE.Mesh | null, type: 'wall' | 'floor' | null) => void;
}

export function WallFloorSelector({ enabled, onSelect }: WallFloorSelectorProps) {
  const { scene, camera, raycaster, pointer } = useThree();
  const [hoveredMesh, setHoveredMesh] = useState<THREE.Mesh | null>(null);

  useEffect(() => {
    if (!enabled) {
      setHoveredMesh(null);
      document.body.style.cursor = 'default';
      return;
    }

    const handlePointerMove = (event: PointerEvent) => {
      const canvas = event.target as HTMLCanvasElement;
      if (!canvas) return;

      const rect = canvas.getBoundingClientRect();
      pointer.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
      pointer.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

      raycaster.setFromCamera(pointer, camera);
      const intersects = raycaster.intersectObjects(scene.children, true);

      for (const intersect of intersects) {
        if (intersect.object instanceof THREE.Mesh) {
          const mesh = intersect.object;
          const meshType = detectMeshType(mesh);
          
          if (meshType) {
            setHoveredMesh(mesh);
            document.body.style.cursor = 'pointer';
            return;
          }
        }
      }

      setHoveredMesh(null);
      document.body.style.cursor = 'default';
    };

    const handleClick = (event: MouseEvent) => {
      if (hoveredMesh) {
        const meshType = detectMeshType(hoveredMesh);
        onSelect(hoveredMesh, meshType);
      } else {
        onSelect(null, null);
      }
    };

    window.addEventListener('pointermove', handlePointerMove);
    window.addEventListener('click', handleClick);

    return () => {
      window.removeEventListener('pointermove', handlePointerMove);
      window.removeEventListener('click', handleClick);
      document.body.style.cursor = 'default';
    };
  }, [enabled, scene, camera, raycaster, pointer, hoveredMesh, onSelect]);

  return null;
}

export function detectMeshType(mesh: THREE.Mesh): 'wall' | 'floor' | null {
  if (mesh.material && (mesh.material as any).type === 'ShadowMaterial') {
    return null;
  }
  
  const geometry = mesh.geometry;
  if (geometry) {
    geometry.computeBoundingBox();
    const box = geometry.boundingBox;
    
    if (box) {
      const size = new THREE.Vector3();
      box.getSize(size);
      
      if ((size.x > 30 || size.z > 30) && size.y < 0.5) {
        return null;
      }
    }
  }
  
  const name = mesh.name.toLowerCase();
  
  if (name.includes('wall')) return 'wall';
  if (name.includes('floor') || name.includes('ground')) return 'floor';

  if (geometry) {
    geometry.computeBoundingBox();
    const box = geometry.boundingBox;
    
    if (box) {
      const size = new THREE.Vector3();
      box.getSize(size);
      
      if (size.y < 0.2 && size.x > 1 && size.z > 1 && size.x < 30 && size.z < 30) {
        return 'floor';
      }
      
      if (size.y > 1.5 && (size.x > 1 || size.z > 1)) {
        return 'wall';
      }
    }
  }

  return null;
}

export async function applyTextureToMesh(
  mesh: THREE.Mesh,
  texturePath: string,
  repeatX: number = 2,
  repeatY: number = 2
): Promise<void> {
  const textureLoader = new THREE.TextureLoader();
  
  return new Promise((resolve, reject) => {
    textureLoader.load(
      texturePath,
      (texture) => {
        texture.wrapS = THREE.RepeatWrapping;
        texture.wrapT = THREE.RepeatWrapping;
        texture.repeat.set(repeatX, repeatY);
        texture.colorSpace = THREE.SRGBColorSpace;

        const material = new THREE.MeshStandardMaterial({
          map: texture,
          side: THREE.DoubleSide,
        });

        mesh.material = material;
        resolve();
      },
      undefined,
      (error) => {
        console.error('Failed to load texture:', error);
        reject(error);
      }
    );
  });
}

export function removeTextureFromMesh(mesh: THREE.Mesh, originalMaterial: THREE.Material): void {
  mesh.material = originalMaterial;
}
