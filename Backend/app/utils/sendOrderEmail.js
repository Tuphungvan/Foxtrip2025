const nodemailer = require("nodemailer");
const QRCode = require("qrcode");
const User = require("../models/User");
const Tour = require("../models/Tour");

/**
 * Tạo transporter dùng cấu hình trong .env
 */
function createTransporter() {
    return nodemailer.createTransport({
        service: process.env.MAIL_SERVICE || "gmail",
        auth: {
            user: process.env.MAIL_USER,
            pass: process.env.MAIL_PASS,
        },
    });
}

// function createTransporter() {
//     return nodemailer.createTransport({
//         host: "smtp.gmail.com",
//         port: 587,        // port STARTTLS (không phải 465)
//         secure: false,    // false để dùng STARTTLS
//         auth: {
//             user: process.env.MAIL_USER,
//             pass: process.env.MAIL_PASS, // App password Gmail
//         },
//         tls: {
//             rejectUnauthorized: false, // Bỏ qua kiểm tra chứng chỉ
//         },
//         connectionTimeout: 20000
//     });
// }


/**
 * Gửi email cho các trường hợp khác nhau:
 * - "confirm": Đơn hàng được xác nhận thành công (có QR)
 * - "delete": Đơn hàng bị hủy (không có QR)
 */
async function sendOrderEmail(order, type = "confirm") {
    try {
        // 1️⃣ Xác định email người nhận
        let email = order?.customerInfo?.email;
        if (!email) {
            const user = await User.findById(order.userId).select("email username");
            email = user?.email;
            if (!order.customerInfo) order.customerInfo = {};
            order.customerInfo.username = order.customerInfo.username || user?.username || "Khách hàng";
        }

        if (!email) {
            console.warn(`⚠️ Không tìm thấy email để gửi cho đơn hàng ${order._id}`);
            return false;
        }

        // 2️⃣ Tạo transporter
        const transporter = createTransporter();

        // 3️⃣ Lấy thông tin tour liên quan
        const slugs = (order.items || []).map(i => i.slug).filter(Boolean);
        const tours = slugs.length > 0 ? await Tour.find({ slug: { $in: slugs } }).lean() : [];

        // helper định dạng ngày
        const fmt = d => d ? new Date(d).toLocaleDateString() : "Không rõ";

        // 4️⃣ Build chi tiết tour
        const tourDetails = (order.items || []).map((item, i) => {
            const tour = tours.find(t => t.slug === item.slug);
            return `
${i + 1}. ${item.name}
- Số lượng: ${item.quantity}
- Giá (1 đơn vị): ${item.finalPrice}đ
- Thời gian: ${fmt(tour?.startDate)} - ${fmt(tour?.endDate)}

📍 Lộ trình:
${tour?.itinerary || "Không có lộ trình."}
`;
        }).join("\n");

        // 5️⃣ Chuẩn bị nội dung email
        let subject = "";
        let emailText = "";
        let emailHtml = "";
        let attachments = [];

        if (type === "confirm") {
            // 🔹 Chỉ tạo QR trong trường hợp xác nhận
            const qrCodeBuffer = await QRCode.toBuffer(order._id.toString(), {
                width: 300,
                margin: 2,
                color: {
                    dark: '#000000',
                    light: '#FFFFFF'
                }
            });

            attachments.push({
                filename: 'qrcode.png',
                content: qrCodeBuffer,
                cid: 'qrcode'
            });

            subject = `Xác nhận đặt tour thành công - Mã đơn #${order._id}`;
            emailText = `
Xin chào ${order.customerInfo?.username || "Khách hàng"},

✅ Đơn hàng #${order._id} của bạn đã được xác nhận thành công!

📍 Thông tin khách hàng:
- Họ tên: ${order.customerInfo?.username || "Không có"}
- SĐT: ${order.customerInfo?.phoneNumber || "Không có"}
- Email: ${email}
- Địa chỉ: ${order.customerInfo?.address || "Không có"}

🕐 Thời gian đặt: ${new Date(order.createdAt || Date.now()).toLocaleString()}

📦 Chi tiết tour đã đặt:
${tourDetails}

💳 Tổng tiền thanh toán: ${order.totalAmount}đ

Cảm ơn bạn đã tin tưởng Foxtrip!
            `;

            emailHtml = `
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
        .container { 
            /* Đổi max-width thành width 100% */
            width: 100%; 
            box-sizing: border-box; /* Đảm bảo padding không làm vỡ layout */
            margin: 0 auto; 
            padding: 0; /* Bỏ padding ngoài cùng */
        }
        .header { 
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
            color: white; 
            padding: 30px; 
            text-align: center; 
            /* Bỏ border-radius để full-width */
        }
        .content { 
            background: #f9f9f9; 
            padding: 30px; 
            /* Bỏ border-radius */
            text-align: center; /* Căn giữa nội dung theo yêu cầu */
        }
        .order-id { font-size: 16px; font-weight: bold; margin: 10px 0; }
        .section { 
            background: white; 
            padding: 20px; 
            margin: 20px auto; /* Căn giữa block section */
            border-radius: 8px; 
            box-shadow: 0 2px 4px rgba(0,0,0,0.1); 
            max-width: 700px; /* Giới hạn chiều rộng nội dung để dễ đọc */
        }
        .section-title { 
            color: #667eea; 
            font-size: 12px; 
            font-weight: bold; 
            margin-bottom: 15px; 
            border-bottom: 2px solid #667eea; 
            padding-bottom: 10px; 
            text-align: center; /* Đã được .content căn */
        }
        .info-row { 
            margin: 10px 0; 
            /* Căn trái text bên trong, nhưng căn giữa block */
            display: table; 
            margin-left: auto;
            margin-right: auto;
            text-align: left; 
        }
        .label { font-weight: bold; color: #555; }
        .qr-section { 
            text-align: center; 
            padding: 20px; 
            background: white; 
            border-radius: 8px; 
            margin: 20px auto; /* Căn giữa block */
            max-width: 700px; /* Giống .section */
        }
        .qr-title { color: #667eea; font-size: 12px; font-weight: bold; margin-bottom: 10px; }
        .qr-code { margin: 20px 0; }
        .tour-item { 
            background: #f5f5f5; 
            padding: 15px; 
            margin: 10px 0; 
            border-radius: 5px; 
            text-align: left; /* Giữ chi tiết tour căn trái để dễ đọc */
        }
        .total { 
            font-size: 14px; 
            font-weight: bold; 
            color: #667eea; 
            text-align: center; /* Căn giữa tổng tiền */
            margin-top: 20px; 
        }
        .footer { 
            text-align: center; 
            margin-top: 30px; 
            color: #888; 
            font-size: 12px; 
        }
    </style>
</head>
<body style="margin: 0; padding: 0;">
    <div class="container">
        <div class="header">
            <h1>🎉 Đặt tour thành công!</h1>
            <div class="order-id">Mã đơn: #${order._id}</div>
        </div>
        
        <div class="content">
            <div class="section" style="text-align: left;"> <!-- Section này căn trái cho dễ đọc -->
                <div class="section-title">📍 Thông tin khách hàng</div>
                <div class="info-row"><span class="label">Họ tên:</span> ${order.customerInfo?.username || "Không có"}</div>
                <div class="info-row"><span class="label">SĐT:</span> ${order.customerInfo?.phoneNumber || "Không có"}</div>
                <div class="info-row"><span class="label">Email:</span> ${email}</div>
                <div class="info-row"><span class="label">Địa chỉ:</span> ${order.customerInfo?.address || "Không có"}</div>
                <div class="info-row"><span class="label">Thời gian đặt:</span> ${new Date(order.createdAt || Date.now()).toLocaleString()}</div>
            </div>

            <div class="section">
                <div class="section-title">📦 Chi tiết tour đã đặt</div>
                ${(order.items || []).map((item, i) => {
                    const tour = tours.find(t => t.slug === item.slug);
                    return `
                    <div class="tour-item">
                        <strong>${i + 1}. ${item.name}</strong><br>
                        Số lượng: ${item.quantity}<br>
                        Giá: ${item.finalPrice}đ<br>
                        Thời gian: ${fmt(tour?.startDate)} - ${fmt(tour?.endDate)}<br>
                        <br>
                        <strong>📍 Lộ trình:</strong><br>
                        <div style="white-space: pre-wrap;">${tour?.itinerary || "Không có lộ trình."}</div>
                    </div>
                    `;
                }).join('')}
                <div class="total">💳 Tổng tiền: ${order.totalAmount.toLocaleString()}đ</div>
            </div>

            <div class="qr-section">
                <div class="qr-title">📱 Mã QR quản trị viên dùng để xác thực</div>
                <div class="qr-code">
                    <img src="cid:qrcode" alt="QR Code" style="max-width: 250px;">
                </div>
                <p style="color: #888; font-size: 12px;">
                    Lưu trữ mã này để nhân viên của Foxtrip xác nhận danh tính của bạn tại địa điểm du lịch
                </p>
            </div>

            <div class="footer">
                Cảm ơn bạn đã tin tưởng Foxtrip!<br>
                Mọi thắc mắc vui lòng liên hệ: cloneappadobe@gmail.com
            </div>
        </div>
    </div>
</body>
</html>
            `;

        } else if (type === "delete") {
            subject = `Thông báo hủy đơn hàng - Mã đơn #${order._id}`;
            emailText = `
Xin chào ${order.customerInfo?.username || "Khách hàng"},

❌ Đơn hàng #${order._id} của bạn đã bị hủy do lỗi hoặc do quá 24h mà chưa hoàn tất thanh toán.

📦 Thông tin tour:
${tourDetails}

Nếu bạn vẫn muốn tham gia tour, vui lòng đặt lại trên hệ thống Foxtrip.

Trân trọng,
Foxtrip Team
            `;

            emailHtml = `
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
        .container { 
            /* Đổi max-width thành width 100% */
            width: 100%; 
            box-sizing: border-box;
            margin: 0 auto; 
            padding: 0;
        }
        .header { 
            background: #e74c3c; 
            color: white; 
            padding: 30px; 
            text-align: center; 
            /* Bỏ border-radius */
        }
        .content { 
            background: #f9f9f9; 
            padding: 30px; 
            /* Bỏ border-radius */
            text-align: center; /* Căn giữa nội dung */
        }
        .section { 
            background: white; 
            padding: 20px; 
            margin: 20px auto; /* Căn giữa block */
            border-radius: 8px; 
            max-width: 700px; /* Giới hạn chiều rộng để dễ đọc */
            text-align: center; /* Căn giữa text trong section này */
        }
        .footer { 
            text-align: center; 
            margin-top: 30px; 
            color: #888; 
            font-size: 12px; 
        }
    </style>
</head>
<body style="margin: 0; padding: 0;">
    <div class="container">
        <div class="header">
            <h1>❌ Đơn hàng đã bị hủy</h1>
            <div style="font-size: 12px; margin-top: 10px;">Mã đơn: #${order._id}</div>
        </div>
        
        <div class="content">
            <div class="section">
                <p>Xin chào <strong>${order.customerInfo?.username || "Khách hàng"}</strong>,</p>
                <p>Đơn hàng #${order._id} của bạn đã bị hủy do lỗi hoặc do quá 24h mà chưa hoàn tất thanh toán.</p>
                <p>Nếu bạn vẫn muốn tham gia tour, vui lòng đặt lại trên hệ thống Foxtrip.</p>
            </div>

            <div class="footer">
                Trân trọng,<br>
                Foxtrip Team<br>
                cloneappadobe@gmail.com
            </div>
        </div>
    </div>
</body>
</html>
            `;
        }

        // 6️⃣ Gửi email
        await transporter.sendMail({
            from: `"Foxtrip" <${process.env.MAIL_USER}>`,
            to: email,
            subject,
            text: emailText,
            html: emailHtml,
            attachments
        });

        console.log(`✅ Email (${type}) đã gửi tới:`, email);
        return true;

    } catch (err) {
        console.error("❌ sendOrderEmail error:", err);
        return false;
    }
}

module.exports = { sendOrderEmail };