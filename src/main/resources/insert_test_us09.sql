-- =====================================================================================
-- KỊCH BẢN DỮ LIỆU KIỂM THỬ: US-09 - KIỂM DUYỆT GIAN HÀNG SHOP (ADMIN APPROVE/REJECT)
-- Sàn thương mại điện tử FlexShop - Database: FlexShop_V2_Full
-- Mật khẩu chung cho tất cả các tài khoản test: Password@123
-- Mã hóa BCrypt (cost factor 12): $2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- =====================================================================================

USE FlexShop_V2_Full;
GO

PRINT N'>>> BẮT ĐẦU KHỞI TẠO DỮ LIỆU KIỂM THỬ US-09 (KIỂM DUYỆT SHOP)...';

-- 1. LẤY MÃ VAI TRÒ HỆ THỐNG
DECLARE @MaVaiTroAdmin INT, @MaVaiTroKhachHang INT, @MaVaiTroNguoiBan INT;
SELECT @MaVaiTroAdmin = ma_vai_tro FROM vai_tro WHERE ten_vai_tro = 'ADMIN' OR ten_vai_tro = 'ROLE_ADMIN';
SELECT @MaVaiTroKhachHang = ma_vai_tro FROM vai_tro WHERE ten_vai_tro = 'KHACH_HANG' OR ten_vai_tro = 'ROLE_KHACH_HANG';
SELECT @MaVaiTroNguoiBan = ma_vai_tro FROM vai_tro WHERE ten_vai_tro = 'NGUOI_BAN' OR ten_vai_tro = 'ROLE_NGUOI_BAN';

IF @MaVaiTroAdmin IS NULL
BEGIN
    INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES ('ADMIN', N'Quản trị viên toàn quyền hệ thống');
    SET @MaVaiTroAdmin = SCOPE_IDENTITY();
END

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

-- 2. DỌN DẸP DỮ LIỆU TEST CŨ CỦA US-09 ĐỂ CÓ THỂ CHẠY LẠI NHIỀU LẦN AN TOÀN
DELETE FROM chung_chi_gian_hang WHERE ma_gian_hang IN (
    SELECT ma_gian_hang FROM gian_hang WHERE duong_dan_slug IN (
        'thoi-trang-genz-us09',
        'my-pham-auth-us09',
        'gia-dung-us09',
        'linh-kien-nhai-us09'
    )
);
DELETE FROM gian_hang WHERE duong_dan_slug IN (
    'thoi-trang-genz-us09',
    'my-pham-auth-us09',
    'gia-dung-us09',
    'linh-kien-nhai-us09'
);
DELETE FROM gio_hang WHERE ma_nguoi_dung IN (
    SELECT ma_nguoi_dung FROM nguoi_dung WHERE email IN (
        'admin_us09@flexshop.vn',
        'us09_choduyet1@flexshop.vn',
        'us09_choduyet2@flexshop.vn',
        'us09_hoatdong@flexshop.vn',
        'us09_tuchoi@flexshop.vn'
    )
);
DELETE FROM nguoi_dung_vai_tro WHERE ma_nguoi_dung IN (
    SELECT ma_nguoi_dung FROM nguoi_dung WHERE email IN (
        'admin_us09@flexshop.vn',
        'us09_choduyet1@flexshop.vn',
        'us09_choduyet2@flexshop.vn',
        'us09_hoatdong@flexshop.vn',
        'us09_tuchoi@flexshop.vn'
    )
);
DELETE FROM nguoi_dung WHERE email IN (
    'admin_us09@flexshop.vn',
    'us09_choduyet1@flexshop.vn',
    'us09_choduyet2@flexshop.vn',
    'us09_hoatdong@flexshop.vn',
    'us09_tuchoi@flexshop.vn'
);

-- =====================================================================================
-- KỊCH BẢN 1: TÀI KHOẢN ADMIN DÙNG ĐỂ ĐĂNG NHẬP VÀ THỰC HIỆN KIỂM DUYỆT
-- Email: admin_us09@flexshop.vn / Password@123
-- =====================================================================================
INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat)
VALUES ('admin_us09@flexshop.vn', '0909009000', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Quản Trị Viên Kiểm Duyệt US09', 'HOAT_DONG', 0, GETDATE(), GETDATE());

DECLARE @Admin_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@Admin_Id, @MaVaiTroAdmin, GETDATE());

-- =====================================================================================
-- KỊCH BẢN 2: GIAN HÀNG 1 - CHỜ DUYỆT (SẴN SÀNG ĐỂ ADMIN TEST BẤM "DUYỆT" -> CẤP ROLE NGUOI_BAN)
-- Chủ shop: us09_choduyet1@flexshop.vn / Password@123 (Hiện chỉ có role KHACH_HANG)
-- Shop: Shop Thời Trang GenZ (thoi-trang-genz-us09) -> CHO_DUYET
-- =====================================================================================
INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat)
VALUES ('us09_choduyet1@flexshop.vn', '0909009001', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Lê Chờ Duyệt Một', 'HOAT_DONG', 0, GETDATE(), GETDATE());

DECLARE @User1_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@User1_Id, @MaVaiTroKhachHang, GETDATE());

INSERT INTO gian_hang (
    ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta, dia_chi_kho, sdt_kho,
    link_logo, trang_thai, hang_gian_hang, diem_sao_qua_ta, diem_danh_gia_tb, tong_danh_gia, tong_don_hang, ty_le_phan_hoi_chat, da_xoa, ngay_tao
) VALUES (
    @User1_Id, N'Shop Thời Trang GenZ', 'thoi-trang-genz-us09',
    N'Chuyên cung cấp quần áo phong cách trẻ trung hiện đại GenZ.',
    N'Số 123 Đường Cầu Giấy, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội', '0909009001',
    'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=200', 'CHO_DUYET', 'TIEM_NANG', 0, 0.0, 0, 0, 100.00, 0, GETDATE()
);

DECLARE @Shop1_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
VALUES (@Shop1_Id, 'GIAY_PHEP_KINH_DOANH', '0109988111', '/uploads/giay-phep-kd/mau_giay_phep_kinh_doanh_01.pdf', 'CHO_DUYET', GETDATE());

-- =====================================================================================
-- KỊCH BẢN 3: GIAN HÀNG 2 - CHỜ DUYỆT (SẴN SÀNG ĐỂ ADMIN TEST BẤM "TỪ CHỐI" + NHẬP LÝ DO)
-- Chủ shop: us09_choduyet2@flexshop.vn / Password@123
-- Shop: Mỹ Phẩm Auth Flex (my-pham-auth-us09) -> CHO_DUYET
-- =====================================================================================
INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat)
VALUES ('us09_choduyet2@flexshop.vn', '0909009002', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Phạm Chờ Duyệt Hai', 'HOAT_DONG', 0, GETDATE(), GETDATE());

DECLARE @User2_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@User2_Id, @MaVaiTroKhachHang, GETDATE());

INSERT INTO gian_hang (
    ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta, dia_chi_kho, sdt_kho,
    link_logo, trang_thai, hang_gian_hang, diem_sao_qua_ta, diem_danh_gia_tb, tong_danh_gia, tong_don_hang, ty_le_phan_hoi_chat, da_xoa, ngay_tao
) VALUES (
    @User2_Id, N'Mỹ Phẩm Auth Flex', 'my-pham-auth-us09',
    N'Hàng xách tay chính hãng từ Nhật Bản và Hàn Quốc.',
    N'Số 88 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP Hồ Chí Minh', '0909009002',
    'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=200', 'CHO_DUYET', 'TIEM_NANG', 0, 0.0, 0, 0, 100.00, 0, GETDATE()
);

DECLARE @Shop2_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
VALUES (@Shop2_Id, 'GIAY_PHEP_KINH_DOANH', '0309988222', '/uploads/giay-phep-kd/mau_giay_phep_kinh_doanh_01.pdf', 'CHO_DUYET', GETDATE());

-- =====================================================================================
-- KỊCH BẢN 4: GIAN HÀNG ĐÃ ĐƯỢC DUYỆT (HOẠT ĐỘNG - HOAT_DONG)
-- Chủ shop: us09_hoatdong@flexshop.vn / Password@123 (Đã có cả role KHACH_HANG & NGUOI_BAN)
-- Shop: Gia Dụng Thông Minh US09 (gia-dung-us09) -> HOAT_DONG
-- =====================================================================================
INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat)
VALUES ('us09_hoatdong@flexshop.vn', '0909009003', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Đỗ Người Bán Flex', 'HOAT_DONG', 0, GETDATE(), GETDATE());

DECLARE @User3_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@User3_Id, @MaVaiTroKhachHang, GETDATE());
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@User3_Id, @MaVaiTroNguoiBan, GETDATE());

INSERT INTO gian_hang (
    ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta, dia_chi_kho, sdt_kho,
    link_logo, trang_thai, hang_gian_hang, diem_sao_qua_ta, diem_danh_gia_tb, tong_danh_gia, tong_don_hang, ty_le_phan_hoi_chat, da_xoa, ngay_tao
) VALUES (
    @User3_Id, N'Gia Dụng Thông Minh US09', 'gia-dung-us09',
    N'Thiết bị gia dụng cao cấp, robot hút bụi và đồ gia đình.',
    N'Số 45 Đường Nguyễn Văn Linh, Phường Nam Dương, Quận Hải Châu, Đà Nẵng', '0909009003',
    'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=200', 'HOAT_DONG', 'TIEM_NANG', 0, 0.0, 0, 0, 100.00, 0, GETDATE()
);

DECLARE @Shop3_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
VALUES (@Shop3_Id, 'GIAY_PHEP_KINH_DOANH', '0108877333', '/uploads/giay-phep-kd/mau_giay_phep_kinh_doanh_01.pdf', 'DA_DUYET', GETDATE());

-- =====================================================================================
-- KỊCH BẢN 5: GIAN HÀNG ĐÃ BỊ TỪ CHỐI (TU_CHOI KÈM LÝ DO)
-- Chủ shop: us09_tuchoi@flexshop.vn / Password@123 (Chỉ có role KHACH_HANG)
-- Shop: Linh Kiện Điện Tử Nhái (linh-kien-nhai-us09) -> TU_CHOI
-- =====================================================================================
INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat)
VALUES ('us09_tuchoi@flexshop.vn', '0909009004', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Hoàng Bị Từ Chối', 'HOAT_DONG', 0, GETDATE(), GETDATE());

DECLARE @User4_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@User4_Id, @MaVaiTroKhachHang, GETDATE());

INSERT INTO gian_hang (
    ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta, dia_chi_kho, sdt_kho,
    link_logo, trang_thai, ly_do_tu_choi, hang_gian_hang, diem_sao_qua_ta, diem_danh_gia_tb, tong_danh_gia, tong_don_hang, ty_le_phan_hoi_chat, da_xoa, ngay_tao
) VALUES (
    @User4_Id, N'Linh Kiện Điện Tử Nhái', 'linh-kien-nhai-us09',
    N'Chuyên cung cấp linh kiện giá rẻ không nguồn gốc.',
    N'Số 99 Hẻm Chợ Trời, Phường Đồng Xuân, Quận Hoàn Kiếm, Hà Nội', '0909009004',
    'https://images.unsplash.com/photo-1518770660439-4636190af475?w=200',
    'TU_CHOI', N'Giấy phép kinh doanh có dấu hiệu bị tẩy xóa, mã số thuế không tồn tại trên hệ thống Tổng cục Thuế.', 'TIEM_NANG', 0, 0.0, 0, 0, 100.00, 0, GETDATE()
);

DECLARE @Shop4_Id BIGINT = SCOPE_IDENTITY();
INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
VALUES (@Shop4_Id, 'GIAY_PHEP_KINH_DOANH', '0107766444', '/uploads/giay-phep-kd/mau_giay_phep_kinh_doanh_01.pdf', 'TU_CHOI', GETDATE());

PRINT N'>>> HOÀN TẤT KHỞI TẠO DỮ LIỆU KIỂM THỬ US-09!';

-- 3. HIỂN THỊ DANH SÁCH TÀI KHOẢN VÀ GIAN HÀNG TEST ĐÃ TẠO
SELECT 
    nd.ma_nguoi_dung AS [MaUser],
    nd.ho_va_ten AS [HoVaTen],
    nd.email AS [Email],
    'Password@123' AS [MatKhauTest],
    vt.ten_vai_tro AS [VaiTro],
    gh.ma_gian_hang AS [MaShop],
    gh.ten_gian_hang AS [TenGianHang],
    gh.duong_dan_slug AS [Slug],
    gh.trang_thai AS [TrangThaiShop],
    gh.ly_do_tu_choi AS [LyDoTuChoi]
FROM nguoi_dung nd
LEFT JOIN nguoi_dung_vai_tro ndvt ON nd.ma_nguoi_dung = ndvt.ma_nguoi_dung
LEFT JOIN vai_tro vt ON ndvt.ma_vai_tro = vt.ma_vai_tro
LEFT JOIN gian_hang gh ON gh.ma_chu_so_huu = nd.ma_nguoi_dung
WHERE nd.email IN (
    'admin_us09@flexshop.vn',
    'us09_choduyet1@flexshop.vn',
    'us09_choduyet2@flexshop.vn',
    'us09_hoatdong@flexshop.vn',
    'us09_tuchoi@flexshop.vn'
)
ORDER BY nd.ma_nguoi_dung ASC;
GO
