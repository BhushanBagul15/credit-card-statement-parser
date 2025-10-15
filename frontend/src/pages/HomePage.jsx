import React, { useState } from 'react';
import {
    Shield,
    Zap,
    CheckCircle2,
    FileText,
    AlertCircle
} from 'lucide-react';
import FileUpload from '../components/features/upload/FileUpload';
import StatementResults from '../components/features/results/StatementResults';
import Card from '../components/comman/Card.jsx'
import Loader from '../components/comman/Loader';
import {statementAPI} from '../services/api';
import { SUPPORTED_BANKS, API_STATUS } from '../constants';
import toast, { Toaster } from 'react-hot-toast';

const HomePage = () => {
    const [apiStatus, setApiStatus] = useState(API_STATUS.IDLE);
    const [parsedData, setParsedData] = useState(null);
    const [error, setError] = useState(null);

    const handleFileSelect = async (file) => {
        console.log('📁 File selected:', file.name, file.size, file.type);
        setApiStatus(API_STATUS.LOADING);
        setError(null);

        try {
            // Parse the statement
            console.log('🚀 Sending request to API...');
            const data = await statementAPI.parseStatement(file);
            console.log('✅ Response received:', data);
            setApiStatus(API_STATUS.SUCCESS);
            setParsedData(data);
            toast.success('Statement parsed successfully!');

        } catch (err) {
            console.error('❌ Error details:', err);  // ← Add this
            console.error('❌ Response:', err.response);
            setApiStatus(API_STATUS.ERROR);
            const errorMessage = err.response?.data?.error || 'Failed to parse statement';
            setError(errorMessage);
            toast.error(errorMessage);
            console.error('Parse error:', err);
        }
    };

    const handleNewUpload = () => {
        setApiStatus(API_STATUS.IDLE);
        setParsedData(null);
        setError(null);
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50">
            {/*<Toaster position="top-right" />*/}

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                {/* Hero Section */}
                {apiStatus === API_STATUS.IDLE && (
                    <div className="text-center mb-12">
                        <div className="flex justify-center mb-4">
                            <div className="bg-primary-600 p-4 rounded-2xl">
                                <FileText className="w-12 h-12 text-white" />
                            </div>
                        </div>
                        <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
                            Credit Card Statement Parser
                        </h1>
                        <p className="text-xl text-gray-600 mb-6 max-w-2xl mx-auto">
                            Upload your credit card statement PDF and extract key information
                            instantly using AI-powered parsing
                        </p>

                        {/* Supported Banks Pills */}
                        <div className="flex flex-wrap justify-center gap-2 mb-8">
                            {SUPPORTED_BANKS.map((bank) => (
                                <span
                                    key={bank.code}
                                    className="px-4 py-2 bg-white rounded-full text-sm font-medium text-gray-700 border border-gray-200 shadow-sm"
                                >
                  {bank.icon} {bank.name}
                </span>
                            ))}
                        </div>

                        {/* Features */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto mb-12">
                            <Card hover className="text-center">
                                <Zap className="w-10 h-10 text-primary-600 mx-auto mb-3" />
                                <h3 className="font-semibold text-gray-900 mb-2">
                                    Lightning Fast
                                </h3>
                                <p className="text-sm text-gray-600">
                                    Parse statements in seconds with our optimized engine
                                </p>
                            </Card>

                            <Card hover className="text-center">
                                <Shield className="w-10 h-10 text-primary-600 mx-auto mb-3" />
                                <h3 className="font-semibold text-gray-900 mb-2">
                                    Secure & Private
                                </h3>
                                <p className="text-sm text-gray-600">
                                    Files are processed securely and deleted immediately
                                </p>
                            </Card>

                            <Card hover className="text-center">
                                <CheckCircle2 className="w-10 h-10 text-primary-600 mx-auto mb-3" />
                                <h3 className="font-semibold text-gray-900 mb-2">
                                    5 Key Data Points
                                </h3>
                                <p className="text-sm text-gray-600">
                                    Extracts card details, dates, amounts, and more
                                </p>
                            </Card>
                        </div>
                    </div>
                )}

                {/* Upload Section */}
                {apiStatus === API_STATUS.IDLE && (
                    <FileUpload
                        onFileSelect={handleFileSelect}
                        loading={false}
                    />
                )}

                {/* Loading State */}
                {apiStatus === API_STATUS.LOADING && (
                    <Card className="max-w-2xl mx-auto text-center py-16">
                        <Loader size="lg" text="Processing your statement..." />
                        <p className="text-sm text-gray-500 mt-4">
                            This may take a few seconds depending on file size
                        </p>
                    </Card>
                )}

                {/* Error State */}
                {apiStatus === API_STATUS.ERROR && error && (
                    <div className="max-w-2xl mx-auto">
                        <Card className="border-l-4 border-red-500 bg-red-50">
                            <div className="flex gap-4">
                                <AlertCircle className="w-6 h-6 text-red-600 flex-shrink-0" />
                                <div>
                                    <h3 className="font-semibold text-red-900 mb-2">
                                        Parsing Failed
                                    </h3>
                                    <p className="text-red-700 mb-4">{error}</p>
                                    <button
                                        onClick={handleNewUpload}
                                        className="text-sm font-medium text-red-600 hover:text-red-800 underline"
                                    >
                                        Try another file
                                    </button>
                                </div>
                            </div>
                        </Card>
                    </div>
                )}

                {/* Success - Results */}
                {apiStatus === API_STATUS.SUCCESS && parsedData && (
                    <StatementResults
                        data={parsedData}
                        onNewUpload={handleNewUpload}
                    />
                )}

                {/* Info Section */}
                {apiStatus === API_STATUS.IDLE && (
                    <div id="supported-banks" className="mt-16">
                        <Card className="max-w-4xl mx-auto">
                            <h2 className="text-2xl font-bold text-gray-900 mb-4">
                                Supported Data Points
                            </h2>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {[
                                    'Card Last 4 Digits',
                                    'Card Type/Variant',
                                    'Statement Date',
                                    'Payment Due Date',
                                    'Total Amount Due',
                                    'Credit Limit'


                                ].map((item, index) => (
                                    <div key={index} className="flex items-center gap-3">
                                        <CheckCircle2 className="w-5 h-5 text-green-600 flex-shrink-0" />
                                        <span className="text-gray-700">{item}</span>
                                    </div>
                                ))}
                            </div>
                        </Card>
                    </div>
                )}
            </div>
        </div>
    );
};

export default HomePage;