const mongoose = require('mongoose');
const { faker } = require('@faker-js/faker');
const bcrypt = require('bcrypt');
const User = require('./app/models/User');
require('dotenv').config();
const { connect } = require('./config/db');

// Kết nối MongoDB
connect();

// Danh sách tên người Việt (100 tên khác nhau)
const vietnameseNames = [
    "Nguyễn Văn Toàn", "Trần Thị Hoa", "Phạm Minh Hải", "Lê Quang Huy", "Hoàng Thị Vân",
    "Nguyễn Thị Mai", "Trần Minh Tuấn", "Phạm Thị Lan", "Lê Minh Thảo", "Hoàng Thị Bích",
    "Nguyễn Tiến Dũng", "Trần Thi Lan", "Phạm Ngọc Mai", "Lê Thị Lan", "Hoàng Minh Thịnh",
    "Nguyễn Quang Duy", "Trần Thế Anh", "Phạm Minh Tuấn", "Lê Thị Hương", "Hoàng Thị Phương",
    "Nguyễn Minh Nhật", "Trần Nhật Hào", "Phạm Hồng Ngọc", "Lê Thái Sơn", "Hoàng Duy Anh",
    "Nguyễn Tuấn Anh", "Trần Hoàng Nam", "Phạm Thị Vân", "Lê Thanh Bình", "Hoàng Minh Tuấn",
    "Nguyễn Hoàng Hải", "Trần Quang Tùng", "Phạm Minh Tân", "Lê Thi Thanh", "Hoàng Quang Minh",
    "Nguyễn Thị Cẩm", "Trần Thị Bích", "Phạm Tuấn Anh", "Lê Hoàng Cường", "Hoàng Thi Minh",
    "Nguyễn Minh Cảnh", "Trần Minh Hảo", "Phạm Thị Hiền", "Lê Thị Thủy", "Hoàng Minh Khôi",
    "Nguyễn Đỗ Thanh", "Trần Quang Lê", "Phạm Duy Quân", "Lê Thanh Lan", "Hoàng Hải Phong",
    "Nguyễn Kim Hoàng", "Trần Hồng Vân", "Phạm Đình Bảo", "Lê Hoàng Sơn", "Hoàng Khánh Chi",
    "Nguyễn Quang Thành", "Trần Tiến Thịnh", "Phạm Tuấn Thành", "Lê Hoàng Giang", "Hoàng Minh Quân",
    "Nguyễn Đạt Minh", "Trần Thị Quỳnh", "Phạm Tiến Nam", "Lê Ngọc Quân", "Hoàng Quang Ngọc",
    "Nguyễn Duy Trí", "Trần Kim Huy", "Phạm Thi Lan", "Lê Thu Hòa", "Hoàng Minh Trí",
    "Nguyễn Thanh Hà", "Trần Minh Hoài", "Phạm Hoàng Quang", "Lê Quang Nhật", "Hoàng Hồng Sơn",
    "Nguyễn Thi Mai", "Trần Minh Thành", "Phạm Thanh Hương", "Lê Thái Bảo", "Phùng Văn Tú",
    "Võ Thị Ngọc", "Đỗ Minh Tuấn", "Bùi Thị Hằng", "Đặng Văn Long", "Dương Thị Linh",
    "Ngô Quang Vinh", "Lý Thị Thu", "Trương Văn Hùng", "Phan Thị Nga", "Vũ Minh Đức",
    "Đinh Thị Hà", "Mai Văn Tân", "Tô Thị Kim", "Hồ Quang Dũng", "Lương Thị Trang",
    "Cao Văn Phong", "Huỳnh Thị Ánh", "Tạ Minh Khoa", "Kiều Thị Huyền", "La Văn Thành"
];

// Danh sách thành phố/tỉnh Việt Nam
const vietnamCities = [
    "Tuyên Quang", "Cao Bằng", "Lai Châu", "Lào Cai", "Thái Nguyên", "Điện Biên", "Lạng Sơn",
    "Sơn La", "Phú Thọ", "Bắc Ninh", "Quảng Ninh", "Hà Nội", "Hải Phòng", "Hưng Yên", "Ninh Bình",
    "Thanh Hóa", "Nghệ An", "Hà Tĩnh", "Quảng Trị", "Huế", "Đà Nẵng", "Quảng Ngãi", "Gia Lai",
    "Đắk Lắk", "Khánh Hòa", "Lâm Đồng", "Đồng Nai", "Tây Ninh", "TP. Hồ Chí Minh", "Đồng Tháp",
    "An Giang", "Vĩnh Long", "Cần Thơ", "Cà Mau"
];

// 🔹 Hàm tạo số điện thoại ngẫu nhiên (không trùng)
const usedPhoneNumbers = new Set();
const generatePhoneNumber = () => {
    let phone;
    do {
        const prefix = ['091', '092', '093', '094', '095', '096', '097', '098', '099', '090'][Math.floor(Math.random() * 10)];
        const suffix = Math.floor(10000000 + Math.random() * 90000000); // 8 chữ số
        phone = prefix + suffix;
    } while (usedPhoneNumbers.has(phone));

    usedPhoneNumbers.add(phone);
    return phone;
};

// Hàm mã hóa mật khẩu
const hashPassword = async (password) => {
    const salt = await bcrypt.genSalt(10);
    return bcrypt.hash(password, salt);
};

// Hàm tạo user giả lập
const generateUser = async (isSuperAdmin = false) => {
    const hashedPassword = await hashPassword('123456');

    return {
        username: isSuperAdmin ? 'Phùng Văn Tú' : vietnameseNames[Math.floor(Math.random() * vietnameseNames.length)],
        email: isSuperAdmin ? 'cloneAppAdobe@gmail.com' : faker.internet.email(),
        password: hashedPassword,
        phoneNumber: isSuperAdmin ? '0123456789' : generatePhoneNumber(), // ✅ Luôn có số điện thoại
        address: vietnamCities[Math.floor(Math.random() * vietnamCities.length)] + ', Việt Nam',
        admin: isSuperAdmin,        // superadmin cũng là admin
        superadmin: isSuperAdmin,   // chỉ 1 người là true
        active: true
    };
};

// Hàm thêm dữ liệu vào MongoDB
const addUsers = async () => {
    try {
        const batchSize = 10;
        const users = [];
        const usedVietnameseNames = new Set();

        // Hàm thêm user theo nhóm
        const addBatch = async (batch) => {
            for (const user of batch) {
                await User.create(user);
            }
        };

        // 🔹 Xóa toàn bộ user cũ (tránh trùng superadmin)
        await User.deleteMany({});
        console.log('🧹 Đã xóa toàn bộ user cũ.');

        // 🔹 Thêm 1 superadmin duy nhất
        const superAdmin = await generateUser(true);
        await User.create(superAdmin);
        console.log('✅ Đã tạo superadmin:', superAdmin.username, '-', superAdmin.email);

        // 🔹 Thêm 100 người Việt Nam
        for (let i = 0; i < 100; i++) {
            let newUser;
            do {
                newUser = await generateUser(false);
            } while (usedVietnameseNames.has(newUser.username) || await User.exists({ username: newUser.username }));
            usedVietnameseNames.add(newUser.username);
            users.push(newUser);

            if (users.length >= batchSize) {
                await addBatch(users.splice(0, batchSize));
            }
        }

        // Thêm những user còn lại
        if (users.length > 0) {
            await addBatch(users);
        }

        console.log('✅ Thêm tất cả user thành công!');
        console.log(`📊 Tổng cộng: 1 superadmin + 100 Vietnamese users = 101 users`);
        process.exit(0);
    } catch (error) {
        console.error('❌ Lỗi:', error);
        process.exit(1);
    }
};

addUsers();