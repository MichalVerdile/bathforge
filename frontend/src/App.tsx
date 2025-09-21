import React, { useState, useEffect } from 'react';
import { systemController } from './controllers/configuration';
import './index.css';

interface ApiStatus {
  message: string;
  status: 'success' | 'error';
}

function App() {
  const [apiStatus, setApiStatus] = useState<ApiStatus | null>(null);

  useEffect(() => {
    // Test connection to backend using the new controller
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

  return (
    <div className="App">
      <header className="App-header">
        <h1>🛁 BathForge</h1>
        <p>Your Bathroom Design Companion</p>
      </header>
      
      <div className="App-content">
        <h2>Welcome to BathForge!</h2>
        <p>This is your React frontend connected to a Spring Boot backend.</p>
        
        {apiStatus && (
          <div className={`api-status ${apiStatus.status}`}>
            <strong>API Status:</strong> {apiStatus.message}
          </div>
        )}
        
        <div style={{ marginTop: '30px' }}>
          <h3>Next Steps:</h3>
          <ul style={{ textAlign: 'left', maxWidth: '600px', margin: '0 auto' }}>
            <li>Start building your bathroom design features</li>
            <li>Add user authentication</li>
            <li>Create bathroom components and layouts</li>
            <li>Implement design tools and visualization</li>
            <li>Add database models for your domain</li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default App;
