import React, { useState, useRef, useEffect } from 'react';
import Scene3D from './Scene3D';
import ModelLoader from './ModelLoader';
import ModelBrowser, { type ModelItem } from './ModelBrowser';
import * as THREE from 'three';
import './Bathroom3D.css';

interface SceneControlsState {
  position: [number, number, number];
  rotation: [number, number, number];
  scale: number;
  autoRotate: boolean;
  wireframe: boolean;
  showGrid: boolean;
  showEnvironment: boolean;
}

interface Bathroom3DViewerProps {
  style?: React.CSSProperties;
}

export default function Bathroom3DViewer({ style }: Bathroom3DViewerProps) {
  const [selectedModel, setSelectedModel] = useState<ModelItem | null>(null);
  const [loadedModels, setLoadedModels] = useState<{ model: ModelItem; id: string }[]>([]);
  const [templateData, setTemplateData] = useState<any>(null);
  const [controls, setControls] = useState<SceneControlsState>({
    position: [0, 0, 0],
    rotation: [0, 0, 0],
    scale: 1,
    autoRotate: false,
    wireframe: false,
    showGrid: false,
    showEnvironment: false
  });

  const sceneRef = useRef<THREE.Scene | null>(null);
  const modelRef = useRef<THREE.Group | null>(null);

  // Load template data from localStorage if available
  useEffect(() => {
    const storedTemplate = localStorage.getItem('selectedTemplate');
    if (storedTemplate) {
      try {
        const template = JSON.parse(storedTemplate);
        setTemplateData(template);
        loadTemplateFixtures(template);
      } catch (error) {
        console.error('Error parsing template data:', error);
      }
    }
  }, []);

  const loadTemplateFixtures = (template: any) => {
    // This function would load the fixtures from the template
    // For now, we'll just log the template data
    console.log('Loading template fixtures:', template);
    
    // Auto-load basic bathroom fixtures based on template
    const fixtureModels: ModelItem[] = [];
    
    template.roomData.fixtures.forEach((fixture: any, index: number) => {
      let modelPath = '';
      switch (fixture.type) {
        case 'bathtub':
          modelPath = '/assets/bathtubs/modern-bathtub.glb';
          break;
        case 'sink':
          modelPath = '/assets/basins/modern-basin.glb';
          break;
        case 'toilet':
          modelPath = '/assets/wcs/modern-toilet.glb';
          break;
        case 'shower':
          modelPath = '/assets/shower/modern-shower.glb';
          break;
        default:
          return;
      }
      
      fixtureModels.push({
        id: index + 1,
        name: `${fixture.type.charAt(0).toUpperCase() + fixture.type.slice(1)}`,
        url: modelPath,
        category: fixture.type,
        categoryId: 1,
        priceRange: 'MEDIUM' as const,
        mountingType: 'FLOOR' as const,
        availableColors: [],
        thumbnail: `/assets/${fixture.type}/${fixture.type}-preview.jpg`
      });
    });
    
    // Load the fixtures into the scene
    fixtureModels.forEach((fixtureModel, index) => {
      const loadedModel = {
        model: fixtureModel,
        id: `template_fixture_${index}_${Date.now()}`
      };
      setLoadedModels(prev => [...prev, loadedModel]);
    });
  };

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
    <div className="bathroom-3d-viewer" style={style}>
      {/* Left Sidebar - Model Browser */}
      <ModelBrowser
        onModelSelect={handleModelSelect}
        selectedModel={selectedModel}
      />

      {/* Main 3D Scene */}
      <div className="scene-container">
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
          <div className="model-info-overlay">
            <h4 className="model-info-title">
              {selectedModel.name}
            </h4>
            <p className="model-info-details">
              Category: {selectedModel.category.charAt(0).toUpperCase() + selectedModel.category.slice(1)}
            </p>
            <p className="model-info-category">
              Price Range: {selectedModel.priceRange} • {selectedModel.mountingType}
            </p>
            {selectedModel.availableColors && selectedModel.availableColors.length > 0 && (
              <p className="model-info-colors">
                {selectedModel.availableColors.length} color{selectedModel.availableColors.length > 1 ? 's' : ''} available
              </p>
            )}
            <p className="model-info-format">
              3D Model Format: GLB/GLTF
            </p>
          </div>
        )}

        {/* Welcome Message */}
        {!selectedModel && (
          <div className="welcome-message">
            <h3 className="welcome-title">
              Welcome to BathForge 3D
            </h3>
            <p className="welcome-description">
              Browse and select bathroom fixtures to preview them in stunning 3D
            </p>
            <p className="welcome-subtitle">
              Click on any product card to get started! 🛁✨
            </p>
          </div>
        )}
      </div>
    </div>
  );
}