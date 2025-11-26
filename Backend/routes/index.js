const siteRouter = require('./site');
const authRouter = require('./auth');
const profileRouter = require('./profile');
const adminRouter = require('./admin');
const cartRouter = require('./cart');
const checkoutRouter = require('./checkout');
const managerOrderRouter = require('./managerOrder');
const chatbotRouter = require("./chatbot");
const tourRouter = require("./tour");
const otpRouter = require('./otp'); //otp

function route(app) {
    app.use('/admin', adminRouter);
    app.use('/auth', authRouter);
    app.use('/profile', profileRouter);

    app.use('/checkout', checkoutRouter);
    app.use('/admin/manager-order', managerOrderRouter);
    app.use('/', siteRouter);
    app.use('/', cartRouter);
    app.use("/chatbot", chatbotRouter);
    app.use("/tour", tourRouter);
    app.use('/otp', otpRouter); //otp
}

module.exports = route;

