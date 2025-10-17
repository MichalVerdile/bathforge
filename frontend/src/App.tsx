import React, { useState, useEffect } from 'react';
import { systemController } from './controllers/configuration';
import { Bathroom3DViewer } from './components/3d';
import './index.css';

interface ApiStatus {
  message: string;
  status: 'success' | 'error';
}

function App() {
  const [apiStatus, setApiStatus] = useState<ApiStatus | null>(null);
  const [currentView, setCurrentView] = useState<'home' | '3d'>('home');

  useEffect(() => {
    const testBackendConnection = async () => {
      try {
        const result = await systemController.testConnection();
        setApiStatus({
          message: result.message,
          status: result.isConnected ? 'success' : 'error'
        });
      } catch (error: any) {
        setApiStatus({
          message: error.message || 'Unexpected error occurred',
          status: 'error'
        });
      }
    };

    testBackendConnection();
  }, []);

  if (currentView === '3d') {
    return (
      <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
        {/* Navigation Header */}
        <header style={{
          background: '#2c3e50',
          color: 'white',
          padding: '10px 20px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexShrink: 0
        }}>
          <h1 style={{ margin: 0, fontSize: '24px' }}>🛁 BathForge 3D</h1>
          <button
            onClick={() => setCurrentView('home')}
            style={{
              background: '#3498db',
              color: 'white',
              border: 'none',
              padding: '8px 16px',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            ← Back to Home
          </button>
        </header>
        
        {/* 3D Viewer */}
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <Bathroom3DViewer />
        </div>
      </div>
    );
  }

  return (
    <div className="App">
      <header className="App-header">
        <h1>🛁 BathForge</h1>
        <p>Your Bathroom Design Companion</p>
      </header>
      
      <div className="App-content">
        <h2>Welcome to BathForge!</h2>
        <p>Design beautiful bathrooms with our 3D visualization tools.</p>
        
        {apiStatus && (
          <div className={`api-status ${apiStatus.status}`}>
            <strong>API Status:</strong> {apiStatus.message}
          </div>
        )}
        
        {/* 3D Viewer Launch Button */}
        <div style={{ margin: '30px 0' }}>
          <button
            onClick={() => setCurrentView('3d')}
            style={{
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              border: 'none',
              padding: '15px 30px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '18px',
              fontWeight: 'bold',
              boxShadow: '0 4px 15px rgba(0,0,0,0.2)',
              transition: 'transform 0.2s ease',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-2px)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0px)';
            }}
          >
            🚀 Launch 3D Bathroom Designer
          </button>
        </div>
        
        <div style={{ marginTop: '30px' }}>
          <h3>Features:</h3>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', 
            gap: '20px', 
            maxWidth: '800px', 
            margin: '20px auto',
            textAlign: 'left'
          }}>
            <div style={{ 
              background: '#f8f9fa', 
              padding: '20px', 
              borderRadius: '8px',
              border: '1px solid #e9ecef'
            }}>
              <h4 style={{ margin: '0 0 10px 0', color: '#495057' }}>🚿 3D Model Library</h4>
              <p style={{ margin: 0, color: '#6c757d', fontSize: '14px' }}>
                Browse our extensive collection of bathroom fixtures including basins, bathtubs, and accessories.
              </p>
            </div>
            
            <div style={{ 
              background: '#f8f9fa', 
              padding: '20px', 
              borderRadius: '8px',
              border: '1px solid #e9ecef'
            }}>
              <h4 style={{ margin: '0 0 10px 0', color: '#495057' }}>🎮 Interactive Controls</h4>
              <p style={{ margin: 0, color: '#6c757d', fontSize: '14px' }}>
                Rotate, scale, and position models with intuitive controls and real-time preview.
              </p>
            </div>
            
            <div style={{ 
              background: '#f8f9fa', 
              padding: '20px', 
              borderRadius: '8px',
              border: '1px solid #e9ecef'
            }}>
              <h4 style={{ margin: '0 0 10px 0', color: '#495057' }}>💡 Realistic Lighting</h4>
              <p style={{ margin: 0, color: '#6c757d', fontSize: '14px' }}>
                Visualize your designs with professional lighting and shadow effects.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
