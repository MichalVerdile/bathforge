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

  // Skip product meshes - they shouldn't be detected as walls/floors
  // Products are loaded from GLB models and shouldn't be textured as room surfaces
  let parent = mesh.parent;
  while (parent) {
    const parentName = parent.name?.toLowerCase() || '';
    // If this mesh is part of a product model, skip it
    if (parentName.includes('basin') || parentName.includes('bathtub') ||
        parentName.includes('wc') || parentName.includes('toilet') ||
        parentName.includes('radiator') || parentName.includes('shower') ||
        parentName.includes('furniture') || parentName.includes('product')) {
      return null;
    }
    parent = parent.parent;
  }

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

      // IMPORTANT: Walls and floors should be reasonably large
      // Skip small meshes that are likely product parts (< 50cm on any side)
      const MIN_SURFACE_SIZE = 0.5; // 50cm minimum
      if (size.x < MIN_SURFACE_SIZE && size.z < MIN_SURFACE_SIZE) {
        return null;
      }

      // Skip very large meshes (likely environment/sky)
      if ((size.x > 30 || size.z > 30) && size.y < 0.5) {
        return null;
      }

      // Calculate aspect ratios to determine type
      const isFlat = size.y < Math.min(size.x, size.z) * 0.1; // Y is much smaller than X or Z
      const isHorizontal = size.x > 1 && size.z > 1 && size.x < 30 && size.z < 30;
      const isTall = size.y > 1.8; // Increased from 1.5 to avoid small products
      const isWide = size.x > 1.5 || size.z > 1.5; // Wall should be reasonably wide
      const isVertical = isWide && size.y > Math.max(size.x, size.z) * 0.5;

      // Floor detection: flat horizontal surface
      if (isFlat && isHorizontal) {
        return 'floor';
      }

      // Wall detection: tall vertical surface with minimum size requirements
      if (isTall && isVertical) {
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
        texture.anisotropy = 16; // Maximum anisotropic filtering for sharp textures
        
        // Enhanced material with realistic PBR properties using MeshPhysicalMaterial for clearcoat
        const material = new THREE.MeshPhysicalMaterial({
          map: texture,
          side: THREE.DoubleSide,
          // Realistic tile/wall material properties
          roughness: 0.4,
          metalness: 0.0,
          envMapIntensity: 1.2,
          // Enable clearcoat for glossy tiles
          clearcoat: 0.2,
          clearcoatRoughness: 0.3,
        });

        mesh.material = material;
        mesh.receiveShadow = true;
        mesh.castShadow = false; // Walls/floors don't cast shadows
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
