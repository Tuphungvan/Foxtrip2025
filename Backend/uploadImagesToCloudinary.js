const cloudinary = require('./config/cloudinary');

async function deleteOldImages(tourName) {
  const prefix = `tours/${tourName.replace(/\s+/g, '_')}`;

  try {
    const result = await cloudinary.api.delete_resources_by_prefix(prefix);
    console.log(`🗑️ Đã xóa ảnh cũ của tour "${tourName}":`, result.deleted);
  } catch (err) {
    console.error(`❌ Lỗi khi xóa ảnh cũ của tour "${tourName}":`, err.message);
  }
}

async function uploadImagesToCloudinary(imageUrls, tourName) {

    await deleteOldImages(tourName);

  const uploadedUrls = [];

  for (const url of imageUrls) {
    try {
      const result = await cloudinary.uploader.upload(url, {
        folder: 'tours',
        public_id: `${tourName.replace(/\s+/g, '_')}_${Date.now()}`,
      });
      uploadedUrls.push(result.secure_url);
    } catch (err) {
      console.error(`❌ Lỗi upload ảnh ${url}:`, err.message);
    }
  }

  return uploadedUrls;
}

module.exports = uploadImagesToCloudinary;
