import React from 'react';
import { ModelItem } from '../model_browser/ModelBrowser';
import './CoveringSelector.css';

interface CoveringSelectorProps {
  coverings: ModelItem[];
  selectedCovering?: ModelItem | null;
  onSelect: (covering: ModelItem) => void;
  onClose: () => void;
  surfaceType: 'wall' | 'floor' | null;
}

export function CoveringSelector({
  coverings,
  selectedCovering,
  onSelect,
  onClose,
  surfaceType
}: CoveringSelectorProps) {
  if (!surfaceType) return null;

  return (
    <div className="covering-selector-panel">
      <div className="covering-panel-header">
        <h3>
          {surfaceType === 'wall' ? '🧱 Wall' : '⬜ Floor'} Covering
        </h3>
        <button className="covering-close-button" onClick={onClose} title="Close">
          ✕
        </button>
      </div>

      <div className="covering-panel-content">
        <p className="covering-instruction">
          Select a texture to apply to the {surfaceType}
        </p>

        <div className="covering-grid">
          {coverings.length === 0 ? (
            <div className="no-coverings-message">
              <p>No coverings available</p>
              <p className="hint">Check your backend connection</p>
            </div>
          ) : (
            coverings.map((covering) => (
              <div
                key={covering.id}
                className={`covering-item ${
                  selectedCovering?.id === covering.id ? 'active' : ''
                }`}
                onClick={() => onSelect(covering)}
                title={covering.name}
              >
                <div className="covering-thumbnail">
                  {covering.thumbnail ? (
                    <img
                      src={covering.thumbnail}
                      alt={covering.name}
                      onError={(e) => {
                        // Fallback to model path if thumbnail fails
                        e.currentTarget.src = covering.url;
                      }}
                    />
                  ) : (
                    <div className="covering-placeholder">
                      <span>🎨</span>
                    </div>
                  )}
                </div>
                <div className="covering-name">{covering.name}</div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
