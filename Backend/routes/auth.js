const express = require('express');
const router = express.Router();
const authController = require('../app/controllers/AuthController');
const isAuthenticated = require('../app/middlewares/isAuthenticated');

// Đăng ký
router.post('/register', authController.register);

// Đăng nhập
router.post('/login', authController.login);

// ✅ Đăng nhập bằng Google
router.post('/login/google', authController.loginWithGoogle); // sua

router.post('/login/facebook', authController.loginWithFacebook);

// Đăng xuất
router.post('/logout', authController.logout);

// Kiểm tra trạng thái đăng nhập
router.get('/check-login-status', isAuthenticated, authController.checkLoginStatus);

module.exports = router;
