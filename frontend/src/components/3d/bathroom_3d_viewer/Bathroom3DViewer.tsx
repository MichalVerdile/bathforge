import React, { useState, useRef, useEffect, useCallback } from "react";
import Scene3D from "../scene/Scene3D";
import ModelBrowser, { type ModelItem } from "../model_browser/ModelBrowser";
import sceneService, {
  SceneProduct,
} from "../../../controllers/api/scenes/SceneService";
import { Color } from "../../../types/api";
import { ProductService } from "../../../controllers/api/products/ProductService";
import * as THREE from "three";
import "./Bathroom3DViewer.css";
import DraggableModel from "./DraggableModel";
import { WallFloorSelector, applyTextureToMesh, detectMeshType } from "./WallFloorSelector";
import { Room } from "../../configurator/custom_room/Room";
import {
  RoomOpenings,
  DoorData,
  WindowData,
  createDefaultOpenings,
} from "../../configurator/custom_room/DoorWindowTypes";

interface Vertex {
  x: number;
  y: number;
}

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

export type ViewType = "2D" | "3D-Person" | "3D-Free";

interface Bathroom3DViewerProps {
  style?: React.CSSProperties;
}

export default function Bathroom3DViewer({ style }: Bathroom3DViewerProps) {
  const [selectedModel, setSelectedModel] = useState<ModelItem | null>(null);
  const [sceneProducts, setSceneProducts] = useState<SceneProduct3D[]>([]);
  const [selectedProductId, setSelectedProductId] = useState<string | null>(
    null
  );
  const [isDraggingModel, setIsDraggingModel] = useState(false);
  const [viewType, setViewType] = useState<ViewType>("2D");
  const [selectedBrowserCategory, setSelectedBrowserCategory] =
    useState<string>("");
  const [currentScene, setCurrentScene] = useState<{
    id?: number;
    name: string;
  }>({
    name: `Scene ${new Date().toLocaleString()}`,
  });
  const [templateData, setTemplateData] = useState<any>(null);
  const [isAutoSaving, setIsAutoSaving] = useState(false);
  const [lastSaveTime, setLastSaveTime] = useState<Date | null>(null);
  const [customRoomData, setCustomRoomData] = useState<{
    vertices: Vertex[];
    height: number;
    openings?: RoomOpenings;
  } | null>(null);
  const [roomOpenings, setRoomOpenings] = useState<RoomOpenings | null>(null);
  const [selectedOpeningId, setSelectedOpeningId] = useState<string | null>(null);
  const [selectedOpeningType, setSelectedOpeningType] = useState<"door" | "window" | null>(null);
  const [controls, setControls] = useState<SceneControlsState>({
    position: [0, 0, 0],
    rotation: [0, 0, 0],
    scale: 1,
    autoRotate: false,
    wireframe: false,
    showGrid: false,
    showEnvironment: false,
  });

  const [selectedSurface, setSelectedSurface] = useState<{
    mesh: THREE.Mesh;
    type: "wall" | "floor";
    originalMaterial: THREE.Material;
    highlightMaterial?: THREE.Material;
  } | null>(null);

  const [appliedCoverings, setAppliedCoverings] = useState<{
    [surfaceIdentifier: string]: {
      productId: number;
      surfaceType: "wall" | "floor";
      repeatX: number;
      repeatY: number;
    };
  }>({});

  const sceneRef = useRef<THREE.Scene | null>(null);
  const cameraRef = useRef<THREE.Camera | null>(null);
  const saveTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const getSurfaceIdentifier = (
    mesh: THREE.Mesh,
    type: "wall" | "floor"
  ): string => {
    const position = mesh.position;
    const rounded = {
      x: Math.round(position.x * 100) / 100,
      y: Math.round(position.y * 100) / 100,
      z: Math.round(position.z * 100) / 100,
    };
    return `${type}-${rounded.x}-${rounded.y}-${rounded.z}`;
  };

  const applyCoveringsFromAI = async (coveringsToApply: Array<{
    productId: number;
    category: string;
    color?: string;
    surfaceType?: 'wall' | 'floor';
    repeatX?: number;
    repeatY?: number;
  }>) => {
    if (!sceneRef.current) {
      return;
    }

    // Batch all covering updates to avoid multiple re-renders
    const newCoverings: { [surfaceIdentifier: string]: {
      productId: number;
      surfaceType: "wall" | "floor";
      repeatX: number;
      repeatY: number;
    }} = {};

    for (const covering of coveringsToApply) {
      try {
        const product = await ProductService.getById(covering.productId);
        const texturePath = product.thumbnail || product.modelPath;

        // Use explicit surfaceType if provided, otherwise determine from category
        let targetType: 'wall' | 'floor';
        if (covering.surfaceType) {
          targetType = covering.surfaceType;
        } else {
          const isFloorCovering = covering.category?.toLowerCase().includes('floor');
          targetType = isFloorCovering ? 'floor' : 'wall';
        }

        const repeatX = covering.repeatX || 3;
        const repeatY = covering.repeatY || 3;

        // Find all meshes of the target type in the scene
        // IMPORTANT: Only target actual room surfaces (not product models)
        const meshesToTexture: THREE.Mesh[] = [];
        sceneRef.current.traverse((child) => {
          if (child instanceof THREE.Mesh) {
            const name = child.name.toLowerCase();

            // Explicit name matching - only room surfaces
            if (targetType === 'floor' && name === 'floor') {
              meshesToTexture.push(child);
            } else if (targetType === 'wall' && name.startsWith('wall-')) {
              meshesToTexture.push(child);
            }
          }
        });

        // Apply texture to all matching surfaces
        for (const mesh of meshesToTexture) {
          try {
            await applyTextureToMesh(mesh, texturePath, repeatX, repeatY);

            const surfaceIdentifier = getSurfaceIdentifier(mesh, targetType);
            // Collect in batch instead of updating state immediately
            newCoverings[surfaceIdentifier] = {
              productId: covering.productId,
              surfaceType: targetType,
              repeatX,
              repeatY,
            };
          } catch (error) {
            console.error(`Failed to apply texture to ${targetType}:`, error);
          }
        }
      } catch (error) {
        console.error("Failed to load covering product:", covering.productId, error);
      }
    }

    // Update state ONCE with all coverings
    setAppliedCoverings((prev) => ({
      ...prev,
      ...newCoverings
    }));

    // Schedule save after applying all coverings
    scheduleAutoSave(1000);
  };

  useEffect(() => {
    // Load AI-generated design if available
    const loadAIDesign = async () => {
      const storedAIResponse = localStorage.getItem("aiDesignResponse");
      const storedAIPreferences = localStorage.getItem("aiPreferences");
      
      if (storedAIResponse && storedAIPreferences) {
        try {
          const aiResponse = JSON.parse(storedAIResponse);
          const aiPreferences = JSON.parse(storedAIPreferences);

          // Set the room data from AI preferences
          if (aiPreferences.room) {
            setCustomRoomData(aiPreferences.room);
            // Initialize openings from AI preferences or create default ones
            if (aiPreferences.room.openings) {
              setRoomOpenings(aiPreferences.room.openings);
            } else {
              // Generate default openings for AI-generated room
              const defaultOpenings = createDefaultOpenings(
                aiPreferences.room.vertices,
                aiPreferences.room.height
              );
              setRoomOpenings(defaultOpenings);
            }
          }

          // Set scene name
          setCurrentScene({
            name: `AI Design ${new Date().toLocaleString()}`,
          });
          
          // Load products from AI recommendations
          if (aiResponse.productRecommendations && aiResponse.productRecommendations.length > 0) {
            const loadedProducts: SceneProduct3D[] = [];
            
            for (const recommendation of aiResponse.productRecommendations) {
              try {
                // Fetch the full product data
                const product = await ProductService.getById(recommendation.productId);
                
                // Get position from AI recommendations or use defaults based on mounting type
                // AI backend now calculates positions to avoid overlap and sets proper heights
                let defaultY = 0.08; // Default for FLOOR/FREESTANDING
                if (recommendation.mountingType === 'WALL') {
                  defaultY = 0.38; // Wall-mounted items
                }

                let position = {
                  x: recommendation.positionX != null ? recommendation.positionX : 0,
                  y: recommendation.positionY != null ? recommendation.positionY : defaultY,
                  z: recommendation.positionZ != null ? recommendation.positionZ : 0
                };

                // Get colors if available
                let colors = product.availableColors || [];
                if (!colors || colors.length === 0) {
                  try {
                    colors = await ProductService.getColors(product.id);
                  } catch (e) {
                    // Color loading failed, continue with empty array
                  }
                }
                
                // Find color ID if specified by AI
                let selectedColorId = colors.length > 0 ? colors[0].id : undefined;
                if (recommendation.color && colors.length > 0) {
                  const matchingColor = colors.find(c => 
                    c.name.toLowerCase().includes(recommendation.color.toLowerCase()) ||
                    c.hexCode.toLowerCase().includes(recommendation.color.toLowerCase())
                  );
                  if (matchingColor) {
                    selectedColorId = matchingColor.id;
                  }
                }
                
                const uniqueId = `ai_product_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
                
                const sceneProduct: SceneProduct3D = {
                  uniqueId,
                  productId: product.id,
                  modelItem: {
                    id: product.id,
                    name: product.name,
                    category: product.categoryName || recommendation.category,
                    categoryId: recommendation.categoryId || product.categoryId,
                    priceRange: (recommendation.priceRange || product.priceRange) as 'LOW' | 'MEDIUM' | 'HIGH',
                    mountingType: (recommendation.mountingType || product.mountingType) as 'FLOOR' | 'WALL' | 'FREESTANDING',
                    url: product.modelPath,
                    thumbnail: product.thumbnail || product.modelPath,
                    availableColors: colors
                  },
                  positionX: position.x,
                  positionY: position.y,
                  positionZ: position.z,
                  rotationX: recommendation.rotationX != null ? recommendation.rotationX : 0,
                  rotationY: recommendation.rotationY != null ? recommendation.rotationY : 0,
                  rotationZ: recommendation.rotationZ != null ? recommendation.rotationZ : 0,
                  scaleX: 1,
                  scaleY: 1,
                  scaleZ: 1,
                  selectedColorId
                };

                loadedProducts.push(sceneProduct);
              } catch (error) {
                console.error("Failed to load product:", recommendation.productId, error);
              }
            }

            setSceneProducts(loadedProducts);
          }
          
          // Apply covering recommendations from AI
          if (aiResponse.coveringRecommendations && aiResponse.coveringRecommendations.length > 0) {
            // Wait for scene to be fully loaded before applying coverings
            setTimeout(async () => {
              try {
                await applyCoveringsFromAI(aiResponse.coveringRecommendations.map((covering: any) => ({
                  productId: covering.productId,
                  category: covering.category,
                  color: covering.color,
                  surfaceType: covering.surfaceType,
                  repeatX: covering.repeatX || 1.0,
                  repeatY: covering.repeatY || 1.0
                })));
              } catch (error) {
                console.error("Failed to apply coverings:", error);
              }
            }, 3000);
          }
          
          // Clean up AI localStorage entries
          localStorage.removeItem("aiDesignResponse");
          localStorage.removeItem("aiPreferences");
          return true;
        } catch (error) {
          console.error("Error parsing AI design data:", error);
        }
      }
      return false;
    };
    
    // Try to load AI design first
    loadAIDesign().then((aiLoaded) => {
      // If AI design was loaded, don't load template or custom room
      // But if only custom room or template is present, load them
      if (aiLoaded) {
        // Clean up other potential sources
        localStorage.removeItem("customRoom");
        localStorage.removeItem("selectedTemplate");
        setTemplateData(null);
        return;
      }
      
      // Load custom room if no AI design
      const storedCustomRoom = localStorage.getItem("customRoom");
      if (storedCustomRoom) {
        try {
          const customRoom = JSON.parse(storedCustomRoom);
          setCustomRoomData(customRoom);
          // Initialize openings from stored data or create default ones
        if (customRoom.openings) {
          setRoomOpenings(customRoom.openings);
        } else {
          // Generate default openings for custom room
          const defaultOpenings = createDefaultOpenings(
            customRoom.vertices,
            customRoom.height
          );
          setRoomOpenings(defaultOpenings);
        }
        setCurrentScene({
            name: `Custom Scene ${new Date().toLocaleString()}`,
          });
          localStorage.removeItem("customRoom");
          localStorage.removeItem("selectedTemplate");
          setTemplateData(null);
          return;
        } catch (error) {
          console.error("Error parsing custom room data:", error);
        }
      }

      // Load template if no AI design or custom room
      const storedTemplate = localStorage.getItem("selectedTemplate");
      if (storedTemplate) {
        try {
          const template = JSON.parse(storedTemplate);
          setTemplateData(template);
          // Initialize openings from template
        if (template.roomData?.openings) {
          setRoomOpenings(template.roomData.openings);
        } else {
          // Generate default openings if template doesn't have them
          const defaultOpenings = createDefaultOpenings(
            template.roomData.vertices,
            template.roomData.height / 100
          );
          setRoomOpenings(defaultOpenings);
        }
        setCurrentScene({
            name: `Template Scene ${new Date().toLocaleString()}`,
          });
        } catch (error) {
          console.error("Error parsing template data:", error);
        }
      }
    });
  }, []);

  const autoSaveScene = async () => {
    if (
      sceneProducts.length === 0 &&
      !customRoomData &&
      !templateData &&
      Object.keys(appliedCoverings).length === 0
    ) {
      console.log("No data to save - skipping auto-save");
      return;
    }

    setIsAutoSaving(true);
    try {
      const sceneData = sceneProducts.map((product) => ({
        productId: product.productId,
        colorId: product.selectedColorId,
        positionX: product.positionX || 0,
        positionY: product.positionY || 0,
        positionZ: product.positionZ || 0,
        rotationX: product.rotationX || 0,
        rotationY: product.rotationY || 0,
        rotationZ: product.rotationZ || 0,
        scaleX: product.scaleX || 1,
        scaleY: product.scaleY || 1,
        scaleZ: product.scaleZ || 1,
        customProperties: product.customProperties,
      }));

      const cameraPosition = cameraRef.current
        ? JSON.stringify({
            x: cameraRef.current.position.x,
            y: cameraRef.current.position.y,
            z: cameraRef.current.position.z,
          })
        : undefined;

      let roomModelData = undefined;

      if (customRoomData) {
        // Prepare room properties with door/window data
        const roomProperties = roomOpenings ? JSON.stringify(roomOpenings) : undefined;
        
        roomModelData = {
          vertices: customRoomData.vertices,
          height: customRoomData.height,
          roomProperties,
        };
        console.log("Saving custom room model:", roomModelData);
      } else if (templateData) {
        // Prepare room properties with door/window data
        const roomProperties = roomOpenings ? JSON.stringify(roomOpenings) : undefined;
        
        roomModelData = {
          vertices: templateData.roomData.vertices,
          height: templateData.roomData.height / 100,
          roomProperties,
        };
        console.log("Saving template room model:", roomModelData);
      }

      if (!roomModelData) {
        console.log("No room model data to save (neither custom nor template)");
      }

      const coveringsData = Object.entries(appliedCoverings).map(
        ([surfaceIdentifier, covering]) => ({
          productId: covering.productId,
          surfaceType: covering.surfaceType,
          surfaceIdentifier,
          repeatX: covering.repeatX,
          repeatY: covering.repeatY,
        })
      );

      console.log("Auto-saving scene with data:", {
        sceneProducts: sceneData.length,
        roomModel: roomModelData,
        coverings: coveringsData.length,
        currentScene: currentScene,
      });

      if (currentScene.id) {
        console.log("Updating existing scene with ID:", currentScene.id);
        await sceneService.saveCurrentSceneWithRoomAndCoverings(
          currentScene.name,
          "guest",
          sceneData,
          roomModelData,
          coveringsData.length > 0 ? coveringsData : undefined,
          cameraPosition,
          undefined,
          "#0f172a",
          currentScene.id
        );
      } else {
        console.log("Creating new scene");
        const newScene = await sceneService.createScene({
          name: currentScene.name,
          user: "guest",
          products: sceneData.map((product) => ({
            productId: product.productId,
            colorId: product.colorId,
            positionX: product.positionX || 0,
            positionY: product.positionY || 0,
            positionZ: product.positionZ || 0,
            rotationX: product.rotationX || 0,
            rotationY: product.rotationY || 0,
            rotationZ: product.rotationZ || 0,
            scaleX: product.scaleX || 1,
            scaleY: product.scaleY || 1,
            scaleZ: product.scaleZ || 1,
            customProperties: product.customProperties,
          })),
          roomModel: roomModelData
            ? {
                verticesData: JSON.stringify(roomModelData.vertices),
                roomHeight: roomModelData.height,
                modelType: templateData ? "TEMPLATE" : "CUSTOM",
                templatePath: templateData?.preview,
                roomProperties: roomModelData.roomProperties,
              }
            : undefined,
          coverings: coveringsData.length > 0 ? coveringsData : undefined,
          cameraPosition,
          lightingSettings: undefined,
          backgroundColor: "#0f172a",
          isPublic: false,
        });

        setCurrentScene((prev) => ({ ...prev, id: newScene.id }));
        console.log("Created new scene with ID:", newScene.id);
      }

      setLastSaveTime(new Date());
    } catch (error) {
      console.error("Failed to auto-save scene:", error);
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

  const addProductToScene = (
    model: ModelItem,
    position?: [number, number, number]
  ) => {
    const uniqueId = `product_${Date.now()}_${Math.random()
      .toString(36)
      .substr(2, 9)}`;

    let defaultHeight = 0.08;
    if (model.mountingType === "WALL") {
      defaultHeight = 0.38;
    } else if (model.mountingType === "FREESTANDING") {
      defaultHeight = 0.08;
    }

    const newProduct: SceneProduct3D = {
      uniqueId,
      productId: model.id,
      modelItem: model,
      positionX: position ? position[0] : 0,
      positionY: position ? position[1] : defaultHeight,
      positionZ: position ? position[2] : 0,
      rotationX: 0,
      rotationY: 0,
      rotationZ: 0,
      selectedColorId:
        model.availableColors && model.availableColors.length > 0
          ? model.availableColors[0].id
          : undefined,
    };

    setSceneProducts((prev) => [...prev, newProduct]);
    setSelectedProductId(uniqueId);
    scheduleAutoSave(400);
  };

  const removeProductFromScene = (uniqueId: string) => {
    setSceneProducts((prev) => prev.filter((p) => p.uniqueId !== uniqueId));
    if (selectedProductId === uniqueId) {
      setSelectedProductId(null);
    }

    scheduleAutoSave(400);
  };

  const updateProductPosition = (
    uniqueId: string,
    position: [number, number, number]
  ) => {
    setSceneProducts((prev) =>
      prev.map((product) =>
        product.uniqueId === uniqueId
          ? {
              ...product,
              positionX: position[0],
              positionY: position[1],
              positionZ: position[2],
            }
          : product
      )
    );

    scheduleAutoSave(1000);
  };

  const updateProductRotation = (
    uniqueId: string,
    rotation: [number, number, number]
  ) => {
    setSceneProducts((prev) =>
      prev.map((product) =>
        product.uniqueId === uniqueId
          ? {
              ...product,
              rotationX: rotation[0],
              rotationY: rotation[1],
              rotationZ: rotation[2],
            }
          : product
      )
    );

    scheduleAutoSave(1000);
  };

  const updateProductColor = (uniqueId: string, colorId: number) => {
    setSceneProducts((prev) =>
      prev.map((product) =>
        product.uniqueId === uniqueId
          ? { ...product, selectedColorId: colorId }
          : product
      )
    );

    scheduleAutoSave(400);
  };

  // Handle opening (door/window) click
  const handleOpeningClick = useCallback(
    (id: string, type: "door" | "window") => {
      if (viewType === "3D-Person") return;
      setSelectedOpeningId(id);
      setSelectedOpeningType(type);
      setSelectedProductId(null); // Deselect any product
      handleSurfaceSelect(null, null); // Deselect any surface
    },
    [viewType]
  );

  // Update opening position along wall
  const updateOpeningPosition = useCallback(
    (openingId: string, newPosition: number) => {
      if (!roomOpenings) return;

      // Clamp position between 0.1 and 0.9 to keep opening on wall
      const clampedPosition = Math.max(0.1, Math.min(0.9, newPosition));

      setRoomOpenings((prev) => {
        if (!prev) return prev;

        const newDoors = prev.doors.map((door) =>
          door.id === openingId
            ? { ...door, position: clampedPosition }
            : door
        );

        const newWindows = prev.windows.map((window) =>
          window.id === openingId
            ? { ...window, position: clampedPosition }
            : window
        );

        return { doors: newDoors, windows: newWindows };
      });

      scheduleAutoSave(1000);
    },
    [roomOpenings]
  );

  // Move opening to a different wall
  const moveOpeningToWall = useCallback(
    (openingId: string, newWallIndex: number) => {
      if (!roomOpenings) return;

      setRoomOpenings((prev) => {
        if (!prev) return prev;

        const newDoors = prev.doors.map((door) =>
          door.id === openingId
            ? { ...door, wallIndex: newWallIndex, position: 0.5 }
            : door
        );

        const newWindows = prev.windows.map((window) =>
          window.id === openingId
            ? { ...window, wallIndex: newWallIndex, position: 0.5 }
            : window
        );

        return { doors: newDoors, windows: newWindows };
      });

      scheduleAutoSave(400);
    },
    [roomOpenings]
  );

  // Get currently selected opening data
  const getSelectedOpening = useCallback((): DoorData | WindowData | null => {
    if (!selectedOpeningId || !roomOpenings) return null;

    const door = roomOpenings.doors.find((d) => d.id === selectedOpeningId);
    if (door) return door;

    const window = roomOpenings.windows.find((w) => w.id === selectedOpeningId);
    return window || null;
  }, [selectedOpeningId, roomOpenings]);

  // Get number of walls for current room
  const getWallCount = useCallback((): number => {
    if (customRoomData) {
      return customRoomData.vertices.length;
    }
    if (templateData) {
      return templateData.roomData.vertices.length;
    }
    return 0;
  }, [customRoomData, templateData]);

  const ensureSelectedProductColors = async (
    uniqueId: string,
    productId: number
  ) => {
    const sp = sceneProducts.find((p) => p.uniqueId === uniqueId);
    if (!sp) return;
    if (sp.modelItem.availableColors && sp.modelItem.availableColors.length > 0)
      return;

    try {
      const colors = await ProductService.getColors(productId);
      setSceneProducts((prev) =>
        prev.map((p) => {
          if (p.uniqueId !== uniqueId) return p;
          const next = {
            ...p,
            modelItem: { ...p.modelItem, availableColors: colors },
          } as typeof p;
          if (!p.selectedColorId && colors.length > 0) {
            next.selectedColorId = colors[0].id;
          }
          return next;
        })
      );
    } catch (e) {
      console.error("Failed to load colors for product", productId, e);
    }
  };

  const getSelectedColor = (product: SceneProduct3D): Color | undefined => {
    if (!product.selectedColorId) return undefined;
    return product.modelItem.availableColors.find(
      (color) => color.id === product.selectedColorId
    );
  };

  const isImageFile = (url: string): boolean => {
    const imageExtensions = [
      ".jpg",
      ".jpeg",
      ".png",
      ".gif",
      ".bmp",
      ".webp",
      ".tiff",
      ".svg",
    ];
    const lowerUrl = url.toLowerCase();
    return imageExtensions.some((ext) => lowerUrl.endsWith(ext));
  };

  const handleModelSelect = async (model: ModelItem) => {
    const isTexture = isImageFile(model.url);

    if (isTexture && selectedSurface) {
      try {
        await applyTextureToMesh(
          selectedSurface.mesh,
          model.thumbnail || model.url,
          3,
          3
        );

        const surfaceIdentifier = getSurfaceIdentifier(
          selectedSurface.mesh,
          selectedSurface.type
        );

        setAppliedCoverings((prev) => ({
          ...prev,
          [surfaceIdentifier]: {
            productId: model.id,
            surfaceType: selectedSurface.type,
            texturePath: model.thumbnail || model.url,
            repeatX: 3,
            repeatY: 3,
          },
        }));

        scheduleAutoSave();
      } catch (error) {
        console.error("Failed to apply covering:", error);
        alert("Failed to apply texture. Please try again.");
      }
    } else if (!isTexture) {
      setSelectedModel(model);
      addProductToScene(model);

      setControls((prev) => ({
        ...prev,
        position: [0, 0, 0],
        rotation: [0, 0, 0],
        scale: 1,
      }));
    } else {
      alert("Please select a wall or floor first before applying a covering.");
    }
  };

  const handleSurfaceSelect = (
    mesh: THREE.Mesh | null,
    type: "wall" | "floor" | null
  ) => {
    if (selectedSurface) {
      selectedSurface.mesh.material = selectedSurface.originalMaterial;
      if (selectedSurface.highlightMaterial) {
        (selectedSurface.highlightMaterial as THREE.Material).dispose();
      }
    }

    if (mesh && type) {
      const originalMaterial = Array.isArray(mesh.material)
        ? mesh.material[0]
        : mesh.material;

      const clonedOriginal = originalMaterial.clone();

      const highlightMaterial = originalMaterial.clone();

      if (highlightMaterial instanceof THREE.MeshStandardMaterial) {
        highlightMaterial.emissive = new THREE.Color(0xffffff);
        highlightMaterial.emissiveIntensity = 0.3;
        highlightMaterial.opacity = 0.8;
        highlightMaterial.transparent = true;
      }

      mesh.material = highlightMaterial;

      setSelectedSurface({
        mesh,
        type,
        originalMaterial: clonedOriginal,
        highlightMaterial,
      });
      setSelectedProductId(null);
    } else {
      setSelectedSurface(null);
    }
  };

  useEffect(() => {
    return () => {
      if (saveTimerRef.current) {
        clearTimeout(saveTimerRef.current);
        saveTimerRef.current = null;
      }
      if (selectedSurface) {
        selectedSurface.mesh.material = selectedSurface.originalMaterial;
        if (selectedSurface.highlightMaterial) {
          (selectedSurface.highlightMaterial as THREE.Material).dispose();
        }
      }
    };
  }, []);

  useEffect(() => {
    if (selectedBrowserCategory !== "coverings" && selectedSurface) {
      handleSurfaceSelect(null, null);
    }
  }, [selectedBrowserCategory]);

  return (
    <div className="bathroom-3d-viewer" style={style}>
      {viewType !== "3D-Person" && (
        <ModelBrowser
          onModelSelect={handleModelSelect}
          selectedModel={selectedModel}
          onCategoryChange={setSelectedBrowserCategory}
        />
      )}

      <div
        className="scene-container"
        style={{ cursor: isDraggingModel ? "grabbing" : "default" }}
      >
        <div className="view-type-selector">
          <button
            className={`view-type-button ${viewType === "2D" ? "active" : ""}`}
            onClick={() => setViewType("2D")}
            title="2D Top View"
          >
            2D View
          </button>
          <button
            className={`view-type-button ${
              viewType === "3D-Person" ? "active" : ""
            }`}
            onClick={() => setViewType("3D-Person")}
            title="First Person 3D View"
          >
            Person View
          </button>
          <button
            className={`view-type-button ${
              viewType === "3D-Free" ? "active" : ""
            }`}
            onClick={() => setViewType("3D-Free")}
            title="Free 3D View"
          >
            Free View
          </button>
        </div>

        <Scene3D
          viewType={viewType}
          showGrid={controls.showGrid}
          showEnvironment={controls.showEnvironment}
          controlsEnabled={!isDraggingModel}
          onBackgroundClick={() => {
            setSelectedProductId(null);
            setSelectedOpeningId(null);
            setSelectedOpeningType(null);
            handleSurfaceSelect(null, null);
          }}
          onSceneReady={(scene) => {
            sceneRef.current = scene;
          }}
          onCameraReady={(camera) => {
            cameraRef.current = camera;
          }}
        >
          {(viewType === "2D" || viewType === "3D-Free") &&
            selectedBrowserCategory === "coverings" && (
              <WallFloorSelector
                enabled={true}
                onSelect={handleSurfaceSelect}
              />
            )}

          {templateData && (
            <Room
              vertices={templateData.roomData.vertices}
              height={templateData.roomData.height / 100}
              viewMode="3D"
              openings={roomOpenings || undefined}
              selectedOpeningId={selectedOpeningId}
              onOpeningClick={handleOpeningClick}
              isInteractive={viewType !== "3D-Person"}
            />
          )}

          {customRoomData && !templateData && (
            <Room
              vertices={customRoomData.vertices}
              height={customRoomData.height}
              viewMode="3D"
              openings={roomOpenings || undefined}
              selectedOpeningId={selectedOpeningId}
              onOpeningClick={handleOpeningClick}
              isInteractive={viewType !== "3D-Person"}
            />
          )}

          {sceneProducts.map((product) => {
            const selectedColor = getSelectedColor(product);
            // Get room data from either custom room or template
            const roomData = customRoomData
              ? {
                  vertices: customRoomData.vertices,
                  height: customRoomData.height,
                }
              : templateData
              ? {
                  vertices: templateData.roomData.vertices,
                  height: templateData.roomData.height / 100,
                }
              : undefined;

            return (
              <DraggableModel
                key={product.uniqueId}
                id={product.uniqueId}
                url={product.modelItem.url}
                position={[
                  product.positionX ?? 0,
                  product.positionY ?? 0,
                  product.positionZ ?? 0,
                ]}
                rotation={[
                  product.rotationX ?? 0,
                  product.rotationY ?? 0,
                  product.rotationZ ?? 0,
                ]}
                color={selectedColor?.hexCode}
                selected={
                  selectedProductId === product.uniqueId &&
                  viewType !== "3D-Person"
                }
                disableInteractions={viewType === "3D-Person"}
                roomVertices={roomData?.vertices}
                roomHeight={roomData?.height}
                onPositionChange={(position) => {
                  if (viewType !== "3D-Person") {
                    updateProductPosition(product.uniqueId, position);
                  }
                }}
                onRotationChange={(rotation) => {
                  if (viewType !== "3D-Person") {
                    updateProductRotation(product.uniqueId, rotation);
                  }
                }}
                onClick={() => {
                  if (viewType !== "3D-Person") {
                    setSelectedProductId(product.uniqueId);
                    ensureSelectedProductColors(
                      product.uniqueId,
                      product.productId
                    );
                  }
                }}
                onDragStart={() => {
                  if (viewType !== "3D-Person") {
                    setIsDraggingModel(true);
                  }
                }}
                onDragEnd={() => {
                  if (viewType !== "3D-Person") {
                    setIsDraggingModel(false);
                  }
                }}
                onError={(error) => {
                  console.error("Failed to load model:", error);
                  alert(
                    `Failed to load model: ${product.modelItem.name}\nError: ${error.message}`
                  );
                }}
              />
            );
          })}
        </Scene3D>

        {viewType !== "3D-Person" && (
          <div className="scene-info-panel">
            <div className="scene-header">
              <h4>{currentScene.name}</h4>
              <div className="scene-stats">
                {sceneProducts.length} product
                {sceneProducts.length !== 1 ? "s" : ""}
                {isAutoSaving && (
                  <span className="saving-indicator">Saving...</span>
                )}
                {lastSaveTime && !isAutoSaving && (
                  <span className="last-saved">
                    Saved {lastSaveTime.toLocaleTimeString()}
                  </span>
                )}
              </div>
            </div>
            {selectedSurface && (
              <div className="surface-selection-info">
                <strong>
                  {selectedSurface.type === "wall" ? "🧱 Wall" : "⬜ Floor"}{" "}
                  Selected
                </strong>
                <p>Select a covering from the browser to apply</p>
                <button
                  className="deselect-button"
                  onClick={() => handleSurfaceSelect(null, null)}
                >
                  Deselect
                </button>
              </div>
            )}
          </div>
        )}

        {selectedProductId && viewType !== "3D-Person" && (
          <div className="product-controls-panel">
            {(() => {
              const selectedProduct = sceneProducts.find(
                (p) => p.uniqueId === selectedProductId
              );
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
                        {selectedProduct.modelItem.availableColors.map(
                          (color) => (
                            <button
                              key={color.id}
                              className={`color-option ${
                                selectedProduct.selectedColorId === color.id
                                  ? "selected"
                                  : ""
                              }`}
                              style={{ backgroundColor: color.hexCode }}
                              onClick={() =>
                                updateProductColor(selectedProductId, color.id)
                              }
                              title={color.name}
                            />
                          )
                        )}
                      </div>
                    </div>
                  )}

                  <div className="slider-control">
                    <label>
                      Rotation:{" "}
                      {Math.round(
                        (selectedProduct.rotationY || 0) * (180 / Math.PI)
                      )}
                      °
                    </label>
                    <input
                      type="range"
                      min="0"
                      max="360"
                      step="15"
                      value={Math.round(
                        (selectedProduct.rotationY || 0) * (180 / Math.PI)
                      )}
                      onChange={(e) => {
                        const degrees = parseFloat(e.target.value);
                        const radians = degrees * (Math.PI / 180);
                        updateProductRotation(selectedProductId, [
                          selectedProduct.rotationX || 0,
                          radians,
                          selectedProduct.rotationZ || 0,
                        ]);
                      }}
                      className="slider"
                    />
                  </div>

                  <div className="slider-control">
                    <label>
                      Height:{" "}
                      {Math.max(
                        0,
                        (selectedProduct.positionY || 0.08) - 0.08
                      ).toFixed(2)}
                      m
                    </label>
                    <input
                      type="range"
                      min="0"
                      max="1.30"
                      step="0.01"
                      value={Math.max(
                        0,
                        (selectedProduct.positionY || 0.08) - 0.08
                      )}
                      onChange={(e) => {
                        const relativeHeight = parseFloat(e.target.value);
                        const actualHeight = relativeHeight + 0.08;
                        updateProductPosition(selectedProductId, [
                          selectedProduct.positionX || 0,
                          actualHeight,
                          selectedProduct.positionZ || 0,
                        ]);
                      }}
                      className="slider"
                    />
                  </div>
                </div>
              );
            })()}
          </div>
        )}

        {/* Opening (Door/Window) controls panel */}
        {selectedOpeningId && viewType !== "3D-Person" && (
          <div className="product-controls-panel opening-controls-panel">
            {(() => {
              const selectedOpening = getSelectedOpening();
              if (!selectedOpening) return null;

              const wallCount = getWallCount();

              return (
                <div className="product-controls">
                  <div className="control-header">
                    <h5>
                      {selectedOpeningType === "door" ? "Door" : "Window"}
                    </h5>
                    <button
                      className="deselect-button"
                      onClick={() => {
                        setSelectedOpeningId(null);
                        setSelectedOpeningType(null);
                      }}
                      title="Deselect"
                    >
                      x
                    </button>
                  </div>

                  <div className="slider-control">
                    <label>
                      Position along wall:{" "}
                      {Math.round(selectedOpening.position * 100)}%
                    </label>
                    <input
                      type="range"
                      min="0.1"
                      max="0.9"
                      step="0.05"
                      value={selectedOpening.position}
                      onChange={(e) => {
                        updateOpeningPosition(
                          selectedOpeningId,
                          parseFloat(e.target.value)
                        );
                      }}
                      className="slider"
                    />
                  </div>

                  <div className="wall-selector">
                    <label>Wall:</label>
                    <div className="wall-buttons">
                      {Array.from({ length: wallCount }, (_, i) => (
                        <button
                          key={i}
                          className={`wall-button ${
                            selectedOpening.wallIndex === i ? "active" : ""
                          }`}
                          onClick={() => moveOpeningToWall(selectedOpeningId, i)}
                        >
                          {i + 1}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="opening-info">
                    <p>
                      Size: {selectedOpening.width.toFixed(2)}m x{" "}
                      {selectedOpening.height.toFixed(2)}m
                    </p>
                    {selectedOpeningType === "window" &&
                      "elevation" in selectedOpening && (
                        <p>
                          Height from floor:{" "}
                          {(selectedOpening as WindowData).elevation.toFixed(2)}m
                        </p>
                      )}
                  </div>
                </div>
              );
            })()}
          </div>
        )}

        {sceneProducts.length === 0 && !templateData && !customRoomData && (
          <div className="welcome-message">
            <h3 className="welcome-title">Welcome to BathForge 3D</h3>
            <p className="welcome-description">
              Browse and select bathroom fixtures to add them to your scene
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
