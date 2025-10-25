import React, { useState, useEffect } from 'react';
import { useModelData } from '../../../hooks/useModelData';
import { ModelItem, ModelCategory } from '../../../types/api';

interface ModelBrowserProps {
  onModelSelect: (model: ModelItem) => void;
  selectedModel?: ModelItem | null;
  style?: React.CSSProperties;
}

const getCategoryIcon = (categoryName: string): string => {
  const iconMap: { [key: string]: string } = {
    'bathtubs': '🛁',
    'basins': '🚰',
    'wcs': '🚽',
    'shower': '🚿',
    'furniture': '🪑',
    'accessories': '🧴',
    'fittings': '🔧',
    'coverings': '🪟',
    'towel_radiators': '🔥',
    'fittings_bathtubs': '⚙️'
  };
  return iconMap[categoryName.toLowerCase()] || '📦';
};

const getProductIcon = (category: string): string => {
  const iconMap: { [key: string]: string } = {
    'bathtub': '🛁',
    'bathtubs': '🛁',
    'basin': '🚰',
    'basins': '🚰',
    'toilet': '🚽',
    'wcs': '🚽',
    'shower': '🚿',
    'furniture': '🪑',
    'accessoire': '🧴',
    'accessories': '🧴',
    'fitting': '🔧',
    'fittings': '🔧',
    'covering': '🪟',
    'coverings': '🪟',
    'towel_radiator': '🔥',
    'towel_radiators': '🔥'
  };
  return iconMap[category.toLowerCase()] || '📦';
};

export default function ModelBrowser({ onModelSelect, selectedModel, style }: ModelBrowserProps) {
  const { categories, loading, error, refresh } = useModelData();
  const [selectedCategory, setSelectedCategory] = useState<string>('');

  useEffect(() => {
    if (categories.length > 0 && !selectedCategory) {
      setSelectedCategory(categories[0].name);
    }
  }, [categories, selectedCategory, loading, error]);

  const currentCategory = categories.find(cat => cat.name === selectedCategory);
  const filteredModels = currentCategory?.models || [];

  if (loading) {
    return (
      <div className="loading-state" style={style}>
        <div className="loading-content">
          <div className="state-icon">⚙️</div>
          <div className="state-title">Loading Models</div>
          <div>Please wait while we load your 3D models...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-state" style={style}>
        <div className="error-content">
          <div className="state-icon">❌</div>
          <div className="state-title">Failed to Load Models</div>
          <div className="state-description">{error}</div>
          <button className="retry-button" onClick={refresh}>
            Try Again
          </button>
        </div>
      </div>
    );
  }

  if (categories.length === 0) {
    return (
      <div className="empty-state" style={style}>
        <div className="empty-content">
          <div className="state-icon">📦</div>
          <div className="state-title">No Models Available</div>
          <div className="state-description">
            3D models need to be imported first
          </div>
          <div style={{ fontSize: '12px', color: '#475569', marginTop: '8px' }}>
            Use the admin panel to scan assets
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="model-browser" style={style}>
      <div className="model-browser-header">
        <div className="model-browser-title">
          <span>🛁 Product Browser</span>
          <button
            className="refresh-button"
            onClick={refresh}
            title="Refresh models"
          >
            🔄
          </button>
        </div>
      </div>

      <div className="category-tabs">
        {categories.map((category) => (
          <button
            key={category.name}
            className={`category-tab ${selectedCategory === category.name ? 'active' : ''}`}
            onClick={() => setSelectedCategory(category.name)}
            data-tooltip={category.displayName}
            title={category.displayName}
          >
            <span className="category-icon">{getCategoryIcon(category.name)}</span>
          </button>
        ))}
      </div>

      <div className="model-list">
        {filteredModels.length === 0 ? (
          <div className="no-models-message">
            No products in this category.
          </div>
        ) : (
          filteredModels.map((model) => (
            <div
              key={model.id}
              className={`model-item ${selectedModel?.id === model.id ? 'selected' : ''}`}
              onClick={() => onModelSelect(model)}
            >
              <div className="model-item-preview">
                {model.thumbnail ? (
                  <img 
                    src={model.thumbnail} 
                    alt={model.name}
                    className="model-item-image"
                    onError={(e) => {
                      const target = e.target as HTMLImageElement;
                      target.style.display = 'none';
                      const placeholder = target.nextElementSibling as HTMLElement;
                      if (placeholder) placeholder.style.display = 'flex';
                    }}
                  />
                ) : null}
                <div 
                  className="model-item-placeholder"
                  style={{ 
                    display: model.thumbnail ? 'none' : 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    width: '100%',
                    height: '100%',
                    background: `linear-gradient(135deg, #1e293b 0%, #0f172a 100%)`
                  }}
                >
                  {getProductIcon(model.category)}
                </div>
                
                <div style={{
                  position: 'absolute',
                  top: '6px',
                  right: '6px',
                  background: 'rgba(148, 163, 184, 0.9)',
                  color: '#0f172a',
                  padding: '2px 6px',
                  borderRadius: '4px',
                  fontSize: '9px',
                  fontWeight: '600',
                  textTransform: 'uppercase'
                }}>
                  3D
                </div>
                
                <button 
                  className="add-to-room-button"
                  onClick={(e) => {
                    e.stopPropagation();
                    onModelSelect(model);
                  }}
                >
                  + Add to Room
                </button>
              </div>

              <div className="model-item-info">
                <div className="model-item-name">
                  {model.name}
                </div>
                <div className="model-item-details">
                  {model.priceRange} • {model.mountingType}
                </div>
                {model.availableColors && model.availableColors.length > 0 && (
                  <div className="model-item-colors">
                    {model.availableColors.length} color{model.availableColors.length > 1 ? 's' : ''}
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      <div className="model-browser-footer">
        {filteredModels.length} product(s) available
        {currentCategory && (
          <div style={{ marginTop: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span>{getCategoryIcon(currentCategory.name)}</span>
            <span>{currentCategory.displayName}</span>
          </div>
        )}
      </div>
    </div>
  );
}

export type { ModelItem, ModelCategory };