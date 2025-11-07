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
    name: 'Square Room',
    preview: '/assets/templates/square.glb',
    roomData: {
      width: 300,
      height: 200,
      depth: 300,
      fixtures: [
        {
          type: 'door',
          position: { x: -150, y: 0, z: 0 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        },
        {
          type: 'window',
          position: { x: 0, y: 80, z: -150 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        }
      ]
    }
  },
  {
    id: 2,
    name: 'Rectangle Room',
    preview: '/assets/templates/rectangle.glb',
    roomData: {
      width: 400,
      height: 200,
      depth: 250,
      fixtures: [
        {
          type: 'door',
          position: { x: -200, y: 0, z: 0 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        },
        {
          type: 'window',
          position: { x: 0, y: 80, z: -125 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        }
      ]
    }
  },
  {
    id: 3,
    name: 'L-Shape Room',
    preview: '/assets/templates/L-shape.glb',
    roomData: {
      width: 350,
      height: 200,
      depth: 350,
      fixtures: [
        {
          type: 'door',
          position: { x: -175, y: 0, z: 100 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        },
        {
          type: 'window',
          position: { x: 100, y: 80, z: -175 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        }
      ]
    }
  },
  {
    id: 4,
    name: 'Trapezoid Room',
    preview: '/assets/templates/trapezoid.glb',
    roomData: {
      width: 320,
      height: 200,
      depth: 280,
      fixtures: [
        {
          type: 'door',
          position: { x: -160, y: 0, z: 50 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        },
        {
          type: 'window',
          position: { x: 80, y: 80, z: -140 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 }
        }
      ]
    }
  }
];

// Preload all template models
useGLTF.preload('/assets/templates/square.glb');
useGLTF.preload('/assets/templates/rectangle.glb');
useGLTF.preload('/assets/templates/L-shape.glb');
useGLTF.preload('/assets/templates/trapezoid.glb');

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