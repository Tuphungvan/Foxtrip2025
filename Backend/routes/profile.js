const express = require('express');
const router = express.Router();
const isAuthenticated = require('../app/middlewares/isAuthenticated');
const profileController = require('../app/controllers/ProfileController');
const ensureActive = require('../app/middlewares/ensureActive');
const upload = require('../app/middlewares/uploadAvatar');


// Profile
router.get('/', isAuthenticated, ensureActive, profileController.index);

// My Orders
router.get('/my-orders', isAuthenticated, ensureActive, profileController.myOrders);
router.get('/my-orders/:orderId', isAuthenticated, ensureActive, profileController.orderDetail);

// History
router.get('/history', isAuthenticated, ensureActive, profileController.history);
router.get('/history/:historyId', isAuthenticated, ensureActive, profileController.historyDetail);

// Update profile
router.get('/update-profile', isAuthenticated, ensureActive, profileController.updateProfile);
router.post('/update-profile', isAuthenticated, ensureActive, profileController.handleUpdateProfile);

// Wallet recharge
router.get('/recharge-wallet', isAuthenticated, ensureActive, profileController.rechargeWallet);
router.post('/recharge-wallet', isAuthenticated, ensureActive, profileController.handleRechargeWallet);

router.post('/avatar', isAuthenticated, ensureActive, upload.single('avatar'), profileController.uploadAvatar);

// Cancel order
router.delete('/my-orders/:orderId/cancel', isAuthenticated, ensureActive, profileController.cancelOrder);

// Pay order
router.post('/my-orders/:orderId/pay', isAuthenticated, ensureActive, profileController.payOrder);


module.exports = router;
