// Supported banks
export const SUPPORTED_BANKS = [
    { name: 'HDFC Bank', code: 'HDFC', icon: '🏦' },
    { name: 'ICICI Bank', code: 'ICICI', icon: '🏦' },
    { name: 'SBI Card', code: 'SBI', icon: '🏦' },
    { name: 'Axis Bank', code: 'AXIS', icon: '🏦' }

];

// File upload constraints
export const FILE_CONSTRAINTS = {
    MAX_SIZE: 10 * 1024 * 1024, // 10 MB
    ACCEPTED_TYPES: ['application/pdf'],
    ACCEPTED_EXTENSIONS: ['.pdf'],
};

// API status
export const API_STATUS = {
    IDLE: 'idle',
    LOADING: 'loading',
    SUCCESS: 'success',
    ERROR: 'error',
};

// Data point labels
export const DATA_POINT_LABELS = {
    cardLastFourDigits: 'Card Number',
    cardVariant: 'Card Type',
    statementDate: 'Statement Date',
    paymentDueDate: 'Payment Due Date',
    totalAmountDue: 'Total Amount Due',
    creditLimit: 'Credit Limit',
    availableCredit: 'Available Credit',
    minimumAmountDue: 'Minimum Payment',
};

// Routes
export const ROUTES = {
    HOME: '/',
    ABOUT: '/about',
    HELP: '/help',
};

// Error messages
export const ERROR_MESSAGES = {
    FILE_TOO_LARGE: `File size exceeds ${FILE_CONSTRAINTS.MAX_SIZE / 1024 / 1024} MB`,
    INVALID_FILE_TYPE: 'Please upload a PDF file',
    UPLOAD_FAILED: 'Failed to upload file. Please try again.',
    PARSE_FAILED: 'Failed to parse statement. The format may not be supported.',
    NETWORK_ERROR: 'Network error. Please check your connection.',
    GENERIC_ERROR: 'Something went wrong. Please try again.',
};