-- =====================================================================================
-- KỊCH BẢN DỮ LIỆU KIỂM THỬ: US-08 - ĐĂNG KÝ MỞ GIAN HÀNG KÈM GIẤY PHÉP KINH DOANH (SELLER)
-- Sàn thương mại điện tử FlexShop - Database: FlexShop_V2_Full
-- Mật khẩu chung cho tất cả các tài khoản test: Password@123
-- Mã hóa BCrypt (cost factor 12): $2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- =====================================================================================

USE FlexShop_V2_Full;
GO

PRINT N'>>> BẮT ĐẦU KHỞI TẠO DỮ LIỆU KIỂM THỬ US-08...';

-- 1. LẤY MÃ VAI TRÒ
DECLARE @MaVaiTroKhachHang BIGINT, @MaVaiTroNguoiBan BIGINT;
SELECT @MaVaiTroKhachHang = ma_vai_tro FROM vai_tro WHERE ten_vai_tro = 'KHACH_HANG' OR ten_vai_tro = 'ROLE_KHACH_HANG';
SELECT @MaVaiTroNguoiBan = ma_vai_tro FROM vai_tro WHERE ten_vai_tro = 'NGUOI_BAN' OR ten_vai_tro = 'ROLE_NGUOI_BAN';

IF @MaVaiTroKhachHang IS NULL
BEGIN
    INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES ('KHACH_HANG', N'Khách hàng mua sắm');
    SET @MaVaiTroKhachHang = SCOPE_IDENTITY();
END

IF @MaVaiTroNguoiBan IS NULL
BEGIN
    INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES ('NGUOI_BAN', N'Người bán hàng (Seller)');
    SET @MaVaiTroNguoiBan = SCOPE_IDENTITY();
END

-- 2. DỌN DẸP DỮ LIỆU CŨ NẾU ĐÃ TỒN TẠI ĐỂ CÓ THỂ CHẠY LẠI NHIỀU LẦN AN TOÀN
DELETE FROM chung_chi_gian_hang WHERE ma_gian_hang IN (
    SELECT ma_gian_hang FROM gian_hang WHERE duong_dan_slug IN ('flex-style-boutique', 'techzone-flex', 'gia-dung-thong-minh-flex', 'shop-test-moi')
);
DELETE FROM gian_hang WHERE duong_dan_slug IN ('flex-style-boutique', 'techzone-flex', 'gia-dung-thong-minh-flex', 'shop-test-moi');
DELETE FROM gio_hang WHERE ma_nguoi_dung IN (
    SELECT ma_nguoi_dung FROM nguoi_dung WHERE email IN (
        'us08_chuadk@flexshop.vn',
        'us08_choduyet@flexshop.vn',
        'us08_tuchoi@flexshop.vn',
        'us08_hoatdong@flexshop.vn'
    )
);
DELETE FROM nguoi_dung_vai_tro WHERE ma_nguoi_dung IN (
    SELECT ma_nguoi_dung FROM nguoi_dung WHERE email IN (
        'us08_chuadk@flexshop.vn',
        'us08_choduyet@flexshop.vn',
        'us08_tuchoi@flexshop.vn',
        'us08_hoatdong@flexshop.vn'
    )
);
DELETE FROM nguoi_dung WHERE email IN (
    'us08_chuadk@flexshop.vn',
    'us08_choduyet@flexshop.vn',
    'us08_tuchoi@flexshop.vn',
    'us08_hoatdong@flexshop.vn'
);

-- =====================================================================================
-- KỊCH BẢN 1: TÀI KHOẢN KHÁCH HÀNG CHƯA ĐĂNG KÝ SHOP (DÙNG ĐỂ TEST FORM ĐĂNG KÝ MỚI)
-- Email: us08_chuadk@flexshop.vn / Password@123
-- =====================================================================================
INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat)
VALUES ('us08_chuadk@flexshop.vn', '0981008001', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Trần Khách Đăng Ký Mới', 'HOAT_DONG', 0, GETDATE(), GETDATE());

DECLARE @User1_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@User1_Id, @MaVaiTroKhachHang, GETDATE());

-- =====================================================================================
-- KỊCH BẢN 2: TÀI KHOẢN ĐÃ GỬI ĐƠN VÀ ĐANG Ở TRẠNG THÁI CHỜ DUYỆT (CHO_DUYET)
-- Email: us08_choduyet@flexshop.vn / Password@123
-- Mục đích: Kiểm tra màn hình theo dõi tiến độ "Hồ sơ đang chờ Quản trị viên duyệt", xem tệp đính kèm
-- =====================================================================================
INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat)
VALUES ('us08_choduyet@flexshop.vn', '0981008002', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Lê Thị Chờ Duyệt', 'HOAT_DONG', 0, GETDATE(), GETDATE());

DECLARE @User2_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@User2_Id, @MaVaiTroKhachHang, GETDATE());

INSERT INTO gian_hang (
    ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta, dia_chi_kho, sdt_kho,
    trang_thai, hang_gian_hang, diem_sao_qua_ta, diem_danh_gia_tb, tong_danh_gia, tong_don_hang, ty_le_phan_hoi_chat, da_xoa, ngay_tao
)
VALUES (
    @User2_Id, N'Flex Style Boutique', 'flex-style-boutique',
    N'Chuyên thời trang công sở và dạo phố cao cấp phong cách Hàn Quốc.',
    N'Số 128 Cầu Giấy, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội', '0981008002',
    'CHO_DUYET', 'TIEM_NANG', 0, 0.0, 0, 0, 100.00, 0, GETDATE()
);

DECLARE @Shop2_Id BIGINT = SCOPE_IDENTITY();

INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
VALUES (@Shop2_Id, 'GIAY_PHEP_KINH_DOANH', '0109988776', '/uploads/giay-phep-kd/mau_giay_phep_kinh_doanh_01.pdf', 'CHO_DUYET', GETDATE());

-- =====================================================================================
-- KỊCH BẢN 3: TÀI KHOẢN BỊ TỪ CHỐI DUYỆT ĐƠN (TU_CHOI) CÓ LÝ DO
-- Email: us08_tuchoi@flexshop.vn / Password@123
-- Mục đích: Kiểm tra cảnh báo hiển thị lý do từ chối từ Admin và cho phép cập nhật / nộp lại hồ sơ
-- =====================================================================================
INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat)
VALUES ('us08_tuchoi@flexshop.vn', '0981008003', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Phạm Văn Bị Từ Chối', 'HOAT_DONG', 0, GETDATE(), GETDATE());

DECLARE @User3_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@User3_Id, @MaVaiTroKhachHang, GETDATE());

INSERT INTO gian_hang (
    ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta, dia_chi_kho, sdt_kho,
    trang_thai, ly_do_tu_choi, hang_gian_hang, diem_sao_qua_ta, diem_danh_gia_tb, tong_danh_gia, tong_don_hang, ty_le_phan_hoi_chat, da_xoa, ngay_tao
)
VALUES (
    @User3_Id, N'TechZone Flex', 'techzone-flex',
    N'Phụ kiện gaming, bàn phím cơ và thiết bị công nghệ.',
    N'Tòa Landmark 81, 720A Điện Biên Phủ, Phường 22, Bình Thạnh, TP. Hồ Chí Minh', '0981008003',
    'TU_CHOI', N'Ảnh giấy phép kinh doanh bị mờ, không rõ mã số thuế và thiếu dấu mộc đỏ của cơ quan chức năng.',
    'TIEM_NANG', 0, 0.0, 0, 0, 100.00, 0, GETDATE()
);

DECLARE @Shop3_Id BIGINT = SCOPE_IDENTITY();

INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
VALUES (@Shop3_Id, 'GIAY_PHEP_KINH_DOANH', '0315566778', '/uploads/giay-phep-kd/mau_giay_phep_kinh_doanh_01.pdf', 'TU_CHOI', GETDATE());

-- =====================================================================================
-- KỊCH BẢN 4: TÀI KHOẢN ĐÃ CÓ GIAN HÀNG HOẠT ĐỘNG (HOAT_DONG)
-- Email: us08_hoatdong@flexshop.vn / Password@123
-- Mục đích: Kiểm tra tự động chuyển hướng sang Kênh người bán (/seller/dashboard)
-- =====================================================================================
INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat)
VALUES ('us08_hoatdong@flexshop.vn', '0981008004', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Hoàng Thị Đã Mở Shop', 'HOAT_DONG', 0, GETDATE(), GETDATE());

DECLARE @User4_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@User4_Id, @MaVaiTroNguoiBan, GETDATE());

INSERT INTO gian_hang (
    ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta, dia_chi_kho, sdt_kho,
    trang_thai, hang_gian_hang, diem_sao_qua_ta, diem_danh_gia_tb, tong_danh_gia, tong_don_hang, ty_le_phan_hoi_chat, da_xoa, ngay_tao
)
VALUES (
    @User4_Id, N'Gia Dụng Thông Minh Flex', 'gia-dung-thong-minh-flex',
    N'Phân phối thiết bị gia dụng và đồ dùng tiện ích gia đình chính hãng.',
    N'Số 45 Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', '0981008004',
    'HOAT_DONG', 'CHUYEN_NGHIEP', 0, 5.0, 10, 25, 100.00, 0, GETDATE()
);

DECLARE @Shop4_Id BIGINT = SCOPE_IDENTITY();

INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
VALUES (@Shop4_Id, 'GIAY_PHEP_KINH_DOANH', '0319988112', '/uploads/giay-phep-kd/mau_giay_phep_kinh_doanh_01.pdf', 'DA_DUYET', GETDATE());

PRINT N'>>> HOÀN TẤT KHỞI TẠO 4 KỊCH BẢN KIỂM THỬ CHO US-08!';
GO

-- XEM LẠI KẾT QUẢ VỪA TẠO
SELECT g.ma_gian_hang, g.ten_gian_hang, g.duong_dan_slug, g.trang_thai, g.ly_do_tu_choi,
       u.email, u.ho_va_ten, c.so_giay_to, c.link_anh_giay_to, c.trang_thai_duyet
FROM gian_hang g
JOIN nguoi_dung u ON g.ma_chu_so_huu = u.ma_nguoi_dung
LEFT JOIN chung_chi_gian_hang c ON g.ma_gian_hang = c.ma_gian_hang
WHERE g.duong_dan_slug IN ('flex-style-boutique', 'techzone-flex', 'gia-dung-thong-minh-flex');
