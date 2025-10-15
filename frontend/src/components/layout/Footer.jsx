import React from 'react';
import { Heart } from 'lucide-react';

const Footer = () => {
    const currentYear = new Date().getFullYear();

    return (
        <footer className="bg-white border-t border-gray-200 mt-auto">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    {/* About */}
                    <div>
                        <h3 className="font-semibold text-gray-900 mb-3">
                            Credit Card Statement Parser
                        </h3>
                        <p className="text-sm text-gray-600">
                            Automatically extract key data points from credit card statements
                            across 5 major Indian issuers.
                        </p>
                    </div>

{/*                     Quick Links */}
                    <div>
{/*                         <h3 className="font-semibold text-gray-900 mb-3">Quick Links</h3> */}
{/*                         <ul className="space-y-2 text-sm"> */}
{/*                             <li> */}
{/*                                 <a */}
{/*                                     href="#features" */}
{/*                                     className="text-gray-600 hover:text-primary-600 transition-colors" */}
{/*                                 > */}
{/*                                     Features */}
{/*                                 </a> */}
{/*                             </li> */}
{/*                             <li> */}
{/*                                 <a */}
{/*                                     href="#supported-banks" */}
{/*                                     className="text-gray-600 hover:text-primary-600 transition-colors" */}
{/*                                 > */}
{/*                                     Supported Banks */}
{/*                                 </a> */}
{/*                             </li> */}
{/*                             <li> */}
{/*                                 <a */}
{/*                                     href="#api-docs" */}
{/*                                     className="text-gray-600 hover:text-primary-600 transition-colors" */}
{/*                                 > */}
{/*                                     API Documentation */}
{/*                                 </a> */}
{/*                             </li> */}
{/*                         </ul> */}
                    </div>

                    {/* Tech Stack */}
                    <div>
                        <h3 className="font-semibold text-gray-900 mb-3">Built With</h3>
                        <ul className="space-y-2 text-sm text-gray-600">
                            <li>• Java 17 & Spring Boot</li>
                            <li>• React 18 & Vite</li>
                            <li>• Apache PDFBox</li>
                            <li>• Tailwind CSS</li>
                        </ul>
                    </div>
                </div>

                {/* Bottom Bar */}
                <div className="mt-8 pt-8 border-t border-gray-200">
                    <div className="flex flex-col md:flex-row justify-between items-center gap-4">
                        <p className="text-sm text-gray-600">
                            © {currentYear} Credit Card Statement Parser.
                        </p>
                    </div>
                </div>
            </div>
        </footer>
    );
};

export default Footer;