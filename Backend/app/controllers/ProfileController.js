// app/controllers/ProfileController.js
const User = require('../models/User');
const Order = require('../models/Order');
const History = require('../models/History');
const Wallet = require('../models/Wallet');
const bcrypt = require('bcrypt');
const Tour = require("../models/Tour");

function validTourCondition(extra = {}) {
    return {
        isBookable: true,
        availableSlots: { $gt: 0 },
        startDate: { $gte: new Date() },
        ...extra
    };
}

class ProfileController {
    // GET /profile (api-profile in your router)
    async index(req, res) {
        try {
            const user = await User.findById(req.user.id).select("-password");
            res.json({ success: true, data: user });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // GET /profile/my-orders
    async myOrders(req, res) {
        try {
            const orders = await Order.find({ userId: req.user.id }).sort({ createdAt: -1 });
            res.json({ success: true, data: orders });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // GET /profile/history
    async history(req, res) {
        try {
            const histories = await History.find({ userId: req.user.id }).sort({ completedAt: -1 });
            res.json({ success: true, data: histories });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // GET /profile/update-profile
    async updateProfile(req, res) {
        try {
            const user = await User.findById(req.user.id).select("-password");
            res.json({ success: true, data: user });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // POST /profile/update-profile
    async handleUpdateProfile(req, res) {
        try {
            const userId = req.user.id;
            const { username, email, phoneNumber, address, password } = req.body;

            // Kiểm tra trùng lặp (loại chính mình)
            const existingUser = await User.findOne({
                $or: [{ username }, { email }, { phoneNumber }]
            });
            if (existingUser && existingUser.id !== userId) {
                return res.status(400).json({
                    success: false,
                    message: 'Username, email, or phone number already in use.'
                });
            }

            let hashedPassword = undefined;
            if (password) {
                const salt = await bcrypt.genSalt(10);
                hashedPassword = await bcrypt.hash(password, salt);
            }

            const updateData = { username, email, phoneNumber, address };
            if (password) updateData.password = hashedPassword;

            await User.findByIdAndUpdate(userId, updateData, { new: true });
            const updatedUser = await User.findById(userId).select("-password");

            res.json({
                success: true,
                data: updatedUser,
                message: 'Profile updated successfully'
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // GET /profile/recharge-wallet
    async rechargeWallet(req, res) {
        try {
            const userId = req.user.id;
            let wallet = await Wallet.findOne({ userId });
            if (!wallet) {
                wallet = await Wallet.create({ userId, balance: 0 });
            }
            res.json({ success: true, data: wallet });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // POST /profile/recharge-wallet
    async handleRechargeWallet(req, res) {
        try {
            const userId = req.user.id;
            const { amount } = req.body;
            if (!amount || isNaN(amount) || Number(amount) <= 0) {
                return res.status(400).json({ success: false, message: "Invalid amount" });
            }

            const wallet = await Wallet.findOneAndUpdate(
                { userId },
                { $inc: { balance: Number(amount) } },
                { new: true, upsert: true }
            );

            res.json({
                success: true,
                data: wallet,
                message: 'Recharge successful'
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // GET /profile/my-orders/:orderId
    async orderDetail(req, res) {
        try {
            const { orderId } = req.params;
            const order = await Order.findById(orderId);
            if (!order) return res.status(404).json({ success: false, message: "Order not found" });
            res.json({ success: true, data: order });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // GET /profile/history/:historyId
    async historyDetail(req, res) {
        try {
            const { historyId } = req.params;
            const history = await History.findById(historyId);
            if (!history) return res.status(404).json({ success: false, message: "History record not found" });
            res.json({ success: true, data: history });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // POST /profile/avatar
    async uploadAvatar(req, res) {
        try {
            if (!req.file || !req.file.path) {
                return res.status(400).json({ success: false, message: "No file uploaded" });
            }

            const avatarUrl = req.file.path; // CloudinaryStorage cung cấp path
            const user = await User.findByIdAndUpdate(
                req.user.id,
                { avatar: avatarUrl },
                { new: true }
            ).select("-password");

            res.json({
                success: true,
                message: "Avatar updated successfully",
                data: user
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // Thêm vào ProfileController.js

    // DELETE /profile/my-orders/:orderId/cancel
    async cancelOrder(req, res) {
        try {
            const { orderId } = req.params;
            const userId = req.user.id;

            const order = await Order.findById(orderId);
            if (!order) {
                return res.status(404).json({ success: false, message: "Order not found" });
            }

            // Kiểm tra order có thuộc user này không
            if (order.userId.toString() !== userId) {
                return res.status(403).json({ success: false, message: "Unauthorized" });
            }

            // Chỉ cho phép hủy đơn hàng ở trạng thái "Chờ thanh toán"
            if (order.status !== "Chờ thanh toán") {
                return res.status(400).json({
                    success: false,
                    message: "Chỉ có thể hủy đơn hàng đang chờ thanh toán"
                });
            }

            // Xóa đơn hàng
            await Order.findByIdAndDelete(orderId);

            res.json({
                success: true,
                message: "Đã hủy đơn hàng thành công"
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // POST /profile/my-orders/:orderId/pay
    async payOrder(req, res) {
        try {
            const { orderId } = req.params;
            const userId = req.user.id;

            const order = await Order.findById(orderId);
            if (!order) {
                return res.status(404).json({ success: false, message: "Order not found" });
            }

            // Kiểm tra order có thuộc user này không
            if (order.userId.toString() !== userId) {
                return res.status(403).json({ success: false, message: "Unauthorized" });
            }

            // Chỉ cho phép thanh toán đơn hàng ở trạng thái "Chờ thanh toán"
            if (order.status !== "Chờ thanh toán") {
                return res.status(400).json({
                    success: false,
                    message: "Đơn hàng này không thể thanh toán"
                });
            }

            for (const item of order.items) {
            const tour = await Tour.findOne(validTourCondition({ slug: item.slug }));
            if (!tour) {
                return res.status(400).json({
                    success: false,
                    message: `Tour "${item.name}" không còn khả dụng. Vui lòng hủy đơn hàng này.`
                });
            }

            // Kiểm tra số lượng slot còn đủ không
            if (tour.availableSlots < item.quantity) {
                return res.status(400).json({
                    success: false,
                    message: `Tour "${item.name}" chỉ còn ${tour.availableSlots} chỗ trống, không đủ cho đơn hàng của bạn (${item.quantity} chỗ).`
                });
            }
        }

            // Kiểm tra số dư ví
            let wallet = await Wallet.findOne({ userId });
            if (!wallet) {
                wallet = await Wallet.create({ userId, balance: 0 });
            }

            if (wallet.balance < order.totalAmount) {
                return res.status(400).json({
                    success: false,
                    message: "Số dư không đủ",
                    wallet: wallet
                });
            }

            for (const item of order.items) {
            await Tour.updateOne(
                { slug: item.slug },
                { $inc: { availableSlots: -item.quantity } }
            );
        }

            // Trừ tiền và cập nhật trạng thái
            wallet.balance -= order.totalAmount;
            await wallet.save();

            order.status = "Đã thanh toán và chờ xác nhận";
            order.paymentMethod = "Foxtrip Wallet";
            await order.save();

            res.json({
                success: true,
                message: "Thanh toán thành công",
                data: {
                    order: order,
                    wallet: wallet
                }
            });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, message: "Server error" });
        }
    }
}

module.exports = new ProfileController();
