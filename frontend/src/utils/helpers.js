import { FILE_CONSTRAINTS } from '../constants';

/**
 * Format date to readable format
 */
export const formatDate = (dateString) => {
    if (!dateString) return 'N/A';

    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-IN', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
        });
    } catch (error) {
        return dateString;
    }
};

/**
 * Format amount in Indian currency
 */
export const formatAmount = (amount) => {
    if (amount === null || amount === undefined) return '₹0.00';

    try {
        const numAmount = typeof amount === 'string' ? parseFloat(amount) : amount;
        return `₹${numAmount.toLocaleString('en-IN', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        })}`;
    } catch (error) {
        return `₹${amount}`;
    }
};

/**
 * Validate file before upload
 */
export const validateFile = (file) => {
    const errors = [];

    // Check if file exists
    if (!file) {
        errors.push('Please select a file');
        return { isValid: false, errors };
    }

    // Check file type
    if (!FILE_CONSTRAINTS.ACCEPTED_TYPES.includes(file.type)) {
        errors.push('Only PDF files are supported');
    }

    // Check file size
    if (file.size > FILE_CONSTRAINTS.MAX_SIZE) {
        const maxSizeMB = FILE_CONSTRAINTS.MAX_SIZE / 1024 / 1024;
        errors.push(`File size must be less than ${maxSizeMB} MB`);
    }

    return {
        isValid: errors.length === 0,
        errors,
    };
};

/**
 * Format file size to readable format
 */
export const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
};

/**
 * Get file extension
 */
export const getFileExtension = (filename) => {
    return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2);
};

/**
 * Truncate text
 */
export const truncateText = (text, maxLength = 50) => {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
};

/**
 * Generate unique ID
 */
export const generateId = () => {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
};

/**
 * Debounce function
 */
export const debounce = (func, wait) => {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
};

/**
 * Check if value is empty
 */
export const isEmpty = (value) => {
    return (
        value === undefined ||
        value === null ||
        (typeof value === 'string' && value.trim().length === 0) ||
        (Array.isArray(value) && value.length === 0) ||
        (typeof value === 'object' && Object.keys(value).length === 0)
    );
};

/**
 * Copy text to clipboard
 */
export const copyToClipboard = async (text) => {
    try {
        await navigator.clipboard.writeText(text);
        return true;
    } catch (error) {
        console.error('Failed to copy:', error);
        return false;
    }
};

/**
 * Download data as JSON file
 */
export const downloadJSON = (data, filename = 'statement-data.json') => {
    const blob = new Blob([JSON.stringify(data, null, 2)], {
        type: 'application/json',
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
};