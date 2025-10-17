import React, { useState } from 'react';

export interface ModelItem {
  name: string;
  url: string;
  category: string;
  thumbnail?: string;
}

export interface ModelCategory {
  name: string;
  displayName: string;
  models: ModelItem[];
}

export const MODEL_CATEGORIES: ModelCategory[] = [
  {
    name: 'basins',
    displayName: 'Basins & Sinks',
    models: [
      { name: '115 Lavabo', url: '/assets/basins/115_lavabo.glb', category: 'basins' },
      { name: '85 Lavabo', url: '/assets/basins/85_lavabo.glb', category: 'basins' },
      { name: 'Delfopianoret 113', url: '/assets/basins/Delfopianoret_113.glb', category: 'basins' },
      { name: 'Delfopianoret 152', url: '/assets/basins/Delfopianoret_152.glb', category: 'basins' },
      { name: 'Filo Doghe', url: '/assets/basins/filo_doghe.glb', category: 'basins' },
      { name: 'Filo Mensola', url: '/assets/basins/filo_mensola.glb', category: 'basins' },
      { name: 'ITFREEC-ITFREEP', url: '/assets/basins/ITFREEC-ITFREEP.glb', category: 'basins' },
      { name: 'ITMT128DX ITMT128SX', url: '/assets/basins/ITMT128DX_ITMT128SX.glb', category: 'basins' },
      { name: 'Kanto87', url: '/assets/basins/kanto87.glb', category: 'basins' },
      { name: 'Narciso Mini Lavabo', url: '/assets/basins/narciso_mini_lavabo.glb', category: 'basins' },
      { name: 'OTLS OTCOL', url: '/assets/basins/OTLS_OTCOL.glb', category: 'basins' },
      { name: 'Siwa Cassetto Specchio Rettangolare', url: '/assets/basins/siwa_cassetto_specchiorettangolare.glb', category: 'basins' },
      { name: 'Siwa Cassetto Specchio Tondo', url: '/assets/basins/siwa_cassetto_specchiotondo.glb', category: 'basins' },
      { name: 'Tiberino', url: '/assets/basins/Tiberino.glb', category: 'basins' },
    ]
  },
  {
    name: 'bathtubs',
    displayName: 'Bathtubs',
    models: [
      { name: '1 Frontale + 1 Laterale Stile 170x70', url: '/assets/bathtubs/1 FRONTALE + 1 LATERALE STILE 170X70.glb', category: 'bathtubs' },
      { name: '1 Frontale + 2 Laterali Stile 170x70', url: '/assets/bathtubs/1 FRONTALE + 2 LATERALI STILE 170X70.glb', category: 'bathtubs' },
    ]
  },
  {
    name: 'accessories',
    displayName: 'Accessories',
    models: [
      { name: 'Shelf Hook', url: '/assets/accessoires/AINAP00001_ACCESSORIES_interlude_14x14x17_shelf_hook.glb', category: 'accessories' },
      { name: 'Hook', url: '/assets/accessoires/AINAP01001_ACCESSORIES_interlude_hook.glb', category: 'accessories' },
      { name: 'Shelf 14x14x17', url: '/assets/accessoires/AINMN00001_ACCESSORIES_interlude_14x14x17_shelf.glb', category: 'accessories' },
      { name: 'Shelf 28x14x15', url: '/assets/accessoires/AINMN00005_ACCESSORIES_interlude_28x14x15_shelf.glb', category: 'accessories' },
      { name: 'Wood Shelf 18x11x1', url: '/assets/accessoires/AINMN01002_ACCESSORIES_interlude_18x11x1_shelf_wood.glb', category: 'accessories' },
      { name: 'Large Shelf 80x16x40', url: '/assets/accessoires/AINMN02001_ACCESSORIES_interlude_80x16x40_shelf.glb', category: 'accessories' },
      { name: 'Paper Holder', url: '/assets/accessoires/AINPR00001_ACCESSORIES_interlude_14x14x17_shelf_paper holder.glb', category: 'accessories' },
    ]
  }
];

interface ModelBrowserProps {
  onModelSelect: (model: ModelItem) => void;
  selectedModel?: ModelItem | null;
  style?: React.CSSProperties;
}

export default function ModelBrowser({ onModelSelect, selectedModel, style }: ModelBrowserProps) {
  const [selectedCategory, setSelectedCategory] = useState<string>('basins');
  const [searchTerm, setSearchTerm] = useState<string>('');

  const currentCategory = MODEL_CATEGORIES.find(cat => cat.name === selectedCategory);
  const filteredModels = currentCategory?.models.filter(model =>
    model.name.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

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
        <h3 style={{ margin: '0 0 12px 0', fontSize: '18px', color: '#212529' }}>
          3D Model Browser
        </h3>
        
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
        overflowX: 'auto'
      }}>
        {MODEL_CATEGORIES.map((category) => (
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
              borderBottom: selectedCategory === category.name ? '2px solid #007bff' : 'none'
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
          filteredModels.map((model, index) => (
            <div
              key={index}
              onClick={() => onModelSelect(model)}
              style={{
                padding: '12px',
                margin: '4px 0',
                background: selectedModel?.name === model.name ? '#e3f2fd' : '#fff',
                border: selectedModel?.name === model.name ? '2px solid #2196f3' : '1px solid #e9ecef',
                borderRadius: '6px',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                fontSize: '14px'
              }}
              onMouseEnter={(e) => {
                if (selectedModel?.name !== model.name) {
                  e.currentTarget.style.background = '#f8f9fa';
                  e.currentTarget.style.borderColor = '#ced4da';
                }
              }}
              onMouseLeave={(e) => {
                if (selectedModel?.name !== model.name) {
                  e.currentTarget.style.background = '#fff';
                  e.currentTarget.style.borderColor = '#e9ecef';
                }
              }}
            >
              <div style={{ fontWeight: '500', color: '#212529', marginBottom: '4px' }}>
                {model.name}
              </div>
              <div style={{ fontSize: '12px', color: '#6c757d' }}>
                {model.category} • GLB Model
              </div>
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
      </div>
    </div>
  );
}