import React, { useState, useRef } from 'react';
import Scene3D from './Scene3D';
import ModelLoader from './ModelLoader';
import ModelBrowser, { ModelItem } from './ModelBrowser';
import SceneControls, { SceneControlsState } from './SceneControls';
import * as THREE from 'three';

interface Bathroom3DViewerProps {
  style?: React.CSSProperties;
}

export default function Bathroom3DViewer({ style }: Bathroom3DViewerProps) {
  const [selectedModel, setSelectedModel] = useState<ModelItem | null>(null);
  const [loadedModels, setLoadedModels] = useState<{ model: ModelItem; id: string }[]>([]);
  const [controls, setControls] = useState<SceneControlsState>({
    position: [0, 0, 0],
    rotation: [0, 0, 0],
    scale: 1,
    autoRotate: false,
    wireframe: false,
    showGrid: true,
    showEnvironment: true
  });

  const sceneRef = useRef<THREE.Scene | null>(null);
  const modelRef = useRef<THREE.Group | null>(null);

  const handleModelSelect = (model: ModelItem) => {
    setSelectedModel(model);
    
    const isAlreadyLoaded = loadedModels.some(loaded => loaded.model.url === model.url);
    if (!isAlreadyLoaded) {
      const newLoadedModel = {
        model,
        id: `model_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
      };
      setLoadedModels(prev => [...prev, newLoadedModel]);
    }

    setControls(prev => ({
      ...prev,
      position: [0, 0, 0],
      rotation: [0, 0, 0],
      scale: 1
    }));
  };

  const handleResetView = () => {
    setControls(prev => ({
      ...prev,
      position: [0, 0, 0],
      rotation: [0, 0, 0],
      scale: 1,
      autoRotate: false
    }));
  };

  const handleCenterModel = () => {
    if (selectedModel) {
      setControls(prev => ({
        ...prev,
        position: [0, 0, 0]
      }));
    }
  };

  const handleModelLoad = (model: THREE.Group) => {
    const box = new THREE.Box3().setFromObject(model);
    const size = box.getSize(new THREE.Vector3());
    const maxDimension = Math.max(size.x, size.y, size.z);
    
    const targetSize = 2.5;
    const scale = targetSize / maxDimension;
    
    setControls(prev => ({
      ...prev,
      scale: scale,
      position: [0, 0, 0]
    }));
  };

  return (
    <div style={{
      width: '100%',
      height: '100vh',
      display: 'flex',
      background: '#f5f5f5',
      ...style
    }}>
      {/* Left Sidebar - Model Browser */}
      <ModelBrowser
        onModelSelect={handleModelSelect}
        selectedModel={selectedModel}
        style={{ flexShrink: 0 }}
      />

      {/* Main 3D Scene */}
      <div style={{ 
        flex: 1, 
        position: 'relative',
        margin: '10px',
        borderRadius: '8px',
        overflow: 'hidden'
      }}>
        <Scene3D
          showGrid={controls.showGrid}
          showEnvironment={controls.showEnvironment}
          onSceneReady={(scene) => {
            sceneRef.current = scene;
          }}
        >
          {selectedModel && (
            <ModelLoader
              url={selectedModel.url}
              position={controls.position}
              rotation={controls.rotation}
              scale={controls.scale}
              autoRotate={controls.autoRotate}
              onLoad={handleModelLoad}
              onError={(error) => {
                console.error('Failed to load model:', error);
                alert(`Failed to load model: ${selectedModel.name}\nError: ${error.message}`);
              }}
            />
          )}
        </Scene3D>

        {/* Model Info Overlay */}
        {selectedModel && (
          <div style={{
            position: 'absolute',
            top: '50px',
            left: '20px',
            background: 'rgba(255, 255, 255, 0.95)',
            padding: '12px 16px',
            borderRadius: '6px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
            maxWidth: '300px'
          }}>
            <h4 style={{ margin: '0 0 8px 0', fontSize: '16px', color: '#212529' }}>
              {selectedModel.name}
            </h4>
            <p style={{ margin: '0', fontSize: '14px', color: '#6c757d' }}>
              Category: {selectedModel.category.charAt(0).toUpperCase() + selectedModel.category.slice(1)}
            </p>
            <p style={{ margin: '4px 0 0 0', fontSize: '12px', color: '#adb5bd' }}>
              Format: GLB/GLTF 3D Model
            </p>
          </div>
        )}

        {/* No Model Selected Message */}
        {!selectedModel && (
          <div style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            textAlign: 'center',
            color: '#6c757d',
            background: 'rgba(255, 255, 255, 0.9)',
            padding: '30px',
            borderRadius: '8px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
          }}>
            <h3 style={{ margin: '0 0 12px 0', fontSize: '20px' }}>
              Welcome to BathForge 3D Viewer
            </h3>
            <p style={{ margin: '0', fontSize: '16px' }}>
              Select a 3D model from the browser on the left to get started
            </p>
            <p style={{ margin: '8px 0 0 0', fontSize: '14px', opacity: 0.8 }}>
              Browse basins, bathtubs, accessories and more!
            </p>
          </div>
        )}
      </div>

      {/* Right Sidebar - Scene Controls */}
      <SceneControls
        controls={controls}
        onControlsChange={setControls}
        onResetView={handleResetView}
        onCenterModel={handleCenterModel}
        style={{ flexShrink: 0, margin: '10px 10px 10px 0' }}
      />
    </div>
  );
}