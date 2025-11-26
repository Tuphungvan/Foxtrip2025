const Cart = require('../models/Cart');
const Tour = require('../models/Tour');

class CartController {
    // [POST] /cart/add/:slug
    async addToCart(req, res) {
        try {
            const userId = req.user?.id;
            if (!userId) {
                return res.status(401).json({ success: false, data: null, message: 'Vui lòng đăng nhập' });
            }

            const { slug } = req.params;
            const qty = parseInt(req.body.quantity, 10) || 1;
            if (qty <= 0) return res.status(400).json({ success: false, data: null, message: 'Số lượng không hợp lệ' });

            const tour = await Tour.findOne({ slug });
            if (!tour) return res.status(404).json({ success: false, data: null, message: 'Tour không tồn tại' });

            if (!tour.isBookable) return res.status(400).json({ success: false, data: null, message: 'Tour hiện không mở đặt' });
            if (!tour.availableSlots || tour.availableSlots <= 0) return res.status(400).json({ success: false, data: null, message: 'Tour đã hết chỗ' });
            if (tour.startDate && new Date(tour.startDate) <= new Date()) return res.status(400).json({ success: false, data: null, message: 'Tour đã bắt đầu' });

            let cart = await Cart.findOne({ userId });
            if (!cart) cart = new Cart({ userId, items: [] });

            const existingItem = cart.items.find(it => it.slug === slug);
            const existingQty = existingItem ? existingItem.quantity : 0;
            const newQtyTotal = existingQty + qty;

            if (newQtyTotal > tour.availableSlots) {
                return res.status(400).json({ success: false, data: null, message: `Không đủ slot. Hiện còn ${tour.availableSlots}` });
            }

            const discountPercent = Number(tour.discount || 0);
            const unitFinalPrice = Math.round(tour.price * (1 - discountPercent / 100));
            const tourImage = Array.isArray(tour.image) && tour.image.length ? tour.image[0] : (tour.image || '');

            if (existingItem) {
                existingItem.quantity = newQtyTotal;
                existingItem.price = tour.price;
                existingItem.discount = discountPercent;
                existingItem.finalPrice = unitFinalPrice;
                existingItem.image = tourImage;
            } else {
                cart.items.push({
                    slug: tour.slug,
                    name: tour.name,
                    price: tour.price || 0,
                    image: tourImage,
                    quantity: qty,
                    discount: discountPercent,
                    finalPrice: unitFinalPrice
                });
            }

            await cart.save();
            return res.json({ success: true, data: { cart }, message: 'Thêm vào giỏ hàng thành công' });
        } catch (err) {
            console.error(err);
            return res.status(500).json({ success: false, data: null, message: 'Lỗi khi thêm vào giỏ' });
        }
    }

    // [GET] /cart
    async viewCart(req, res) {
        try {
            const userId = req.user?.id;
            if (!userId) {
                return res.status(401).json({ success: false, data: null, message: 'Vui lòng đăng nhập' });
            }

            const cart = await Cart.findOne({ userId });
            if (!cart || !cart.items || cart.items.length === 0) {
                return res.json({ success: true, data: { cart: { items: [] }, total: 0 }, message: 'Giỏ hàng trống' });
            }

            cart.items = cart.items.map(item => ({
                ...item.toObject ? item.toObject() : item,
                image: Array.isArray(item.image) ? (item.image[0] || '') : item.image
            }));

            const total = cart.items.reduce((sum, it) => sum + (Number(it.finalPrice || 0) * Number(it.quantity || 0)), 0);
            return res.json({ success: true, data: { cart, total }, message: 'Lấy giỏ hàng thành công' });
        } catch (err) {
            console.error(err);
            return res.status(500).json({ success: false, data: null, message: 'Lỗi khi xem giỏ hàng' });
        }
    }

    // [DELETE] /cart/:slug
    async removeFromCart(req, res) {
        try {
            const userId = req.user?.id;
            if (!userId) {
                return res.status(401).json({ success: false, data: null, message: 'Vui lòng đăng nhập' });
            }

            const { slug } = req.params;
            const cart = await Cart.findOne({ userId });
            if (!cart) return res.status(404).json({ success: false, data: null, message: 'Giỏ hàng không tồn tại' });

            const beforeLen = cart.items.length;
            cart.items = cart.items.filter(it => it.slug !== slug);
            if (cart.items.length === beforeLen) {
                return res.status(404).json({ success: false, data: null, message: 'Item không tồn tại' });
            }

            await cart.save();
            return res.json({ success: true, data: { cart }, message: 'Đã xóa item' });
        } catch (err) {
            console.error(err);
            return res.status(500).json({ success: false, data: null, message: 'Lỗi khi xóa item' });
        }
    }

    // [GET] /cart/count
    async cartItemCount(req, res) {
        try {
            const userId = req.user?.id;
            if (!userId) {
                return res.json({ success: true, data: { count: 0 }, message: 'Chưa đăng nhập' });
            }

            const cart = await Cart.findOne({ userId });
            const totalQuantity = cart?.items?.reduce((sum, it) => sum + (Number(it.quantity) || 0), 0) || 0;
            return res.json({ success: true, data: { count: totalQuantity }, message: 'Lấy số lượng thành công' });
        } catch (err) {
            console.error(err);
            return res.status(500).json({ success: false, data: null, message: 'Lỗi khi lấy số lượng' });
        }
    }

    // [POST] /cart/decrease/:slug
    async decreaseQuantity(req, res) {
        try {
            const userId = req.user?.id;
            if (!userId) {
                return res.status(401).json({ success: false, data: null, message: 'Vui lòng đăng nhập' });
            }

            const { slug } = req.params;
            const cart = await Cart.findOne({ userId });
            if (!cart) return res.status(404).json({ success: false, data: null, message: "Giỏ hàng không tồn tại" });

            const item = cart.items.find(i => i.slug === slug);
            if (!item) return res.status(404).json({ success: false, data: null, message: "Tour không có trong giỏ hàng" });

            if (item.quantity > 1) {
                item.quantity -= 1;
            } else {
                cart.items = cart.items.filter(i => i.slug !== slug);
            }

            await cart.save();
            res.json({ success: true, data: { cart }, message: "Cập nhật giỏ hàng thành công" });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, data: null, message: "Lỗi khi giảm số lượng" });
        }
    }

    // [POST] /cart/increase/:slug
    async increaseQuantity(req, res) {
        try {
            const userId = req.user?.id;
            if (!userId) {
                return res.status(401).json({ success: false, data: null, message: 'Vui lòng đăng nhập' });
            }

            const { slug } = req.params;
            const cart = await Cart.findOne({ userId });
            if (!cart) return res.status(404).json({ success: false, data: null, message: "Giỏ hàng không tồn tại" });

            const item = cart.items.find(i => i.slug === slug);
            if (!item) return res.status(404).json({ success: false, data: null, message: "Tour không có trong giỏ hàng" });

            const tour = await Tour.findOne({ slug });
            if (!tour) return res.status(404).json({ success: false, data: null, message: "Tour không tồn tại" });

            if (item.quantity + 1 > tour.availableSlots) {
                return res.status(400).json({ success: false, data: null, message: `Không đủ chỗ. Hiện còn ${tour.availableSlots}` });
            }

            item.quantity += 1;
            await cart.save();

            res.json({ success: true, data: { cart }, message: "Cập nhật giỏ hàng thành công" });
        } catch (err) {
            console.error(err);
            res.status(500).json({ success: false, data: null, message: "Lỗi khi tăng số lượng" });
        }
    }
}

module.exports = new CartController();
