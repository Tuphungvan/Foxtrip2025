const express = require("express");
const router = express.Router();
const chatbotController = require("../app/controllers/ChatbotController");
const isAuthenticated = require('../app/middlewares/isAuthenticated');
const ensureActive = require('../app/middlewares/ensureActive');

// Gọi chatbot
router.post("/chat", isAuthenticated, ensureActive, chatbotController.chat);

module.exports = router;
