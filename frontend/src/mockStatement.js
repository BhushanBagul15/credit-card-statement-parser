export const mockStatementData = {
    cardDetails: {
        cardNumber: '4XXX XXXX XXXX 5678',
        cardHolder: 'JPC-12 Developer',
    },
    totalAmount: 4500.50, // Total amount due
    billingCycle: '2025-09-15 - 2025-10-14',
    minimumPayment: 500.00,
    dueDate: '2025-11-01',

    transactions: [
        { date: '2025-10-10', description: 'Amazon.in Purchase', amount: 1250.00, type: 'debit' },
        { date: '2025-10-05', description: 'Grocery Store Payment', amount: 890.50, type: 'debit' },
        { date: '2025-09-30', description: 'Online Subscription Fee', amount: 150.00, type: 'debit' },
        { date: '2025-09-25', description: 'Credit Card Payment', amount: 5000.00, type: 'credit' },
        { date: '2025-09-17', description: 'Restaurant Dinner', amount: 2210.00, type: 'debit' },
    ],
};

// You can create another one for an empty state:
export const emptyStatementData = {
    cardDetails: {
        cardNumber: 'XXXX XXXX XXXX 0000',
        cardHolder: 'Test User',
    },
    totalAmount: 0.00,
    billingCycle: 'N/A',
    minimumPayment: 0.00,
    dueDate: 'N/A',
    transactions: [],
};