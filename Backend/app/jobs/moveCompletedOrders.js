const cron = require('node-cron');
const Order = require('../models/Order');
const History = require('../models/History');
const Tour = require('../models/Tour');
const RevenueReport = require('../models/RevenueReport');

// 🕛 Chạy lúc 0h mỗi ngày
cron.schedule('0 0 * * *', async () => {
    console.log('📦 [JOB] Kiểm tra đơn hàng đã hết hạn tour...');

    try {
        const now = new Date();
        const completedOrders = await Order.find({ status: 'Hoàn tất' });

        for (const order of completedOrders) {
            const tour = await Tour.findOne({ slug: order.items[0]?.slug });
            if (!tour) continue;

            // Nếu tour đã kết thúc
            if (new Date(tour.endDate) <= now) {
                const historyItems = order.items.map(item => ({
                    slug: item.slug,
                    name: item.name,
                    price: item.price,
                    image: Array.isArray(item.image) ? item.image[0] : item.image,
                    quantity: item.quantity,
                    discount: item.discount,
                    finalPrice: item.finalPrice
                }));

                // ✅ Ghi vào lịch sử đầy đủ customerInfo
                const history = new History({
                    userId: order.userId,
                    orderId: order._id,
                    customerInfo: {
                        username: order.customerInfo.username,
                        phoneNumber: order.customerInfo.phoneNumber,
                        address: order.customerInfo.address,
                        email: order.customerInfo.email || ''
                    },
                    completedAt: now,
                    endDate: tour.endDate,
                    items: historyItems,
                });

                await history.save();

                // 📊 Cập nhật doanh thu tháng
                const month = now.getMonth() + 1;
                const year = now.getFullYear();
                let report = await RevenueReport.findOne({ month, year });
                if (!report) {
                    report = new RevenueReport({ month, year, totalRevenue: 0, totalOrders: 0 });
                }
                report.totalRevenue += order.totalAmount;
                report.totalOrders += 1;
                await report.save();

                // 🧹 Xóa đơn khỏi bảng chính
                await Order.findByIdAndDelete(order._id);

                console.log(`✅ [JOB] Đã chuyển đơn #${order._id} sang lịch sử.`);
            }
        }
    } catch (error) {
        console.error('❌ [JOB] Lỗi khi chuyển đơn sang lịch sử:', error);
    }
});

console.log('🚀 [JOB] moveCompletedOrders đã được khởi động.');
