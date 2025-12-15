import React from "react";
import "./HelpModal.css";

interface HelpModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentPage: string;
  aiDesignerStep?: number;
}

interface HelpContent {
  title: string;
  sections: {
    subtitle: string;
    features: {
      name: string;
      description: string;
    }[];
  }[];
}

const helpContent: { [key: string]: HelpContent } = {
  home: {
    title: "Bathroom Planner - Home",
    sections: [
      {
        subtitle: "Getting Started",
        features: [
          {
            name: "Choose a Template",
            description: "Browse and select from pre-designed bathroom templates. This is the quickest way to start your project with professionally designed layouts that you can customize further.",
          },
          {
            name: "Custom Room",
            description: "Create a bathroom from scratch by defining your room's exact dimensions, shape, and layout. Perfect for non-standard spaces or when you want complete control over the design.",
          },
          {
            name: "AI Designer",
            description: "Let our AI create a personalized bathroom design based on your preferences, budget, and style choices. Answer a few questions and get instant design suggestions.",
          },
        ],
      },
      {
        subtitle: "Navigation",
        features: [
          {
            name: "User Account",
            description: "Click the user icon in the top-right corner to log in or access your dashboard where you can manage your saved designs and quotes.",
          },
        ],
      },
    ],
  },
  "template-selection": {
    title: "Template Selection",
    sections: [
      {
        subtitle: "Available Features",
        features: [
          {
            name: "Browse Templates",
            description: "Scroll through various pre-designed bathroom layouts. Each template shows a preview and key specifications like dimensions and included fixtures.",
          },
          {
            name: "3D Preview",
            description: "Click on any template to view it in interactive 3D. Rotate and zoom to examine the design from all angles before making your selection.",
          },
          {
            name: "Select Template",
            description: "Click 'Select This Template' to load it into the 3D designer where you can further customize colors, materials, and product choices.",
          },
          {
            name: "Back Button",
            description: "Use the back arrow in the header to return to the home page and choose a different starting point.",
          },
        ],
      },
    ],
  },
  "custom-room": {
    title: "Custom Room Designer",
    sections: [
      {
        subtitle: "Room Creation",
        features: [
          {
            name: "2D Floor Plan Editor",
            description: "Click on the canvas to place vertices and create your custom room shape. You can create any polygon shape - rectangular, L-shaped, or complex layouts. The floor area is automatically calculated and displayed in square meters.",
          },
          {
            name: "Reset Shape",
            description: "Use the 'Reset Shape' button in 2D view to clear your current shape and start over with a default square room.",
          },
          {
            name: "View Mode Toggle",
            description: "Switch between 2D (top-down floor plan) and 3D (perspective view) using the view mode buttons. 2D is best for drawing the shape, 3D is for visualizing the room.",
          },
          {
            name: "Room Height Slider",
            description: "In 3D view, adjust the room height using the slider (range: 1.5m to 4m). The default height is 2 meters. This controls the ceiling height of your bathroom.",
          },
          {
            name: "Floor Area Display",
            description: "The total floor area of your room is shown in both 2D and 3D views, updated automatically as you modify the shape.",
          },
          {
            name: "Start Furnishing",
            description: "Once you're satisfied with your room shape and height, click 'Start Furnishing' to save your custom room and proceed to the 3D designer where you can add fixtures, furniture, and finishes.",
          },
          {
            name: "Go Back",
            description: "Click 'Go Back' to return to the home page and choose a different design method.",
          },
        ],
      },
    ],
  },
  "ai-design": {
    title: "AI Designer",
    sections: [
      {
        subtitle: "Getting Started",
        features: [
          {
            name: "Step-by-Step Process",
            description: "The AI Designer guides you through 6 steps to create your perfect bathroom: Style, Colors, Features, Price Range, Room Shape, and Summary. Each step helps the AI understand your preferences.",
          },
          {
            name: "Navigation",
            description: "Use 'Next' to proceed to the next step, 'Back' to review previous choices, or 'Cancel' to return home. You must complete each step before moving forward.",
          },
          {
            name: "Generate Design",
            description: "After completing all steps, click 'Generate My Bathroom' to let the AI create a custom design based on your preferences. This takes a few moments.",
          },
        ],
      },
    ],
  },
  "ai-design-step-1": {
    title: "AI Designer - Step 1: Choose Your Style",
    sections: [
      {
        subtitle: "Style Selection",
        features: [
          {
            name: "Available Styles",
            description: "Choose from Modern, Classic, Minimalist, Luxury, Industrial, or Scandinavian styles. Each style influences the overall aesthetic, product choices, and design elements.",
          },
          {
            name: "What This Affects",
            description: "Your style choice guides the AI in selecting appropriate fixtures, colors, materials, and layout patterns that match the chosen aesthetic.",
          },
        ],
      },
    ],
  },
  "ai-design-step-2": {
    title: "AI Designer - Step 2: Select Color Palette",
    sections: [
      {
        subtitle: "Color Preferences",
        features: [
          {
            name: "Choose Colors",
            description: "Select one or more color palettes that appeal to you. Options typically include neutral tones, bold colors, earth tones, monochromatic schemes, and more.",
          },
          {
            name: "Multiple Selections",
            description: "You can select multiple color palettes. The AI will blend these preferences when choosing wall coverings, floor materials, and product colors.",
          },
        ],
      },
    ],
  },
  "ai-design-step-3": {
    title: "AI Designer - Step 3: Pick Your Features",
    sections: [
      {
        subtitle: "Bathroom Features",
        features: [
          {
            name: "Essential Fixtures",
            description: "Select which fixtures you want in your bathroom: bathtub, shower, toilet (WC), sink/basin, storage furniture, accessories, and more.",
          },
          {
            name: "Multiple Features",
            description: "Choose all the features you need. The AI will arrange them optimally in your space and ensure they work together functionally and aesthetically.",
          },
        ],
      },
    ],
  },
  "ai-design-step-4": {
    title: "AI Designer - Step 4: Select Price Range",
    sections: [
      {
        subtitle: "Budget Selection",
        features: [
          {
            name: "Price Ranges",
            description: "Choose from LOW (economy/budget-friendly), MEDIUM (mid-range quality), or HIGH (premium/luxury) price ranges. This guides product selection.",
          },
          {
            name: "Product Quality",
            description: "The AI will select products from your chosen price category, ensuring your design matches your budget while maintaining quality and style.",
          },
        ],
      },
    ],
  },
  "ai-design-step-5": {
    title: "AI Designer - Step 5: Design Your Room Shape",
    sections: [
      {
        subtitle: "Room Configuration",
        features: [
          {
            name: "Draw Room Shape",
            description: "Click on the 2D canvas to create vertices and define your bathroom's floor plan. Create any shape: rectangle, square, L-shape, or complex polygons.",
          },
          {
            name: "View Modes",
            description: "Toggle between 2D (for drawing) and 3D (for visualization) using the view buttons. The floor area is calculated automatically.",
          },
          {
            name: "Room Height",
            description: "In 3D view, use the slider to set your room height (1.5m to 4m). Default is 2 meters.",
          },
          {
            name: "Reset if Needed",
            description: "Click 'Reset Shape' in 2D view to start over with a default square room.",
          },
        ],
      },
    ],
  },
  "ai-design-step-6": {
    title: "AI Designer - Step 6: Review Your Preferences",
    sections: [
      {
        subtitle: "Summary Review",
        features: [
          {
            name: "Review All Choices",
            description: "This step shows a summary of all your selections: style, colors, features, price range, and room dimensions. Review everything before generating.",
          },
          {
            name: "Make Changes",
            description: "If you want to adjust any preference, use the 'Back' button to return to previous steps and modify your choices.",
          },
          {
            name: "Generate Design",
            description: "Once satisfied, click 'Generate My Bathroom'. The AI will process your preferences and create a custom bathroom design, then take you to the 3D designer to view and refine it.",
          },
        ],
      },
    ],
  },
  "3d-designer": {
    title: "3D Designer & Editor",
    sections: [
      {
        subtitle: "View Controls",
        features: [
          {
            name: "2D Top View",
            description: "Overhead view showing your bathroom from above. Perfect for precise product placement and seeing the overall layout. Click on products to select them.",
          },
          {
            name: "3D-Person View",
            description: "First-person perspective that simulates being in the bathroom. Use WASD keys to move around and mouse to look. Great for experiencing the space realistically.",
          },
          {
            name: "3D-Free View",
            description: "Free camera control with full orbit capability. Left-click and drag to rotate, scroll to zoom, right-click and drag to pan. This is the default view.",
          },
        ],
      },
      {
        subtitle: "Product Management",
        features: [
          {
            name: "Model Browser",
            description: "Browse products by category in the right panel: Bathtubs, Showers, Basins (sinks), WCs (toilets), Furniture, Fittings, Accessories, and more. Each product shows a thumbnail and details.",
          },
          {
            name: "Add Products",
            description: "Click 'Add to Scene' on any product to place it in your bathroom. Products are initially placed at the center and can be moved immediately.",
          },
          {
            name: "Select & Move Products",
            description: "Click on any product in the 3D view to select it (turns green). Drag the selected product to move it around your bathroom. Use the position controls in the left panel for precise placement.",
          },
          {
            name: "Rotate Products",
            description: "With a product selected, use the rotation slider in the left panel to rotate it. Values are in degrees (0-360). Rotate products to face the right direction.",
          },
          {
            name: "Scale Products",
            description: "Adjust product size using the scale slider (0.5x to 2x). This allows you to fit products better or create different size variations.",
          },
          {
            name: "Color Selection",
            description: "When a product is selected and has multiple color options, choose from available colors in the product panel. The 3D model updates instantly.",
          },
          {
            name: "Delete Products",
            description: "Select a product and click the 'Delete Selected' button in the left panel to remove it from your scene.",
          },
        ],
      },
      {
        subtitle: "Materials & Finishes",
        features: [
          {
            name: "Wall Coverings",
            description: "Click on a wall in 3D view to select it, then browse the Coverings category in the product browser. Click 'Apply to Selected Wall' to add tiles, paint, or other finishes. Each wall can have a different covering.",
          },
          {
            name: "Floor Coverings",
            description: "Click the floor to select it, then choose from floor materials in the Coverings category. Apply ceramic tiles, stone, wood-look tiles, or other flooring options.",
          },
          {
            name: "Texture Repeat",
            description: "After applying a covering, you can adjust how the texture repeats on the surface for the perfect scale and look.",
          },
        ],
      },
      {
        subtitle: "Scene Management",
        features: [
          {
            name: "Save Scene",
            description: "Save your design to your account by clicking the 'Save Scene' button. You must be logged in. Scenes auto-save periodically to prevent losing your work.",
          },
          {
            name: "Load Scene",
            description: "Access your saved scenes from the user dashboard. Click 'Load Scene' to continue working on any previously saved design.",
          },
          {
            name: "Scene Name",
            description: "Give your design a meaningful name in the scene settings. This helps you identify it later in your saved scenes list.",
          },
        ],
      },
      {
        subtitle: "Sharing & Quotes",
        features: [
          {
            name: "Request Quote",
            description: "Click the 'Request Quote' button in the header to generate a detailed quote including all products, materials, room dimensions, and an image of your design. This sends your request to industry partners.",
          },
          {
            name: "Scene Snapshot",
            description: "Your current 3D view is captured when requesting a quote, providing contractors with a visual reference of your design.",
          },
        ],
      },
    ],
  },
  dashboard: {
    title: "User Dashboard",
    sections: [
      {
        subtitle: "Your Designs",
        features: [
          {
            name: "My Scenes Tab",
            description: "View all your saved bathroom designs. Each scene card shows the scene name, whether it's public or private, creation date, and last modified time.",
          },
          {
            name: "Load Scene",
            description: "Click 'Load Scene' button on any saved design to open it in the 3D designer and continue editing.",
          },
          {
            name: "Delete Scene",
            description: "Remove designs you no longer need by clicking the delete button. This action cannot be undone.",
          },
          {
            name: "Create New Scene",
            description: "Start a fresh bathroom design by clicking the '+ Create New Scene' button in the header, which takes you to the planner home page.",
          },
        ],
      },
      {
        subtitle: "Quote Requests",
        features: [
          {
            name: "Quote Requests Tab",
            description: "Access all quote requests you've submitted. View the status (Pending, In Progress, Completed, or Rejected) of each request.",
          },
          {
            name: "View Quote Details",
            description: "Click 'View Details & History' on any quote to see the complete information including product list, room dimensions, admin responses, and any uploaded documents.",
          },
          {
            name: "Status Tracking",
            description: "Monitor the progress of your quotes. Color-coded status badges show whether your quote is pending review, being processed, completed, or has been rejected.",
          },
        ],
      },
      {
        subtitle: "Account",
        features: [
          {
            name: "Help",
            description: "Click the '❓ Help' button at any time to access context-sensitive help for the current page.",
          },
          {
            name: "Logout",
            description: "Securely sign out of your account when you're finished. Your saved scenes and quotes are preserved.",
          },
        ],
      },
    ],
  },
  admin: {
    title: "Admin Dashboard",
    sections: [
      {
        subtitle: "Quote Management",
        features: [
          {
            name: "Quote Requests Tab",
            description: "View all incoming quote requests from users. See design details, room dimensions, product lists, and customer information for each request.",
          },
          {
            name: "Review & Update Quotes",
            description: "Click on a quote request to view full details. Update the status (PENDING, IN_PROGRESS, COMPLETED, REJECTED) and add admin responses or notes.",
          },
          {
            name: "Upload Documents",
            description: "Attach PDF documents, detailed quotes, or other files to quote requests. Users can view and download these documents from their dashboard.",
          },
          {
            name: "Customer Information",
            description: "View requester details including name, email, phone, company, and any additional notes they provided.",
          },
        ],
      },
      {
        subtitle: "User Management",
        features: [
          {
            name: "Users Tab",
            description: "View all registered users with their ID, name, email, company, role (USER or ADMIN), number of scenes created, quote requests made, and registration date.",
          },
          {
            name: "User Roles",
            description: "See user roles displayed with color-coded badges. Users can be regular users or administrators.",
          },
        ],
      },
      {
        subtitle: "Scene Management",
        features: [
          {
            name: "Scenes Tab",
            description: "View all bathroom designs created by users. See scene ID, name, creator information, public/private status, and creation date.",
          },
          {
            name: "Monitor Activity",
            description: "Track user engagement and popular design trends through the scenes they create.",
          },
        ],
      },
      {
        subtitle: "Product Management",
        features: [
          {
            name: "Products Tab",
            description: "Manage the product catalog. View all available products with their categories, prices, and availability status.",
          },
          {
            name: "Create Product",
            description: "Add new bathroom products to the catalog by filling in product details, selecting category, price range, mounting type, and model paths.",
          },
          {
            name: "Edit Products",
            description: "Modify existing product details, update information, or change availability.",
          },
          {
            name: "Delete Products",
            description: "Remove products from the active catalog. Products used in existing scenes are preserved but won't appear for new designs.",
          },
        ],
      },
      {
        subtitle: "Navigation",
        features: [
          {
            name: "Help Button",
            description: "Click the '❓ Help' button in the header to access this help information at any time.",
          },
          {
            name: "Logout",
            description: "Sign out of the admin account securely.",
          },
        ],
      },
    ],
  },
  quote: {
    title: "Quote Request",
    sections: [
      {
        subtitle: "Request Details",
        features: [
          {
            name: "Design Summary",
            description: "View a summary of your bathroom design including all selected products, materials, and a 3D snapshot.",
          },
          {
            name: "Contact Information",
            description: "Provide or confirm your contact details so we can send you the quote and follow up if needed.",
          },
          {
            name: "Additional Notes",
            description: "Add any special requirements, questions, or preferences for the quote. Mention timeline, installation needs, or budget concerns.",
          },
          {
            name: "Product List",
            description: "Review the complete list of products included in your design with quantities and current catalog prices.",
          },
          {
            name: "Submit Request",
            description: "Send your quote request to our team. You'll receive a confirmation email and can track the status in your dashboard.",
          },
        ],
      },
      {
        subtitle: "After Submission",
        features: [
          {
            name: "Track Status",
            description: "Check your dashboard to see when your quote is being reviewed, processed, or completed.",
          },
          {
            name: "Modify Request",
            description: "If you need changes before we process it, you can update your request or create a new one.",
          },
        ],
      },
    ],
  },
};

const HelpModal: React.FC<HelpModalProps> = ({ isOpen, onClose, currentPage, aiDesignerStep }) => {
  if (!isOpen) return null;

  // Determine which help content to show
  let pageKey = currentPage;
  if (currentPage === "ai-design" && aiDesignerStep) {
    pageKey = `ai-design-step-${aiDesignerStep}`;
  }

  const content = helpContent[pageKey] || helpContent["home"];

  return (
    <div className="help-modal-overlay" onClick={onClose}>
      <div className="help-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="help-modal-header">
          <h2>{content.title}</h2>
          <button className="help-modal-close" onClick={onClose} aria-label="Close help">
            ✕
          </button>
        </div>

        <div className="help-modal-body">
          {content.sections.map((section, sectionIdx) => (
            <div key={sectionIdx} className="help-section">
              <h3 className="help-section-title">{section.subtitle}</h3>
              <div className="help-features">
                {section.features.map((feature, featureIdx) => (
                  <div key={featureIdx} className="help-feature">
                    <h4 className="help-feature-name">
                      {feature.name}
                    </h4>
                    <p className="help-feature-description">{feature.description}</p>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>

        <div className="help-modal-footer">
          <button className="help-modal-close-button" onClick={onClose}>
            Got it!
          </button>
        </div>
      </div>
    </div>
  );
};

export default HelpModal;
