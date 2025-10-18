import React, { useState } from 'react';
import './BathroomPlanner.css';

interface BathroomPlannerProps {
  onNavigate: (view: string) => void;
}

const BathroomPlanner: React.FC<BathroomPlannerProps> = ({ onNavigate }) => {
  const [selectedOption, setSelectedOption] = useState<string | null>(null);

  const handleOptionSelect = (option: string) => {
    setSelectedOption(option);
  };

  const handleContinue = () => {
    if (selectedOption === 'template') {
      onNavigate('template-selection');
    } else if (selectedOption === 'custom') {
      onNavigate('custom-room');
    } else if (selectedOption === 'ai') {
      onNavigate('ai-design');
    }
  };

  return (
    <div className="bathroom-planner">
      {/* Header */}
      <header className="planner-header">
        <div className="header-content">
          <h1 className="planner-title">Bathroom Planner</h1>
          <h2 className="planner-subtitle">Choose your Starting Point</h2>
        </div>
      </header>

      <div className="planner-content">
        <div className="planner-left">
          {/* Option Cards */}
          <div className="options-container">
            {/* Template Option */}
            <div 
              className={`option-card ${selectedOption === 'template' ? 'selected' : ''}`}
              onClick={() => handleOptionSelect('template')}
            >
              <div className="option-icon">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/>
                </svg>
              </div>
              <div className="option-content">
                <h3>Choose a Template</h3>
                <p>Start with a pre-made room</p>
              </div>
            </div>

            {/* Custom Room Option */}
            <div 
              className={`option-card ${selectedOption === 'custom' ? 'selected' : ''}`}
              onClick={() => handleOptionSelect('custom')}
            >
              <div className="option-icon">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
              </div>
              <div className="option-content">
                <h3>Custom Room</h3>
                <p>Set your own dimensions</p>
              </div>
            </div>

            {/* AI Option */}
            <div 
              className={`option-card ${selectedOption === 'ai' ? 'selected' : ''}`}
              onClick={() => handleOptionSelect('ai')}
            >
              <div className="option-icon">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                </svg>
              </div>
              <div className="option-content">
                <h3>AI Designer</h3>
                <p>Let AI create your perfect bathroom</p>
              </div>
            </div>
          </div>

          {/* Continue Button */}
          <button 
            className={`continue-button ${selectedOption ? 'enabled' : 'disabled'}`}
            onClick={handleContinue}
            disabled={!selectedOption}
          >
            Continue
          </button>
        </div>

        {/* 3D Preview */}
        <div className="planner-right">
          <div className="preview-container">
            <div className="room-preview">
              <img 
                src="/assets/3d-realistic-bathroom-scene-jpg.webp" 
                alt="3D Bathroom Preview" 
                className="bathroom-preview-image"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BathroomPlanner;