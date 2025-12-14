import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, useNavigate } from "react-router-dom";
import { systemController } from "./controllers/configuration";
import { Bathroom3DViewer } from "./components/3d";
import BathroomPlanner from "./components/configurator/bathroom_planner/BathroomPlanner";
import TemplateSelection from "./components/configurator/template_selection/TemplateSelection";
import "./index.css";
import CustomRoom from "./components/configurator/custom_room/CustomRoom";
import AIDesigner from "./components/configurator/ai_designer/AIDesigner";
import { Header, QuoteRequestModal, SceneData, QuoteFormData } from "./components/common";
import { quoteService } from "./controllers/api/quote/QuoteService";
import LoginModal from "./components/common/LoginModal";
import UserDashboard from "./components/user/UserDashboard";
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
  const [apiStatus, setApiStatus] = useState<ApiStatus | null>(null);
  const [currentView, setCurrentView] = useState<ViewType>("planner");
  const [aiDesignerTitle, setAiDesignerTitle] = useState<string>("AI Designer");
  const [isQuoteModalOpen, setIsQuoteModalOpen] = useState(false);
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [quoteSceneData, setQuoteSceneData] = useState<SceneData | null>(null);
  const [sceneSnapshot, setSceneSnapshot] = useState<string | undefined>(undefined);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

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

  const handleNavigate = (view: string) => {
    setCurrentView(view as ViewType);
  };

  const handleNavigateHome = () => {
    setCurrentView('planner');
  };if (isAuthenticated) {
      navigate('/dashboard');
    } else {
      setIsLoginModalOpen(true);
    }
  };

  const handleLoginSuccess = async () => {
    if (bathroom3DViewerRef) {
      const snapshot = bathroom3DViewerRef.captureSnapshot();
      const sceneData = awaitogin = () => {
    // Login functionality to be implemented later
    console.log('Login clicked - not yet implemented');
  };

  const handleRequestQuote = () => {
    if (bathroom3DViewerRef) {
      const snapshot = bathroom3DViewerRef.captureSnapshot();
      const sceneData = bathroom3DViewerRef.getSceneData();
      setQuoteSceneData(sceneData);
      setSceneSnapshot(snapshot || undefined);
      setIsQuoteModalOpen(true);
    } else {
      // Fallback if ref is not ready
      setIsQuoteModalOpen(true);
    }
  };

  const handleQuoteSubmit = async (formData: QuoteFormData) => {
    try {
      const response = await quoteService.submitQuoteRequest(formData);
      
      if (response.success) {
        alert(`Quote request submitted successfully!\n\nAn account has been created with email: ${response.userEmail}\n\nYour quote request has been sent to our industry partners.`);
        setIsQuoteModalOpen(false);
      } else {
        throw new Error(response.message);
      }
    } catch (error: any) {
      throw error; // Re-throw to be handled by the modal
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
          showQuoteButton={true}
          onRequestQuote={handleRequestQuote}
        />
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <Bathroom3DViewer 
            onRequestQuote={(sceneData, snapshot) => {
              setQuoteSceneData(sceneData);
              setSceneSnapshot(snapshot);
              setIsQuoteModalOpen(true);
            }}
          />
        </div>
        {quoteSceneData && (
          <QuoteRequestModal
            isOpen={isQuoteModalOpen}
            onClose={() => setIsQuoteModalOpen(false)}
            sceneData={quoteSceneData}
            sceneSnapshot={sceneSnapshot}
            onSubmit={handleQuoteSubmit}
          />
        )}
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
            onRequestQuote={(sceneData, snapshot) => {
              setQuoteSceneData(sceneData);
              setSceneSnapshot(snapshot);
              setIsQuoteModalOpen(true);
            }}
          />
        </div>
        {quoteSceneData && (
          <QuoteRequestModal
            isOpen={isQuoteModalOpen}
            onClose={() => setIsQuoteModalOpen(false)}
            sceneData={quoteSceneData}
            sceneSnapshot={sceneSnapshot}
            onSubmit={handleQuoteSubmit}
          />
        )}
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
      </Routes>
    </Routeritle="Home"
      />
      <div style={{ flex: 1, overflow: 'hidden' }}>
        <BathroomPlanner onNavigate={handleNavigate} />
      </div>
    </div>
  );
}

export default App;
