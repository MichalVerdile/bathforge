import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { SceneData, QuoteFormData } from "./QuoteRequestModal";
import { quoteService } from "../../controllers/api/quote/QuoteService";
import authService from "../../controllers/api/auth/authService";
import "./QuoteRequestModal.css";
import Header from "./Header";
import HelpModal from "./HelpModal";

const QuoteRequestPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { sceneData, sceneSnapshot, returnPath } = location.state || {};

  // Check if user is logged in
  const currentUser = authService.getCurrentUser();
  const isLoggedIn = authService.isAuthenticated();

  const [formData, setFormData] = useState({
    firstName: currentUser?.firstName || "",
    lastName: currentUser?.lastName || "",
    email: currentUser?.email || "",
    password: "",
    phone: "",
    company: "",
    additionalNotes: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"form" | "preview">("form");
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [isHelpModalOpen, setIsHelpModalOpen] = useState(false);

  useEffect(() => {
    // If no scene data is provided, redirect back
    if (!sceneData) {
      navigate(-1);
    }
  }, [sceneData, navigate]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Only validate password if user is not logged in
    if (!isLoggedIn && formData.password.length < 8) {
      setError("Password must be at least 8 characters long");
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const submitData: QuoteFormData = {
        ...formData,
        sceneId: sceneData.sceneId,
        roomDimensions: sceneData.roomDimensions,
        wallLengths: sceneData.wallLengths,
        roomData: sceneData.roomData,
        products: sceneData.products,
        coverings: sceneData.coverings,
        sceneSnapshot,
      };

      const response = await quoteService.submitQuoteRequest(submitData);
      
      if (response.success) {
        // If user wasn't logged in and a token was returned, log them in automatically
        if (!isLoggedIn && response.token) {
          localStorage.setItem('bathforge_auth_token', response.token);
          if (response.userEmail) {
            const userData = {
              id: response.userId,
              email: response.userEmail,
              firstName: formData.firstName,
              lastName: formData.lastName,
              phone: formData.phone || null,
              company: formData.company || null,
            };
            localStorage.setItem('bathforge_user', JSON.stringify(userData));
          }
        }

        const message = isLoggedIn 
          ? "Your quote request has been sent to our industry partners."
          : `An account has been created with email: ${response.userEmail}.\n\nYour quote request has been sent to our industry partners.`;
        setSuccessMessage(message);
        setShowSuccessModal(true);
      } else {
        throw new Error(response.message);
      }
    } catch (err: any) {
      setError(err.message || "Failed to submit quote request. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!sceneData) {
    return null;
  }

  return (
    <>
      {/* Success Confirmation Modal */}
      {showSuccessModal && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.7)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 10000,
          }}
        >
          <div
            style={{
              background: '#1e293b',
              borderRadius: '12px',
              padding: '32px',
              maxWidth: '500px',
              width: '90%',
              boxShadow: '0 20px 60px rgba(0, 0, 0, 0.5)',
            }}
          >
            <div style={{ marginBottom: '24px', textAlign: 'center' }}>
              <div
                style={{
                  width: '64px',
                  height: '64px',
                  borderRadius: '50%',
                  background: '#10b981',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  margin: '0 auto 16px',
                  fontSize: '32px',
                }}
              >
                ✓
              </div>
              <h2
                style={{
                  fontSize: '24px',
                  fontWeight: '600',
                  color: '#ffffff',
                  margin: '0 0 12px 0',
                }}
              >
                Quote Request Submitted!
              </h2>
              <p
                style={{
                  fontSize: '14px',
                  color: '#94a3b8',
                  margin: 0,
                  whiteSpace: 'pre-line',
                  lineHeight: '1.6',
                }}
              >
                {successMessage}
              </p>
            </div>
            <div
              style={{
                display: 'flex',
                gap: '12px',
                marginTop: '24px',
              }}
            >
              <button
                style={{
                  flex: 1,
                  padding: '12px 24px',
                  background: '#3b82f6',
                  border: 'none',
                  borderRadius: '8px',
                  color: '#ffffff',
                  fontSize: '14px',
                  fontWeight: '500',
                  cursor: 'pointer',
                  transition: 'all 0.2s',
                }}
                onClick={() => {
                  setShowSuccessModal(false);
                  navigate('/dashboard');
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = '#2563eb';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = '#3b82f6';
                }}
              >
                Go to Dashboard
              </button>
              <button
                style={{
                  flex: 1,
                  padding: '12px 24px',
                  background: 'transparent',
                  border: '1px solid #475569',
                  borderRadius: '8px',
                  color: '#ffffff',
                  fontSize: '14px',
                  fontWeight: '500',
                  cursor: 'pointer',
                  transition: 'all 0.2s',
                }}
                onClick={() => {
                  setShowSuccessModal(false);
                  navigate('/');
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = '#334155';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = 'transparent';
                }}
              >
                Go to Home
              </button>
            </div>
          </div>
        </div>
      )}

    <div style={{ 
      height: '100vh', 
      display: 'flex', 
      flexDirection: 'column',
      background: '#1e293b',
    }}>
      <Header 
        onNavigateHome={() => navigate("/")}
        onNavigateLogin={() => {}}
        onOpenHelp={() => setIsHelpModalOpen(true)}
        showBackButton={true}
        onNavigateBack={() => navigate(-1)}
        title="Request for Quote"
      />
      
      <div style={{ 
        flex: 1, 
        overflow: 'auto', 
        padding: '20px',
      }}>
        <div style={{ 
          maxWidth: '900px', 
          margin: '0 auto',
        }}>
          <div style={{
            display: 'flex',
            gap: '8px',
            marginBottom: '20px',
          }}>
            <button
              style={{
                background: activeTab === "form" ? '#3b82f6' : 'transparent',
                border: 'none',
                padding: '12px 24px',
                fontSize: '14px',
                fontWeight: '500',
                color: activeTab === "form" ? '#ffffff' : '#94a3b8',
                cursor: 'pointer',
                borderRadius: '8px',
                transition: 'all 0.2s',
              }}
              onClick={() => setActiveTab("form")}
            >
              Your Information
            </button>
            <button
              style={{
                background: activeTab === "preview" ? '#3b82f6' : 'transparent',
                border: 'none',
                padding: '12px 24px',
                fontSize: '14px',
                fontWeight: '500',
                color: activeTab === "preview" ? '#ffffff' : '#94a3b8',
                cursor: 'pointer',
                borderRadius: '8px',
                transition: 'all 0.2s',
              }}
              onClick={() => setActiveTab("preview")}
            >
              Scene Details
            </button>
          </div>

          <div style={{
            padding: '32px',
          }}>
            {activeTab === "form" ? (
              <div className="quote-form-content">
                {!isLoggedIn && (
                  <div className="quote-info-message">
                    <strong>Note:</strong> By submitting this request, an account will
                    be created for you to track your quote.
                  </div>
                )}
                {isLoggedIn && (
                  <div className="quote-info-message" style={{ background: '#3b82f6', borderLeft: '4px solid #2563eb' }}>
                    <strong>Logged in as:</strong> {currentUser?.email}
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  <div className="quote-form-row">
                    <div className="quote-form-group">
                      <label htmlFor="firstName">
                        First Name <span className="required">*</span>
                      </label>
                      <input
                        type="text"
                        id="firstName"
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleChange}
                        required
                        disabled={isLoggedIn}
                      />
                    </div>

                    <div className="quote-form-group">
                      <label htmlFor="lastName">
                        Last Name <span className="required">*</span>
                      </label>
                      <input
                        type="text"
                        id="lastName"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleChange}
                        required
                        disabled={isLoggedIn}
                      />
                    </div>
                  </div>

                  {!isLoggedIn && (
                    <>
                      <div className="quote-form-group">
                        <label htmlFor="email">
                          Email <span className="required">*</span>
                        </label>
                        <input
                          type="email"
                          id="email"
                          name="email"
                          value={formData.email}
                          onChange={handleChange}
                          required
                        />
                      </div>

                      <div className="quote-form-group">
                        <label htmlFor="password">
                          Password <span className="required">*</span>
                          <span className="field-hint">
                            (Min. 8 characters)
                          </span>
                        </label>
                        <div className="password-input-wrapper">
                          <input
                            type={showPassword ? "text" : "password"}
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            minLength={8}
                            required
                          />
                          <button
                            type="button"
                            className="password-toggle"
                            onClick={() => setShowPassword(!showPassword)}
                          >
                            {showPassword ? "Hide" : "Show"}
                          </button>
                        </div>
                      </div>
                    </>
                  )}

                  <div className="quote-form-row">
                    <div className="quote-form-group">
                      <label htmlFor="phone">Phone</label>
                      <input
                        type="tel"
                        id="phone"
                        name="phone"
                        value={formData.phone}
                        onChange={handleChange}
                      />
                    </div>

                    <div className="quote-form-group">
                      <label htmlFor="company">Company</label>
                      <input
                        type="text"
                        id="company"
                        name="company"
                        value={formData.company}
                        onChange={handleChange}
                      />
                    </div>
                  </div>

                  <div className="quote-form-group">
                    <label htmlFor="additionalNotes">Additional Notes</label>
                    <textarea
                      id="additionalNotes"
                      name="additionalNotes"
                      value={formData.additionalNotes}
                      onChange={handleChange}
                      rows={4}
                      placeholder="Any special requirements or notes..."
                    />
                  </div>

                  {error && <div className="quote-error-message">{error}</div>}

                  <div className="quote-modal-footer">
                    <button
                      type="button"
                      className="quote-btn quote-btn-secondary"
                      onClick={() => returnPath ? navigate(returnPath) : navigate(-1)}
                      disabled={isSubmitting}
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="quote-btn quote-btn-primary"
                      disabled={isSubmitting}
                    >
                      {isSubmitting ? "Submitting..." : "Submit Quote Request"}
                    </button>
                  </div>
                </form>
              </div>
            ) : (
              <div style={{
                display: 'flex',
                flexDirection: 'column',
                gap: '24px',
              }}>
                {sceneSnapshot && (
                  <div>
                    <h3 style={{ 
                      color: '#f1f5f9', 
                      marginBottom: '16px',
                      fontSize: '18px',
                      fontWeight: '600',
                    }}>Scene Preview</h3>
                    <div style={{
                      background: '#0f172a',
                      borderRadius: '8px',
                      padding: '16px',
                      display: 'flex',
                      justifyContent: 'center',
                    }}>
                      <img 
                        src={sceneSnapshot} 
                        alt="Scene snapshot" 
                        style={{
                          maxWidth: '100%',
                          height: 'auto',
                          borderRadius: '4px',
                        }}
                      />
                    </div>
                  </div>
                )}

                {sceneData.roomDimensions && (
                  <div>
                    <h3 style={{ 
                      color: '#f1f5f9', 
                      marginBottom: '12px',
                      fontSize: '18px',
                      fontWeight: '600',
                    }}>Room Dimensions</h3>
                    <p style={{ 
                      color: '#cbd5e1',
                      fontSize: '14px',
                      lineHeight: '1.6',
                    }}>{sceneData.roomDimensions}</p>
                    {sceneData.wallLengths && sceneData.wallLengths.length > 0 && (
                      <div style={{ marginTop: '12px' }}>
                        <h4 style={{ 
                          color: '#f1f5f9', 
                          marginBottom: '8px',
                          fontSize: '14px',
                          fontWeight: '500',
                        }}>Wall Lengths</h4>
                        <div style={{
                          display: 'grid',
                          gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))',
                          gap: '8px',
                        }}>
                          {sceneData.wallLengths.map((wall: { wall: string | number; length: number }) => (
                            <div key={wall.wall} style={{
                              background: '#0f172a',
                              borderRadius: '6px',
                              padding: '8px 12px',
                              fontSize: '13px',
                            }}>
                              <span style={{ color: '#94a3b8' }}>Wall {wall.wall}: </span>
                              <span style={{ color: '#60a5fa', fontWeight: '500' }}>{wall.length} cm</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {sceneData.products && sceneData.products.length > 0 && (
                  <div>
                    <h3 style={{ 
                      color: '#f1f5f9', 
                      marginBottom: '16px',
                      fontSize: '18px',
                      fontWeight: '600',
                    }}>Selected Products ({sceneData.products.length})</h3>
                    <div style={{
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '12px',
                    }}>
                      {sceneData.products.map((product: { name: string | number | boolean | React.ReactElement<any, string | React.JSXElementConstructor<any>> | Iterable<React.ReactNode> | React.ReactPortal | null | undefined; category: string | number | boolean | React.ReactElement<any, string | React.JSXElementConstructor<any>> | Iterable<React.ReactNode> | React.ReactPortal | null | undefined; color: string | number | boolean | React.ReactElement<any, string | React.JSXElementConstructor<any>> | Iterable<React.ReactNode> | React.ReactPortal | null | undefined; position: string | number | boolean | React.ReactElement<any, string | React.JSXElementConstructor<any>> | Iterable<React.ReactNode> | React.ReactPortal | null | undefined; }, index: React.Key | null | undefined) => (
                        <div key={index} style={{
                          background: '#0f172a',
                          borderRadius: '8px',
                          padding: '16px',
                        }}>
                          <div style={{ 
                            color: '#f1f5f9',
                            fontWeight: '500',
                            marginBottom: '8px',
                          }}>{product.name}</div>
                          <div style={{
                            display: 'flex',
                            flexWrap: 'wrap',
                            gap: '12px',
                            fontSize: '13px',
                          }}>
                            <span style={{ 
                              color: '#60a5fa',
                              background: 'rgba(59, 130, 246, 0.1)',
                              padding: '4px 12px',
                              borderRadius: '4px',
                            }}>{product.category}</span>
                            {product.color && (
                              <span style={{ 
                                color: '#94a3b8',
                              }}>Color: {product.color}</span>
                            )}
                            {product.position && (
                              <span style={{ 
                                color: '#94a3b8',
                                fontSize: '12px',
                              }}>
                                Position: {product.position}
                              </span>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {sceneData.coverings && sceneData.coverings.length > 0 && (
                  <div>
                    <h3 style={{ 
                      color: '#f1f5f9', 
                      marginBottom: '16px',
                      fontSize: '18px',
                      fontWeight: '600',
                    }}>Wall & Floor Coverings ({sceneData.coverings.length})</h3>
                    <div style={{
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '12px',
                    }}>
                      {sceneData.coverings.map((covering: { type: string | number | boolean | React.ReactElement<any, string | React.JSXElementConstructor<any>> | Iterable<React.ReactNode> | React.ReactPortal | null | undefined; name: string | number | boolean | React.ReactElement<any, string | React.JSXElementConstructor<any>> | Iterable<React.ReactNode> | React.ReactPortal | null | undefined; color: string | number | boolean | React.ReactElement<any, string | React.JSXElementConstructor<any>> | Iterable<React.ReactNode> | React.ReactPortal | null | undefined; }, index: React.Key | null | undefined) => (
                        <div key={index} style={{
                          background: '#0f172a',
                          borderRadius: '8px',
                          padding: '16px',
                        }}>
                          <div style={{ 
                            color: '#f1f5f9',
                            fontWeight: '500',
                            marginBottom: '8px',
                          }}>{covering.type}</div>
                          <div style={{
                            display: 'flex',
                            flexWrap: 'wrap',
                            gap: '12px',
                            fontSize: '13px',
                          }}>
                            <span style={{ 
                              color: '#cbd5e1',
                            }}>{covering.name}</span>
                            {covering.color && (
                              <span style={{ 
                                color: '#94a3b8',
                              }}>
                                Color: {covering.color}
                              </span>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      <HelpModal
        isOpen={isHelpModalOpen}
        onClose={() => setIsHelpModalOpen(false)}
        currentPage="quote"
      />
    </div>
    </>
  );
};

export default QuoteRequestPage;
