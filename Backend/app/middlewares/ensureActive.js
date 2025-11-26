const User = require("../models/User");

async function ensureActive(req, res, next) {
    if (!req.user) {
        return res.status(401).json({ message: "Bạn chưa đăng nhập." });
    }

    try {
        const user = await User.findById(req.user.id);
        if (!user || !user.active) {
            return res.status(403).json({ message: "Tài khoản đã bị khóa." });
        }
        next();
    } catch (err) {
        return res.status(500).json({ message: "Server error", error: err.message });
    }
}

module.exports = ensureActive;
