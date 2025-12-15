import React, { useState } from "react";
import "./QuoteRequestModal.css";

export interface SceneData {
  sceneId: string;
  roomDimensions?: string;
  wallLengths?: Array<{
    wall: number;
    length: number;
  }>;
  roomData?: {
    verticesData: string;
    roomHeight: number;
    roomProperties?: string;
  };
  products: Array<{
    name: string;
    category: string;
    color?: string;
    position?: string;
    productId?: number;
    colorId?: number;
    positionX?: number;
    positionY?: number;
    positionZ?: number;
    rotationX?: number;
    rotationY?: number;
    rotationZ?: number;
    scaleX?: number;
    scaleY?: number;
    scaleZ?: number;
  }>;
  coverings: Array<{
    type: string;
    name: string;
    color?: string;
    productId?: number;
    surfaceIdentifier?: string;
    repeatX?: number;
    repeatY?: number;
  }>;
}

interface QuoteRequestModalProps {
  isOpen: boolean;
  onClose: () => void;
  sceneData: SceneData;
  onSubmit: (formData: QuoteFormData) => Promise<void>;
  sceneSnapshot?: string; // Base64 encoded image
}

export interface QuoteFormData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  company?: string;
  additionalNotes?: string;
  sceneId: string;
  roomDimensions?: string;
  wallLengths?: Array<{
    wall: number;
    length: number;
  }>;
  roomData?: {
    verticesData: string;
    roomHeight: number;
    roomProperties?: string;
  };
  products: any[];
  coverings: any[];
  sceneSnapshot?: string;
}

const QuoteRequestModal: React.FC<QuoteRequestModalProps> = ({
  isOpen,
  onClose,
  sceneData,
  onSubmit,
  sceneSnapshot,
}) => {
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    phone: "",
    company: "",
    additionalNotes: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<"form" | "preview">("form");

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (formData.password.length < 8) {
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

      await onSubmit(submitData);
      
      // Reset form on success
      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        phone: "",
        company: "",
        additionalNotes: "",
      });
      
      onClose();
    } catch (err: any) {
      setError(err.message || "Failed to submit quote request. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="quote-modal-overlay" onClick={onClose}>
      <div className="quote-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="quote-modal-header">
          <h2>Request for Quote</h2>
          <button className="quote-modal-close" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="quote-modal-tabs">
          <button
            className={`quote-tab ${activeTab === "form" ? "active" : ""}`}
            onClick={() => setActiveTab("form")}
          >
            Your Information
          </button>
          <button
            className={`quote-tab ${activeTab === "preview" ? "active" : ""}`}
            onClick={() => setActiveTab("preview")}
          >
            Scene Details
          </button>
        </div>

        <div className="quote-modal-body">
          {activeTab === "form" ? (
            <div className="quote-form-content">
              <div className="quote-info-message">
                <strong>Note:</strong> By submitting this request, an account will
                be created for you to track your quote.
              </div>

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
                    />
                  </div>
                </div>

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
                    onClick={onClose}
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
            <div className="quote-scene-preview">
              {sceneSnapshot && (
                <div className="quote-snapshot">
                  <h3>Scene Preview</h3>
                  <img src={sceneSnapshot} alt="Scene snapshot" />
                </div>
              )}

              {sceneData.roomDimensions && (
                <div className="quote-section">
                  <h3>Room Dimensions</h3>
                  <p>{sceneData.roomDimensions}</p>
                </div>
              )}

              {sceneData.products && sceneData.products.length > 0 && (
                <div className="quote-section">
                  <h3>Selected Products ({sceneData.products.length})</h3>
                  <div className="quote-products-list">
                    {sceneData.products.map((product, index) => (
                      <div key={index} className="quote-product-item">
                        <div className="product-name">{product.name}</div>
                        <div className="product-details">
                          <span className="product-category">{product.category}</span>
                          {product.color && (
                            <span className="product-color">Color: {product.color}</span>
                          )}
                          {product.position && (
                            <span className="product-position">
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
                <div className="quote-section">
                  <h3>Wall & Floor Coverings ({sceneData.coverings.length})</h3>
                  <div className="quote-coverings-list">
                    {sceneData.coverings.map((covering, index) => (
                      <div key={index} className="quote-covering-item">
                        <div className="covering-type">{covering.type}</div>
                        <div className="covering-details">
                          <span className="covering-name">{covering.name}</span>
                          {covering.color && (
                            <span className="covering-color">
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
  );
};

export default QuoteRequestModal;
