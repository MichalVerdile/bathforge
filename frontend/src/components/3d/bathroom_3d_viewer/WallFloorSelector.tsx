import { useEffect, useState } from 'react';
import { useThree } from '@react-three/fiber';
import * as THREE from 'three';

interface WallFloorSelectorProps {
  enabled: boolean;
  onSelect: (mesh: THREE.Mesh | null, type: 'wall' | 'floor' | null) => void;
}

// Helper to check if a mesh or any of its ancestors is hidden
function isVisibleInHierarchy(object: THREE.Object3D): boolean {
  let current: THREE.Object3D | null = object;
  while (current) {
    if (!current.visible) return false;
    current = current.parent;
  }
  return true;
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

          // Skip meshes that are hidden or have hidden ancestors
          if (!isVisibleInHierarchy(mesh)) continue;

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
      // Only select if hovered mesh is still visible
      if (hoveredMesh && isVisibleInHierarchy(hoveredMesh)) {
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
  
  const name = mesh.name.toLowerCase();
  
  // First check name-based detection (most reliable)
  if (name.includes('wall')) return 'wall';
  if (name.includes('floor') || name.includes('ground')) return 'floor';
  
  const geometry = mesh.geometry;
  if (geometry) {
    geometry.computeBoundingBox();
    const box = geometry.boundingBox;
    
    if (box) {
      const size = new THREE.Vector3();
      box.getSize(size);
      
      // Skip very large meshes (likely environment/sky)
      if ((size.x > 30 || size.z > 30) && size.y < 0.5) {
        return null;
      }
      
      // Calculate aspect ratios to determine type
      const isFlat = size.y < Math.min(size.x, size.z) * 0.1; // Y is much smaller than X or Z
      const isHorizontal = size.x > 1 && size.z > 1 && size.x < 30 && size.z < 30;
      const isTall = size.y > 1.5;
      const isVertical = (size.x > 1 || size.z > 1) && size.y > Math.max(size.x, size.z) * 0.5;
      
      // Floor detection: flat horizontal surface
      if (isFlat && isHorizontal) {
        console.log(`Detected floor mesh: ${mesh.name}, size:`, size);
        return 'floor';
      }
      
      // Wall detection: tall vertical surface
      if (isTall && isVertical) {
        console.log(`Detected wall mesh: ${mesh.name}, size:`, size);
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
