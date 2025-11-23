import React from "react";
import { AIPreferences } from "./AIDesigner";
import { COLOR_LABELS, FEATURE_LABELS, STYLE_LABELS } from "./data";

interface SummaryStepProps {
  preferences: AIPreferences;
}

const formatSelection = <Id extends string>(
  ids: Id[] | undefined,
  labels: Record<Id, string>
): string => {
  if (!ids || ids.length === 0) {
    return "Not selected";
  }

  return ids
    .map((id) => labels[id] ?? id)
    .filter(Boolean)
    .join(", ");
};

const SummaryStep: React.FC<SummaryStepProps> = ({ preferences }) => {
  const styleName =
    preferences.style && STYLE_LABELS[preferences.style]
      ? STYLE_LABELS[preferences.style]
      : "Not selected";

  return (
    <div className="summary-step">
      <div className="summary-content">
        <div className="summary-section">
          <h3 className="summary-section-title">Style</h3>
          <p className="summary-section-value">{styleName}</p>
        </div>

        <div className="summary-section">
          <h3 className="summary-section-title">Color Palettes</h3>
          <p className="summary-section-value">
            {formatSelection(preferences.colors, COLOR_LABELS)}
          </p>
        </div>

        <div className="summary-section">
          <h3 className="summary-section-title">Features</h3>
          <p className="summary-section-value">
            {formatSelection(preferences.features, FEATURE_LABELS)}
          </p>
        </div>

        <div className="summary-section">
          <h3 className="summary-section-title">Room Configuration</h3>
          <p className="summary-section-value">
            {preferences.room && preferences.room.vertices.length > 0
              ? `Custom room shape with ${preferences.room.vertices.length} corners, Height: ${preferences.room.height}m`
              : "Default room shape"}
          </p>
        </div>
      </div>

      <div className="summary-note">
        <p>
          Our AI will use these preferences and your custom room shape to design a bathroom 
          perfectly tailored to your space and requirements. You'll be able to further 
          customize it in the 3D viewer.
        </p>
      </div>
    </div>
  );
};

export default SummaryStep;
