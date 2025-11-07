import React, { useState, useRef, useEffect } from 'react';
import Scene3D from '../scene/Scene3D';
import ModelLoader from './ModelLoader';
import ModelBrowser, { type ModelItem } from '../model_browser/ModelBrowser';
import sceneService, { SceneProduct } from '../../../controllers/api/scenes/SceneService';
import { Color } from '../../../types/api';
import { ProductService } from '../../../controllers/api/products/ProductService';
import * as THREE from 'three';
import './Bathroom3DViewer.css';
import DraggableModel from './DraggableModel';

interface SceneProduct3D extends SceneProduct {
  uniqueId: string;
  modelItem: ModelItem;
  selectedColorId?: number;
}

interface SceneControlsState {
  position: [number, number, number];
  rotation: [number, number, number];
  scale: number;
  autoRotate: boolean;
  wireframe: boolean;
  showGrid: boolean;
  showEnvironment: boolean;
}

export type ViewType = '2D' | '3D-Person' | '3D-Free';

interface Bathroom3DViewerProps {
  style?: React.CSSProperties;
}

export default function Bathroom3DViewer({ style }: Bathroom3DViewerProps) {
  const [selectedModel, setSelectedModel] = useState<ModelItem | null>(null);
  const [sceneProducts, setSceneProducts] = useState<SceneProduct3D[]>([]);
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null);
  const [isDraggingModel, setIsDraggingModel] = useState(false);
  const [viewType, setViewType] = useState<ViewType>('3D-Person');
  const [currentScene, setCurrentScene] = useState<{ id?: number; name: string }>({
    name: `Scene ${new Date().toLocaleString()}`
  });
  const [templateData, setTemplateData] = useState<any>(null);
  const [isAutoSaving, setIsAutoSaving] = useState(false);
  const [lastSaveTime, setLastSaveTime] = useState<Date | null>(null);
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
  const cameraRef = useRef<THREE.Camera | null>(null);
  const saveTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    const storedTemplate = localStorage.getItem('selectedTemplate');
    if (storedTemplate) {
      try {
        const template = JSON.parse(storedTemplate);
        setTemplateData(template);
      } catch (error) {
        console.error('Error parsing template data:', error);
      }
    }
  }, []);

  const autoSaveScene = async () => {
    if (sceneProducts.length === 0) return;
    
    setIsAutoSaving(true);
    try {
      const sceneData = sceneProducts.map(product => ({
        productId: product.productId,
        colorId: product.selectedColorId,
        positionX: product.positionX || 0,
        positionY: product.positionY || 0,
        positionZ: product.positionZ || 0,
        rotationX: product.rotationX || 0,
        rotationY: product.rotationY || 0,
        rotationZ: product.rotationZ || 0
      }));

      const cameraPosition = cameraRef.current ? JSON.stringify({
        x: cameraRef.current.position.x,
        y: cameraRef.current.position.y,
        z: cameraRef.current.position.z
      }) : undefined;

      await sceneService.saveCurrentScene(
        currentScene.name,
        'guest',
        sceneData,
        cameraPosition,
        undefined,
        '#0f172a',
        currentScene.id
      );

      setLastSaveTime(new Date());
    } catch (error) {
      console.error('Failed to auto-save scene:', error);
    } finally {
      setIsAutoSaving(false);
    }
  };

  const scheduleAutoSave = (delay: number = 1000) => {
    if (saveTimerRef.current) {
      clearTimeout(saveTimerRef.current);
    }
    saveTimerRef.current = setTimeout(() => {
      autoSaveScene();
      saveTimerRef.current = null;
    }, delay);
  };

  const addProductToScene = (model: ModelItem, position?: [number, number, number]) => {
    const uniqueId = `product_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const newProduct: SceneProduct3D = {
      uniqueId,
      productId: model.id,
      modelItem: model,
      positionX: position ? position[0] : 0,
      positionY: position ? position[1] : 0.3,
      positionZ: position ? position[2] : 0,
      rotationX: 0,
      rotationY: 0,
      rotationZ: 0,
      selectedColorId: model.availableColors && model.availableColors.length > 0 ? model.availableColors[0].id : undefined
    };

    setSceneProducts(prev => [...prev, newProduct]);
    setSelectedProductId(uniqueId);
    scheduleAutoSave(400);
  };

  const removeProductFromScene = (uniqueId: string) => {
    setSceneProducts(prev => prev.filter(p => p.uniqueId !== uniqueId));
    if (selectedProductId === uniqueId) {
      setSelectedProductId(null);
    }
    
    scheduleAutoSave(400);
  };

  const updateProductPosition = (uniqueId: string, position: [number, number, number]) => {
    setSceneProducts(prev => 
      prev.map(product => 
        product.uniqueId === uniqueId 
          ? { ...product, positionX: position[0], positionY: position[1], positionZ: position[2] }
          : product
      )
    );
    
    scheduleAutoSave(1000);
  };

  const updateProductColor = (uniqueId: string, colorId: number) => {
    setSceneProducts(prev => 
      prev.map(product => 
        product.uniqueId === uniqueId 
          ? { ...product, selectedColorId: colorId }
          : product
      )
    );
    
    scheduleAutoSave(400);
  };

  const ensureSelectedProductColors = async (uniqueId: string, productId: number) => {
    const sp = sceneProducts.find(p => p.uniqueId === uniqueId);
    if (!sp) return;
    if (sp.modelItem.availableColors && sp.modelItem.availableColors.length > 0) return;

    try {
      const colors = await ProductService.getColors(productId);
      setSceneProducts(prev => prev.map(p => {
        if (p.uniqueId !== uniqueId) return p;
        const next = {
          ...p,
          modelItem: { ...p.modelItem, availableColors: colors }
        } as typeof p;
        if (!p.selectedColorId && colors.length > 0) {
          next.selectedColorId = colors[0].id;
        }
        return next;
      }));
    } catch (e) {
      console.error('Failed to load colors for product', productId, e);
    }
  };

  const getSelectedColor = (product: SceneProduct3D): Color | undefined => {
    if (!product.selectedColorId) return undefined;
    return product.modelItem.availableColors.find(color => color.id === product.selectedColorId);
  };

  const handleModelSelect = (model: ModelItem) => {
    setSelectedModel(model);
    addProductToScene(model);

    setControls(prev => ({
      ...prev,
      position: [0, 0, 0],
      rotation: [0, 0, 0],
      scale: 1
    }));
  };

  useEffect(() => {
    return () => {
      if (saveTimerRef.current) {
        clearTimeout(saveTimerRef.current);
        saveTimerRef.current = null;
      }
    };
  }, []);

  return (
    <div className="bathroom-3d-viewer" style={style}>
      {viewType !== '3D-Person' && (
        <ModelBrowser
          onModelSelect={handleModelSelect}
          selectedModel={selectedModel}
        />
      )}

      <div className="scene-container" style={{ cursor: isDraggingModel ? 'grabbing' : 'default' }}>
        <div className="view-type-selector">
          <button
            className={`view-type-button ${viewType === '2D' ? 'active' : ''}`}
            onClick={() => setViewType('2D')}
            title="2D Top View"
          >
            📐 2D View
          </button>
          <button
            className={`view-type-button ${viewType === '3D-Person' ? 'active' : ''}`}
            onClick={() => setViewType('3D-Person')}
            title="First Person 3D View"
          >
            👤 Person View
          </button>
          <button
            className={`view-type-button ${viewType === '3D-Free' ? 'active' : ''}`}
            onClick={() => setViewType('3D-Free')}
            title="Free 3D View"
          >
            🌐 Free View
          </button>
        </div>

        <Scene3D
          viewType={viewType}
          showGrid={controls.showGrid}
          showEnvironment={controls.showEnvironment}
          controlsEnabled={!isDraggingModel}
          onBackgroundClick={() => setSelectedProductId(null)}
          onSceneReady={(scene) => {
            sceneRef.current = scene;
          }}
          onCameraReady={(camera) => {
            cameraRef.current = camera;
          }}
        >
          {templateData?.preview && (
            <ModelLoader
              url={templateData.preview}
              position={[0, 2.41, 0]}
              rotation={[0, 0, 0]}
              scale={[2.2, 2.2, 2.2]}
              applyUnitDetection={true}
              castShadow={true}
              receiveShadow={true}
              onError={(err) => console.error('Failed to load template model:', err)}
            />
          )}

          {sceneProducts.map((product) => {
            const selectedColor = getSelectedColor(product);
            return (
              <DraggableModel
                key={product.uniqueId}
                id={product.uniqueId}
                url={product.modelItem.url}
                position={[
                  product.positionX || 0,
                  product.positionY || 0,
                  product.positionZ || 0
                ]}
                rotation={[
                  product.rotationX || 0,
                  product.rotationY || 0,
                  product.rotationZ || 0
                ]}
                color={selectedColor?.hexCode}
                selected={selectedProductId === product.uniqueId && viewType !== '3D-Person'}
                disableInteractions={viewType === '3D-Person'}
                onPositionChange={(position) => {
                  if (viewType !== '3D-Person') {
                    updateProductPosition(product.uniqueId, position);
                  }
                }}
                onClick={() => {
                  if (viewType !== '3D-Person') {
                    setSelectedProductId(product.uniqueId);
                    ensureSelectedProductColors(product.uniqueId, product.productId);
                  }
                }}
                onDragStart={() => {
                  if (viewType !== '3D-Person') {
                    setIsDraggingModel(true);
                  }
                }}
                onDragEnd={() => {
                  if (viewType !== '3D-Person') {
                    setIsDraggingModel(false);
                  }
                }}
                onError={(error) => {
                  console.error('Failed to load model:', error);
                  alert(`Failed to load model: ${product.modelItem.name}\nError: ${error.message}`);
                }}
              />
            );
          })}
        </Scene3D>

        {viewType !== '3D-Person' && (
          <div className="scene-info-panel">
            <div className="scene-header">
              <h4>{currentScene.name}</h4>
              <div className="scene-stats">
                {sceneProducts.length} product{sceneProducts.length !== 1 ? 's' : ''}
                {isAutoSaving && <span className="saving-indicator">Saving...</span>}
                {lastSaveTime && !isAutoSaving && (
                  <span className="last-saved">
                    Saved {lastSaveTime.toLocaleTimeString()}
                  </span>
                )}
              </div>
            </div>
          </div>
        )}

        {selectedProductId && viewType !== '3D-Person' && (
          <div className="product-controls-panel">
            {(() => {
              const selectedProduct = sceneProducts.find(p => p.uniqueId === selectedProductId);
              if (!selectedProduct) return null;
              
              return (
                <div className="product-controls">
                  <div className="control-header">
                    <h5>{selectedProduct.modelItem.name}</h5>
                    <button 
                      className="remove-button"
                      onClick={() => removeProductFromScene(selectedProductId)}
                      title="Remove from scene"
                    >
                      🗑️
                    </button>
                  </div>
                  
                  {selectedProduct.modelItem.availableColors.length > 0 && (
                    <div className="color-selector">
                      <label>Color:</label>
                      <div className="color-options">
                        {selectedProduct.modelItem.availableColors.map((color) => (
                          <button
                            key={color.id}
                            className={`color-option ${
                              selectedProduct.selectedColorId === color.id ? 'selected' : ''
                            }`}
                            style={{ backgroundColor: color.hexCode }}
                            onClick={() => updateProductColor(selectedProductId, color.id)}
                            title={color.name}
                          />
                        ))}
                      </div>
                    </div>
                  )}
                  
                  <div className="position-info">
                    <small>
                      Position: [{(selectedProduct.positionX || 0).toFixed(1)}, {(selectedProduct.positionY || 0).toFixed(1)}, {(selectedProduct.positionZ || 0).toFixed(1)}]
                    </small>
                  </div>
                </div>
              );
            })()}
          </div>
        )}

        {sceneProducts.length === 0 && !templateData?.preview && (
          <div className="welcome-message">
            <h3 className="welcome-title">
              Welcome to BathForge 3D
            </h3>
            <p className="welcome-description">
              Browse and select bathroom fixtures to add them to your scene
            </p>
          </div>
        )}
      </div>
    </div>
  );
}