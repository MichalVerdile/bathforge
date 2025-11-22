import React, { useState } from "react";
import { useGLTF } from "@react-three/drei";
import Template3DPreview from "./Template3DPreview";
import "./TemplateSelection.css";

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
      type: "bathtub" | "sink" | "toilet" | "shower" | "window" | "door";
      position: { x: number; y: number; z: number };
      rotation: { x: number; y: number; z: number };
      scale: { x: number; y: number; z: number };
    }>;
  };
}

const templates: Template[] = [
  {
    id: 1,
    name: "Square",
    preview: "/assets/templates/square.glb",
    roomData: {
      width: 300,
      height: 200,
      depth: 300,
      fixtures: [
        {
          type: "door",
          position: { x: -150, y: 0, z: 0 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
        {
          type: "window",
          position: { x: 0, y: 80, z: -150 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
      ],
    },
  },
  {
    id: 2,
    name: "Rectangle",
    preview: "/assets/templates/rectangle.glb",
    roomData: {
      width: 400,
      height: 200,
      depth: 250,
      fixtures: [
        {
          type: "door",
          position: { x: -200, y: 0, z: 0 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
        {
          type: "window",
          position: { x: 0, y: 80, z: -125 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
      ],
    },
  },
  {
    id: 3,
    name: "L-Shape",
    preview: "/assets/templates/L-shape.glb",
    roomData: {
      width: 350,
      height: 200,
      depth: 350,
      fixtures: [
        {
          type: "door",
          position: { x: -175, y: 0, z: 100 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
        {
          type: "window",
          position: { x: 100, y: 80, z: -175 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
      ],
    },
  },
  {
    id: 4,
    name: "Trapezoid",
    preview: "/assets/templates/trapezoid.glb",
    roomData: {
      width: 320,
      height: 200,
      depth: 280,
      fixtures: [
        {
          type: "door",
          position: { x: -160, y: 0, z: 50 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
        {
          type: "window",
          position: { x: 80, y: 80, z: -140 },
          rotation: { x: 0, y: 0, z: 0 },
          scale: { x: 1, y: 1, z: 1 },
        },
      ],
    },
  },
];

useGLTF.preload("/assets/templates/square.glb");
useGLTF.preload("/assets/templates/rectangle.glb");
useGLTF.preload("/assets/templates/L-shape.glb");
useGLTF.preload("/assets/templates/trapezoid.glb");

const TemplateSelection: React.FC<TemplateSelectionProps> = ({
  onNavigate,
}) => {
  const [selectedTemplate, setSelectedTemplate] = useState<Template | null>(
    null
  );
  const [hoveredTemplate, setHoveredTemplate] = useState<Template | null>(null);

  const handleTemplateSelect = (template: Template) => {
    setSelectedTemplate(template);
  };

  const handleStartFurnishing = () => {
    if (selectedTemplate) {
      localStorage.setItem(
        "selectedTemplate",
        JSON.stringify(selectedTemplate)
      );
      onNavigate("3d");
    }
  };

  const getTemplateIcon = (templateId: number) => {
    switch (templateId) {
      case 1:
        return (
          <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
            <defs>
              <linearGradient
                id="square-gradient"
                x1="0%"
                y1="0%"
                x2="100%"
                y2="100%"
              >
                <stop offset="0%" stopColor="currentColor" stopOpacity="0.6" />
                <stop
                  offset="100%"
                  stopColor="currentColor"
                  stopOpacity="0.3"
                />
              </linearGradient>
            </defs>
            <rect
              x="20"
              y="20"
              width="40"
              height="40"
              fill="url(#square-gradient)"
              stroke="currentColor"
              strokeWidth="2.5"
              rx="2"
            />
          </svg>
        );
      case 2:
        return (
          <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
            <defs>
              <linearGradient
                id="rect-gradient"
                x1="0%"
                y1="0%"
                x2="100%"
                y2="100%"
              >
                <stop offset="0%" stopColor="currentColor" stopOpacity="0.6" />
                <stop
                  offset="100%"
                  stopColor="currentColor"
                  stopOpacity="0.3"
                />
              </linearGradient>
            </defs>
            <rect
              x="12"
              y="25"
              width="56"
              height="30"
              fill="url(#rect-gradient)"
              stroke="currentColor"
              strokeWidth="2.5"
              rx="2"
            />
          </svg>
        );
      case 3:
        return (
          <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
            <defs>
              <linearGradient
                id="l-gradient"
                x1="0%"
                y1="0%"
                x2="100%"
                y2="100%"
              >
                <stop offset="0%" stopColor="currentColor" stopOpacity="0.6" />
                <stop
                  offset="100%"
                  stopColor="currentColor"
                  stopOpacity="0.3"
                />
              </linearGradient>
            </defs>
            <path
              d="M 18 18 L 18 62 L 45 62 L 45 45 L 62 45 L 62 18 Z"
              fill="url(#l-gradient)"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinejoin="round"
            />
          </svg>
        );
      case 4:
        return (
          <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
            <defs>
              <linearGradient
                id="trap-gradient"
                x1="0%"
                y1="0%"
                x2="0%"
                y2="100%"
              >
                <stop offset="0%" stopColor="currentColor" stopOpacity="0.6" />
                <stop
                  offset="100%"
                  stopColor="currentColor"
                  stopOpacity="0.3"
                />
              </linearGradient>
            </defs>
            <path
              d="M 18 20 L 18 60 L 45 60 L 62 40 L 62 20 Z"
              fill="url(#trap-gradient)"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinejoin="round"
            />
          </svg>
        );
      default:
        return null;
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
                className={`template-card ${
                  selectedTemplate?.id === template.id ? "selected" : ""
                }`}
                onClick={() => handleTemplateSelect(template)}
                onMouseEnter={() => setHoveredTemplate(template)}
                onMouseLeave={() => setHoveredTemplate(null)}
              >
                <div className="template-icon">
                  {getTemplateIcon(template.id)}
                </div>
                <div className="template-name">{template.name}</div>
              </div>
            ))}
          </div>

          <div className="action-buttons">
            <button
              className="go-back-button"
              onClick={() => onNavigate("planner")}
            >
              Go back
            </button>
            <button
              className={`start-furnishing-button ${
                selectedTemplate ? "enabled" : "disabled"
              }`}
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
