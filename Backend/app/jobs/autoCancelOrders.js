const cron = require('node-cron');
const Order = require('../models/Order');
const { sendOrderEmail } = require('../utils/sendOrderEmail');

// 🕐 Mỗi 1 tiếng (vào phút 0)
cron.schedule('0 * * * *', async () => {
    console.log('🕐 [JOB] Kiểm tra đơn hàng chờ thanh toán quá 24h...');

    try {
        const limit = new Date(Date.now() - 24 * 60 * 60 * 1000); // 24h trước
        const expiredOrders = await Order.find({
            status: 'Chờ thanh toán',
            createdAt: { $lt: limit },
        });

        if (expiredOrders.length === 0) {
            console.log('✅ Không có đơn hàng nào cần hủy.');
            return;
        }

        for (const order of expiredOrders) {
            await sendOrderEmail(order, 'delete');
            await Order.findByIdAndDelete(order._id);
            console.log(`🗑 Đã xóa đơn quá hạn #${order._id}`);
        }
    } catch (error) {
        console.error('❌ [JOB] Lỗi khi dọn dẹp đơn hàng:', error);
    }
});

console.log('🚀 [JOB] autoCancelOrders đã được khởi động.');
