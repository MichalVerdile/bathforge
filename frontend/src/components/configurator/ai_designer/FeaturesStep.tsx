import React from "react";
import type { FeatureId } from "./data";
import { FEATURE_OPTIONS } from "./data";

interface FeaturesStepProps {
  selectedFeatures: FeatureId[];
  onFeaturesSelect: (features: FeatureId[]) => void;
}

const FeaturesStep: React.FC<FeaturesStepProps> = ({
  selectedFeatures,
  onFeaturesSelect,
}) => {
  const toggleFeature = (featureId: FeatureId) => {
    if (selectedFeatures.includes(featureId)) {
      onFeaturesSelect(selectedFeatures.filter((id) => id !== featureId));
      return;
    }

    onFeaturesSelect([...selectedFeatures, featureId]);
  };

  return (
    <div className="features-step">
      <p className="step-description">
        Select the essential features you want in your bathroom
      </p>
      <div className="features-grid">
        {FEATURE_OPTIONS.map((feature) => (
          <div
            key={feature.id}
            className={`feature-card ${
              selectedFeatures.includes(feature.id) ? "selected" : ""
            }`}
            onClick={() => toggleFeature(feature.id)}
          >
            <div className="feature-icon">
              {(() => {
                const Icon = feature.icon as React.ComponentType<{
                  size?: number;
                }>;
                return <Icon size={28} />;
              })()}
            </div>
            <h3 className="feature-name">{feature.name}</h3>
            <p className="feature-description">{feature.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default FeaturesStep;
