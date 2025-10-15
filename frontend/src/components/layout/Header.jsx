import React from 'react';
import { CreditCard, Github, Menu, X } from 'lucide-react';
import { SUPPORTED_BANKS } from '../../constants';

const Header = () => {
    const [mobileMenuOpen, setMobileMenuOpen] = React.useState(false);

    return (
        <header className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    {/* Logo & Title */}
                    <div className="flex items-center gap-3">
                        <div className="bg-primary-600 p-2 rounded-lg">
                            <CreditCard className="w-6 h-6 text-white" />
                        </div>
                        <div>
                            <h1 className="text-xl font-bold text-gray-900">
                                Statement Parser
                            </h1>
                            <p className="text-xs text-gray-500 hidden sm:block">
                                Extract data from credit card statements
                            </p>
                        </div>
                    </div>

                    {/* Desktop Navigation */}
                    <nav className="hidden md:flex items-center gap-6">
                        <a
                            href="#features"
                            className="text-gray-600 hover:text-primary-600 transition-colors text-sm font-medium"
                        >
                            Features
                        </a>
                        <a
                            href="#supported-banks"
                            className="text-gray-600 hover:text-primary-600 transition-colors text-sm font-medium"
                        >
                            Supported Banks
                        </a>
                        <a
                            href="https://github.com/BhushanBagul15/credit-card-statement-parser"
                            target="_blank"
                            rel="noopener noreferrer"
                            className="flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors text-sm font-medium"
                        >
                            <Github className="w-4 h-4" />
                            GitHub
                        </a>
                    </nav>

                    {/* Mobile Menu Button */}
                    <button
                        className="md:hidden p-2 text-gray-600 hover:text-primary-600 transition-colors"
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                    >
                        {mobileMenuOpen ? (
                            <X className="w-6 h-6" />
                        ) : (
                            <Menu className="w-6 h-6" />
                        )}
                    </button>
                </div>

                {/* Mobile Menu */}
                {mobileMenuOpen && (
                    <div className="md:hidden py-4 border-t border-gray-200">
                        <nav className="flex flex-col gap-4">
                            <a
                                href="#features"
                                className="text-gray-600 hover:text-primary-600 transition-colors text-sm font-medium"
                                onClick={() => setMobileMenuOpen(false)}
                            >
                                Features
                            </a>
                            <a
                                href="#supported-banks"
                                className="text-gray-600 hover:text-primary-600 transition-colors text-sm font-medium"
                                onClick={() => setMobileMenuOpen(false)}
                            >
                                Supported Banks
                            </a>
                            <a
                                href="https://github.com/yourusername/credit-card-parser"
                                target="_blank"
                                rel="noopener noreferrer"
                                className="flex items-center gap-2 text-gray-600 hover:text-primary-600 transition-colors text-sm font-medium"
                            >
                                <Github className="w-4 h-4" />
                                GitHub
                            </a>
                        </nav>
                    </div>
                )}
            </div>
        </header>
    );
};

export default Header;