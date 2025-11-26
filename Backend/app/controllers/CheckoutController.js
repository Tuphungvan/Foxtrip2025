const Cart = require('../models/Cart');
const Order = require('../models/Order');
const Wallet = require('../models/Wallet');
const Tour = require('../models/Tour');
const User = require('../models/User');

function validTourCondition(extra = {}) {
    return {
        isBookable: true,
        availableSlots: { $gt: 0 },
        startDate: { $gte: new Date() },
        ...extra
    };
}

class CheckoutController {
    // [GET] /checkout
    async index(req, res) {
        try {
            const userId = req.user?.id;
            if (!userId) {
                return res.status(401).json({ success: false, data: null, message: 'Vui lòng đăng nhập' });
            }

            const cart = await Cart.findOne({ userId });
            if (!cart || cart.items.length === 0) {
                return res.json({ success: false, data: null, message: 'Giỏ hàng trống' });
            }

            const user = await User.findById(userId).select('username phoneNumber address email');

            const total = cart.items.reduce((sum, item) => sum + item.finalPrice * item.quantity, 0);

            res.json({
                success: true,
                data: { cart, total, user },
                message: 'Lấy thông tin checkout thành công'
            });
        } catch (error) {
            console.error(error);
            res.status(500).json({ success: false, data: null, message: 'Lỗi hệ thống' });
        }
    }

    // [POST] /checkout/place-order
    async placeOrder(req, res) {
        try {
            const userId = req.user?.id;
            if (!userId) {
                return res.status(401).json({ success: false, data: null, message: 'Vui lòng đăng nhập' });
            }

            const cart = await Cart.findOne({ userId });
            if (!cart || cart.items.length === 0) {
                return res.json({ success: false, data: null, message: 'Giỏ hàng trống' });
            }

            const { username, phoneNumber, address, email } = req.body;

            // KIỂM TRA TỪNG TOUR TRƯỚC KHI TẠO ORDER
            for (const item of cart.items) {
                const tour = await Tour.findOne(validTourCondition({ slug: item.slug }));
                if (!tour) {
                    // Xóa tour lỗi khỏi giỏ hàng
                    await Cart.updateOne(
                        { userId },
                        { $pull: { items: { slug: item.slug } } }
                    );

                    return res.status(400).json({
                        success: false,
                        message: `Tour "${item.name}" không còn khả dụng và đã bị xóa khỏi giỏ hàng.`,
                    });
                }
            }

            const totalAmount = cart.items.reduce((sum, item) => sum + item.finalPrice * item.quantity, 0);

            // Kiểm tra ví
            const wallet = await Wallet.findOne({ userId });
            const hasEnoughBalance = wallet && wallet.balance >= totalAmount;

            // ✅ TẠO NHIỀU ORDER (mỗi tour khác nhau = 1 order)
            const createdOrders = [];

            for (const item of cart.items) {
                let status = 'Chờ thanh toán';
                const itemTotal = item.finalPrice * item.quantity;

                // Nếu đủ tiền thì thanh toán và giảm slot
                if (hasEnoughBalance) {
                    await Tour.updateOne(
                        { slug: item.slug },
                        { $inc: { availableSlots: -item.quantity } }
                    );
                    status = 'Đã thanh toán và chờ xác nhận';
                }

                const newOrder = new Order({
                    userId,
                    customerInfo: { username, phoneNumber, address, email },
                    items: [item], // ✅ Chỉ chứa 1 loại tour
                    totalAmount: itemTotal,
                    status,
                    paymentMethod: 'Ví MyWallet'
                });

                await newOrder.save();
                createdOrders.push({
                    orderId: newOrder._id,
                    tourName: item.name,
                    status
                });
            }

            // Trừ tiền ví nếu đủ
            if (hasEnoughBalance) {
                wallet.balance -= totalAmount;
                await wallet.save();
            }

            // Xóa giỏ hàng
            await Cart.deleteOne({ userId });

            res.json({
                success: true,
                data: {
                    orders: createdOrders,
                    totalOrders: createdOrders.length,
                    status: hasEnoughBalance ? 'Đã thanh toán và chờ xác nhận' : 'Chờ thanh toán'
                },
                message: hasEnoughBalance
                    ? `Đã tạo ${createdOrders.length} đơn hàng và thanh toán thành công`
                    : `Đã tạo ${createdOrders.length} đơn hàng, vui lòng thanh toán sau`
            });
        } catch (error) {
            console.error(error);
            res.status(500).json({ success: false, data: null, message: 'Lỗi đặt hàng' });
        }
    }

    // [GET] /checkout/payment/:id
    async showPaymentPage(req, res) {
        try {
            const userId = req.user?.id;
            if (!userId) {
                return res.status(401).json({ success: false, data: null, message: 'Vui lòng đăng nhập' });
            }

            const orderId = req.params.id;
            const order = await Order.findById(orderId);
            if (!order) {
                return res.json({ success: false, data: null, message: 'Đơn hàng không tồn tại' });
            }

            const wallet = await Wallet.findOne({ userId });
            res.json({ success: true, data: { order, wallet }, message: 'Lấy thông tin thanh toán thành công' });
        } catch (error) {
            console.error(error);
            res.status(500).json({ success: false, data: null, message: 'Lỗi hệ thống' });
        }
    }
}

module.exports = new CheckoutController();