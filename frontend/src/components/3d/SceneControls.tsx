import React, { useState } from 'react';

export interface SceneControlsState {
  position: [number, number, number];
  rotation: [number, number, number];
  scale: number;
  autoRotate: boolean;
  wireframe: boolean;
  showGrid: boolean;
  showEnvironment: boolean;
}

interface SceneControlsProps {
  controls: SceneControlsState;
  onControlsChange: (controls: SceneControlsState) => void;
  onResetView: () => void;
  onCenterModel: () => void;
  style?: React.CSSProperties;
}

export default function SceneControls({
  controls,
  onControlsChange,
  onResetView,
  onCenterModel,
  style
}: SceneControlsProps) {
  const [isExpanded, setIsExpanded] = useState(true);

  const handlePositionChange = (axis: 'x' | 'y' | 'z', value: number) => {
    const newPosition = [...controls.position] as [number, number, number];
    const axisIndex = axis === 'x' ? 0 : axis === 'y' ? 1 : 2;
    newPosition[axisIndex] = value;
    onControlsChange({ ...controls, position: newPosition });
  };

  const handleRotationChange = (axis: 'x' | 'y' | 'z', value: number) => {
    const newRotation = [...controls.rotation] as [number, number, number];
    const axisIndex = axis === 'x' ? 0 : axis === 'y' ? 1 : 2;
    newRotation[axisIndex] = (value * Math.PI) / 180;
    onControlsChange({ ...controls, rotation: newRotation });
  };

  const handleScaleChange = (value: number) => {
    onControlsChange({ ...controls, scale: value });
  };

  const handleToggle = (property: keyof SceneControlsState) => {
    onControlsChange({ ...controls, [property]: !controls[property] });
  };

  return (
    <div className="scene-controls" style={style}>
      {/* Header */}
      <div 
        className="controls-header"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <h4 className="controls-title">
          ⚙️ View Controls
        </h4>
        <span className="controls-toggle">
          {isExpanded ? '▼' : '▶'}
        </span>
      </div>

      {isExpanded && (
        <div className="controls-content">
          {/* Quick Actions */}
          <div className="controls-section">
            <h5 className="controls-section-title">
              Quick Actions
            </h5>
            <div className="quick-actions">
              <button
                className="action-button"
                onClick={onResetView}
              >
                🔄 Reset View
              </button>
              <button
                className="action-button secondary"
                onClick={onCenterModel}
              >
                🎯 Center
              </button>
            </div>
          </div>

          {/* Simple Scale Control */}
          <div className="controls-section">
            <h5 className="controls-section-title">
              Size: {controls.scale.toFixed(1)}x
            </h5>
            <div className="slider-control">
              <input
                className="slider-input"
                type="range"
                min="0.2"
                max="3"
                step="0.1"
                value={controls.scale}
                onChange={(e) => handleScaleChange(parseFloat(e.target.value))}
              />
            </div>
          </div>

          {/* Rotation - Y axis only for simplicity */}
          <div className="controls-section">
            <h5 className="controls-section-title">
              Rotate: {((controls.rotation[1] * 180) / Math.PI).toFixed(0)}°
            </h5>
            <div className="slider-control">
              <input
                className="slider-input"
                type="range"
                min="-180"
                max="180"
                step="10"
                value={(controls.rotation[1] * 180) / Math.PI}
                onChange={(e) => handleRotationChange('y', parseFloat(e.target.value))}
              />
            </div>
          </div>

          {/* Simple Options */}
          <div className="controls-section">
            <h5 className="controls-section-title">
              Display Options
            </h5>
            
            <label className="checkbox-control">
              <input
                className="checkbox-input"
                type="checkbox"
                checked={controls.autoRotate}
                onChange={() => handleToggle('autoRotate')}
              />
              Auto Rotate
            </label>

            <label className="checkbox-control">
              <input
                className="checkbox-input"
                type="checkbox"
                checked={controls.showGrid}
                onChange={() => handleToggle('showGrid')}
              />
              Show Grid
            </label>

            <label className="checkbox-control">
              <input
                className="checkbox-input"
                type="checkbox"
                checked={controls.showEnvironment}
                onChange={() => handleToggle('showEnvironment')}
              />
              Environment Lighting
            </label>
          </div>
        </div>
      )}
    </div>
  );
}