import React from 'react';
import {
    CreditCard,
    Calendar,
    DollarSign,
    CheckCircle,
    Download,
    Copy
} from 'lucide-react';
import Card from '../../comman/Card';
import Button from '../../comman/Button';
import { formatDate, formatAmount, downloadJSON, copyToClipboard } from '../../../utils/helpers';
import toast from 'react-hot-toast';

const StatementResults = ({ data, onNewUpload }) => {
    if (!data) return null;

    const handleDownload = () => {
        downloadJSON(data, `statement-${data.issuerName}-${Date.now()}.json`);
        toast.success('Statement data downloaded!');
    };

    const handleCopy = async () => {
        const success = await copyToClipboard(JSON.stringify(data, null, 2));
        if (success) {
            toast.success('Copied to clipboard!');
        } else {
            toast.error('Failed to copy');
        }
    };

    const keyDataPoints = [
        {
            label: 'Card Number',
            value: data.cardLastFourDigits ? `•••• ${data.cardLastFourDigits}` : 'N/A',
            icon: CreditCard,
            color: 'indigo',
        },
        {
            label: 'Card Type',
            value: data.cardVariant || 'N/A',
            icon: CreditCard,
            color: 'purple',
        },
        {
            label: 'Statement Date',
            value: formatDate(data.statementDate),
            icon: Calendar,
            color: 'blue',
        },
        {
            label: 'Payment Due Date',
            value: formatDate(data.paymentDueDate),
            icon: Calendar,
            color: 'orange',
        },
        {
            label: 'Total Amount Due',
            value: formatAmount(data.totalAmountDue),
            icon: DollarSign,
            color: 'red',
            highlight: true,
        },
    ];

    const additionalInfo = [
        { label: 'Credit Limit', value: formatAmount(data.creditLimit) },
        { label: 'Available Credit', value: formatAmount(data.availableCredit) },
        { label: 'Minimum Payment', value: formatAmount(data.minimumAmountDue) },
    ];

    return (
        <div className="w-full max-w-6xl mx-auto space-y-6">
            {/* Success Header */}
            <Card className="bg-gradient-to-r from-green-500 to-emerald-500 text-white">
                <div className="flex items-center gap-4">
                    <CheckCircle className="w-10 h-10 flex-shrink-0" />
                    <div>
                        <h2 className="text-2xl font-bold">Successfully Parsed!</h2>
                        <p className="opacity-90">
                            Extracted data from {data.issuerName} credit card statement
                        </p>
                    </div>
                </div>
            </Card>

            {/* Key Data Points Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {keyDataPoints.map((item, index) => {
                    const Icon = item.icon;
                    const colorClasses = {
                        indigo: 'border-indigo-500 bg-indigo-50',
                        purple: 'border-purple-500 bg-purple-50',
                        blue: 'border-blue-500 bg-blue-50',
                        orange: 'border-orange-500 bg-orange-50',
                        red: 'border-red-500 bg-red-50',
                    };

                    return (
                        <Card
                            key={index}
                            className={`border-l-4 ${colorClasses[item.color]} ${
                                item.highlight ? 'ring-2 ring-red-200' : ''
                            }`}
                        >
                            <div className="flex items-center gap-3 mb-2">
                                <Icon className={`w-5 h-5 text-${item.color}-600`} />
                                <h3 className="font-semibold text-gray-700 text-sm">
                                    {item.label}
                                </h3>
                            </div>
                            <p className={`text-2xl font-bold text-gray-900 ${
                                item.highlight ? 'text-red-600' : ''
                            }`}>
                                {item.value}
                            </p>
                        </Card>
                    );
                })}
            </div>

            {/* Additional Information */}
            {(data.creditLimit || data.availableCredit || data.minimumAmountDue) && (
                <Card>
                    <h3 className="text-xl font-bold text-gray-900 mb-4">
                        Additional Information
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        {additionalInfo.map((item, index) => (
                            <div key={index} className="p-4 bg-gray-50 rounded-lg">
                                <p className="text-sm text-gray-600 mb-1">{item.label}</p>
                                <p className="text-lg font-semibold text-gray-900">
                                    {item.value}
                                </p>
                            </div>
                        ))}
                    </div>
                </Card>
            )}

            {/* Transactions Table */}
            {data.transactions && data.transactions.length > 0 && (
                <Card>
                    <h3 className="text-xl font-bold text-gray-900 mb-4">
                        Recent Transactions ({data.transactions.length})
                    </h3>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                            <tr className="border-b-2 border-gray-200">
                                <th className="text-left py-3 px-4 text-gray-600 font-semibold text-sm">
                                    Date
                                </th>
                                <th className="text-left py-3 px-4 text-gray-600 font-semibold text-sm">
                                    Description
                                </th>
                                <th className="text-right py-3 px-4 text-gray-600 font-semibold text-sm">
                                    Amount
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            {data.transactions.slice(0, 10).map((txn, idx) => (
                                <tr
                                    key={idx}
                                    className="border-b border-gray-100 hover:bg-gray-50 transition-colors"
                                >
                                    <td className="py-3 px-4 text-gray-700 text-sm">
                                        {formatDate(txn.transactionDate)}
                                    </td>
                                    <td className="py-3 px-4 text-gray-900 text-sm">
                                        {txn.description || txn.merchantName || 'N/A'}
                                    </td>
                                    <td className="py-3 px-4 text-right font-semibold text-gray-900 text-sm">
                                        {formatAmount(txn.amount)}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                        {data.transactions.length > 10 && (
                            <p className="text-sm text-gray-500 text-center mt-3">
                                Showing 10 of {data.transactions.length} transactions
                            </p>
                        )}
                    </div>
                </Card>
            )}

            {/* Action Buttons */}
            <div className="flex flex-wrap gap-4">
                <Button
                    variant="primary"
                    onClick={onNewUpload}
                    className="flex-1 md:flex-none"
                >
                    Parse Another Statement
                </Button>
                <Button
                    variant="outline"
                    icon={Download}
                    onClick={handleDownload}
                    className="flex-1 md:flex-none"
                >
                    Download JSON
                </Button>
                <Button
                    variant="secondary"
                    icon={Copy}
                    onClick={handleCopy}
                    className="flex-1 md:flex-none"
                >
                    Copy Data
                </Button>
            </div>
        </div>
    );
};

export default StatementResults;