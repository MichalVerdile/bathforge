import React, { useState } from "react";
import StyleStep from "./StyleStep";
import ColorStep from "./ColorStep";
import FeaturesStep from "./FeaturesStep";
import SummaryStep from "./SummaryStep";
import "./AIDesigner.css";
import type { ColorPaletteId, FeatureId, StyleId } from "./data";

interface AIDesignerProps {
  onNavigate: (view: string) => void;
}

export interface AIPreferences {
  style?: StyleId;
  colors?: ColorPaletteId[];
  features?: FeatureId[];
}

const AIDesigner: React.FC<AIDesignerProps> = ({ onNavigate }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [preferences, setPreferences] = useState<AIPreferences>({});

  const totalSteps = 4;

  const updatePreferences = <Key extends keyof AIPreferences>(
    key: Key,
    value: AIPreferences[Key]
  ) => {
    setPreferences((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

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

  const handleGenerate = () => {
    localStorage.setItem("aiPreferences", JSON.stringify(preferences));
    onNavigate("3d");
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
          <div className="step-content">{renderStep()}</div>

          <div
            className={`action-buttons ${
              currentStep === 4 ? "summary-spacing" : ""
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
                className="continue-button enabled"
                onClick={handleGenerate}
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
