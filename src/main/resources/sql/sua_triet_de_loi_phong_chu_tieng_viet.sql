USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BẮT ĐẦU SỬA TRIỆT ĐỂ LỖI PHÔNG CHỮ TIẾNG VIỆT TOÀN BỘ DATABASE ===';

-- 1. BẢNG vai_tro (Sửa lỗi Mojibake)
UPDATE vai_tro SET mo_ta = N'Khách hàng mua sắm trên sàn' WHERE ma_vai_tro = 1;
UPDATE vai_tro SET mo_ta = N'Người bán / Chủ gian hàng' WHERE ma_vai_tro = 2;
UPDATE vai_tro SET mo_ta = N'Nhân viên hỗ trợ & giải quyết tranh chấp CSKH' WHERE ma_vai_tro = 3;
UPDATE vai_tro SET mo_ta = N'Quản trị viên sàn FlexShop' WHERE ma_vai_tro = 4;
UPDATE vai_tro SET mo_ta = N'Tài xế giao vận chuyển phát nhanh' WHERE ma_vai_tro = 5;
PRINT N'1. Đã chuẩn hóa phông chữ bảng vai_tro';

-- 2. BẢNG nguoi_dung (Sửa dấu hỏi chấm trong họ tên)
UPDATE nguoi_dung SET ho_va_ten = N'Nguyễn Dân' WHERE ma_nguoi_dung = 1;
UPDATE nguoi_dung SET ho_va_ten = N'Trần Minh Đức (TechZone)' WHERE ma_nguoi_dung = 2;
UPDATE nguoi_dung SET ho_va_ten = N'Lê Thu Hà (Flex Fashion)' WHERE ma_nguoi_dung = 3;
UPDATE nguoi_dung SET ho_va_ten = N'Nguyễn Văn An' WHERE ma_nguoi_dung = 4;
UPDATE nguoi_dung SET ho_va_ten = N'Hoàng Thùy Linh' WHERE ma_nguoi_dung = 5;
UPDATE nguoi_dung SET ho_va_ten = N'Ngô Thị CSKH Hỗ Trợ' WHERE ma_nguoi_dung = 6;
UPDATE nguoi_dung SET ho_va_ten = N'Ban Quản Trị FlexShop' WHERE ma_nguoi_dung = 7;
UPDATE nguoi_dung SET ho_va_ten = N'Phạm Văn Giao Vận (Shipper Nhanh)' WHERE ma_nguoi_dung = 8;
PRINT N'2. Đã chuẩn hóa họ và tên bảng nguoi_dung';

-- 3. BẢNG tai_xe_giao_hang
UPDATE tai_xe_giao_hang SET loai_phuong_tien = N'Xe máy Honda Wave Alpha' WHERE ma_tai_xe = 1;
PRINT N'3. Đã chuẩn hóa phương tiện bảng tai_xe_giao_hang';

-- 4. BẢNG gian_hang
UPDATE gian_hang SET 
    ten_gian_hang = N'TechZone Flagship Store',
    mo_ta = N'Gian hàng chính hãng phân phối thiết bị công nghệ hàng đầu Việt Nam',
    dia_chi_kho = N'Kho Tổng Flex Logistics, Cụm CN vừa và nhỏ Từ Liêm, Hà Nội'
WHERE ma_gian_hang = 1;

UPDATE gian_hang SET 
    ten_gian_hang = N'Flex Fashion Official',
    mo_ta = N'Thương hiệu thời trang công sở và dạo phố cao cấp thiết kế hiện đại',
    dia_chi_kho = N'Kho Tổng Flex TP.HCM, Quận Tân Bình, TP. Hồ Chí Minh'
WHERE ma_gian_hang = 2;
PRINT N'4. Đã chuẩn hóa bảng gian_hang';

-- 5. BẢNG dia_chi_nguoi_dung
UPDATE dia_chi_nguoi_dung SET 
    ten_nguoi_nhan = N'Nguyễn Dân',
    dia_chi_chi_tiet = N'Số 123 Đường Cầu Giấy, Tòa nhà FPT',
    xa_phuong = N'Phường Dịch Vọng',
    quan_huyen = N'Quận Cầu Giấy',
    tinh_thanh = N'Hà Nội'
WHERE ma_dia_chi = 1;

UPDATE dia_chi_nguoi_dung SET 
    ten_nguoi_nhan = N'Nguyễn Văn An',
    dia_chi_chi_tiet = N'Số 123 Đường Cầu Giấy, Tòa nhà FPT',
    xa_phuong = N'Phường Dịch Vọng',
    quan_huyen = N'Quận Cầu Giấy',
    tinh_thanh = N'Hà Nội'
WHERE ma_dia_chi = 2;

-- Cập nhật tất cả các địa chỉ khác có dấu ?
UPDATE dia_chi_nguoi_dung SET 
    xa_phuong = N'Phường Dịch Vọng',
    quan_huyen = N'Quận Cầu Giấy',
    tinh_thanh = N'Hà Nội'
WHERE tinh_thanh LIKE N'%?%' OR tinh_thanh LIKE N'%H%N%';
PRINT N'5. Đã chuẩn hóa bảng dia_chi_nguoi_dung';

-- 6. BẢNG san_pham & bien_the_san_pham
UPDATE san_pham SET 
    ten_san_pham = N'Tai nghe không dây Bluetooth chống ồn Sony WH-1000XM5',
    mo_ta_ngan = N'Tai nghe chống ồn đỉnh cao, thời lượng pin 30 giờ, đàm thoại sắc nét',
    mo_ta_chi_tiet = N'Tai nghe chụp tai Sony WH-1000XM5 với công nghệ chống ồn tự động tối ưu, âm thanh độ phân giải cao High-Resolution Audio.'
WHERE ma_san_pham = 1;

UPDATE san_pham SET 
    ten_san_pham = N'Áo Sơ Mi Nam Oxford Form Slimfit Không Nhăn',
    mo_ta_ngan = N'Chất liệu cotton cao cấp, thoáng mát, chống nhăn tự nhiên',
    mo_ta_chi_tiet = N'Áo sơ mi nam công sở cao cấp may từ vải Oxford dệt mật độ cao, thấm hút mồ hôi tốt.'
WHERE ma_san_pham = 2;

UPDATE bien_the_san_pham SET ten_bien_the = N'Màu Đen Nhám - Bluetooth 5.3' WHERE ma_bien_the = 1;
UPDATE bien_the_san_pham SET ten_bien_the = N'Màu Xanh Pastel - Size L' WHERE ma_bien_the = 2;
PRINT N'6. Đã chuẩn hóa bảng san_pham và bien_the_san_pham';

-- 7. BẢNG chi_tiet_don_hang
UPDATE chi_tiet_don_hang SET 
    ten_san_pham = N'Tai nghe Bluetooth Sony WH-1000XM5 Chống Ồn Cao Cấp',
    ten_bien_the = N'Màu Đen Nhám - Bluetooth 5.3'
WHERE ma_don_hang_shop = 1;

UPDATE chi_tiet_don_hang SET 
    ten_san_pham = N'Áo Sơ Mi Nam Oxford Form Slimfit Không Nhăn',
    ten_bien_the = N'Màu Xanh Pastel - Size L'
WHERE ma_don_hang_shop = 2;

UPDATE chi_tiet_don_hang SET 
    ten_san_pham = N'Bàn Phím Cơ Không Dây RGB Cao Cấp',
    ten_bien_the = N'Bản LED RGB / Red Switch'
WHERE ma_don_hang_shop = 20;

UPDATE chi_tiet_don_hang SET 
    ten_san_pham = N'Áo Polo Nam Thể Thao Co Giãn Thoáng Khí Cao Cấp',
    ten_bien_the = N'Màu Trắng Phối Sọc - Size L'
WHERE ma_don_hang_shop = 62;
PRINT N'7. Đã chuẩn hóa bảng chi_tiet_don_hang';

-- 8. BẢNG phieu_khieu_nai
UPDATE phieu_khieu_nai SET 
    noi_dung_mo_ta = N'Kiện hàng giao đến bị móp méo hộp nặng, tai nghe bên trong bị nứt khớp nối gọng bên phải và không nghe được một bên tai. Yêu cầu đổi mới hoặc hoàn tiền ngay.',
    ghi_chu_phan_quyet = N'CSKH đã đối chiếu 3 bên: Đối chiếu ảnh POD shipper thấy vỏ hộp đã móp trước khi giao. Quyết định hoàn tiền cho khách, bên chịu phí: Người bán (do không đóng gói xốp chống sốc đúng quy định).'
WHERE ma_code_phieu = N'KN-260901-1001';

UPDATE phieu_khieu_nai SET 
    noi_dung_mo_ta = N'Tôi đặt 2 áo sơ mi màu xanh pastel size L nhưng shop đóng gói giao nhầm thành màu đen size M. Áo còn nguyên tem mác chưa sử dụng.',
    ghi_chu_phan_quyet = N'Chấp thuận khiếu nại giao sai phân loại. Thu hồi hàng về shop và hoàn tiền cho người mua.'
WHERE ma_code_phieu = N'KN-260902-1002';

UPDATE phieu_khieu_nai SET 
    noi_dung_mo_ta = N'Sản phẩm không có tem kiểm định chính hãng như cam kết của gian hàng Mall, nghi vấn hàng kém chất lượng.',
    ghi_chu_phan_quyet = N'Đã chuyển hồ sơ cho phòng thẩm định xuất xứ sản phẩm. Tạm giữ thanh toán đơn hàng để kiểm tra chứng từ.'
WHERE ma_code_phieu = N'KN-260903-1003';

UPDATE phieu_khieu_nai SET 
    noi_dung_mo_ta = N'Tôi muốn trả hàng vì đổi ý không thích màu này nữa sau khi đã giặt và cắt tag gắn trên áo.',
    ghi_chu_phan_quyet = N'Bác bỏ khiếu nại: Khách hàng đã cắt tem mác và giặt sản phẩm, không đủ điều kiện trả hàng theo chính sách sàn.'
WHERE ma_code_phieu = N'KN-260904-1004';

UPDATE phieu_khieu_nai SET 
    noi_dung_mo_ta = N'Bàn phím cơ bị móp gãy khung nhôm bên trong, hộp ngoài có vết chèn ép rách thủng do va đập vận chuyển.',
    ghi_chu_phan_quyet = N'Đã xác minh qua camera đóng gói của shop: lúc xuất kho nguyên vẹn. Hư hại do đơn vị vận chuyển làm rơi trong lúc phân loại tại Hub trung chuyển. Duyệt bồi thường cho Shop 100% giá trị đơn hàng.'
WHERE ma_code_phieu = N'KN-260905-1005';

UPDATE phieu_khieu_nai SET 
    noi_dung_mo_ta = N'Tôi đặt áo size L màu đen nhưng shop giao size S màu trắng. Tôi có đính kèm ảnh chụp tem mác làm bằng chứng đối chiếu.',
    ghi_chu_phan_quyet = N'Đang thụ lý đối chiếu bằng chứng mở hộp của khách và đơn xuất kho của shop.'
WHERE ma_code_phieu = N'KN-260905-6924';
PRINT N'8. Đã chuẩn hóa nội dung bảng phieu_khieu_nai';

-- 9. BẢNG ghi_chu_noi_bo_khieu_nai
UPDATE ghi_chu_noi_bo_khieu_nai SET 
    noi_dung = N'[Đối chiếu Shipper]: Đã liên hệ trực tiếp với tài xế Phạm Văn Giao Vận. Tài xế cho biết lúc nhận hàng từ kho vỏ hộp đã có dấu hiệu bị chèn ép móp góc, tuy nhiên vẫn nhận đi giao vì tưởng đóng gói chống sốc bên trong còn tốt.'
WHERE noi_dung LIKE N'%Shipper%' AND (noi_dung LIKE N'%mp%' OR noi_dung LIKE N'%?%');

UPDATE ghi_chu_noi_bo_khieu_nai SET 
    noi_dung = N'[Đối chiếu Shop TechZone]: Yêu cầu Shop gửi video đóng gói tại bàn kiểm tra. Shop đã phản hồi qua email xác nhận hộp xuất kho còn nguyên vẹn, nghi vấn xảy ra va đập mạnh trong quá trình phân loại ở Hub trung chuyển.'
WHERE noi_dung LIKE N'%TechZone%' AND (noi_dung LIKE N'%xu?t kho%' OR noi_dung LIKE N'%?%');
PRINT N'9. Đã chuẩn hóa bảng ghi_chu_noi_bo_khieu_nai';

-- 10. BẢNG lich_su_trang_thai_don
UPDATE lich_su_trang_thai_don SET 
    nguoi_thuc_hien = N'Hệ thống tự động',
    ghi_chu = N'Khách hàng đặt hàng thành công qua cổng thanh toán VNPay'
WHERE ghi_chu LIKE N'%VNPay%' OR ghi_chu LIKE N'%thanh to%' OR ghi_chu LIKE N'%?%';

UPDATE lich_su_trang_thai_don SET 
    nguoi_thuc_hien = N'Chủ Shop (TechZone)',
    ghi_chu = N'Gian hàng đã xác nhận đơn và in phiếu đóng gói'
WHERE ghi_chu LIKE N'%xác nhận%' OR ghi_chu LIKE N'%x?c nh?n%' OR ghi_chu LIKE N'%đóng gói%';

UPDATE lich_su_trang_thai_don SET 
    nguoi_thuc_hien = N'Shipper: Phạm Văn Giao Vận',
    ghi_chu = N'Đã lấy hàng tại Kho Tổng Từ Liêm, đang vận chuyển đến khách'
WHERE ghi_chu LIKE N'%Kho T?ng%' OR ghi_chu LIKE N'%v?n chuy?n%' OR ghi_chu LIKE N'%Kho Tổng%';

UPDATE lich_su_trang_thai_don SET 
    nguoi_thuc_hien = N'Shipper: Phạm Văn Giao Vận',
    ghi_chu = N'Giao hàng thành công tận tay người nhận, đã chụp ảnh POD xác thực'
WHERE ghi_chu LIKE N'%Giao%' AND (ghi_chu LIKE N'%POD%' OR ghi_chu LIKE N'%thnh cng%' OR ghi_chu LIKE N'%thành công%');

UPDATE lich_su_trang_thai_don SET 
    nguoi_thuc_hien = N'Khách: Nguyễn Văn An',
    ghi_chu = N'Khách mở phiếu khiếu nại KN-260901-1001: Tai nghe bị hỏng vỡ gọng khi nhận'
WHERE ghi_chu LIKE N'%KN-260901-1001%';

UPDATE lich_su_trang_thai_don SET 
    nguoi_thuc_hien = N'CSKH: Ngô Thị CSKH Hỗ Trợ',
    ghi_chu = N'Bắt đầu quy trình đối chiếu 3 bên (Khách - Shop - Shipper POD)'
WHERE ghi_chu LIKE N'%đối chiếu 3 bên%' OR ghi_chu LIKE N'%d?i chi?u 3 b?n%';
PRINT N'10. Đã chuẩn hóa bảng lich_su_trang_thai_don';

PRINT N'=== HOÀN TẤT 100% SỬA LỖI PHÔNG CHỮ TIẾNG VIỆT TOÀN DIỆN DATABASE! ===';
GO
