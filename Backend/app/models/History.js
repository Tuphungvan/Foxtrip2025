const mongoose = require('mongoose');

// Schema cho order_history
const HistorySchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    orderId: { type: mongoose.Schema.Types.ObjectId, ref: 'Order', required: true },

    customerInfo: {
        username: { type: String, required: true },
        phoneNumber: { type: String, required: true },
        address: { type: String, required: true },
        email: { type: String }
    },

    completedAt: { type: Date, default: Date.now }, // Thời gian chuyển sang lịch sử
    endDate: { type: Date, required: true }, // Ngày kết thúc tour
    items: [{
        slug: { type: String, required: true },
        name: { type: String, required: true },
        price: { type: Number, required: true },
        discount: { type: Number, default: 0 },
        finalPrice: { type: Number, required: true },
        image: { type: String, required: true },
        quantity: { type: Number, default: 1 },
    }]
}, { timestamps: true });

module.exports = mongoose.model('History', HistorySchema);
