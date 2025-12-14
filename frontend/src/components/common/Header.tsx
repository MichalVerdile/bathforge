import React from "react";
import { HiHome } from "react-icons/hi";
import { HiUser } from "react-icons/hi";
import { HiDocumentText } from "react-icons/hi";
import "./Header.css";

interface HeaderProps {
  onNavigateHome: () => void;
  onNavigateLogin?: () => void;
  showBackButton?: boolean;
  onNavigateBack?: () => void;
  title?: string;
  disableHomeButton?: boolean;
  showQuoteButton?: boolean;
  onRequestQuote?: () => void;
}

const Header: React.FC<HeaderProps> = ({
  onNavigateHome,
  onNavigateLogin,
  showBackButton = false,
  onNavigateBack,
  title,
  disableHomeButton = false,
  showQuoteButton = false,
  onRequestQuote,
}) => {
  return (
    <header className="app-header">
      <div className="header-left">
        <button
          className="header-icon-button home-button"
          onClick={onNavigateHome}
          title="Go to home"
          disabled={disableHomeButton}
        >
          <HiHome size={24} />
        </button>
      </div>

      <div className="header-center">
        <h1 className="header-title">
          BathForge
          {title && <span className="header-subtitle"> - {title}</span>}
        </h1>
      </div>

      <div className="header-right">
        {showQuoteButton && (
          <button
            className="header-quote-button"
            onClick={onRequestQuote}
            title="Request for Quote"
          >
            <HiDocumentText size={20} />
            <span>Request Quote</span>
          </button>
        )}
        <button
          className="header-icon-button login-button"
          onClick={onNavigateLogin}
          title="Login"
          aria-label="Log in"
        >
          <HiUser size={24} />
        </button>
      </div>
    </header>
  );
};

export default Header;
