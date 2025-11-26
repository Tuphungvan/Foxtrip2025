
const express = require('express');
const router = express.Router();
const siteController = require('../app/controllers/SiteController');
const isAuthenticated = require('../app/middlewares/isAuthenticated');
const ensureActive = require('../app/middlewares/ensureActive');

// API cho Android
// Search
router.get('/search', isAuthenticated, ensureActive, siteController.search);

// Tours theo vùng
router.get('/region/bac', isAuthenticated, ensureActive, siteController.toursBac);
router.get('/region/trung', isAuthenticated, ensureActive, siteController.toursTrung);
router.get('/region/nam', isAuthenticated, ensureActive, siteController.toursNam);

// Tours hot
router.get('/hot', isAuthenticated, ensureActive, siteController.hotTours);

// Tours theo mùa
router.get('/discount', isAuthenticated, ensureActive, siteController.discountTours);

// Detail
router.get('/tours/:slug', isAuthenticated, ensureActive, siteController.detail);

module.exports = router;
