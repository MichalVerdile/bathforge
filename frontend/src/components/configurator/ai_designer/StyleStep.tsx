import React from "react";
import type { StyleId } from "./data";
import { STYLE_OPTIONS } from "./data";

interface StyleStepProps {
  selectedStyle?: StyleId;
  onStyleSelect: (style: StyleId) => void;
}

const StyleStep: React.FC<StyleStepProps> = ({
  selectedStyle,
  onStyleSelect,
}) => {
  return (
    <div className="style-step">
      <p className="step-description">
        Choose the design style that best fits your vision for the perfect
        bathroom
      </p>
      <div className="styles-grid">
        {STYLE_OPTIONS.map((style) => (
          <div
            key={style.id}
            className={`style-card ${
              selectedStyle === style.id ? "selected" : ""
            }`}
            onClick={() => onStyleSelect(style.id)}
          >
            <div className="style-icon">{style.icon}</div>
            <h3 className="style-name">{style.name}</h3>
            <p className="style-description">{style.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default StyleStep;
