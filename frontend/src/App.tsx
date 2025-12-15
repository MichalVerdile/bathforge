import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, useNavigate, useLocation } from "react-router-dom";
import { systemController } from "./controllers/configuration";
import { Bathroom3DViewer } from "./components/3d";
import BathroomPlanner from "./components/configurator/bathroom_planner/BathroomPlanner";
import TemplateSelection from "./components/configurator/template_selection/TemplateSelection";
import "./index.css";
import CustomRoom from "./components/configurator/custom_room/CustomRoom";
import AIDesigner from "./components/configurator/ai_designer/AIDesigner";
import { Header, QuoteRequestPage, SceneData } from "./components/common";
import LoginModal from "./components/common/LoginModal";
import UserDashboard from "./components/user/UserDashboard";
import AdminDashboard from "./components/admin/AdminDashboard";
import authService from "./controllers/api/auth/authService";

interface ApiStatus {
  message: string;
  status: "success" | "error";
}

type ViewType =
  | "3d"
  | "planner"
  | "template-selection"
  | "custom-room"
  | "ai-design";

function MainApp() {
  const navigate = useNavigate();
  const location = useLocation();
  const [apiStatus, setApiStatus] = useState<ApiStatus | null>(null);
  const [currentView, setCurrentView] = useState<ViewType>("planner");
  const [aiDesignerTitle, setAiDesignerTitle] = useState<string>("AI Designer");
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [sceneIdToLoad, setSceneIdToLoad] = useState<number | null>(null);

  // Ref to communicate with Bathroom3DViewer
  const [bathroom3DViewerRef, setBathroom3DViewerRef] = useState<any>(null);

  useEffect(() => {
    const testBackendConnection = async () => {
      try {
        const result = await systemController.testConnection();
        setApiStatus({
          message: result.message,
          status: result.isConnected ? "success" : "error"
        });
      } catch (error: any) {
        setApiStatus({
          message: error.message || "Unexpected error occurred",
          status: "error"
        });
      }
    };

    // Check authentication status
    setIsAuthenticated(authService.isAuthenticated());

    testBackendConnection();
  }, []);

  // Handle sceneId from navigation state (when loading a scene from dashboard)
  useEffect(() => {
    if (location.state && location.state.sceneId) {
      setSceneIdToLoad(location.state.sceneId);
      setCurrentView('3d');
      // Clear the state to avoid reloading on subsequent renders
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const handleNavigate = (view: string) => {
    setCurrentView(view as ViewType);
  };

  const handleNavigateHome = () => {
    setCurrentView('planner');
  };

  const handleNavigateLogin = () => {
    if (isAuthenticated) {
      navigate('/dashboard');
    } else {
      setIsLoginModalOpen(true);
    }
  };

  const handleLoginSuccess = async () => {
    setIsAuthenticated(true);
    setIsLoginModalOpen(false);
    
    // Check if user is admin and redirect accordingly
    if (authService.isAdmin()) {
      navigate('/admin');
    } else {
      navigate('/dashboard');
    }
  };

  const handleRequestQuote = async () => {
    if (bathroom3DViewerRef) {
      const snapshot = bathroom3DViewerRef.captureSnapshot();
      const sceneData = await bathroom3DViewerRef.getSceneData();
      navigate('/quote', { state: { sceneData, sceneSnapshot: snapshot, returnPath: '/' } });
    } else {
      // Fallback if ref is not ready
      navigate('/quote', { state: { returnPath: '/' } });
    }
  };


  if (currentView === "planner") {
    return (
      <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <Header 
          onNavigateHome={handleNavigateHome}
          onNavigateLogin={handleNavigateLogin}
          title="Home"
          disableHomeButton={true}
        />
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <BathroomPlanner onNavigate={handleNavigate} />
        </div>
        <LoginModal
          isOpen={isLoginModalOpen}
          onClose={() => setIsLoginModalOpen(false)}
          onLoginSuccess={handleLoginSuccess}
        />
      </div>
    );
  }

  if (currentView === "template-selection") {
    return (
      <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <Header 
          onNavigateHome={handleNavigateHome}
          onNavigateLogin={handleNavigateLogin}
          showBackButton={true}
          onNavigateBack={() => handleNavigate('planner')}
          title="Template Selection"
        />
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <TemplateSelection onNavigate={handleNavigate} />
        </div>
        <LoginModal
          isOpen={isLoginModalOpen}
          onClose={() => setIsLoginModalOpen(false)}
          onLoginSuccess={handleLoginSuccess}
        />
      </div>
    );
  }


  if (currentView === "custom-room") {
    return (
      <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <Header 
          onNavigateHome={handleNavigateHome}
          onNavigateLogin={handleNavigateLogin}
          showBackButton={true}
          onNavigateBack={() => handleNavigate('planner')}
          title="Custom Room"
        />
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <CustomRoom onNavigate={handleNavigate} />
        </div>
        <LoginModal
          isOpen={isLoginModalOpen}
          onClose={() => setIsLoginModalOpen(false)}
          onLoginSuccess={handleLoginSuccess}
        />
      </div>
    );
  }

  if (currentView === "ai-design") {
    return (
      <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <Header
          onNavigateHome={handleNavigateHome}
          onNavigateLogin={handleNavigateLogin}
          showBackButton={true}
          onNavigateBack={() => handleNavigate('planner')}
          title={aiDesignerTitle}
        />
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <AIDesigner onNavigate={handleNavigate} />
        </div>
        <LoginModal
          isOpen={isLoginModalOpen}
          onClose={() => setIsLoginModalOpen(false)}
          onLoginSuccess={handleLoginSuccess}
        />
      </div>
    );
  }

  if (currentView === "3d") {
    return (
      <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <Header 
          onNavigateHome={handleNavigateHome}
          onNavigateLogin={handleNavigateLogin}
          showBackButton={true}
          onNavigateBack={() => handleNavigate('planner')}
          title="3D Designer"
          showQuoteButton={true}
          onRequestQuote={handleRequestQuote}
        />
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <Bathroom3DViewer 
            ref={setBathroom3DViewerRef}
            sceneIdToLoad={sceneIdToLoad}
            onSceneLoaded={() => setSceneIdToLoad(null)}
            onRequestQuote={(sceneData, snapshot) => {
              navigate('/quote', { state: { sceneData, sceneSnapshot: snapshot, returnPath: '/' } });
            }}
          />
        </div>
        <LoginModal
          isOpen={isLoginModalOpen}
          onClose={() => setIsLoginModalOpen(false)}
          onLoginSuccess={handleLoginSuccess}
        />
      </div>
    );
  }

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
      <Header 
        onNavigateHome={handleNavigateHome}
        onNavigateLogin={handleNavigateLogin}
      />
      <LoginModal
        isOpen={isLoginModalOpen}
        onClose={() => setIsLoginModalOpen(false)}
        onLoginSuccess={handleLoginSuccess}
      />
    </div>
  );
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainApp />} />
        <Route path="/dashboard" element={<UserDashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/quote" element={<QuoteRequestPage />} />
      </Routes>
    </Router>
  );
}

export default App;
