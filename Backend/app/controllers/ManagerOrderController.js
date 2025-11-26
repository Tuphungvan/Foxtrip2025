const Order = require('../models/Order');
const History = require('../models/History');
const Tour = require('../models/Tour');
const { sendOrderEmail } = require("../utils/sendOrderEmail");

const RevenueReport = require('../models/RevenueReport');

class ManagerOrderController {

    //0. Tìm tour theo id:
    async searchOrderById(req, res) {
        try {
            const { q } = req.query;
            
            if (!q || q.trim() === '') {
                return res.json({ 
                    success: true, 
                    data: {
                        orders: [],
                        status: null
                    }
                });
            }

            const searchTerm = q.trim();

            // Kiểm tra format ObjectId hợp lệ
            if (!searchTerm.match(/^[0-9a-fA-F]{24}$/)) {
                return res.json({ 
                    success: true, 
                    data: {
                        orders: [],
                        status: null
                    }
                });
            }

            // Tìm order theo _id
            const order = await Order.findById(searchTerm);
            
            if (!order) {
                return res.json({ 
                    success: true, 
                    data: {
                        orders: [],
                        status: null
                    }
                });
            }

            res.json({ 
                success: true, 
                data: {
                    orders: [order],
                    status: order.status
                }
            });

        } catch (error) {
            console.error('Lỗi tìm kiếm đơn hàng:', error);
            res.status(500).json({ 
                success: false, 
                message: 'Có lỗi xảy ra khi tìm kiếm đơn hàng.' 
            });
        }
    }

    // Lấy order detail
    async getOrderDetail(req, res) {
        try {
            const { orderId } = req.params;
            
            const order = await Order.findById(orderId);
            if (!order) {
                return res.status(404).json({ 
                    success: false, 
                    message: 'Không tìm thấy đơn hàng.' 
                });
            }

            res.json({ 
                success: true, 
                data: order 
            });

        } catch (error) {
            console.error('Lỗi lấy chi tiết đơn hàng:', error);
            res.status(500).json({ 
                success: false, 
                message: 'Có lỗi xảy ra khi lấy chi tiết đơn hàng.' 
            });
        }
    }

    // 1. Lấy danh sách đơn hàng "Chờ thanh toán"
    async getOrdersPendingPayment(req, res) {
        try {
            const orders = await Order.find({ status: 'Chờ thanh toán' });
            res.json({ success: true, data: orders });
        } catch (error) {
            console.error(error);
            res.status(500).json({ success: false, message: 'Có lỗi xảy ra khi lấy danh sách đơn hàng chờ thanh toán.' });
        }
    }

    // 2. Xóa đơn hàng chưa thanh toán
    async deletePendingOrder(req, res) {
        const { orderId } = req.params;
        try {
            const order = await Order.findById(orderId);
            if (!order) return res.status(404).json({ success: false, message: 'Không tìm thấy đơn hàng.' });

            await Order.findByIdAndDelete(orderId);

            // Gửi email thông báo hủy đơn (nếu có email)
            try {
                await sendOrderEmail(order, "delete");
            } catch (e) {
                console.error("Gửi mail hủy đơn thất bại:", e);
            }

            res.json({
                success: true,
                message: 'Đơn hàng đã bị hủy và email đã được gửi (nếu có).',
                data: {
                    orderId: order._id.toString(),
                    status: 'Đã hủy'
                }
            });
        } catch (error) {
            console.error(error);
            res.status(500).json({ success: false, message: 'Có lỗi xảy ra khi xóa đơn hàng chưa thanh toán.' });
        }
    }


    // 3. Lấy danh sách đơn hàng "Đã thanh toán và chờ xác nhận"
    async getOrdersToConfirm(req, res) {
        try {
            const orders = await Order.find({ status: 'Đã thanh toán và chờ xác nhận' });
            res.json({ success: true, data: orders });
        } catch (error) {
            console.error(error);
            res.status(500).json({ success: false, message: 'Có lỗi xảy ra khi lấy danh sách đơn hàng chờ xác nhận.' });
        }
    }

    // 4. Xác nhận đơn hàng và chuyển sang trạng thái "Hoàn tất"
    async confirmOrder(req, res) {
        const { orderId } = req.params;
        try {
            const order = await Order.findById(orderId);
            if (!order) return res.status(404).json({ success: false, message: 'Không tìm thấy đơn hàng.' });
            if (order.status !== 'Đã thanh toán và chờ xác nhận')
                return res.status(400).json({ success: false, message: 'Đơn hàng không hợp lệ để xác nhận.' });

            order.status = 'Hoàn tất';
            await order.save();

            // Gửi email bất đồng bộ, nhưng chờ kết quả nếu bạn muốn
            try {
                await sendOrderEmail(order, "confirm");
            } catch (e) {
                console.error("Gửi mail thất bại (không rollback):", e);
            }

            res.json({
                success: true,
                message: 'Đơn hàng đã được xác nhận. Email (nếu có) đã được gửi.',
                data: {
                    orderId: order._id.toString(),
                    status: order.status
                }
            });
        } catch (error) {
            console.error(error);
            res.status(500).json({ success: false, message: 'Có lỗi xảy ra khi xác nhận đơn hàng.' });
        }
    }

    // 5. Di chuyển đơn hàng vào lịch sử
    static async moveOrderToHistory(orderId) {
        try {
            const order = await Order.findById(orderId);
            if (!order) return { success: false, message: 'Không tìm thấy đơn hàng.' };

            const tour = await Tour.findOne({ slug: order.items[0].slug });
            if (!tour) return { success: false, message: 'Không tìm thấy tour tương ứng.' };

            // Chuẩn hóa danh sách items cho History
            const historyItems = order.items.map(item => ({
                slug: item.slug,
                name: item.name,
                price: item.price,
                image: Array.isArray(item.image) ? item.image[0] : item.image,
                quantity: item.quantity,
                discount: item.discount,
                finalPrice: item.finalPrice
            }));

            //Thêm đầy đủ customerInfo từ order
            const history = new History({
                userId: order.userId,
                orderId: order._id,
                customerInfo: {
                    username: order.customerInfo.username,
                    phoneNumber: order.customerInfo.phoneNumber,
                    address: order.customerInfo.address,
                    email: order.customerInfo.email || ''
                },
                completedAt: new Date(),
                endDate: tour.endDate,
                items: historyItems,
            });

            await history.save();

            // Cập nhật báo cáo doanh thu
            const completedAt = history.completedAt;
            const month = completedAt.getMonth() + 1;
            const year = completedAt.getFullYear();

            let revenueReport = await RevenueReport.findOne({ month, year });
            if (!revenueReport) {
                revenueReport = new RevenueReport({ month, year, totalRevenue: 0, totalOrders: 0 });
            }

            revenueReport.totalRevenue += order.totalAmount;
            revenueReport.totalOrders += 1;
            await revenueReport.save();

            // Xóa order gốc sau khi chuyển lịch sử
            await Order.findByIdAndDelete(order._id);

            return { success: true, message: 'Đơn hàng đã chuyển vào lịch sử và cập nhật doanh thu.' };

        } catch (error) {
            console.error('Lỗi khi chuyển đơn hàng vào lịch sử:', error);
            return { success: false, message: 'Lỗi khi chuyển đơn hàng vào lịch sử.' };
        }
    }


    // 6. Lấy danh sách các đơn hàng "Hoàn tất"
    async getOrdersCompleted(req, res) {
        try {
            const orders = await Order.find({ status: 'Hoàn tất' });
            res.json({ success: true, data: orders });
        } catch (error) {
            console.error(error);
            res.status(500).json({ success: false, message: 'Có lỗi xảy ra khi lấy danh sách đơn hàng.' });
        }
    }

    // 7. Xác nhận đơn hàng đã hết hạn (hoàn tất tour)
    async confirmExpiredOrder(req, res) {
        const { orderId } = req.params;
        try {
            const result = await ManagerOrderController.moveOrderToHistory(orderId);

            if (result.success) {
                res.json({
                    success: true,
                    message: 'Đơn hàng đã được xác nhận hoàn tất.',
                    data: {
                        orderId,
                        status: 'Hoàn tất'
                    }
                });
            } else {
                res.status(400).json({
                    success: false,
                    message: result.message
                });
            }
        } catch (error) {
            console.error('Lỗi khi xác nhận đơn hàng hoàn tất:', error);
            res.status(500).json({
                success: false,
                message: 'Có lỗi xảy ra khi xác nhận đơn hàng hoàn tất.'
            });
        }
    }
}

module.exports = new ManagerOrderController();