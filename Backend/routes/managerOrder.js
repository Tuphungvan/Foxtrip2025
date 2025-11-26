const express = require('express');
const router = express.Router();
const managerOrderController = require('../app/controllers/ManagerOrderController');
const isAdmin = require('../app/middlewares/isAdmin');
const ensureActive = require('../app/middlewares/ensureActive');
const isAuthenticated = require('../app/middlewares/isAuthenticated');

// Lấy danh sách đơn hàng chờ thanh toán
router.get('/pending-payment', isAuthenticated, isAdmin, ensureActive, managerOrderController.getOrdersPendingPayment);

// Lấy danh sách đơn hàng đã thanh toán và chờ xác nhận
router.get('/to-confirm', isAuthenticated, isAdmin, ensureActive, managerOrderController.getOrdersToConfirm);

// Xác nhận đơn hàng (chuyển sang Hoàn tất)
router.post('/confirm/:orderId', isAuthenticated, isAdmin, ensureActive, managerOrderController.confirmOrder);

// Xóa đơn hàng chưa thanh toán
router.delete('/delete/:orderId', isAuthenticated, isAdmin, ensureActive, managerOrderController.deletePendingOrder);

// Hoàn tất đơn hàng đã hết hạn
router.post('/complete/:orderId', isAuthenticated, isAdmin, ensureActive, managerOrderController.confirmExpiredOrder);

// Lấy danh sách đơn hàng Hoàn tất
router.get('/completed', isAuthenticated, isAdmin, ensureActive, managerOrderController.getOrdersCompleted);

// tìm order theo id
router.get('/search', isAuthenticated, isAdmin, ensureActive, managerOrderController.searchOrderById);

router.get('/:orderId', isAuthenticated, isAdmin, ensureActive, managerOrderController.getOrderDetail);

module.exports = router;
