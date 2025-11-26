function isAdmin(req, res, next) {
    if (req.user && (req.user.admin || req.user.superadmin)) {
        return next();
    }
    return res.status(403).json({ message: "Chỉ admin mới có quyền truy cập." });
}

module.exports = isAdmin;