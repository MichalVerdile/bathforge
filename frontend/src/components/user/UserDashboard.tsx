import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../../controllers/api/auth/authService';
import userService, { SceneData, QuoteRequestHistory } from '../../controllers/api/user/userService';
import './UserDashboard.css';

const UserDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [scenes, setScenes] = useState<SceneData[]>([]);
  const [quoteRequests, setQuoteRequests] = useState<QuoteRequestHistory[]>([]);
  const [activeTab, setActiveTab] = useState<'scenes' | 'quotes'>('scenes');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const user = authService.getCurrentUser();

  useEffect(() => {
    if (!authService.isAuthenticated()) {
      navigate('/');
      return;
    }

    loadUserData();
  }, [navigate]);

  const loadUserData = async () => {
    setIsLoading(true);
    setError('');

    try {
      const [scenesData, quotesData] = await Promise.all([
        userService.getUserScenes(),
        userService.getUserQuoteRequests()
      ]);

      setScenes(scenesData);
      setQuoteRequests(quotesData);
    } catch (err: any) {
      setError('Failed to load your data. Please try again.');
      console.error('Error loading user data:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    authService.logout();
    navigate('/');
  };

  const handleCreateNewScene = () => {
    navigate('/');
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (isLoading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner"></div>
        <p>Loading your dashboard...</p>
      </div>
    );
  }

  return (
    <div className="user-dashboard">
      <div className="dashboard-header">
        <div className="dashboard-user-info">
          <h1>Welcome, {user?.firstName}!</h1>
          <p className="user-email">{user?.email}</p>
        </div>
        <div className="dashboard-actions">
          <button className="btn-create-scene" onClick={handleCreateNewScene}>
            + Create New Scene
          </button>
          <button className="btn-logout" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>

      {error && <div className="dashboard-error">{error}</div>}

      <div className="dashboard-tabs">
        <button
          className={`tab ${activeTab === 'scenes' ? 'active' : ''}`}
          onClick={() => setActiveTab('scenes')}
        >
          My Scenes ({scenes.length})
        </button>
        <button
          className={`tab ${activeTab === 'quotes' ? 'active' : ''}`}
          onClick={() => setActiveTab('quotes')}
        >
          Quote Requests ({quoteRequests.length})
        </button>
      </div>

      <div className="dashboard-content">
        {activeTab === 'scenes' && (
          <div className="scenes-grid">
            {scenes.length === 0 ? (
              <div className="empty-state">
                <h3>No scenes yet</h3>
                <p>Create your first bathroom design!</p>
                <button className="btn-create-scene" onClick={handleCreateNewScene}>
                  Create Scene
                </button>
              </div>
            ) : (
              scenes.map((scene) => (
                <div key={scene.id} className="scene-card">
                  <div className="scene-card-header">
                    <h3>{scene.name}</h3>
                    {scene.isPublic && <span className="badge-public">Public</span>}
                  </div>
                  {scene.description && (
                    <p className="scene-description">{scene.description}</p>
                  )}
                  <div className="scene-card-footer">
                    <span className="scene-date">Created: {formatDate(scene.createdAt)}</span>
                    <button className="btn-load-scene" onClick={() => navigate('/', { state: { sceneId: scene.id } })}>
                      Load Scene
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {activeTab === 'quotes' && (
          <div className="quotes-grid">
            {quoteRequests.length === 0 ? (
              <div className="empty-state">
                <h3>No quote requests yet</h3>
                <p>Request a quote for your bathroom design!</p>
              </div>
            ) : (
              quoteRequests.map((quote) => (
                <div key={quote.id} className="quote-card">
                  <div className="quote-card-header">
                    <span className={`badge-status ${quote.status.toLowerCase()}`}>
                      {quote.status}
                    </span>
                    <span className="quote-date">{formatDate(quote.createdAt)}</span>
                  </div>
                  
                  {quote.sceneSnapshot && (
                    <div className="quote-snapshot">
                      <img src={quote.sceneSnapshot} alt="Scene preview" />
                    </div>
                  )}
                  
                  <div className="quote-details">
                    <div className="quote-detail-row">
                      <strong>Room:</strong>
                      <span>{quote.roomDimensions}</span>
                    </div>
                    {quote.additionalNotes && (
                      <div className="quote-detail-row">
                        <strong>Notes:</strong>
                        <span>{quote.additionalNotes}</span>
                      </div>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default UserDashboard;
