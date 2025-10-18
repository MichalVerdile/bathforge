import React, { useState, useEffect } from 'react';
import { useModelData } from '../../hooks/useModelData';
import { ModelItem, ModelCategory } from '../../types/api';

interface ModelBrowserProps {
  onModelSelect: (model: ModelItem) => void;
  selectedModel?: ModelItem | null;
  style?: React.CSSProperties;
}

export default function ModelBrowser({ onModelSelect, selectedModel, style }: ModelBrowserProps) {
  const { categories, loading, error, refresh } = useModelData();
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [searchTerm, setSearchTerm] = useState<string>('');

  // Set default category when categories are loaded
  useEffect(() => {
    if (categories.length > 0 && !selectedCategory) {
      setSelectedCategory(categories[0].name);
    }
  }, [categories, selectedCategory]);

  const currentCategory = categories.find(cat => cat.name === selectedCategory);
  const filteredModels = currentCategory?.models.filter(model =>
    model.name.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  if (loading) {
    return (
      <div style={{
        width: '300px',
        height: '100%',
        background: '#f8f9fa',
        border: '1px solid #e9ecef',
        borderRadius: '8px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        ...style
      }}>
        <div style={{ textAlign: 'center', color: '#6c757d' }}>
          <div style={{ marginBottom: '12px', fontSize: '18px' }}>⚙️</div>
          <div>Loading models...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{
        width: '300px',
        height: '100%',
        background: '#f8f9fa',
        border: '1px solid #e9ecef',
        borderRadius: '8px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '20px',
        ...style
      }}>
        <div style={{ textAlign: 'center', color: '#dc3545' }}>
          <div style={{ marginBottom: '12px', fontSize: '18px' }}>❌</div>
          <div style={{ marginBottom: '12px', fontWeight: 'bold' }}>Failed to load models</div>
          <div style={{ fontSize: '14px', marginBottom: '16px' }}>{error}</div>
          <button
            onClick={refresh}
            style={{
              padding: '8px 16px',
              background: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (categories.length === 0) {
    return (
      <div style={{
        width: '300px',
        height: '100%',
        background: '#f8f9fa',
        border: '1px solid #e9ecef',
        borderRadius: '8px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '20px',
        ...style
      }}>
        <div style={{ textAlign: 'center', color: '#6c757d' }}>
          <div style={{ marginBottom: '12px', fontSize: '18px' }}>📦</div>
          <div style={{ marginBottom: '12px', fontWeight: 'bold' }}>No models available</div>
          <div style={{ fontSize: '14px', marginBottom: '16px' }}>
            Models need to be imported from assets first
          </div>
          <div style={{ fontSize: '12px', color: '#adb5bd' }}>
            Use the admin panel to scan assets
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      width: '300px',
      height: '100%',
      background: '#f8f9fa',
      border: '1px solid #e9ecef',
      borderRadius: '8px',
      overflow: 'hidden',
      display: 'flex',
      flexDirection: 'column',
      ...style
    }}>
      {/* Header */}
      <div style={{
        padding: '16px',
        borderBottom: '1px solid #e9ecef',
        background: '#fff'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
          <h3 style={{ margin: 0, fontSize: '18px', color: '#212529' }}>
            3D Model Browser
          </h3>
          <button
            onClick={refresh}
            style={{
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              fontSize: '16px',
              color: '#6c757d',
              padding: '4px'
            }}
            title="Refresh models"
          >
            🔄
          </button>
        </div>
        
        {/* Search */}
        <input
          type="text"
          placeholder="Search models..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{
            width: '100%',
            padding: '8px 12px',
            border: '1px solid #ced4da',
            borderRadius: '4px',
            fontSize: '14px',
            boxSizing: 'border-box'
          }}
        />
      </div>

      {/* Category tabs */}
      <div style={{
        display: 'flex',
        background: '#fff',
        borderBottom: '1px solid #e9ecef',
        overflowX: 'auto',
        flexWrap: 'wrap'
      }}>
        {categories.map((category) => (
          <button
            key={category.name}
            onClick={() => setSelectedCategory(category.name)}
            style={{
              padding: '12px 16px',
              border: 'none',
              background: selectedCategory === category.name ? '#007bff' : 'transparent',
              color: selectedCategory === category.name ? '#fff' : '#6c757d',
              cursor: 'pointer',
              fontSize: '14px',
              whiteSpace: 'nowrap',
              borderBottom: selectedCategory === category.name ? '2px solid #007bff' : 'none',
              flexShrink: 0
            }}
          >
            {category.displayName}
          </button>
        ))}
      </div>

      {/* Model list */}
      <div style={{
        flex: 1,
        overflow: 'auto',
        padding: '8px'
      }}>
        {filteredModels.length === 0 ? (
          <div style={{
            padding: '20px',
            textAlign: 'center',
            color: '#6c757d',
            fontSize: '14px'
          }}>
            {searchTerm ? 'No models found matching your search.' : 'No models in this category.'}
          </div>
        ) : (
          filteredModels.map((model) => (
            <div
              key={model.id}
              onClick={() => onModelSelect(model)}
              style={{
                padding: '12px',
                margin: '4px 0',
                background: selectedModel?.id === model.id ? '#e3f2fd' : '#fff',
                border: selectedModel?.id === model.id ? '2px solid #2196f3' : '1px solid #e9ecef',
                borderRadius: '6px',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                fontSize: '14px'
              }}
              onMouseEnter={(e) => {
                if (selectedModel?.id !== model.id) {
                  e.currentTarget.style.background = '#f8f9fa';
                  e.currentTarget.style.borderColor = '#ced4da';
                }
              }}
              onMouseLeave={(e) => {
                if (selectedModel?.id !== model.id) {
                  e.currentTarget.style.background = '#fff';
                  e.currentTarget.style.borderColor = '#e9ecef';
                }
              }}
            >
              <div style={{ fontWeight: '500', color: '#212529', marginBottom: '4px' }}>
                {model.name}
              </div>
              <div style={{ fontSize: '12px', color: '#6c757d', marginBottom: '4px' }}>
                {model.category} • {model.priceRange} • {model.mountingType}
              </div>
              {model.availableColors && model.availableColors.length > 0 && (
                <div style={{ fontSize: '11px', color: '#adb5bd' }}>
                  {model.availableColors.length} color{model.availableColors.length > 1 ? 's' : ''} available
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* Footer */}
      <div style={{
        padding: '12px 16px',
        borderTop: '1px solid #e9ecef',
        background: '#f8f9fa',
        fontSize: '12px',
        color: '#6c757d'
      }}>
        {filteredModels.length} model(s) available
        {currentCategory && (
          <div style={{ marginTop: '4px' }}>
            Category: {currentCategory.displayName}
          </div>
        )}
      </div>
    </div>
  );
}

export type { ModelItem, ModelCategory };