const nodemailer = require("nodemailer");
const crypto = require("crypto");

// Lưu tạm OTP (trong thực tế nên dùng Redis)
const otpStore = new Map(); // { email: { code, expiresAt, userData } }

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
 * Tạo mã OTP 6 số
 */
function generateOTP() {
    return Math.floor(100000 + Math.random() * 900000).toString();
}

/**
 * Gửi mã OTP qua email
 */
async function sendVerificationOTP(email, username) {
    try {
        const otp = generateOTP();
        const expiresAt = Date.now() + 10 * 60 * 1000; // Hết hạn sau 10 phút

        // Lưu OTP tạm
        otpStore.set(email, { code: otp, expiresAt });

        const transporter = createTransporter();

        await transporter.sendMail({
            from: `"Foxtrip" <${process.env.MAIL_USER}>`,
            to: email,
            subject: "Mã xác thực đặt hàng",
            text: `
Xin chào ${username},

Mã xác thực của bạn là: ${otp}

Mã này có hiệu lực trong 10 phút.

Vui lòng không chia sẻ mã này với bất kỳ ai.

Trân trọng,
Foxtrip Team
            `,
            html: `
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #4CAF50;">Xác thực đặt hàng tại Foxtrip</h2>
                    <p>Xin chào <strong>${username}</strong>,</p>
                    <p>Mã xác thực của bạn là:</p>
                    <div style="background-color: #f0f0f0; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; margin: 20px 0;">
                        ${otp}
                    </div>
                    <p style="color: #666;">Mã này có hiệu lực trong <strong>10 phút</strong>.</p>
                    <p style="color: #999; font-size: 12px;">Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                    <hr style="margin-top: 30px;">
                    <p style="color: #999; font-size: 11px;">Trân trọng,<br>Foxtrip Team</p>
                </div>
            `
        });

        console.log(`✅ OTP đã gửi tới: ${email}`);
        return true;
    } catch (err) {
        console.error("❌ sendVerificationOTP error:", err);
        return false;
    }
}

/**
 * Xác thực OTP
 */
function verifyOTP(email, code) {
    const stored = otpStore.get(email);

    if (!stored) {
        return { success: false, message: "Mã OTP không tồn tại hoặc đã hết hạn." };
    }

    if (Date.now() > stored.expiresAt) {
        otpStore.delete(email);
        return { success: false, message: "Mã OTP đã hết hạn." };
    }

    if (stored.code !== code) {
        return { success: false, message: "Mã OTP không đúng." };
    }

    // Xóa OTP sau khi xác thực thành công
    otpStore.delete(email);
    return { success: true, message: "Xác thực thành công!" };
}

module.exports = {
    sendVerificationOTP,
    verifyOTP
};