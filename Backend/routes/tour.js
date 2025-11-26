
const express = require('express');
const router = express.Router();
const tourController = require('../app/controllers/TourController');
const isAuthenticated = require('../app/middlewares/isAuthenticated');
const ensureActive = require('../app/middlewares/ensureActive');


router.get('/shorts', isAuthenticated, ensureActive, tourController.getRandomShort);

module.exports = router;
