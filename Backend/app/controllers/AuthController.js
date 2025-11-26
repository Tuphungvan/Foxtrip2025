// file: controllers/AuthController.js (hoặc nơi bạn đang để controller)
const User = require("../models/User");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { OAuth2Client } = require('google-auth-library'); //sua
const googleClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID); //sua

class AuthController {
    // Đăng ký
    async register(req, res) {
        try {
            const { username, email, password, phoneNumber, address } = req.body;

            if (!username || !email || !phoneNumber || !password) {
                return res.status(400).json({ message: "Thiếu thông tin bắt buộc." });
            }

            if (username.length < 6) {
                return res.status(400).json({ message: "Tên người dùng phải dài ít nhất 6 ký tự." });
            }

            // Kiểm tra trùng lặp
            const existingUser = await User.findOne({
                $or: [{ email }, { phoneNumber }],
            });

            if (existingUser) {
                let errorMessage = "Thông tin đã được sử dụng.";
                if (existingUser.email === email) errorMessage = "Email đã được đăng ký.";
                if (existingUser.phoneNumber === phoneNumber) errorMessage = "Số điện thoại đã được đăng ký.";
                return res.status(400).json({ message: errorMessage });
            }

            // Kiểm tra admin
            const adminExists = await User.findOne({ admin: true });
            const isAdmin = !adminExists;

            // Hash mật khẩu
            const hashedPassword = await bcrypt.hash(password, 10);

            const newUser = new User({
                username,
                email,
                password: hashedPassword,
                phoneNumber,
                address,
                admin: isAdmin,
                superadmin: false, // ✅ User đăng ký thường không phải superadmin
                active: true
            });

            await newUser.save();

            // ✅ Tạo JWT với đầy đủ thông tin
            const token = jwt.sign({
                id: newUser._id,
                username: newUser.username,
                email: newUser.email,
                admin: newUser.admin,
                superadmin: newUser.superadmin // ✅ Thêm superadmin vào token
            }, process.env.JWT_SECRET || "your-secret", { expiresIn: "30d" });

            // Trả data trực tiếp (không dùng data.user con)
            return res.status(201).json({
                success: true,
                data: {
                    id: newUser._id,
                    username: newUser.username,
                    email: newUser.email,
                    admin: newUser.admin,
                    superadmin: newUser.superadmin, // ✅ Thêm vào response
                    token
                },
                message: "Đăng ký thành công!"
            });

        } catch (err) {
            console.error(err);
            return res.status(500).json({ message: "Server error", error: err.message });
        }
    }

    // Đăng nhập
    async login(req, res) {
        try {
            const { email, password } = req.body;

            const user = await User.findOne({ email });
            if (!user) return res.status(400).json({ message: "Email không tồn tại." });
            if (!user.active) return res.status(403).json({ message: "Tài khoản bị vô hiệu." });

            const validPassword = await bcrypt.compare(password, user.password);
            if (!validPassword) return res.status(400).json({ message: "Mật khẩu không đúng." });

            // ✅ Tạo JWT với đầy đủ thông tin
            const token = jwt.sign({
                id: user._id,
                username: user.username,
                email: user.email,
                admin: user.admin,
                superadmin: user.superadmin || false // ✅ Thêm superadmin vào token
            }, process.env.JWT_SECRET || "your-secret", { expiresIn: "30d" });

            // Trả data trực tiếp (không dùng data.user con)
            return res.status(200).json({
                success: true,
                data: {
                    id: user._id,
                    username: user.username,
                    email: user.email,
                    admin: user.admin,
                    superadmin: user.superadmin || false, // ✅ Thêm vào response
                    token
                },
                message: "Đăng nhập thành công!"
            });

        } catch (err) {
            console.error(err);
            return res.status(500).json({ message: "Server error", error: err.message });
        }
    }

    // ✅ ĐĂNG NHẬP BẰNG GOOGLE
    async loginWithGoogle(req, res) {
        try {
            const { idToken } = req.body;

            if (!idToken) {
                return res.status(400).json({ message: "Thiếu Google ID Token" });
            }

            // Xác thực token với Google
            const ticket = await googleClient.verifyIdToken({
                idToken: idToken,
                audience: process.env.GOOGLE_CLIENT_ID,
            });

            const payload = ticket.getPayload();
            const googleId = payload['sub'];
            const email = payload['email'];
            const name = payload['name'];
            const picture = payload['picture'];

            console.log('Google login - Email:', email, 'GoogleId:', googleId);

            // Tìm user theo googleId TRƯỚC
            let user = await User.findOne({ googleId });

            // Nếu không tìm thấy, tìm theo email
            if (!user) {
                user = await User.findOne({ email });
            }

            if (user) {
                // User đã tồn tại - cập nhật thông tin
                let needUpdate = false;

                if (!user.googleId) {
                    user.googleId = googleId;
                    needUpdate = true;
                }
                
                // Cập nhật avatar nếu chưa có hoặc đang dùng default
                if (picture && (!user.avatar || user.avatar.includes('doremon'))) {
                    user.avatar = picture;
                    needUpdate = true;
                }
                
                // Cập nhật username nếu chưa có
                if (!user.username && name) {
                    user.username = name;
                    needUpdate = true;
                }

                if (needUpdate) {
                    await user.save();
                }

                if (!user.active) {
                    return res.status(403).json({ message: "Tài khoản bị vô hiệu." });
                }
            } else {
                // Tạo user mới
                const adminExists = await User.findOne({ admin: true });
                const isAdmin = !adminExists;

                // Tạo username từ name hoặc email (đảm bảo >= 6 ký tự)
                let username = name || email.split('@')[0];
                
                // Nếu username < 6 ký tự, thêm suffix
                if (username.length < 6) {
                    username = username + '_' + Math.random().toString(36).substring(2, 6);
                }

                // Kiểm tra username trùng và tạo unique nếu cần
                let baseUsername = username;
                let counter = 1;
                while (await User.findOne({ username })) {
                    username = baseUsername + counter;
                    counter++;
                }

                user = new User({
                    email,
                    username,
                    googleId,
                    avatar: picture || undefined,
                    admin: isAdmin,
                    superadmin: false,
                    active: true
                });

                await user.save();
                console.log('Created new user via Google:', user.email);
            }

            // Tạo JWT token
            const token = jwt.sign({
                id: user._id,
                username: user.username,
                email: user.email,
                admin: user.admin,
                superadmin: user.superadmin || false
            }, process.env.JWT_SECRET || "your-secret", { expiresIn: "30d" });

            return res.status(200).json({
                success: true,
                data: {
                    id: user._id,
                    username: user.username,
                    email: user.email,
                    admin: user.admin,
                    superadmin: user.superadmin || false,
                    avatar: user.avatar,
                    token
                },
                message: "Đăng nhập Google thành công!"
            });

        } catch (err) {
            console.error("Google login error:", err);
            return res.status(500).json({ message: "Lỗi đăng nhập Google", error: err.message });
        }
    }

    //LOGIN WITH FACEBOOK
    async loginWithFacebook(req, res) {
    try {
        const { accessToken, userID } = req.body;

        if (!accessToken || !userID) {
            return res.status(400).json({ message: "Thiếu Facebook access token" });
        }

        // Xác thực với Facebook Graph API
        const response = await fetch(
            `https://graph.facebook.com/me?fields=id,name,email,picture&access_token=${accessToken}`
        );

        if (!response.ok) {
            return res.status(400).json({ message: "Facebook token không hợp lệ" });
        }

        const fbData = await response.json();

        if (fbData.id !== userID) {
            return res.status(400).json({ message: "Facebook user ID không khớp" });
        }

        const facebookId = fbData.id;
        const email = fbData.email || null;  // ← CHO PHÉP null
        const name = fbData.name;
        const picture = fbData.picture?.data?.url;

        console.log('Facebook login - Email:', email || 'N/A', 'FacebookId:', facebookId);

        // Tìm user theo facebookId TRƯỚC
        let user = await User.findOne({ facebookId });

        // Nếu không tìm thấy và có email, tìm theo email
        if (!user && email) {
            user = await User.findOne({ email });
        }

        if (user) {
            // User đã tồn tại - cập nhật thông tin
            let needUpdate = false;

            if (!user.facebookId) {
                user.facebookId = facebookId;
                needUpdate = true;
            }
            
            // Cập nhật email nếu Facebook có email và user chưa có
            if (email && !user.email) {
                user.email = email;
                needUpdate = true;
            }
            
            // Cập nhật avatar nếu chưa có hoặc đang dùng default
            if (picture && (!user.avatar || user.avatar.includes('doremon'))) {
                user.avatar = picture;
                needUpdate = true;
            }
            
            // Cập nhật username nếu chưa có
            if (!user.username && name) {
                user.username = name;
                needUpdate = true;
            }

            if (needUpdate) {
                await user.save();
            }

            if (!user.active) {
                return res.status(403).json({ message: "Tài khoản bị vô hiệu." });
            }
        } else {
            // ✅ TẠO USER MỚI - CHO PHÉP KHÔNG CÓ EMAIL
            const adminExists = await User.findOne({ admin: true });
            const isAdmin = !adminExists;

            // Tạo username từ name, hoặc email, hoặc facebookId
            let username;
            if (name) {
                username = name;
            } else if (email) {
                username = email.split('@')[0];
            } else {
                // Trường hợp không có cả name và email
                username = `fb_user_${facebookId.substring(0, 8)}`;
            }
            
            // Nếu username < 6 ký tự, thêm suffix
            if (username.length < 6) {
                username = username + '_' + Math.random().toString(36).substring(2, 6);
            }

            // Kiểm tra username trùng và tạo unique nếu cần
            let baseUsername = username;
            let counter = 1;
            while (await User.findOne({ username })) {
                username = baseUsername + counter;
                counter++;
            }

            user = new User({
                email: email || null,  // ← CHO PHÉP null khi có facebookId
                username,
                facebookId,
                avatar: picture || undefined,
                admin: isAdmin,
                superadmin: false,
                active: true
            });

            await user.save();
            console.log('Created new user via Facebook:', user.username, '- Email:', user.email || 'N/A');
        }

        // Tạo JWT token
        const token = jwt.sign({
            id: user._id,
            username: user.username,
            email: user.email || null,
            admin: user.admin,
            superadmin: user.superadmin || false
        }, process.env.JWT_SECRET || "your-secret", { expiresIn: "30d" });

        return res.status(200).json({
            success: true,
            data: {
                id: user._id,
                username: user.username,
                email: user.email || null,
                admin: user.admin,
                superadmin: user.superadmin || false,
                avatar: user.avatar,
                token
            },
            message: "Đăng nhập Facebook thành công!"
        });

    } catch (err) {
        console.error("Facebook login error:", err);
        return res.status(500).json({ message: "Lỗi đăng nhập Facebook", error: err.message });
    }
}

    // Logout (stateless, client tự xóa token)
    async logout(req, res) {
        return res.status(200).json({ success: true, data: null, message: "Đăng xuất thành công. Xóa token trên client là đủ." });
    }

    // Check login status
    async checkLoginStatus(req, res) {
        if (!req.user) {
            return res.status(401).json({ success: false, data: null, message: "Bạn chưa đăng nhập." });
        }
        // req.user nên được gán từ middleware verify token
        return res.json({
            success: true,
            data: {
                id: req.user.id,
                username: req.user.username,
                email: req.user.email,
                admin: req.user.admin,
                superadmin: req.user.superadmin || false // ✅ Thêm vào response
            },
            message: "Đã xác thực"
        });
    }
}

module.exports = new AuthController();