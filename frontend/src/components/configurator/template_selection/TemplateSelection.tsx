import React, { useState } from 'react';
import { useGLTF } from '@react-three/drei';
import Template3DPreview from './Template3DPreview';
import './TemplateSelection.css';

interface TemplateSelectionProps {
  onNavigate: (view: string) => void;
}

interface Template {
  id: number;
  name: string;
  preview: string;
  roomData: {
    width: number;
    height: number;
    depth: number;
    fixtures: Array<{
      type: 'bathtub' | 'sink' | 'toilet' | 'shower' | 'window' | 'door';
      position: { x: number; y: number; z: number };
      rotation: { x: number; y: number; z: number };
      scale: { x: number; y: number; z: number };
    }>;
  };
}

const templates: Template[] = [
  {
    id: 1,
    name: 'Empty Room',
    preview: '/assets/empty_room.glb',
    roomData: {
      width: 300,
      height: 200,
      depth: 200,
      fixtures: [
        {
          type: 'door',
          position: { x: -120, y: 0, z: 70 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        },
        {
          type: 'window',
          position: { x: 120, y: 80, z: -30 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        }
      ]
    }
  }
];

useGLTF.preload('/assets/empty_room.glb');

const TemplateSelection: React.FC<TemplateSelectionProps> = ({ onNavigate }) => {
  const [selectedTemplate, setSelectedTemplate] = useState<Template | null>(null);
  const [hoveredTemplate, setHoveredTemplate] = useState<Template | null>(null);

  const handleTemplateSelect = (template: Template) => {
    setSelectedTemplate(template);
  };

  const handleStartFurnishing = () => {
    if (selectedTemplate) {
      localStorage.setItem('selectedTemplate', JSON.stringify(selectedTemplate));
      onNavigate('3d');
    }
  };

  const previewTemplate = hoveredTemplate || selectedTemplate;

  return (
    <div className="template-selection">
      <div className="template-content">
        <div className="template-left">
          <div className="page-title">
            <h1 className="step-number">Template Selection</h1>
            <h2 className="step-title">Choose your room shape</h2>
          </div>
          
          <div className="template-grid">
            {templates.map((template) => (
              <div
                key={template.id}
                className={`template-card ${selectedTemplate?.id === template.id ? 'selected' : ''}`}
                onClick={() => handleTemplateSelect(template)}
                onMouseEnter={() => setHoveredTemplate(template)}
                onMouseLeave={() => setHoveredTemplate(null)}
              >
                <div className="template-preview">
                  <div className="template-icon">
                    <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
                    </svg>
                  </div>
                </div>
                <div className="template-name">{template.name}</div>
              </div>
            ))}
          </div>

          <div className="action-buttons">
            <button 
              className="go-back-button"
              onClick={() => onNavigate('planner')}
            >
              Go back
            </button>
            <button 
              className={`start-furnishing-button ${selectedTemplate ? 'enabled' : 'disabled'}`}
              onClick={handleStartFurnishing}
              disabled={!selectedTemplate}
            >
              Start Furnishing
            </button>
          </div>
        </div>

        <div className="template-right">
          <div className="preview-container">
            <div className="room-3d-preview">
              {previewTemplate ? (
                <Template3DPreview template={previewTemplate} />
              ) : (
                <div className="no-preview">
                  <span>Select a template to see preview</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TemplateSelection;