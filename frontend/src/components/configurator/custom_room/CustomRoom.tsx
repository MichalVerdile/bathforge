import React, { useState, useRef } from "react";
import "./CustomRoom.css";
import { RoomEditor, RoomEditorRef } from "./RoomEditor";

interface CustomRoomProps {
  onNavigate: (view: string) => void;
}

const CustomRoom: React.FC<CustomRoomProps> = ({ onNavigate }) => {
  const [viewMode, setViewMode] = useState<"2D" | "3D">("2D");
  const roomEditorRef = useRef<RoomEditorRef>(null);

  const [roomHeight, setRoomHeight] = useState(2.5); // e.g., 2.5 units/meters
  const MIN_HEIGHT = 1.75;
  const MAX_HEIGHT = 4;

  const handleReset = () => {
    if (roomEditorRef.current) {
      roomEditorRef.current.reset();
    }
  };

  return (
    <>
      <div className="planner-page">
        <header className="planner-header">
          <h1 className="step-number">Step 2/3</h1>
          <h2 className="step-title">Define Your Room Shape</h2>
        </header>

        <div className="viewer-container">
          <div className="viewer-placeholder">
            <RoomEditor
              ref={roomEditorRef}
              viewMode={viewMode}
              height={roomHeight}
            />
          </div>

          {viewMode === "2D" && (
            <div className="reset-button-container">
              <button
                className="reset-button"
                onClick={handleReset}
                aria-label="Reset room shape"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  strokeWidth="1.5"
                  stroke="currentColor"
                  aria-hidden="true"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0 3.181 3.183a8.25 8.25 0 0 0 13.803-3.7M4.031 9.865a8.25 8.25 0 0 1 13.803-3.7l3.181 3.182m0-4.991v4.99"
                  />
                </svg>
                <span>Reset</span>
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

          {/* Height Input - Only show in 3D mode */}
          {viewMode === "3D" && (
            <div className="height-input-container">
              <label className="height-input-label">Room Height (m)</label>
              <input
                type="number"
                min={MIN_HEIGHT}
                max={MAX_HEIGHT}
                step="0.05"
                value={roomHeight}
                onChange={(e) => {
                  const val = parseFloat(e.target.value);
                  if (!isNaN(val) && val >= MIN_HEIGHT && val <= MAX_HEIGHT) {
                    setRoomHeight(val);
                  }
                }}
                className="height-input"
              />
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
              onClick={() => onNavigate("furnish")}
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
