import React, { useState } from 'react';
import { useGLTF } from '@react-three/drei';
import Template3DPreview from './Template3DPreview';
import './TemplateSelection.css';
import {
  RoomOpenings,
  DEFAULT_DOOR_WIDTH,
  DEFAULT_DOOR_HEIGHT,
  DEFAULT_WINDOW_WIDTH,
  DEFAULT_WINDOW_HEIGHT,
  DEFAULT_WINDOW_ELEVATION,
} from '../custom_room/DoorWindowTypes';

interface TemplateSelectionProps {
  onNavigate: (view: string) => void;
}

interface Template {
  id: number;
  name: string;
  preview: string;
  roomData: {
    height: number;
    vertices: Array<{ x: number; y: number }>;
    openings: RoomOpenings;
  };
}

const templates: Template[] = [
  {
    id: 1,
    name: 'Square Room',
    preview: '/assets/templates/square.glb',
    roomData: {
      height: 200,
      vertices: [
        { x: 0, y: 0 },
        { x: 300, y: 0 },
        { x: 300, y: 300 },
        { x: 0, y: 300 }
      ],
      openings: {
        doors: [
          {
            id: 'door_square_1',
            wallIndex: 0, // Bottom wall
            position: 0.5, // Center
            width: DEFAULT_DOOR_WIDTH,
            height: DEFAULT_DOOR_HEIGHT,
          },
        ],
        windows: [
          {
            id: 'window_square_1',
            wallIndex: 1, // Right wall
            position: 0.5,
            width: DEFAULT_WINDOW_WIDTH,
            height: DEFAULT_WINDOW_HEIGHT,
            elevation: DEFAULT_WINDOW_ELEVATION,
          },
        ],
      },
    }
  },
  {
    id: 2,
    name: 'Rectangle Room',
    preview: '/assets/templates/rectangle.glb',
    roomData: {
      height: 200,
      vertices: [
        { x: 0, y: 0 },
        { x: 400, y: 0 },
        { x: 400, y: 250 },
        { x: 0, y: 250 }
      ],
      openings: {
        doors: [
          {
            id: 'door_rect_1',
            wallIndex: 0, // Bottom wall (longest)
            position: 0.3, // Offset left
            width: DEFAULT_DOOR_WIDTH,
            height: DEFAULT_DOOR_HEIGHT,
          },
        ],
        windows: [
          {
            id: 'window_rect_1',
            wallIndex: 2, // Top wall
            position: 0.5,
            width: DEFAULT_WINDOW_WIDTH,
            height: DEFAULT_WINDOW_HEIGHT,
            elevation: DEFAULT_WINDOW_ELEVATION,
          },
        ],
      },
    }
  },
  {
    id: 3,
    name: 'L-Shape Room',
    preview: '/assets/templates/L-shape.glb',
    roomData: {
      height: 200,
      vertices: [
        { x: 0, y: 0 },
        { x: 350, y: 0 },
        { x: 350, y: 175 },
        { x: 175, y: 175 },
        { x: 175, y: 350 },
        { x: 0, y: 350 }
      ],
      openings: {
        doors: [
          {
            id: 'door_lshape_1',
            wallIndex: 0, // Bottom wall (longest)
            position: 0.5,
            width: DEFAULT_DOOR_WIDTH,
            height: DEFAULT_DOOR_HEIGHT,
          },
        ],
        windows: [
          {
            id: 'window_lshape_1',
            wallIndex: 5, // Left wall
            position: 0.5,
            width: DEFAULT_WINDOW_WIDTH,
            height: DEFAULT_WINDOW_HEIGHT,
            elevation: DEFAULT_WINDOW_ELEVATION,
          },
        ],
      },
    }
  },
  {
    id: 4,
    name: 'Trapezoid Room',
    preview: '/assets/templates/trapezoid.glb',
    roomData: {
      height: 200,
      vertices: [
        { x: 0, y: 0 },
        { x: 320, y: 0 },
        { x: 320, y: 200 },
        { x: 240, y: 280 },
        { x: 0, y: 280 }
      ],
      openings: {
        doors: [
          {
            id: 'door_trap_1',
            wallIndex: 0, // Bottom wall
            position: 0.5,
            width: DEFAULT_DOOR_WIDTH,
            height: DEFAULT_DOOR_HEIGHT,
          },
        ],
        windows: [
          {
            id: 'window_trap_1',
            wallIndex: 1, // Right wall
            position: 0.5,
            width: DEFAULT_WINDOW_WIDTH,
            height: DEFAULT_WINDOW_HEIGHT,
            elevation: DEFAULT_WINDOW_ELEVATION,
          },
        ],
      },
    }
  }
];

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