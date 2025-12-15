import React, { useState, useRef } from "react";
import "./CustomRoom.css";
import { RoomEditor, RoomEditorRef } from "./RoomEditor";

interface Vertex {
  x: number;
  y: number;
}

// Calculate polygon area using Shoelace formula (1 pixel = 0.01 meters)
const calculateRoomArea = (vertices: Vertex[]): number => {
  if (vertices.length < 3) return 0;
  
  let area = 0;
  for (let i = 0; i < vertices.length; i++) {
    const j = (i + 1) % vertices.length;
    area += vertices[i].x * vertices[j].y;
    area -= vertices[j].x * vertices[i].y;
  }
  
  // Convert from pixels² to m² (1 pixel = 0.01m, so 1 pixel² = 0.0001m²)
  return Math.abs(area / 2) * 0.0001;
};

interface CustomRoomProps {
  onNavigate: (view: string) => void;
}

const CustomRoom: React.FC<CustomRoomProps> = ({ onNavigate }) => {
  const [viewMode, setViewMode] = useState<"2D" | "3D">("2D");
  const roomEditorRef = useRef<RoomEditorRef>(null);
  const [roomArea, setRoomArea] = useState<number>(0);

  const [roomHeight, setRoomHeight] = useState(2); // 2 meters
  const MIN_HEIGHT = 1.5;
  const MAX_HEIGHT = 4;

  // Update area when room changes
  React.useEffect(() => {
    const interval = setInterval(() => {
      if (roomEditorRef.current) {
        const roomData = roomEditorRef.current.getRoomData();
        setRoomArea(calculateRoomArea(roomData.vertices));
      }
    }, 500);
    return () => clearInterval(interval);
  }, []);

  const handleReset = () => {
    if (roomEditorRef.current) {
      roomEditorRef.current.reset();
    }
  };

  return (
    <>
      <div className="planner-page">
        <div className="viewer-container">
          <div className="viewer-placeholder">
            <RoomEditor
              ref={roomEditorRef}
              viewMode={viewMode}
              height={roomHeight}
            />
          </div>

          {viewMode === "2D" && (
            <div style={{
              position: 'absolute',
              top: '2rem',
              right: '2rem',
              zIndex: 20,
              display: 'flex',
              gap: '10px',
              alignItems: 'center'
            }}>
              <div style={{
                padding: '8px 16px',
                borderRadius: '8px',
                background: 'rgba(30, 41, 59, 0.95)',
                border: '1px solid rgba(148, 163, 184, 0.3)',
                backdropFilter: 'blur(10px)',
                fontSize: '12px',
                fontWeight: '600',
                color: '#cbd5e1'
              }}>
                Floor Area: {roomArea.toFixed(2)} m²
              </div>
              <button
                onClick={handleReset}
                style={{
                  padding: '8px 16px',
                  border: '1px solid rgba(148, 163, 184, 0.3)',
                  borderRadius: '8px',
                  background: 'rgba(239, 68, 68, 0.9)',
                  color: '#fff',
                  cursor: 'pointer',
                  fontWeight: '500',
                  transition: 'all 0.2s ease',
                  backdropFilter: 'blur(5px)'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = 'rgba(220, 38, 38, 1)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = 'rgba(239, 68, 68, 0.9)';
                }}
                aria-label="Reset room shape"
              >
                Reset Shape
              </button>
            </div>
          )}

          <div className="view-toggle-buttons">
            <button
              className={`view-toggle-btn ${viewMode === "2D" ? "active" : ""}`}
              onClick={() => setViewMode("2D")}
            >
              <span>2D</span>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth="1.5"
                stroke="currentColor"
                aria-hidden="true"
              >
                {" "}
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M3.75 6A2.25 2.25 0 0 1 6 3.75h2.25A2.25 2.25 0 0 1 10.5 6v2.25a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 8.25V6ZM3.75 15.75A2.25 2.25 0 0 1 6 13.5h2.25a2.25 2.25 0 0 1 2.25 2.25V18a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 18v-2.25ZM13.5 6a2.25 2.25 0 0 1 2.25-2.25H18A2.25 2.25 0 0 1 20.25 6v2.25A2.25 2.25 0 0 1 18 10.5h-2.25a2.25 2.25 0 0 1-2.25-2.25V6ZM13.5 15.75a2.25 2.25 0 0 1 2.25-2.25H18a2.25 2.25 0 0 1 2.25 2.25V18A2.25 2.25 0 0 1 18 20.25h-2.25A2.25 2.25 0 0 1 13.5 18v-2.25Z"
                />{" "}
              </svg>
            </button>
            <button
              className={`view-toggle-btn ${viewMode === "3D" ? "active" : ""}`}
              onClick={() => setViewMode("3D")}
            >
              <span>3D</span>
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth="1.5"
                stroke="currentColor"
                aria-hidden="true"
              >
                {" "}
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="m21 7.5-9-5.25L3 7.5m18 0-9 5.25m9-5.25v9l-9 5.25M3 7.5l9 5.25M3 7.5v9l9 5.25m0-9v9"
                />{" "}
              </svg>
            </button>
          </div>

          {/* Height Slider - Only show in 3D mode */}
          {viewMode === "3D" && (
            <div style={{
              position: 'absolute',
              top: '2rem',
              right: '2rem',
              zIndex: 20,
              background: 'rgba(30, 41, 59, 0.95)',
              padding: '10px 14px',
              borderRadius: '8px',
              border: '1px solid rgba(148, 163, 184, 0.3)',
              backdropFilter: 'blur(10px)'
            }}>
              <label style={{
                display: 'block',
                marginBottom: '6px',
                fontSize: '12px',
                fontWeight: '600',
                color: '#cbd5e1'
              }}>
                Room Height: {roomHeight.toFixed(1)}m
              </label>
              <input
                type="range"
                min={MIN_HEIGHT}
                max={MAX_HEIGHT}
                step="0.1"
                value={roomHeight}
                onChange={(e) => setRoomHeight(parseFloat(e.target.value))}
                style={{ width: '120px' }}
              />
              <div style={{
                marginTop: '10px',
                paddingTop: '10px',
                borderTop: '1px solid rgba(148, 163, 184, 0.3)',
                fontSize: '12px',
                fontWeight: '600',
                color: '#cbd5e1'
              }}>
                Floor Area: {roomArea.toFixed(2)} m²
              </div>
            </div>
          )}

          <div className="bottom-nav-buttons">
            <button
              className="go-back-button"
              onClick={() => onNavigate("planner")}
            >
              Go Back
            </button>
            <button
              className="start-furnishing-button"
              onClick={() => {
                if (roomEditorRef.current) {
                  const roomData = roomEditorRef.current.getRoomData();
                  localStorage.setItem("customRoom", JSON.stringify(roomData));
                }
                onNavigate("3d");
              }}
            >
              Start Furnishing
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default CustomRoom;
