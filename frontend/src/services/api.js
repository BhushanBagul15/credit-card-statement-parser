import axios from 'axios';

// Base API configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/statements';

const api = axios.create({
    baseURL: 'http://localhost:8080/api/statements',
    timeout: 30000, // 30 seconds
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor (for adding auth tokens, etc.)
api.interceptors.request.use(
    (config) => {
        // Add auth token if available
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor (for handling errors globally)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response) {
            // Server responded with error status
            console.error('API Error:', error.response.data);
        } else if (error.request) {
            // Request made but no response
            console.error('Network Error:', error.message);
        } else {
            // Something else happened
            console.error('Error:', error.message);
        }
        return Promise.reject(error);
    }
);

// API methods
export const statementAPI = {
    /**
     * Health check
     */
    healthCheck: async () => {
        const response = await api.get('/health');
        return response.data;
    },

    /**
     * Parse a credit card statement PDF
     * @param {File} file - PDF file to parse
     */
    parseStatement: async (file) => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post('/parse', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });

        return response.data;
    },

    /**
     * Debug a PDF (extract raw text)
     * @param {File} file - PDF file to debug
     */
    debugPDF: async (file) => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post('/debug', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });

        return response.data;
    },

    /**
     * Get list of supported issuers
     */
    getSupportedIssuers: async () => {
        const response = await api.get('/statements/supported-issuers');
        return response.data;
    },
};

export default api;