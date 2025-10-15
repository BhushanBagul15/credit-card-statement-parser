import React from 'react';

// A simple utility to format currency
const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR', // Or your target currency
    }).format(amount);
};

const StatementCard = ({ data }) => {
    // Ensure the keys here match your backend response structure
    const { cardDetails, totalAmount, billingCycle, transactions } = data;

    if (!cardDetails) return null;

    return (
        <div className="max-w-4xl mx-auto">
            {/* -------------------- Main Summary Card -------------------- */}
            <div className="bg-white p-8 rounded-xl shadow-2xl mb-8 border-t-4 border-red-600">
                <h2 className="text-2xl font-bold text-red-800 mb-6">Statement Summary</h2>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-center">
                    {/* Card Number */}
                    <div className="p-4 bg-indigo-50 rounded-lg">
                        <p className="text-sm font-medium text-indigo-600">Card Number</p>
                        <p className="text-xl font-semibold text-gray-900 mt-1">
                            {cardDetails.cardNumber || 'XXXX XXXX XXXX 1234'}
                        </p>
                    </div>

                    {/* Total Amount */}
                    <div className="p-4 bg-green-50 rounded-lg">
                        <p className="text-sm font-medium text-green-600">Total Amount Due</p>
                        <p className="text-2xl font-bold text-gray-900 mt-1">
                            {formatCurrency(totalAmount || 0)}
                        </p>
                    </div>

                    {/* Billing Cycle */}
                    <div className="p-4 bg-yellow-50 rounded-lg">
                        <p className="text-sm font-medium text-yellow-600">Billing Period</p>
                        <p className="text-lg font-semibold text-gray-900 mt-1">
                            {billingCycle || 'MM/DD - MM/DD'}
                        </p>
                    </div>
                </div>
            </div>

            {/* -------------------- Transactions List -------------------- */}
            <h3 className="text-xl font-bold text-red-800 mb-4 ml-2">Transactions ({transactions?.length || 0})</h3>

            <div className="bg-white rounded-xl shadow-lg overflow-hidden">
                <ul className="divide-y divide-gray-200">
                    {transactions && transactions.length > 0 ? (
                        transactions.map((txn, index) => (
                            <li key={index} className="flex justify-between items-center p-4 hover:bg-gray-50 transition duration-100">

                                {/* Transaction Details */}
                                <div>
                                    <p className="text-base font-medium text-red-800">{txn.description}</p>
                                    <p className="text-xs text-pink-500 mt-0.5">Date: {txn.date}</p>
                                </div>

                                {/* Amount */}
                                <p className={`text-lg font-semibold text-black-600`}>
                                    {formatCurrency(txn.amount)}
                                </p>
                            </li>
                        ))
                    ) : (
                        <li className="p-4 text-center text-gray-500">No transactions found in the statement.</li>
                    )}
                </ul>
            </div>
        </div>
    );
};

export default StatementCard;