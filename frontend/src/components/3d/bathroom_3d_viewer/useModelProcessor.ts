import { useState, useEffect } from 'react';
import * as THREE from 'three';
import { GLTF } from 'three/examples/jsm/loaders/GLTFLoader';
import {
  SCALING_CONFIG,
  detectUnitScale,
  getCategoryFromUrl,
  normalizeCategoryName,
} from './modelScalingConfig';

interface UseModelProcessorProps {
  gltf: GLTF | null;
  url: string;
  category?: string;
  color?: string;
  castShadow: boolean;
  receiveShadow: boolean;
  highlightColor: string;
  onLoad?: (model: THREE.Group) => void;
  onError?: (error: Error) => void;
}

interface UseModelProcessorResult {
  processedModel: THREE.Group | null;
  highlightModel: THREE.Group | null;
  error: string | null;
}

/**
 * Hook to process and scale 3D models
 */
export function useModelProcessor({
  gltf,
  url,
  category,
  color,
  castShadow,
  receiveShadow,
  highlightColor,
  onLoad,
  onError,
}: UseModelProcessorProps): UseModelProcessorResult {
  const [processedModel, setProcessedModel] = useState<THREE.Group | null>(null);
  const [highlightModel, setHighlightModel] = useState<THREE.Group | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!gltf?.scene) {
      setError('Failed to load model');
      console.warn(`Model not loaded yet for: ${url}`);
      return;
    }

    try {
      const model = processModel(
        gltf.scene,
        url,
        category,
        color,
        castShadow,
        receiveShadow
      );

      setProcessedModel(model);

      const glow = createHighlightModel(model, highlightColor);
      setHighlightModel(glow);

      setError(null);
      if (onLoad) onLoad(model);
    } catch (err) {
      const errorMessage = `Failed to process model: ${
        err instanceof Error ? err.message : 'Unknown error'
      }`;
      setError(errorMessage);
      console.error('Model processing error:', err);
      if (onError) onError(err instanceof Error ? err : new Error(errorMessage));
    }
  }, [gltf, url, category, color, castShadow, receiveShadow, highlightColor, onLoad, onError]);

  return { processedModel, highlightModel, error };
}

/**
 * Process and scale a model
 */
function processModel(
  scene: THREE.Group,
  url: string,
  category: string | undefined,
  color: string | undefined,
  castShadow: boolean,
  receiveShadow: boolean
): THREE.Group {
  const model = scene.clone(true);

  // Apply scaling
  applyScaling(model, url, category);

  // Center the model
  centerModel(model);

  // Apply materials
  applyMaterials(model, color, castShadow, receiveShadow);

  return model;
}

/**
 * Apply scaling to model based on category
 */
function applyScaling(model: THREE.Group, url: string, category?: string) {
  let box = new THREE.Box3().setFromObject(model);
  let size = box.getSize(new THREE.Vector3());
  let maxDimension = Math.max(size.x, size.y, size.z);

  // Apply unit scale detection
  const unitScale = detectUnitScale(maxDimension);
  model.scale.multiplyScalar(unitScale);

  // Get category configuration
  const categoryName = category
    ? normalizeCategoryName(category)
    : getCategoryFromUrl(url);
  const categoryConfig = categoryName
    ? SCALING_CONFIG.categories[categoryName]
    : null;

  if (categoryConfig) {
    // Apply category-specific scaling
    model.updateMatrixWorld(true);
    box = new THREE.Box3().setFromObject(model);
    size = box.getSize(new THREE.Vector3());

    let currentDimension: number;
    if (categoryConfig.axis === 'max') {
      currentDimension = Math.max(size.x, size.y, size.z);
    } else {
      currentDimension = size[categoryConfig.axis];
    }

    if (currentDimension > 0) {
      const categoryScale = categoryConfig.targetMeters;
      model.scale.multiplyScalar(categoryScale);
    }

    // Apply room size capping
    model.updateMatrixWorld(true);
    box = new THREE.Box3().setFromObject(model);
    size = box.getSize(new THREE.Vector3());

    const roomExceedX = size.x / SCALING_CONFIG.room.x;
    const roomExceedY = size.y / SCALING_CONFIG.room.y;
    const roomExceedZ = size.z / SCALING_CONFIG.room.z;
    const maxExceed = Math.max(roomExceedX, roomExceedY, roomExceedZ);

    if (maxExceed > 1) {
      const roomCapScale = 1 / maxExceed;
      model.scale.multiplyScalar(roomCapScale);
    }
  } else {
    // Fallback scaling
    model.updateMatrixWorld(true);
    box = new THREE.Box3().setFromObject(model);
    size = box.getSize(new THREE.Vector3());
    maxDimension = Math.max(size.x, size.y, size.z);

    const TARGET_MAX = 1.5;
    const scaleFactor = maxDimension > 0 ? Math.min(1, TARGET_MAX / maxDimension) : 1;
    if (scaleFactor !== 1) model.scale.multiplyScalar(scaleFactor);
  }
}

/**
 * Center model on ground
 */
function centerModel(model: THREE.Group) {
  model.updateMatrixWorld(true);
  const box = new THREE.Box3().setFromObject(model);
  const center = box.getCenter(new THREE.Vector3());

  model.position.x -= center.x;
  model.position.z -= center.z;
  model.position.y -= box.min.y;
}

/**
 * Apply materials to model with photorealistic properties
 */
function applyMaterials(
  model: THREE.Group,
  color: string | undefined,
  castShadow: boolean,
  receiveShadow: boolean
) {
  model.traverse((child: THREE.Object3D) => {
    if (child instanceof THREE.Mesh) {
      child.castShadow = castShadow;
      child.receiveShadow = receiveShadow;
      child.frustumCulled = false;

      if (child.geometry) {
        child.geometry.computeVertexNormals();
        child.geometry.computeBoundingSphere();
        child.geometry.computeTangents();
      }

      const applyColor = (mat: THREE.Material) => {
        if (mat instanceof THREE.MeshStandardMaterial) {
          mat.needsUpdate = true;
          mat.wireframe = false;
          mat.transparent = false;
          mat.opacity = 1;
          mat.depthWrite = true;
          mat.side = THREE.FrontSide;
          
          // Enhanced PBR properties for photorealism with better visibility
          mat.envMapIntensity = 2.0; // Increased for better reflections on dark materials
          
          if (color) {
            try {
              mat.color.set(color);
            } catch {}
          }

          // Realistic material property ranges
          // Bathroom fixtures are typically ceramic/porcelain or metal
          const matName = mat.name?.toLowerCase() || '';
          
          // Check material color brightness to adjust properties for dark materials
          const colorBrightness = mat.color ? (mat.color.r + mat.color.g + mat.color.b) / 3 : 0.5;
          const isDarkMaterial = colorBrightness < 0.3;
          
          // Check if material is metallic (faucets, handles, etc.)
          if (matName.includes('chrome') || matName.includes('metal') || 
              matName.includes('steel') || matName.includes('brass')) {
            mat.metalness = 0.95;
            mat.roughness = isDarkMaterial ? 0.1 : 0.15; // Smoother for dark metals
            mat.envMapIntensity = 2.5;
          }
          // Check if material is glass or mirror
          else if (matName.includes('glass') || matName.includes('mirror')) {
            mat.metalness = 0.0;
            mat.roughness = 0.05;
            mat.envMapIntensity = 2.2;
            mat.transparent = true;
            mat.opacity = 0.3;
          }
          // Check if material is ceramic/porcelain (sinks, toilets, bathtubs)
          else if (matName.includes('ceramic') || matName.includes('porcelain') || 
                   matName.includes('white') || matName.includes('enamel')) {
            mat.metalness = 0.0;
            mat.roughness = isDarkMaterial ? 0.2 : 0.25;
            mat.envMapIntensity = 1.8;
          }
          // Check if material is wood (furniture, cabinets)
          else if (matName.includes('wood') || matName.includes('oak') || 
                   matName.includes('walnut')) {
            mat.metalness = 0.0;
            mat.roughness = isDarkMaterial ? 0.6 : 0.7;
            mat.envMapIntensity = isDarkMaterial ? 1.2 : 0.8;
          }
          // Check if material is tile
          else if (matName.includes('tile') || matName.includes('marble') || 
                   matName.includes('granite')) {
            mat.metalness = 0.0;
            mat.roughness = isDarkMaterial ? 0.25 : 0.3;
            mat.envMapIntensity = 1.8;
          }
          // Default material properties with better handling for dark materials
          else {
            // For dark materials, reduce roughness to show more reflections\n            
            if (isDarkMaterial) {
              mat.roughness = 0.3;
              mat.metalness = 0.1;
              mat.envMapIntensity = 2.5;
            } else {
              // Use existing values if reasonable, otherwise set defaults
              if (typeof mat.roughness === 'number' && mat.roughness > 0) {
                mat.roughness = Math.min(1, Math.max(0.2, mat.roughness));
              } else {
                mat.roughness = 0.4;
              }
              
              if (typeof mat.metalness === 'number') {
                mat.metalness = Math.min(0.3, Math.max(0, mat.metalness));
              } else {
                mat.metalness = 0.0;
              }
              
              mat.envMapIntensity = 1.5;
            }
          }
        } else if (mat instanceof THREE.MeshPhysicalMaterial) {
          // For physical materials, enhance even further
          mat.needsUpdate = true;
          mat.clearcoat = 0.5;
          mat.clearcoatRoughness = 0.1;
          mat.reflectivity = 0.9;
          mat.envMapIntensity = 2.5;
        }
      };

      if (Array.isArray(child.material)) {
        child.material.forEach(applyColor);
      } else if (child.material) {
        applyColor(child.material);
      }
    }
  });
}

/**
 * Create highlight/glow model for selection
 */
function createHighlightModel(
  model: THREE.Group,
  highlightColor: string
): THREE.Group | null {
  try {
    const glow = model.clone(true);
    glow.traverse((child: THREE.Object3D) => {
      if (child instanceof THREE.Mesh) {
        child.castShadow = false;
        child.receiveShadow = false;
        const mat = new THREE.MeshBasicMaterial({
          color: new THREE.Color(highlightColor),
          transparent: true,
          opacity: 0.8,
          depthWrite: false,
          side: THREE.BackSide,
        });
        child.material = mat;
      }
    });

    glow.scale.multiplyScalar(1.02);
    glow.position.set(
      model.position.x * 1.02,
      model.position.y * 1.02,
      model.position.z * 1.02
    );
    glow.renderOrder = -1;
    return glow;
  } catch {
    return null;
  }
}
