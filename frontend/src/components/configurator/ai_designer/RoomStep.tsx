import React, { forwardRef, useImperativeHandle, useRef, useState, useEffect, useCallback } from "react";
import { RoomEditor, type RoomEditorRef } from "../custom_room/RoomEditor";

interface Vertex {
  x: number;
  y: number;
}

interface RoomConfiguration {
  vertices: Vertex[];
  height: number;
}

interface RoomStepProps {
  currentRoom?: RoomConfiguration;
  onRoomChange: (room: RoomConfiguration) => void;
}

export interface RoomStepRef {
  getRoomData: () => RoomConfiguration | null;
}

const RoomStep = forwardRef<RoomStepRef, RoomStepProps>(function RoomStep(
  { currentRoom, onRoomChange },
  ref
) {
  const [viewMode, setViewMode] = useState<"2D" | "3D">("2D");
  const [roomHeight, setRoomHeight] = useState(currentRoom?.height || 2.5);
  const roomEditorRef = useRef<RoomEditorRef>(null);

  const MIN_HEIGHT = 1.5;
  const MAX_HEIGHT = 4;

  useImperativeHandle(ref, () => ({
    getRoomData: () => {
      if (roomEditorRef.current) {
        const roomData = roomEditorRef.current.getRoomData();
        return {
          vertices: roomData.vertices,
          height: roomData.height
        };
      }
      return null;
    }
  }));

  // Update room configuration whenever room data changes
  const handleRoomDataChange = useCallback(() => {
    if (roomEditorRef.current) {
      const roomData = roomEditorRef.current.getRoomData();
      const newRoomConfig: RoomConfiguration = {
        vertices: roomData.vertices,
        height: roomHeight
      };
      onRoomChange(newRoomConfig);
    }
  }, [roomHeight, onRoomChange]);

  // Update room height
  const handleHeightChange = (newHeight: number) => {
    setRoomHeight(newHeight);
    if (roomEditorRef.current) {
      const roomData = roomEditorRef.current.getRoomData();
      const newRoomConfig: RoomConfiguration = {
        vertices: roomData.vertices,
        height: newHeight
      };
      onRoomChange(newRoomConfig);
    }
  };

  // Reset room to default shape
  const handleReset = () => {
    if (roomEditorRef.current) {
      roomEditorRef.current.reset();
      // Trigger room data update after reset
      setTimeout(handleRoomDataChange, 100);
    }
  };

  // Auto-save room data when component mounts or updates
  useEffect(() => {
    const timer = setTimeout(handleRoomDataChange, 500);
    return () => clearTimeout(timer);
  }, [handleRoomDataChange]);

  return (
    <div className="room-step-container">
      <div className="room-step-instructions">
        <h3>Design Your Bathroom Shape</h3>
        <p>
          Create the perfect room shape for your AI-generated bathroom. 
          Use the 2D editor to draw your room outline, then switch to 3D to see the result.
        </p>
      </div>

      <div className="room-editor-wrapper" style={{ 
        height: '400px', 
        border: '2px solid #e0e0e0', 
        borderRadius: '8px',
        marginBottom: '1rem',
        position: 'relative',
        backgroundColor: '#f8f9fa'
      }}>
        <RoomEditor
          ref={roomEditorRef}
          viewMode={viewMode}
          height={roomHeight}
        />

        {/* View Toggle Buttons */}
        <div style={{
          position: 'absolute',
          top: '10px',
          right: '10px',
          display: 'flex',
          gap: '5px'
        }}>
          <button
            className={`view-toggle-btn ${viewMode === "2D" ? "active" : ""}`}
            onClick={() => setViewMode("2D")}
            style={{
              padding: '8px 12px',
              border: viewMode === "2D" ? '2px solid #007bff' : '1px solid #ccc',
              borderRadius: '4px',
              background: viewMode === "2D" ? '#007bff' : 'white',
              color: viewMode === "2D" ? 'white' : '#333',
              cursor: 'pointer'
            }}
          >
            2D Editor
          </button>
          <button
            className={`view-toggle-btn ${viewMode === "3D" ? "active" : ""}`}
            onClick={() => setViewMode("3D")}
            style={{
              padding: '8px 12px',
              border: viewMode === "3D" ? '2px solid #007bff' : '1px solid #ccc',
              borderRadius: '4px',
              background: viewMode === "3D" ? '#007bff' : 'white',
              color: viewMode === "3D" ? 'white' : '#333',
              cursor: 'pointer'
            }}
          >
            3D Preview
          </button>
        </div>

        {/* Reset Button for 2D mode */}
        {viewMode === "2D" && (
          <div style={{
            position: 'absolute',
            bottom: '10px',
            left: '10px'
          }}>
            <button
              onClick={handleReset}
              style={{
                padding: '8px 12px',
                border: '1px solid #dc3545',
                borderRadius: '4px',
                background: '#dc3545',
                color: 'white',
                cursor: 'pointer'
              }}
            >
              Reset Shape
            </button>
          </div>
        )}

        {/* Height Control for 3D mode */}
        {viewMode === "3D" && (
          <div style={{
            position: 'absolute',
            bottom: '10px',
            left: '10px',
            background: 'rgba(255, 255, 255, 0.9)',
            padding: '8px 12px',
            borderRadius: '4px',
            border: '1px solid #ccc'
          }}>
            <label style={{ display: 'block', marginBottom: '4px', fontSize: '12px', fontWeight: 'bold' }}>
              Room Height: {roomHeight}m
            </label>
            <input
              type="range"
              min={MIN_HEIGHT}
              max={MAX_HEIGHT}
              step="0.1"
              value={roomHeight}
              onChange={(e) => handleHeightChange(parseFloat(e.target.value))}
              style={{ width: '120px' }}
            />
          </div>
        )}
      </div>

      <div className="room-step-tips" style={{
        background: '#e8f4fd',
        border: '1px solid #bee5eb',
        borderRadius: '6px',
        padding: '12px',
        fontSize: '14px'
      }}>
        <strong>Tips:</strong>
        <ul style={{ margin: '8px 0 0 20px', padding: 0 }}>
          <li>In 2D mode: Click and drag to create custom room shapes</li>
          <li>In 3D mode: Use the height slider to adjust ceiling height</li>
          <li>The AI will use this room shape to generate your bathroom layout</li>
        </ul>
      </div>
    </div>
  );
});

export default RoomStep;