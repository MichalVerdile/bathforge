import React, { forwardRef, useImperativeHandle, useRef, useState, useEffect, useCallback } from "react";
import { RoomEditor, type RoomEditorRef } from "../custom_room/RoomEditor";
import type { RoomOpenings, DoorData, WindowData } from "../custom_room/DoorWindowTypes";

interface Vertex {
  x: number;
  y: number;
}

interface RoomConfiguration {
  vertices: Vertex[];
  height: number;
  openings?: RoomOpenings;
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
  const [selectedOpeningId, setSelectedOpeningId] = useState<string | null>(null);
  const [selectedOpeningType, setSelectedOpeningType] = useState<"door" | "window" | null>(null);
  const roomEditorRef = useRef<RoomEditorRef>(null);

  const MIN_HEIGHT = 1.5;
  const MAX_HEIGHT = 4;

  useImperativeHandle(ref, () => ({
    getRoomData: () => {
      if (roomEditorRef.current) {
        const roomData = roomEditorRef.current.getRoomData();
        return {
          vertices: roomData.vertices,
          height: roomData.height,
          openings: roomData.openings
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
        height: roomHeight,
        openings: roomData.openings
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
        height: newHeight,
        openings: roomData.openings
      };
      onRoomChange(newRoomConfig);
    }
  };

  // Handle opening selection
  const handleOpeningClick = (id: string, type: "door" | "window") => {
    setSelectedOpeningId(id);
    setSelectedOpeningType(type);
  };

  // Get current opening data
  const getCurrentOpening = useCallback((): (DoorData | WindowData) | null => {
    if (!roomEditorRef.current || !selectedOpeningId) return null;
    
    const openings = roomEditorRef.current.getOpenings();
    if (selectedOpeningType === "door") {
      return openings.doors.find(d => d.id === selectedOpeningId) || null;
    } else {
      return openings.windows.find(w => w.id === selectedOpeningId) || null;
    }
  }, [selectedOpeningId, selectedOpeningType]);

  // Update opening position
  const updateOpeningPosition = useCallback((newPosition: number) => {
    if (!roomEditorRef.current || !selectedOpeningId || !selectedOpeningType) return;
    
    const openings = roomEditorRef.current.getOpenings();
    const updatedOpenings = { ...openings };

    if (selectedOpeningType === "door") {
      updatedOpenings.doors = openings.doors.map(door => 
        door.id === selectedOpeningId ? { ...door, position: newPosition } : door
      );
    } else {
      updatedOpenings.windows = openings.windows.map(window => 
        window.id === selectedOpeningId ? { ...window, position: newPosition } : window
      );
    }

    roomEditorRef.current.updateOpenings(updatedOpenings);
    handleRoomDataChange();
  }, [selectedOpeningId, selectedOpeningType, handleRoomDataChange]);

  // Update opening wall
  const updateOpeningWall = useCallback((newWallIndex: number) => {
    if (!roomEditorRef.current || !selectedOpeningId || !selectedOpeningType) return;
    
    const openings = roomEditorRef.current.getOpenings();
    const updatedOpenings = { ...openings };

    if (selectedOpeningType === "door") {
      updatedOpenings.doors = openings.doors.map(door => 
        door.id === selectedOpeningId ? { ...door, wallIndex: newWallIndex } : door
      );
    } else {
      updatedOpenings.windows = openings.windows.map(window => 
        window.id === selectedOpeningId ? { ...window, wallIndex: newWallIndex } : window
      );
    }

    roomEditorRef.current.updateOpenings(updatedOpenings);
    handleRoomDataChange();
  }, [selectedOpeningId, selectedOpeningType, handleRoomDataChange]);

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
      <div className="room-editor-wrapper" style={{ 
        height: '550px', 
        border: '2px solid #e0e0e0', 
        borderRadius: '8px',
        position: 'relative',
        backgroundColor: '#f8f9fa',
        width: '1000px'
      }}>
        <RoomEditor
          ref={roomEditorRef}
          viewMode={viewMode}
          height={roomHeight}
          selectedOpeningId={selectedOpeningId}
          onOpeningClick={handleOpeningClick}
          onOpeningHover={(id) => {
            // Optional: could add hover effects here
          }}
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
              padding: '8px 16px',
              border: viewMode === "2D" ? '2px solid #94a3b8' : '1px solid rgba(148, 163, 184, 0.3)',
              borderRadius: '8px',
              background: viewMode === "2D" ? 'linear-gradient(135deg, #cbd5e1 0%, #94a3b8 100%)' : 'rgba(30, 41, 59, 0.5)',
              color: viewMode === "2D" ? '#0f172a' : '#cbd5e1',
              cursor: 'pointer',
              fontWeight: viewMode === "2D" ? '600' : '400',
              transition: 'all 0.2s ease',
              backdropFilter: 'blur(5px)'
            }}
          >
            2D
          </button>
          <button
            className={`view-toggle-btn ${viewMode === "3D" ? "active" : ""}`}
            onClick={() => setViewMode("3D")}
            style={{
              padding: '8px 16px',
              border: viewMode === "3D" ? '2px solid #94a3b8' : '1px solid rgba(148, 163, 184, 0.3)',
              borderRadius: '8px',
              background: viewMode === "3D" ? 'linear-gradient(135deg, #cbd5e1 0%, #94a3b8 100%)' : 'rgba(30, 41, 59, 0.5)',
              color: viewMode === "3D" ? '#0f172a' : '#cbd5e1',
              cursor: 'pointer',
              fontWeight: viewMode === "3D" ? '600' : '400',
              transition: 'all 0.2s ease',
              backdropFilter: 'blur(5px)'
            }}
          >
            3D
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

        {/* Opening Selection Info */}
        {viewMode === "3D" && selectedOpeningId && (() => {
          const opening = getCurrentOpening();
          if (!opening) return null;

          const roomData = roomEditorRef.current?.getRoomData();
          const numWalls = roomData?.vertices.length || 4;

          return (
            <div className="product-controls-panel opening-controls-panel" style={{
              position: 'absolute',
              top: '60px',
              right: '10px',
              background: 'rgba(30, 41, 59, 0.95)',
              padding: '16px',
              borderRadius: '8px',
              border: '1px solid rgba(148, 163, 184, 0.3)',
              backdropFilter: 'blur(10px)',
              minWidth: '220px',
              boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)'
            }}>
              <div className="product-controls">
                <div className="control-header" style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  marginBottom: '16px'
                }}>
                  <h5 style={{
                    margin: 0,
                    fontSize: '14px',
                    fontWeight: '600',
                    color: '#cbd5e1',
                    textTransform: 'capitalize'
                  }}>
                    {selectedOpeningType === "door" ? "Door" : "Window"}
                  </h5>
                  <button
                    className="deselect-button"
                    onClick={() => {
                      setSelectedOpeningId(null);
                      setSelectedOpeningType(null);
                    }}
                    style={{
                      background: 'transparent',
                      border: 'none',
                      color: '#cbd5e1',
                      fontSize: '18px',
                      cursor: 'pointer',
                      padding: '2px 6px',
                      lineHeight: 1
                    }}
                    title="Deselect"
                  >
                    ×
                  </button>
                </div>

                <div className="slider-control" style={{ marginBottom: '16px' }}>
                  <label style={{
                    display: 'block',
                    fontSize: '12px',
                    fontWeight: '500',
                    color: '#94a3b8',
                    marginBottom: '8px'
                  }}>
                    Position along wall: {Math.round(opening.position * 100)}%
                  </label>
                  <input
                    type="range"
                    min="0.1"
                    max="0.9"
                    step="0.05"
                    value={opening.position}
                    onChange={(e) => {
                      updateOpeningPosition(parseFloat(e.target.value));
                    }}
                    className="slider"
                    style={{
                      width: '100%',
                      height: '6px',
                      borderRadius: '3px',
                      background: '#334155',
                      outline: 'none',
                      cursor: 'pointer'
                    }}
                  />
                </div>

                <div className="wall-selector" style={{ marginBottom: '12px' }}>
                  <label style={{
                    display: 'block',
                    fontSize: '12px',
                    fontWeight: '500',
                    color: '#94a3b8',
                    marginBottom: '8px'
                  }}>Wall:</label>
                  <div className="wall-buttons" style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(40px, 1fr))',
                    gap: '6px'
                  }}>
                    {Array.from({ length: numWalls }, (_, i) => (
                      <button
                        key={i}
                        className={`wall-button ${opening.wallIndex === i ? 'active' : ''}`}
                        onClick={() => updateOpeningWall(i)}
                        style={{
                          padding: '8px',
                          border: opening.wallIndex === i ? '2px solid #94a3b8' : '1px solid rgba(148, 163, 184, 0.3)',
                          borderRadius: '6px',
                          background: opening.wallIndex === i ? '#94a3b8' : 'rgba(51, 65, 85, 0.5)',
                          color: opening.wallIndex === i ? '#0f172a' : '#cbd5e1',
                          fontSize: '13px',
                          fontWeight: opening.wallIndex === i ? '600' : '500',
                          cursor: 'pointer',
                          transition: 'all 0.2s ease'
                        }}
                        onMouseEnter={(e) => {
                          if (opening.wallIndex !== i) {
                            e.currentTarget.style.background = 'rgba(51, 65, 85, 0.8)';
                          }
                        }}
                        onMouseLeave={(e) => {
                          if (opening.wallIndex !== i) {
                            e.currentTarget.style.background = 'rgba(51, 65, 85, 0.5)';
                          }
                        }}
                      >
                        {i + 1}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="opening-info" style={{
                  padding: '10px',
                  background: 'rgba(15, 23, 42, 0.5)',
                  borderRadius: '6px',
                  fontSize: '11px',
                  color: '#94a3b8'
                }}>
                  <p style={{ margin: '0 0 4px 0' }}>
                    Size: {opening.width.toFixed(2)}m × {opening.height.toFixed(2)}m
                  </p>
                  {selectedOpeningType === "window" && "elevation" in opening && (
                    <p style={{ margin: 0 }}>
                      Height from floor: {(opening as WindowData).elevation.toFixed(2)}m
                    </p>
                  )}
                </div>
              </div>
            </div>
          );
        })()}
      </div>
    </div>
  );
});

export default RoomStep;