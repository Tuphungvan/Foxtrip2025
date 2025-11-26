const Tour = require("../models/Tour");

class TourController {
    async getShorts(req, res) {
        try {
            const shorts = await Tour.find(
                { shortUrl: { $exists: true, $ne: "" } },
                { name: 1, slug: 1, shortUrl: 1, image: 1, price: 1 }
            ).lean();
            return res.json({
                success: true,
                data: shorts,
                message: "Danh sách video short"
            });
        } catch (err) {
            return res.status(500).json({ success: false, message: "Server error" });
        }
    }

    // API mới: Trả về 1 video ngẫu nhiên
    async getRandomShort(req, res) {
        try {
            const count = await Tour.countDocuments({
                shortUrl: { $exists: true, $ne: "" }
            });

            if (count === 0) {
                return res.json({
                    success: true,
                    data: null,
                    message: "Không có video nào"
                });
            }

            const random = Math.floor(Math.random() * count);
            const short = await Tour.findOne(
                { shortUrl: { $exists: true, $ne: "" } },
                { name: 1, slug: 1, shortUrl: 1, image: 1, price: 1 }
            )
                .skip(random)
                .lean();

            return res.json({
                success: true,
                data: short,
                message: "Video ngẫu nhiên"
            });
        } catch (err) {
            return res.status(500).json({
                success: false,
                message: "Server error"
            });
        }
    }
}

module.exports = new TourController();
