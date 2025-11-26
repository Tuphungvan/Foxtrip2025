const multer = require('multer');
const { CloudinaryStorage } = require('multer-storage-cloudinary');
const cloudinary = require('../config/cloudinary');

// Cấu hình storage cho multer
const storage = new CloudinaryStorage({
    cloudinary: cloudinary,
    params: async (req, file) => {
        const tourName = req.body.tourName || 'unknown';
        return {
            folder: `tours/${tourName.replace(/\s+/g, '_')}`,
            allowed_formats: ['jpg', 'png', 'jpeg', 'webp'],
            public_id: `${tourName.replace(/\s+/g, '_')}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
        };
    },
});

const upload = multer({ 
    storage: storage,
    limits: {
        fileSize: 25 * 1024 * 1024 // 25MB
    }
});

/**
 * Xóa một ảnh từ Cloudinary
 */
async function deleteImageFromCloudinary(imageUrl) {
    try {
        // Trích xuất public_id từ URL
        const parts = imageUrl.split('/');
        const filename = parts[parts.length - 1].split('.')[0];
        const folder = parts.slice(-3, -1).join('/');
        const publicId = `${folder}/${filename}`;

        const result = await cloudinary.uploader.destroy(publicId);
        console.log(`🗑️ Đã xóa ảnh: ${publicId}`, result);
        return result;
    } catch (err) {
        console.error(`❌ Lỗi xóa ảnh từ Cloudinary:`, err.message);
        throw err;
    }
}

/**
 * Xóa tất cả ảnh của một tour
 */
async function deleteAllTourImages(tourName) {
    const prefix = `tours/${tourName.replace(/\s+/g, '_')}`;
    try {
        const result = await cloudinary.api.delete_resources_by_prefix(prefix);
        console.log(`🗑️ Đã xóa tất cả ảnh của tour "${tourName}":`, result.deleted);
        return result;
    } catch (err) {
        console.error(`❌ Lỗi xóa ảnh tour "${tourName}":`, err.message);
        throw err;
    }
}

/**
 * Xóa thư mục tour trên Cloudinary
 */
async function deleteTourFolder(tourName) {
    const folder = `tours/${tourName.replace(/\s+/g, '_')}`;
    try {
        await cloudinary.api.delete_folder(folder);
        console.log(`🗑️ Đã xóa thư mục: ${folder}`);
    } catch (err) {
        console.error(`❌ Lỗi xóa thư mục "${folder}":`, err.message);
    }
}

module.exports = {
    upload,
    deleteImageFromCloudinary,
    deleteAllTourImages,
    deleteTourFolder
};