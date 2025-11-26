const path = require('path');
const express = require('express');
const morgan = require('morgan');
const cors = require('cors');
const dotenv = require('dotenv');
const mongoose = require('mongoose');
const cookieParser = require('cookie-parser');
const methodOverride = require('method-override');
const route = require('./routes');
const db = require('./config/db');

// Load biến môi trường
dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(methodOverride('_method'));
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(cors());
app.use(cookieParser());
app.use(morgan('combined'));

// Kết nối DB
db.connect();

// Routes
route(app);

require('./app/jobs/autoCancelOrders');
require('./app/jobs/moveCompletedOrders');

// Start server
app.listen(port, '0.0.0.0', () => {
    console.log(`🚀 Server đang chạy tại port ${port}`);
});
