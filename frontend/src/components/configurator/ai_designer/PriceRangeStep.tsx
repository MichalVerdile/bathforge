import React from "react";
import type { PriceRangeId } from "./data";
import { PRICE_RANGE_OPTIONS } from "./data";

interface PriceRangeStepProps {
  selectedPriceRange?: PriceRangeId;
  onPriceRangeSelect: (priceRange: PriceRangeId) => void;
}

const PriceRangeStep: React.FC<PriceRangeStepProps> = ({
  selectedPriceRange,
  onPriceRangeSelect,
}) => {
  return (
    <div className="price-range-step">
      <p className="step-description">
        Choose your budget level to get product recommendations that match your price expectations
      </p>
      <div className="styles-grid">
        {PRICE_RANGE_OPTIONS.map((priceRange) => (
          <div
            key={priceRange.id}
            className={`style-card ${
              selectedPriceRange === priceRange.id ? "selected" : ""
            }`}
            onClick={() => onPriceRangeSelect(priceRange.id)}
          >
            <div className="style-icon">{priceRange.icon}</div>
            <h3 className="style-name">{priceRange.name}</h3>
            <p className="style-description">{priceRange.description}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PriceRangeStep;
