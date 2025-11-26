// controllers/ChatbotController.js
const { queryGroq } = require("../../config/queryGroq");
const Tour = require("../models/Tour");
const Order = require("../models/Order");
const History = require("../models/History");
const Wallet = require("../models/Wallet");

/**
 * 🧮 Hàm bắt số tiền trong câu hỏi
 * Ví dụ: "trên 7 triệu", "dưới 2,5 triệu", "từ 3 triệu đến 5 triệu"
 */
function parsePriceQuery(msg) {
    const regex = /(\d+(?:[.,]\d+)*)\s*(triệu|tr|k|nghìn|ngàn|đ|d)?/gi;
    const numbers = [];
    let match;

    while ((match = regex.exec(msg)) !== null) {
        let value = parseFloat(match[1].replace(',', '.'));
        const unit = match[2] || '';
        if (unit.includes('triệu') || unit.includes('tr')) value *= 1_000_000;
        else if (unit.includes('k') || unit.includes('nghìn') || unit.includes('ngàn')) value *= 1_000;
        numbers.push(value);
    }

    let minPrice = 0;
    let maxPrice = Infinity;

    if (numbers.length === 1) {
        if (msg.includes('trên') || msg.includes('từ') || msg.includes('hơn')) {
            minPrice = numbers[0];
        } else if (msg.includes('dưới') || msg.includes('ít hơn') || msg.includes('<=')) {
            maxPrice = numbers[0];
        } else {
            minPrice = numbers[0] * 0.9;
            maxPrice = numbers[0] * 1.1;
        }
    } else if (numbers.length === 2) {
        minPrice = Math.min(numbers[0], numbers[1]);
        maxPrice = Math.max(numbers[0], numbers[1]);
    }

    return { minPrice, maxPrice };
}

/**
 * 🧠 Hàm lọc tour theo nội dung người dùng
 */
function filterTours(message, tours) {
    const lower = message.toLowerCase();
    const { minPrice, maxPrice } = parsePriceQuery(lower);

    return tours.filter(t => {
        const name = t.name.toLowerCase();
        const desc = t.description.toLowerCase();
        const itinerary = t.itinerary.toLowerCase();
        const province = t.province.toLowerCase();

        // match theo tên / mô tả / lịch trình
        if (lower.includes(name) || desc.includes(lower) || itinerary.includes(lower)) return true;

        // tỉnh
        if (lower.includes(province)) return true;

        // vùng
        if (lower.includes('bắc') && t.region === 'Bắc') return true;
        if (lower.includes('trung') && t.region === 'Trung') return true;
        if (lower.includes('nam') && t.region === 'Nam') return true;

        // thể loại
        if (lower.includes('biển') && t.category === 'Biển') return true;
        if (lower.includes('văn hóa') && t.category === 'Văn hóa') return true;
        if (lower.includes('nghỉ dưỡng') && t.category === 'Nghỉ dưỡng') return true;

        // giảm giá
        if (lower.includes('giảm giá') && t.discount > 0) return true;

        // giá
        if (t.price >= minPrice && t.price <= maxPrice) return true;

        // số chỗ
        if (lower.includes('còn chỗ') && t.availableSlots > 0) return true;

        return false;
    });
}

class ChatbotController {
    async chat(req, res) {
        try {
            const { message, userId } = req.body;
            if (!message) {
                return res.status(400).json({ success: false, message: "Thiếu nội dung câu hỏi." });
            }

            const lowerMsg = message.toLowerCase();
            let preAnswer = "";

            // ===== 1️⃣: Lấy dữ liệu cơ bản =====
            const [tours, orders, wallet, histories] = await Promise.all([
                Tour.find({ isBookable: true, availableSlots: { $gt: 0 } }).sort({ createdAt: -1 }),
                userId ? Order.find({ userId }).sort({ createdAt: -1 }) : [],
                userId ? Wallet.findOne({ userId }) : null,
                userId ? History.find({ userId }).sort({ completedAt: -1 }) : []
            ]);

            // ===== 2️⃣: Các loại câu hỏi cơ bản =====
            if (
                lowerMsg.includes('xem đơn hàng') ||
                lowerMsg.includes('nạp tiền') ||
                lowerMsg.includes('lịch sử') ||
                lowerMsg.includes('đổi mật khẩu')
            ) {
                preAnswer = "Vui lòng truy cập trang *Tài khoản cá nhân* để xem hoặc thực hiện thao tác này.";
            }

            else if (
                lowerMsg.includes('làm sao nhận vé') ||
                lowerMsg.includes('vé') ||
                lowerMsg.includes('vé du lịch') ||
                lowerMsg.includes('nhận vé') ||
                lowerMsg.includes('đặt xong thì nhận tour kiểu gì')
            ) {
                preAnswer = "Nếu bạn đang hỏi về cách thức nhận vé tour du lịch. Chúng tôi sẽ gọi điện để xác nhận đơn hàng, gửi mail xác nhận đồng thời gửi vé cứng về nhận nhà theo địa chỉ của bạn. Bạn có thể xác minh bằng vé cứng hoặc email tại địa điểm du lịch";
            }

            else if (
                lowerMsg.includes('foxtrip') ||
                lowerMsg.includes('liên lạc') ||
                lowerMsg.includes('liên hệ') ||
                lowerMsg.includes('hỗ trợ')
            ) {
                preAnswer = "Foxtrip là công ty du lịch tại Việt Nam. Bạn có thể liên hệ với chúng tôi qua số điện thoại 0859605024, email cloneappadobe@gmail.com, fanpage chính thức https://www.facebook.com/foxtrip.2025/";
            }

            else if (
                lowerMsg.includes('đã thanh toán') ||
                lowerMsg.includes('chưa hoàn tất') ||
                lowerMsg.includes('chưa thấy email') ||
                lowerMsg.includes('không thấy email') ||
                lowerMsg.includes('chưa xác nhận')
            ) {
                preAnswer = "Yêu cầu của bạn đang chờ được hệ thống phê duyệt. Nếu lâu quá, bạn có thể liên hệ bộ phận hỗ trợ của Foxtrip theo số điện thoại 0859605024.";
            }

            else if (
                lowerMsg.includes('đơn hàng bị hủy') ||
                lowerMsg.includes('không thấy đơn hàng') ||
                lowerMsg.includes('không thấy đơn hàng nữa')
            ) {
                preAnswer = "Đơn hàng của bạn có thể bị hủy do không thanh toán trong 24h hoặc do tour đã hết hạn. Vui lòng liên hệ bộ phận hỗ trợ của Foxtrip theo số điện thoại 0859605024 để biết chi tiết.";
            }

            // ===== 3️⃣: Trạng thái đơn hàng =====
            else if (lowerMsg.includes("đơn hàng") || lowerMsg.includes("đặt hàng")) {
                if (orders.length === 0) {
                    preAnswer = "Bạn chưa có đơn hàng nào trong hệ thống.";
                } else {
                    const latest = orders[0];
                    preAnswer = `Đơn hàng gần nhất (${latest._id}) hiện ở trạng thái *${latest.status}*.`;
                }
            }

            // ===== 4️⃣: Ví / thanh toán =====
            else if (lowerMsg.includes("thanh toán") || lowerMsg.includes("liên kết ngân hàng")) {
                if (!wallet) {
                    preAnswer = "Bạn chưa có ví điện tử. Hãy vào trang Tài khoản để tạo ví hoặc liên hệ hỗ trợ.";
                } else if (wallet.balance <= 0) {
                    preAnswer = "Ví của bạn không đủ số dư. Vui lòng nạp tiền trong trang Tài khoản.";
                } else {
                    preAnswer = `Ví của bạn hiện có ${wallet.balance.toLocaleString()}đ.`;
                }
            }

            // ===== 5️⃣: Tour - lọc chi tiết =====
            else {
                const filtered = filterTours(message, tours);
                if (filtered.length > 0) {
                    preAnswer = `Tìm thấy ${filtered.length} tour phù hợp:\n` +
                        filtered.slice(0, 10).map(t =>
                            `- ${t.name} (${t.province}) - ${t.price.toLocaleString()}đ${t.discount > 0 ? ` (Giảm ${t.discount}%)` : ""}`
                        ).join("\n");
                } else {
                    preAnswer = "Không tìm thấy tour nào phù hợp với yêu cầu của bạn.";
                }
            }

            // ===== 6️⃣: Tạo prompt gửi LLM =====
            let prompt;

            if (preAnswer && preAnswer.trim() !== "") {
                // Nếu đã có câu trả lời → chỉ cần viết lại
                prompt = `
Bạn là trợ lý chatbot du lịch chuyên nghiệp của công ty foxtrip.
Dưới đây là câu trả lời hệ thống đã chuẩn bị.
Hãy viết lại thân thiện, tự nhiên, KHÔNG bịa thêm thông tin, nếu không có thông tin về yêu cầu thì hãy nói "Rất tiếc, tôi không có thông tin về điều đó." Nhưng tôi có thể hỗ trợ bạn vấn đề khác liên quan đến app du lịch foxtrip:
"${preAnswer}"
                `;
            } else {
                // Nếu chưa có → gửi context để model tự suy luận
                const context = JSON.stringify({
                    tours: tours.map(t => ({
                        id: t._id,
                        name: t.name,
                        price: t.price,
                        province: t.province,
                        category: t.category,
                        region: t.region,
                        discount: t.discount,
                        availableSlots: t.availableSlots
                    })),
                    orders,
                    wallet,
                    histories
                });

                prompt = `
Bạn là trợ lý chatbot du lịch.
Dữ liệu hệ thống: ${context}
Người dùng hỏi: "${message}"
Hãy trả lời thân thiện và chính xác dựa trên dữ liệu. Không được bịa thông tin tour.
                `;
            }

            // ===== 7️⃣: Gọi model =====
            const reply = await queryGroq("llama-3.1-8b-instant", prompt);

            return res.json({
                success: true,
                data: reply,
                message: "Chatbot đã phản hồi thành công."
            });

        } catch (error) {
            console.error("❌ Lỗi chatbot:", error);
            return res.status(500).json({
                success: false,
                message: "Đã xảy ra lỗi với chatbot. Vui lòng thử lại sau."
            });
        }
    }
}

module.exports = new ChatbotController();
