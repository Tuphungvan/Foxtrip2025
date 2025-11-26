const mongoose = require('mongoose');
const slugify = require('slugify');
const mongooseDelete = require('mongoose-delete');

const Schema = mongoose.Schema;

// Khai báo schema cho Tour
const Tour = new Schema(
    {
        name: { type: String, required: true },
        description: { type: String, required: true },
        province: { type: String, required: true },
        region: { type: String, required: true }, // Bắc, Trung, Nam
        category: { type: String, required: true }, // Biển, văn hóa, nghỉ dưỡng...
        image: {
            type: [String],
            required: true,
            validate: [arr => arr.length > 0, 'Tour phải có ít nhất 1 ảnh']
        },
        videoId: { type: String, required: true },
        shortUrl: { type: String },
        startDate: { type: Date, required: true },
        endDate: { type: Date, required: true },
        itinerary: { type: String, required: true },
        price: { type: Number, required: true },
        slug: { type: String, unique: true },
        slots: { type: Number, required: true },
        availableSlots: { type: Number, required: true },
        isBookable: { type: Boolean, default: true },
        discount: { type: Number, default: 0 },
    },
    {
        timestamps: true,
    }
);

// Tạo slug trước khi lưu
Tour.pre('save', function (next) {
    if (this.name) {
        this.slug = slugify(this.name, { lower: true, strict: true });
    }
    next();
});

// Soft delete
Tour.plugin(mongooseDelete, {
    deletedAt: true,
    overrideMethods: 'all',
});

module.exports = mongoose.model('Tour', Tour);
