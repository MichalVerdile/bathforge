import React from "react";
import type { ColorPaletteId } from "./data";
import { COLOR_PALETTES } from "./data";

interface ColorStepProps {
  selectedColors: ColorPaletteId[];
  onColorsSelect: (colors: ColorPaletteId[]) => void;
}

const swatchOrder: Array<keyof (typeof COLOR_PALETTES)[number]["colors"]> = [
  "primary",
  "secondary",
  "accent1",
  "accent2",
];

const ColorStep: React.FC<ColorStepProps> = ({
  selectedColors,
  onColorsSelect,
}) => {
  const selectColor = (paletteId: ColorPaletteId) => {
    onColorsSelect([paletteId]);
  };

  const handleKeyDown = (
    event: React.KeyboardEvent<HTMLDivElement>,
    paletteId: ColorPaletteId
  ) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      selectColor(paletteId);
    }
  };

  return (
    <div className="color-step">
      <p className="step-description">
        Choose a color palette for your bathroom
      </p>
      <div className="color-palettes-grid">
        {COLOR_PALETTES.map((palette) => {
          const isSelected = selectedColors.includes(palette.id);

          return (
            <div
              key={palette.id}
              className={`palette-card ${isSelected ? "selected" : ""}`}
              onClick={() => selectColor(palette.id)}
              onKeyDown={(event) => handleKeyDown(event, palette.id)}
              role="button"
              tabIndex={0}
              aria-pressed={isSelected}
              aria-label={`Select ${palette.name} palette`}
            >
              <div className="palette-preview">
                {swatchOrder.map((key) => (
                  <div
                    key={key}
                    className="palette-tone"
                    style={{ backgroundColor: palette.colors[key] }}
                  />
                ))}
              </div>
              <div className="palette-info">
                <h3 className="palette-name">{palette.name}</h3>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default ColorStep;
