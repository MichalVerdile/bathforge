import React from 'react';
import { Text } from '@react-three/drei';
import * as THREE from 'three';

/**
 * SceneWatermark Component
 * 
 * Renders a subtle, repeated watermark throughout the 3D scene
 * displaying "© New Living Design" at strategic positions
 * without disrupting the user experience.
 */
function SceneWatermark() {
  // Watermark text
  const watermarkText = "© New Living Design";
  
  // Configuration - adjust these values
  const config = {
    fontSize: 0.14,
    color: "#e8e8e8",
    outlineWidth: 0.002,
    outlineColor: "#000000ff",
    opacity: 0.3,
  };
  
  // Dynamically generate symmetric watermark positions
  const generateWatermarkPositions = () => {
    const positions: Array<{
      position: [number, number, number];
      rotation: [number, number, number];
    }> = [];
    
    const floorY = 0.1;
    const floorRotation: [number, number, number] = [-Math.PI / 2, 0, 0];
    
    // Center watermark
    positions.push({ position: [0, floorY, 0], rotation: floorRotation });
    
    // Generate concentric circles of watermarks
    const rings = [
      { radius: 3, count: 8 },
      { radius: 5, count: 12 },
      { radius: 7, count: 16 },
      { radius: 9, count: 20 },
    ];
    
    rings.forEach(ring => {
      for (let i = 0; i < ring.count; i++) {
        const angle = (i / ring.count) * Math.PI * 2;
        const x = Math.cos(angle) * ring.radius;
        const z = Math.sin(angle) * ring.radius;
        
        positions.push({
          position: [x, floorY, z],
          rotation: floorRotation
        });
      }
    });
    
    return positions;
  };

  const watermarkPositions = generateWatermarkPositions();

  return (
    <>
      {watermarkPositions.map((position, index) => (
        <Text
          key={`watermark-${index}`}
          position={position.position}
          rotation={position.rotation}
          fontSize={config.fontSize}
          color={config.color}
          anchorX="center"
          anchorY="middle"
          outlineWidth={config.outlineWidth}
          outlineColor={config.outlineColor}
        >
          {watermarkText}
          <meshBasicMaterial
            transparent
            opacity={config.opacity}
            depthWrite={false}
            side={THREE.DoubleSide}
          />
        </Text>
      ))}
    </>
  );
}

export default SceneWatermark;
