const express = require('express');
const router = express.Router();
const otpController = require('../app/controllers/OTPController');
const isAuthenticated = require('../app/middlewares/isAuthenticated');
const ensureActive = require('../app/middlewares/ensureActive');

router.post('/send', isAuthenticated, ensureActive, otpController.sendOTP);
router.post('/verify', isAuthenticated, ensureActive, otpController.verifyOTPCode);

module.exports = router;