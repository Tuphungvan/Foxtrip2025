const mongoose = require('mongoose');

const OrderSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },

  customerInfo: {
    username: { type: String, required: true },
    phoneNumber: { type: String, required: true },
    address: { type: String, required: true },
    email: { type: String }
  },

  items: [{
    slug: { type: String, required: true },
    name: { type: String, required: true },
    price: { type: Number, required: true },
    discount: { type: Number, default: 0 },
    finalPrice: { type: Number, required: true },
    image: { type: String, required: true },
    quantity: { type: Number, default: 1 },
  }],
  totalAmount: { type: Number, required: true },
  status: {
    type: String,
    enum: ['Chờ thanh toán', 'Đã thanh toán và chờ xác nhận', 'Hoàn tất'],
    default: 'Chờ thanh toán',
  },
  paymentMethod: { type: String, required: true, default: 'Giả lập' }, // Phương thức thanh toán giả lập
  createdAt: { type: Date, default: Date.now },
}, { timestamps: true });

module.exports = mongoose.model('Order', OrderSchema);
