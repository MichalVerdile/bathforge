import React, { useState, useEffect } from "react";
import { systemController } from "./controllers/configuration";
import { Bathroom3DViewer } from "./components/3d";
import BathroomPlanner from "./components/configurator/bathroom_planner/BathroomPlanner";
import TemplateSelection from "./components/configurator/template_selection/TemplateSelection";
import "./index.css";
import CustomRoom from "./components/configurator/custom_room/CustomRoom";
import { Header } from "./components/common";

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

function App() {
  const [apiStatus, setApiStatus] = useState<ApiStatus | null>(null);
  const [currentView, setCurrentView] = useState<ViewType>("planner");

  useEffect(() => {
    const testBackendConnection = async () => {
      try {
        const result = await systemController.testConnection();
        setApiStatus({
          message: result.message,
          status: result.isConnected ? "success" : "error",
        });
      } catch (error: any) {
        setApiStatus({
          message: error.message || "Unexpected error occurred",
          status: "error",
        });
      }
    };

    testBackendConnection();
  }, []);

  const handleNavigate = (view: string) => {
    setCurrentView(view as ViewType);
  };

  const handleNavigateHome = () => {
    setCurrentView("planner");
  };

  const handleNavigateLogin = () => {
    // Login functionality to be implemented later
    console.log("Login clicked - not yet implemented");
  };

  if (currentView === "planner") {
    return <BathroomPlanner onNavigate={handleNavigate} />;
  }

  if (currentView === "template-selection") {
    return <TemplateSelection onNavigate={handleNavigate} />;
  }

  if (currentView === "custom-room") {
    return <CustomRoom onNavigate={handleNavigate} />;
  }

  if (currentView === "ai-design") {
    return (
      <div style={{ padding: "2rem", textAlign: "center" }}>
        <h1>AI Bathroom Designer</h1>
        <p>Let our AI create the perfect bathroom for you</p>
        <button onClick={() => handleNavigate("planner")}>
          Back to Planner
        </button>
        <button
          onClick={() => handleNavigate("3d")}
          style={{ marginLeft: "1rem" }}
        >
          Continue to 3D Designer
        </button>
      </div>
    );
  }

  if (currentView === "3d") {
    return (
      <div
        style={{ height: "100vh", display: "flex", flexDirection: "column" }}
      >
        <header
          style={{
            background: "#2c3e50",
            color: "white",
            padding: "10px 20px",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            flexShrink: 0,
          }}
        >
          <h1 style={{ margin: 0, fontSize: "24px" }}>🛁 BathForge 3D</h1>
          <button
            onClick={() => handleNavigate("planner")}
            style={{
              background: "#3498db",
              color: "white",
              border: "none",
              padding: "8px 16px",
              borderRadius: "4px",
              cursor: "pointer",
              fontSize: "14px",
            }}
          >
            ← Back to Planner
          </button>
        </header>

        <div style={{ flex: 1, overflow: "hidden" }}>
          <Bathroom3DViewer />
        </div>
      </div>
    );
  }

  return (
    <div
      style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}
    >
      <Header
        onNavigateHome={handleNavigateHome}
        onNavigateLogin={handleNavigateLogin}
        title="Home"
      />
      <div style={{ flex: 1 }}>
        <BathroomPlanner onNavigate={handleNavigate} />
      </div>
    </div>
  );
}

export default App;
