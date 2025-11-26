const { sendVerificationOTP, verifyOTP } = require('../utils/emailService');

class OTPController {
    // [POST] /otp/send
    async sendOTP(req, res) {
        try {
            const userId = req.user.id;

            const { email, username } = req.body;
            if (!email || !username) {
                return res.status(400).json({ 
                    success: false, 
                    message: 'Thiếu thông tin email hoặc tên' 
                });
            }

            const sent = await sendVerificationOTP(email, username);
            
            if (sent) {
                res.json({ 
                    success: true, 
                    message: 'Mã OTP đã được gửi đến email của bạn' 
                });
            } else {
                res.status(500).json({ 
                    success: false, 
                    message: 'Không thể gửi OTP, vui lòng thử lại' 
                });
            }
        } catch (error) {
            console.error(error);
            res.status(500).json({ 
                success: false, 
                message: 'Lỗi hệ thống' 
            });
        }
    }

    // [POST] /otp/verify
    async verifyOTPCode(req, res) {
        try {
            const userId = req.user.id;

            const { email, code } = req.body;
            if (!email || !code) {
                return res.status(400).json({ 
                    success: false, 
                    message: 'Thiếu thông tin email hoặc mã OTP' 
                });
            }

            const result = verifyOTP(email, code);
            
            if (result.success) {
                res.json({ 
                    success: true, 
                    message: result.message 
                });
            } else {
                res.status(400).json({ 
                    success: false, 
                    message: result.message 
                });
            }
        } catch (error) {
            console.error(error);
            res.status(500).json({ 
                success: false, 
                message: 'Lỗi hệ thống' 
            });
        }
    }
}

module.exports = new OTPController();