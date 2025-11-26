const express = require('express');
const router = express.Router();
const checkoutController = require('../app/controllers/CheckoutController');
const isAuthenticated = require('../app/middlewares/isAuthenticated');
const ensureActive = require('../app/middlewares/ensureActive');

// Xem giỏ hàng + tổng tiền
router.get('/', isAuthenticated, ensureActive, checkoutController.index);

// Đặt hàng
router.post('/place-order', isAuthenticated, ensureActive, checkoutController.placeOrder);

// Lấy thông tin thanh toán (order + ví)
router.get('/payment/:id', isAuthenticated, ensureActive, checkoutController.showPaymentPage);

module.exports = router;
