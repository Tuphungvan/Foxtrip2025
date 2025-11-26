const mongoose = require('mongoose');

const CartSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    items: [{
        slug: { type: String, required: true },
        name: { type: String, required: true },
        price: { type: Number, required: true },
        image: { type: String, required: true },
        quantity: { type: Number, default: 1 },
        discount: { type: Number, default: 0 },
        finalPrice: { type: Number, required: true },
    }]
});

module.exports = mongoose.model('Cart', CartSchema);
