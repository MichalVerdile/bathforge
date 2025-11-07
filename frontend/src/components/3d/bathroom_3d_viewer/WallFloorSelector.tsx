import { useEffect, useState } from 'react';
import { useThree } from '@react-three/fiber';
import * as THREE from 'three';

interface WallFloorSelectorProps {
  enabled: boolean;
  onSelect: (mesh: THREE.Mesh | null, type: 'wall' | 'floor' | null) => void;
}

/**
 * Component that handles raycasting to detect and select walls/floors in the scene
 * Only active in 2D and Free 3D views
 */
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
      // Update pointer position
      const canvas = event.target as HTMLCanvasElement;
      if (!canvas) return;

      const rect = canvas.getBoundingClientRect();
      pointer.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
      pointer.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

      // Raycast
      raycaster.setFromCamera(pointer, camera);
      const intersects = raycaster.intersectObjects(scene.children, true);

      // Find first wall or floor mesh
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

/**
 * Detects if a mesh is a wall or floor based on its properties
 */
export function detectMeshType(mesh: THREE.Mesh): 'wall' | 'floor' | null {
  // Exclude background/environment elements
  // Check if mesh uses ShadowMaterial (ground plane)
  if (mesh.material && (mesh.material as any).type === 'ShadowMaterial') {
    return null;
  }
  
  // Exclude very large planes that are likely the background ground
  const geometry = mesh.geometry;
  if (geometry) {
    geometry.computeBoundingBox();
    const box = geometry.boundingBox;
    
    if (box) {
      const size = new THREE.Vector3();
      box.getSize(size);
      
      // Exclude very large planes (background ground is 50x50)
      if ((size.x > 30 || size.z > 30) && size.y < 0.5) {
        return null;
      }
    }
  }
  
  // Check mesh name for common patterns
  const name = mesh.name.toLowerCase();
  
  if (name.includes('wall')) return 'wall';
  if (name.includes('floor') || name.includes('ground')) return 'floor';

  // Check geometry orientation
  // Floors are typically horizontal (normal pointing up)
  // Walls are typically vertical
  if (geometry) {
    geometry.computeBoundingBox();
    const box = geometry.boundingBox;
    
    if (box) {
      const size = new THREE.Vector3();
      box.getSize(size);
      
      // If very flat in Y dimension, likely a floor (but not too large)
      if (size.y < 0.2 && size.x > 1 && size.z > 1 && size.x < 30 && size.z < 30) {
        return 'floor';
      }
      
      // If tall in Y dimension, likely a wall
      if (size.y > 1.5 && (size.x > 1 || size.z > 1)) {
        return 'wall';
      }
    }
  }

  return null;
}

/**
 * Applies a texture to a wall or floor mesh
 */
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

        // Create new material with texture
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

/**
 * Removes texture from mesh and restores original material
 */
export function removeTextureFromMesh(mesh: THREE.Mesh, originalMaterial: THREE.Material): void {
  mesh.material = originalMaterial;
}
