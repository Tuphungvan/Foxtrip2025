const express = require('express');
const router = express.Router();
const cartController = require('../app/controllers/cartController');
const isAuthenticated = require('../app/middlewares/isAuthenticated');
const ensureActive = require('../app/middlewares/ensureActive');

router.post('/cart/add/:slug', isAuthenticated, ensureActive, cartController.addToCart);
router.get('/cart', isAuthenticated, ensureActive, cartController.viewCart);
router.delete('/cart/:slug', isAuthenticated, ensureActive, cartController.removeFromCart);
router.get('/cart/count', isAuthenticated, ensureActive, cartController.cartItemCount);
router.post('/cart/decrease/:slug', isAuthenticated, ensureActive, cartController.decreaseQuantity);
router.post('/cart/increase/:slug', isAuthenticated, ensureActive, cartController.increaseQuantity);

module.exports = router;
