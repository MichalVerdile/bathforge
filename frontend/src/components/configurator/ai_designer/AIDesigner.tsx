import React, { useState, useRef, useCallback } from "react";
import StyleStep from "./StyleStep";
import ColorStep from "./ColorStep";
import FeaturesStep from "./FeaturesStep";
import RoomStep from "./RoomStep";
import SummaryStep from "./SummaryStep";
import "./AIDesigner.css";
import type { ColorPaletteId, FeatureId, StyleId } from "./data";
import { aiDesignerController } from "../../../controllers/api/ai/AIDesignerController";
import type { RoomEditorRef } from "../custom_room/RoomEditor";
import type { RoomOpenings } from "../custom_room/DoorWindowTypes";

interface AIDesignerProps {
  onNavigate: (view: string) => void;
}

interface Vertex {
  x: number;
  y: number;
}

interface RoomConfiguration {
  vertices: Vertex[];
  height: number;
  openings?: RoomOpenings;
}

export interface AIPreferences {
  style?: StyleId;
  colors?: ColorPaletteId[];
  features?: FeatureId[];
  room?: RoomConfiguration;
}

const AIDesigner: React.FC<AIDesignerProps> = ({ onNavigate }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [preferences, setPreferences] = useState<AIPreferences>({});
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const roomEditorRef = useRef<RoomEditorRef>(null);

  const totalSteps = 5;

  const updatePreferences = useCallback(<Key extends keyof AIPreferences>(
    key: Key,
    value: AIPreferences[Key]
  ) => {
    setPreferences((prev) => ({
      ...prev,
      [key]: value,
    }));
  }, []);

  const handleNext = () => {
    if (currentStep < totalSteps) {
      setCurrentStep((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    if (currentStep > 1) {
      setCurrentStep((prev) => prev - 1);
    } else {
      onNavigate("planner");
    }
  };

  const handleRoomChange = useCallback((room: RoomConfiguration) => {
    updatePreferences("room", room);
  }, [updatePreferences]);

  const handleGenerate = async () => {
    setIsGenerating(true);
    setError(null);
    
    try {
      console.log("Generating AI design with preferences:", preferences);
      
      // Call the backend AI service
      const designResponse = await aiDesignerController.generateDesign(preferences);
      
      console.log("AI design generated:", designResponse);
      
      // Save both original preferences and AI response to localStorage
      localStorage.setItem("aiPreferences", JSON.stringify(preferences));
      localStorage.setItem("aiDesignResponse", JSON.stringify(designResponse));
      
      // Navigate to 3D view to show the generated design
      onNavigate("3d");
      
    } catch (error: any) {
      console.error("Failed to generate AI design:", error);
      setError(error.message || "Failed to generate design. Please try again.");
    } finally {
      setIsGenerating(false);
    }
  };

  const isStepComplete = (): boolean => {
    switch (currentStep) {
      case 1:
        return !!preferences.style;
      case 2:
        return !!preferences.colors && preferences.colors.length > 0;
      case 3:
        return !!preferences.features && preferences.features.length > 0;
      case 4:
        return !!preferences.room && preferences.room.vertices.length > 0;
      case 5:
        return true;
      default:
        return false;
    }
  };

  const getStepTitle = (): string => {
    switch (currentStep) {
      case 1:
        return "Choose Your Style";
      case 2:
        return "Select Color Palette";
      case 3:
        return "Pick Your Features";
      case 4:
        return "Design Your Room Shape";
      case 5:
        return "Review Your Preferences";
      default:
        return "AI Bathroom Designer";
    }
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <StyleStep
            selectedStyle={preferences.style}
            onStyleSelect={(style) => updatePreferences("style", style)}
          />
        );
      case 2:
        return (
          <ColorStep
            selectedColors={preferences.colors || []}
            onColorsSelect={(colors) => updatePreferences("colors", colors)}
          />
        );
      case 3:
        return (
          <FeaturesStep
            selectedFeatures={preferences.features || []}
            onFeaturesSelect={(features) =>
              updatePreferences("features", features)
            }
          />
        );
      case 4:
        return (
          <RoomStep
            ref={roomEditorRef}
            currentRoom={preferences.room}
            onRoomChange={handleRoomChange}
          />
        );
      case 5:
        return <SummaryStep preferences={preferences} />;
      default:
        return null;
    }
  };

  return (
    <div className="ai-designer">
      <div className="page-title">
        <h1 className="step-number">
          Step {currentStep} of {totalSteps}
        </h1>
        <h2 className="step-title">{getStepTitle()}</h2>
      </div>

      <div className="ai-designer-content">
        <div className="ai-left">
          {/* Loading Overlay */}
          {isGenerating && (
            <div className="loading-overlay">
              <div className="loading-content">
                <div className="loading-spinner"></div>
                <p className="loading-text">Generating your bathroom design...</p>
              </div>
            </div>
          )}

          <div className="step-content">{renderStep()}</div>

          {/* Error message */}
          {error && (
            <div style={{ 
              color: 'red', 
              margin: '1rem 0', 
              padding: '0.5rem', 
              background: '#ffe6e6', 
              borderRadius: '4px',
              border: '1px solid #ffcccc'
            }}>
              {error}
            </div>
          )}

          <div
            className={`action-buttons ${
              currentStep === 5 ? "summary-spacing" : ""
            }`}
          >
            <button className="go-back-button" onClick={handleBack}>
              {currentStep === 1 ? "Cancel" : "Back"}
            </button>

            {currentStep < totalSteps ? (
              <button
                className={`continue-button ${
                  isStepComplete() ? "enabled" : "disabled"
                }`}
                onClick={handleNext}
                disabled={!isStepComplete()}
              >
                Next
              </button>
            ) : (
              <button
                className={`continue-button ${isGenerating ? "disabled" : "enabled"}`}
                onClick={handleGenerate}
                disabled={isGenerating}
              >
                Generate My Bathroom
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AIDesigner;
