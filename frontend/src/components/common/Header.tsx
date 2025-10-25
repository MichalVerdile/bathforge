import React from 'react';
import './Header.css';

interface HeaderProps {
    onNavigateHome: () => void;
    onNavigateLogin?: () => void;
    showBackButton?: boolean;
    onNavigateBack?: () => void;
    title?: string;
}

const Header: React.FC<HeaderProps> = ({
    onNavigateHome,
    onNavigateLogin,
    showBackButton = false,
    onNavigateBack,
    title
}) => {
    return (
        <header className="app-header">
            <div className="header-left">
                {showBackButton && onNavigateBack && (
                    <button
                        className="header-icon-button back-button"
                        onClick={onNavigateBack}
                        title="Go back"
                    >
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.42-1.41L7.83 13H20v-2z" />
                        </svg>
                    </button>
                )}

                <button
                    className="header-icon-button home-button"
                    onClick={onNavigateHome}
                    title="Go to home"
                >
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" />
                    </svg>
                </button>
            </div>

            <div className="header-center">
                <h1 className="header-title">
                    🛁 BathForge
                    {title && <span className="header-subtitle"> - {title}</span>}
                </h1>
            </div>

            <div className="header-right">
                <button
                    className="header-icon-button login-button"
                    onClick={onNavigateLogin}
                    title="Login"
                >
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor" aria-label="Log in icon" xmlns="http://www.w3.org/2000/svg">
                        <path d="M3 3h2v18H3z" />
                        <path d="M18 11h-6.17l2.58-2.59L13 7l-5 5 5 5 1.41-1.41L11.83 13H18v-2z" />
                    </svg>

                </button>
            </div>
        </header>
    );
};

export default Header;