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
    <div style={{
      width: '280px',
      background: '#fff',
      border: '1px solid #e9ecef',
      borderRadius: '8px',
      overflow: 'hidden',
      ...style
    }}>
      {/* Header */}
      <div 
        style={{
          padding: '12px 16px',
          background: '#f8f9fa',
          borderBottom: '1px solid #e9ecef',
          cursor: 'pointer',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <h4 style={{ margin: 0, fontSize: '16px', color: '#212529' }}>
          Scene Controls
        </h4>
        <span style={{ fontSize: '12px', color: '#6c757d' }}>
          {isExpanded ? '▼' : '▶'}
        </span>
      </div>

      {isExpanded && (
        <div style={{ padding: '16px' }}>
          {/* Quick Actions */}
          <div style={{ marginBottom: '20px' }}>
            <h5 style={{ margin: '0 0 12px 0', fontSize: '14px', color: '#495057' }}>
              Quick Actions
            </h5>
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <button
                onClick={onResetView}
                style={{
                  padding: '6px 12px',
                  border: '1px solid #007bff',
                  background: '#007bff',
                  color: '#fff',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '12px'
                }}
              >
                Reset View
              </button>
              <button
                onClick={onCenterModel}
                style={{
                  padding: '6px 12px',
                  border: '1px solid #6c757d',
                  background: '#fff',
                  color: '#6c757d',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '12px'
                }}
              >
                Center Model
              </button>
            </div>
          </div>

          {/* Position Controls */}
          <div style={{ marginBottom: '20px' }}>
            <h5 style={{ margin: '0 0 12px 0', fontSize: '14px', color: '#495057' }}>
              Position
            </h5>
            {(['x', 'y', 'z'] as const).map((axis) => (
              <div key={axis} style={{ marginBottom: '8px' }}>
                <label style={{ 
                  display: 'block', 
                  fontSize: '12px', 
                  color: '#6c757d', 
                  marginBottom: '4px',
                  textTransform: 'uppercase'
                }}>
                  {axis}: {controls.position[axis === 'x' ? 0 : axis === 'y' ? 1 : 2].toFixed(2)}
                </label>
                <input
                  type="range"
                  min="-10"
                  max="10"
                  step="0.1"
                  value={controls.position[axis === 'x' ? 0 : axis === 'y' ? 1 : 2]}
                  onChange={(e) => handlePositionChange(axis, parseFloat(e.target.value))}
                  style={{ width: '100%' }}
                />
              </div>
            ))}
          </div>

          {/* Rotation Controls */}
          <div style={{ marginBottom: '20px' }}>
            <h5 style={{ margin: '0 0 12px 0', fontSize: '14px', color: '#495057' }}>
              Rotation (degrees)
            </h5>
            {(['x', 'y', 'z'] as const).map((axis) => (
              <div key={axis} style={{ marginBottom: '8px' }}>
                <label style={{ 
                  display: 'block', 
                  fontSize: '12px', 
                  color: '#6c757d', 
                  marginBottom: '4px',
                  textTransform: 'uppercase'
                }}>
                  {axis}: {((controls.rotation[axis === 'x' ? 0 : axis === 'y' ? 1 : 2] * 180) / Math.PI).toFixed(0)}°
                </label>
                <input
                  type="range"
                  min="-180"
                  max="180"
                  step="5"
                  value={(controls.rotation[axis === 'x' ? 0 : axis === 'y' ? 1 : 2] * 180) / Math.PI}
                  onChange={(e) => handleRotationChange(axis, parseFloat(e.target.value))}
                  style={{ width: '100%' }}
                />
              </div>
            ))}
          </div>

          {/* Scale Control */}
          <div style={{ marginBottom: '20px' }}>
            <h5 style={{ margin: '0 0 12px 0', fontSize: '14px', color: '#495057' }}>
              Scale: {controls.scale.toFixed(2)}x
            </h5>
            <input
              type="range"
              min="0.1"
              max="5"
              step="0.1"
              value={controls.scale}
              onChange={(e) => handleScaleChange(parseFloat(e.target.value))}
              style={{ width: '100%' }}
            />
          </div>

          {/* Toggle Options */}
          <div>
            <h5 style={{ margin: '0 0 12px 0', fontSize: '14px', color: '#495057' }}>
              Options
            </h5>
            
            <label style={{ 
              display: 'flex', 
              alignItems: 'center', 
              marginBottom: '8px',
              cursor: 'pointer',
              fontSize: '14px'
            }}>
              <input
                type="checkbox"
                checked={controls.autoRotate}
                onChange={() => handleToggle('autoRotate')}
                style={{ marginRight: '8px' }}
              />
              Auto Rotate
            </label>

            <label style={{ 
              display: 'flex', 
              alignItems: 'center', 
              marginBottom: '8px',
              cursor: 'pointer',
              fontSize: '14px'
            }}>
              <input
                type="checkbox"
                checked={controls.showGrid}
                onChange={() => handleToggle('showGrid')}
                style={{ marginRight: '8px' }}
              />
              Show Grid
            </label>

            <label style={{ 
              display: 'flex', 
              alignItems: 'center', 
              marginBottom: '8px',
              cursor: 'pointer',
              fontSize: '14px'
            }}>
              <input
                type="checkbox"
                checked={controls.showEnvironment}
                onChange={() => handleToggle('showEnvironment')}
                style={{ marginRight: '8px' }}
              />
              Environment Lighting
            </label>
          </div>
        </div>
      )}
    </div>
  );
}