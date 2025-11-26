// controllers/SiteController.js
const Tour = require('../models/Tour');
const Order = require('../models/Order');

function escapeRegex(text) {
    return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, '\\$&');
}

function shuffle(arr) {
    return arr.sort(() => 0.5 - Math.random());
}

function validTourCondition(extra = {}) {
    return {
        isBookable: true,
        availableSlots: { $gt: 0 },
        startDate: { $gte: new Date() },
        ...extra
    };
}

function mapTourForApi(tour) {
    const t = tour.toObject ? tour.toObject() : tour;
    if (t.image && Array.isArray(t.image)) {
        t.image = t.image; // giữ nguyên array
    } else if (t.image && typeof t.image === 'string') {
        // Nếu DB cũ lưu string thì convert thành array
        t.image = [t.image];
    } else {
        t.image = [];
    }
    return t;
}

class SiteController {
    // GET /api/tours/region/bac
    async toursBac(req, res) {
        try {
            const tours = await Tour.find(validTourCondition({ region: "Bắc" }));
            res.json({ success: true, data: shuffle(tours).slice(0, 6).map(mapTourForApi) });
        } catch (err) {
            res.status(500).json({ success: false, message: 'Lỗi lấy tour miền Bắc', error: err.message });
        }
    }

    // GET /api/tours/region/trung
    async toursTrung(req, res) {
        try {
            const tours = await Tour.find(validTourCondition({ region: "Trung" }));
            res.json({ success: true, data: shuffle(tours).slice(0, 6).map(mapTourForApi) });
        } catch (err) {
            res.status(500).json({ success: false, message: 'Lỗi lấy tour miền Trung', error: err.message });
        }
    }

    // GET /api/tours/region/nam
    async toursNam(req, res) {
        try {
            const tours = await Tour.find(validTourCondition({ region: "Nam" }));
            res.json({ success: true, data: shuffle(tours).slice(0, 6).map(mapTourForApi) });
        } catch (err) {
            res.status(500).json({ success: false, message: 'Lỗi lấy tour miền Nam', error: err.message });
        }
    }

    // GET /api/tours/hot
    async hotTours(req, res) {
        try {
            const agg = await Order.aggregate([
                { $group: { _id: "$tourId", orderCount: { $sum: 1 } } },
                { $sort: { orderCount: -1 } },
                { $limit: 20 }
            ]);
            const ids = agg.map(o => o._id);
            const tours = await Tour.find(validTourCondition({ _id: { $in: ids } }));
            res.json({ success: true, data: shuffle(tours).slice(0, 6).map(mapTourForApi) });
        } catch (err) {
            res.status(500).json({ success: false, message: "Lỗi lấy hot tours", error: err.message });
        }
    }

    // GET /api/tours/discount
    async discountTours(req, res) {
        try {
            const tours = await Tour.find(validTourCondition({ discount: { $gt: 0 } }))
                .sort({ discount: -1 });

            res.json({
                success: true,
                data: shuffle(tours).slice(0, 6).map(mapTourForApi)
            });
        } catch (err) {
            res.status(500).json({
                success: false,
                message: "Lỗi lấy tour giảm giá",
                error: err.message
            });
        }
    }

    // controllers/SiteController.js
    async search(req, res) {
        try {
            const { q, startDate, endDate, priceMin, priceMax, province, category } = req.query;
            const query = validTourCondition();

            if (q) {
                const escaped = escapeRegex(q.trim());
                query.name = { $regex: `(^|\\s)${escaped}(\\s|$)`, $options: 'i' };
            }

            if (province && province !== "null") query.province = province;
            if (category && category !== "null") query.category = category;
            if (startDate && startDate !== "null") query.startDate = { $gte: new Date(startDate) };
            if (endDate && endDate !== "null") query.endDate = { $lte: new Date(endDate) };

            if (priceMin && priceMax) {
                query.price = {
                    $gte: parseInt(priceMin, 10),
                    $lte: parseInt(priceMax, 10)
                };
            } else if (priceMax && !priceMin) {
                query.price = { $lte: parseInt(priceMax, 10) };
            } else if (priceMin && !priceMax) {
                query.price = { $gte: parseInt(priceMin, 10) };
            }

            const tours = await Tour.find(query);

            const result = tours.map(t => {
                const obj = mapTourForApi(t);
                obj.finalPrice = t.price * (1 - (t.discount || 0) / 100);
                return obj;
            });

            res.json({
                success: true,
                data: result,
                message: result.length ? null : "Không tìm thấy tour phù hợp"
            });
        } catch (err) {
            res.status(500).json({ success: false, message: 'Lỗi tìm kiếm', error: err.message });
        }
    }


    // GET /api/tours/:slug
    async detail(req, res) {
        try {
            const tour = await Tour.findOne(validTourCondition({ slug: req.params.slug }));
            if (!tour) {
                return res.status(404).json({ success: false, message: 'Tour không tồn tại hoặc đã dừng đặt.' });
            }
            res.json({ success: true, data: tour });
        } catch (err) {
            res.status(500).json({ success: false, message: 'Lỗi chi tiết tour', error: err.message });
        }
    }
}

module.exports = new SiteController();
