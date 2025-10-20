import React, { useState } from "react";
import "./CustomRoom.css";

interface CustomRoomProps {
  onNavigate: (view: string) => void;
}

const CustomRoom: React.FC<CustomRoomProps> = ({ onNavigate }) => {
  const [isInfoVisible, setInfoVisible] = useState(false);
  const [viewMode, setViewMode] = useState<"2D" | "3D">("3D");

  return (
    <>
      <div className="planner-page">
        <header className="planner-header">
          <h1 className="step-number">Step 2/3</h1>
          <h2 className="step-title">Define Your Room Shape</h2>
        </header>

        <div className="viewer-container">
          <div className="viewer-placeholder">
            <p>3D/2D Room Viewer Area</p>
          </div>

          <div
            className="info-popup-container"
            onMouseEnter={() => setInfoVisible(true)}
            onMouseLeave={() => setInfoVisible(false)}
          >
            <button className="info-icon-button" aria-label="Show help">
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
                  d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z"
                />{" "}
              </svg>
            </button>
            {isInfoVisible && (
              <div className="info-popup-content">
                <div className="info-item">
                  <svg
                    className="info-item-icon"
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
                      d="M3.75 3.75v4.5m0-4.5h4.5m-4.5 0L9 9M3.75 20.25v-4.5m0 4.5h4.5m-4.5 0L9 15M20.25 3.75v4.5m0-4.5h-4.5m4.5 0L15 9m5.25 11.25v-4.5m0 4.5h-4.5m4.5 0L15 15"
                    />{" "}
                  </svg>
                  <div className="info-item-text">
                    <h5>Drag to Resize</h5>
                    <p>Click and drag walls to adjust dimensions.</p>
                  </div>
                </div>

                <div className="info-item">
                  <svg
                    className="info-item-icon"
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
                      d="M16.862 4.487l1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931ZM18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10"
                    />{" "}
                  </svg>
                  <div className="info-item-text">
                    <h5>Precise Input</h5>
                    <p>Click a dimension to type an exact value.</p>
                  </div>
                </div>

                <div className="info-item">
                  <svg
                    className="info-item-icon"
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
                      d="M12 9v6m3-3H9m12 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"
                    />{" "}
                  </svg>
                  <div className="info-item-text">
                    <h5>Add Points</h5>
                    <p>Right-click a wall to create custom shapes.</p>
                  </div>
                </div>
              </div>
            )}
          </div>

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
