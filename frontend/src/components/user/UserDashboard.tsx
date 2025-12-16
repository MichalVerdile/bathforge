import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../../controllers/api/auth/authService';
import userService, { SceneData, QuoteRequestHistory, QuoteRequestDetail } from '../../controllers/api/user/userService';
import { HelpModal } from '../common';
import './UserDashboard.css';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const BACKEND_URL = API_BASE_URL.replace('/api', '');

const UserDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [scenes, setScenes] = useState<SceneData[]>([]);
  const [quoteRequests, setQuoteRequests] = useState<QuoteRequestHistory[]>([]);
  const [selectedQuote, setSelectedQuote] = useState<QuoteRequestDetail | null>(null);
  const [activeTab, setActiveTab] = useState<'scenes' | 'quotes'>('scenes');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [isHelpModalOpen, setIsHelpModalOpen] = useState(false);

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

  const handleViewQuoteDetails = async (quoteId: number) => {
    try {
      setIsLoading(true);
      const details = await userService.getQuoteRequestDetail(quoteId);
      setSelectedQuote(details);
    } catch (err: any) {
      setError('Failed to load quote request details');
      console.error('Error loading quote details:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCloseQuoteDetails = () => {
    setSelectedQuote(null);
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
          <button className="btn-help" onClick={() => setIsHelpModalOpen(true)} title="Help">
            ❓ Help
          </button>
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
          <div className="quotes-container">
            {selectedQuote ? (
              <div className="quote-detail-view">
                <button className="btn-back" onClick={handleCloseQuoteDetails}>
                  ← Back to Requests
                </button>

                <div className="quote-detail-header">
                  <h2>Quote Request #{selectedQuote.id}</h2>
                  <span className={`badge-status ${selectedQuote.status.toLowerCase()}`}>
                    {selectedQuote.status}
                  </span>
                </div>

                {selectedQuote.sceneSnapshot && (
                  <div className="quote-snapshot-large">
                    <img src={selectedQuote.sceneSnapshot} alt="Scene preview" />
                  </div>
                )}

                <div className="quote-info-section">
                  <h3>Request Information</h3>
                  <div className="info-grid">
                    <div className="info-item">
                      <strong>Room Dimensions:</strong>
                      <span>{selectedQuote.roomDimensions}</span>
                    </div>
                    <div className="info-item">
                      <strong>Created:</strong>
                      <span>{formatDate(selectedQuote.createdAt)}</span>
                    </div>
                    <div className="info-item">
                      <strong>Last Updated:</strong>
                      <span>{formatDate(selectedQuote.updatedAt)}</span>
                    </div>
                    {selectedQuote.additionalNotes && (
                      <div className="info-item full-width">
                        <strong>Your Notes:</strong>
                        <p>{selectedQuote.additionalNotes}</p>
                      </div>
                    )}
                  </div>
                </div>

                {selectedQuote.documentUrl && (
                  <div className="quote-info-section">
                    <h3>Attached Document</h3>
                    <a 
                      href={`${BACKEND_URL}${selectedQuote.documentUrl}`}
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="btn-download-doc"
                    >
                      📄 Download Document
                    </a>
                  </div>
                )}

                <div className="quote-info-section">
                  <h3>Communication History</h3>
                  {selectedQuote.messages && selectedQuote.messages.length > 0 ? (
                    <div className="messages-timeline">
                      {selectedQuote.messages.map((message) => (
                        <div 
                          key={message.id} 
                          className={`message-item ${message.senderType.toLowerCase()}`}
                        >
                          <div className="message-header">
                            <span className="message-sender">
                              {message.senderType === 'ADMIN' ? '👤 Admin' : '🔔 System'}
                            </span>
                            <span className="message-time">
                              {formatDate(message.createdAt)}
                            </span>
                          </div>
                          <div className="message-body">
                            {message.message}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="no-messages">No messages yet. You'll be notified when there are updates.</p>
                  )}
                </div>
              </div>
            ) : (
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

                      <button 
                        className="btn-view-details"
                        onClick={() => handleViewQuoteDetails(quote.id)}
                      >
                        View Details & History
                      </button>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        )}
      </div>

      <HelpModal
        isOpen={isHelpModalOpen}
        onClose={() => setIsHelpModalOpen(false)}
        currentPage="dashboard"
      />
    </div>
  );
};

export default UserDashboard;
