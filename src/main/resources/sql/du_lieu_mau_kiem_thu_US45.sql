-- ============================================================================
-- KỊCH BẢN DỮ LIỆU MẪU KIỂM THỬ US-45: KHIẾU NẠI / ĐỔI TRẢ / HOÀN TIỀN
-- Database: FlexShop_V2_Full
-- Ngày tạo: 2026-09-04
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BẮT ĐẦU ĐỒNG BỘ DỮ LIỆU MẪU CHO US-45 (CSKH) ===';

-- 1. DANH SÁCH VAI TRÒ
IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = N'KHACH_HANG')
    INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES (N'KHACH_HANG', N'Khách hàng mua sắm trên sàn');
IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = N'NGUOI_BAN')
    INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES (N'NGUOI_BAN', N'Người bán / Chủ gian hàng');
IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = N'CSKH')
    INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES (N'CSKH', N'Nhân viên hỗ trợ & giải quyết tranh chấp');
IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = N'ADMIN')
    INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES (N'ADMIN', N'Quản trị viên sàn FlexShop');
IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = N'SHIPPER')
    INSERT INTO vai_tro (ten_vai_tro, mo_ta) VALUES (N'SHIPPER', N'Tài xế giao vận chuyển phát');
GO

-- 2. NGƯỜI DÙNG MẪU
-- Khách hàng 1: Nguyễn Văn An (khachhang@flexshop.vn)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (N'khachhang@flexshop.vn', N'0901234567', N'$2a$10$7EqJtq98hPqEX7fNZaFWoO3jS8e1s89P1dJ9q6V8J3v8v8k9v7v9m', N'Nguyễn Văn An', N'HOAT_DONG', 0);
END

-- Khách hàng 2: Hoàng Thùy Linh (thuylinh@gmail.com)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'thuylinh@gmail.com')
BEGIN
    INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (N'thuylinh@gmail.com', N'0912334455', N'$2a$10$7EqJtq98hPqEX7fNZaFWoO3jS8e1s89P1dJ9q6V8J3v8v8k9v7v9m', N'Hoàng Thùy Linh', N'HOAT_DONG', 0);
END

-- Chủ Shop 1: Trần Minh Đức (techzone@flexshop.vn)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'techzone@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (N'techzone@flexshop.vn', N'0988889999', N'$2a$10$7EqJtq98hPqEX7fNZaFWoO3jS8e1s89P1dJ9q6V8J3v8v8k9v7v9m', N'Trần Minh Đức (TechZone)', N'HOAT_DONG', 0);
END

-- Chủ Shop 2: Lê Thu Hà (flexfashion@flexshop.vn)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'flexfashion@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (N'flexfashion@flexshop.vn', N'0977112233', N'$2a$10$7EqJtq98hPqEX7fNZaFWoO3jS8e1s89P1dJ9q6V8J3v8v8k9v7v9m', N'Lê Thu Hà (Flex Fashion)', N'HOAT_DONG', 0);
END

-- Nhân viên CSKH: Lê CSKH Hỗ Trợ (cskh@flexshop.vn)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'cskh@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (N'cskh@flexshop.vn', N'0933445566', N'$2a$10$7EqJtq98hPqEX7fNZaFWoO3jS8e1s89P1dJ9q6V8J3v8v8k9v7v9m', N'Ngô Thị CSKH Hỗ Trợ', N'HOAT_DONG', 0);
END

-- Admin: Ban Quản Trị FlexShop (admin@flexshop.vn)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'admin@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (N'admin@flexshop.vn', N'0999999999', N'$2a$10$7EqJtq98hPqEX7fNZaFWoO3jS8e1s89P1dJ9q6V8J3v8v8k9v7v9m', N'Ban Quản Trị FlexShop', N'HOAT_DONG', 0);
END
GO

-- 3. ĐỊA CHỈ KHÁCH HÀNG
DECLARE @MaKhachHang1 BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');
IF @MaKhachHang1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dia_chi_nguoi_dung WHERE ma_nguoi_dung = @MaKhachHang1)
BEGIN
    INSERT INTO dia_chi_nguoi_dung (ma_nguoi_dung, ten_nguoi_nhan, so_dien_thoai, tinh_thanh, quan_huyen, xa_phuong, dia_chi_chi_tiet, la_mac_dinh, da_xoa)
    VALUES (@MaKhachHang1, N'Nguyễn Văn An', N'0901234567', N'Hà Nội', N'Cầu Giấy', N'Dịch Vọng', N'Số 123 Đường Cầu Giấy, Tòa nhà FPT', 1, 0);
END
GO

-- 4. GIAN HÀNG BÁN LẺ
DECLARE @MaChuShop1 BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'techzone@flexshop.vn');
IF @MaChuShop1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM gian_hang WHERE duong_dan_slug = N'techzone-flagship-store')
BEGIN
    INSERT INTO gian_hang (ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta, dia_chi_kho, sdt_kho, trang_thai, hang_gian_hang, diem_danh_gia_tb, tong_danh_gia)
    VALUES (@MaChuShop1, N'TechZone Flagship Store', N'techzone-flagship-store', N'Gian hàng thiết bị điện tử, âm thanh chính hãng', N'Kho tổng Long Biên, Hà Nội', N'02438889999', N'DA_DUYET', N'MALL', 4.9, 128);
END

DECLARE @MaChuShop2 BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'flexfashion@flexshop.vn');
IF @MaChuShop2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM gian_hang WHERE duong_dan_slug = N'flex-fashion-official')
BEGIN
    INSERT INTO gian_hang (ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta, dia_chi_kho, sdt_kho, trang_thai, hang_gian_hang, diem_danh_gia_tb, tong_danh_gia)
    VALUES (@MaChuShop2, N'Flex Fashion Official', N'flex-fashion-official', N'Thương hiệu thời trang nam thanh lịch', N'Kho Tân Bình, TP.HCM', N'02837778888', N'DA_DUYET', N'CHUAN', 4.7, 95);
END
GO

-- 5. DANH MỤC & SẢN PHẨM & BIẾN THỂ
IF NOT EXISTS (SELECT 1 FROM danh_muc WHERE duong_dan_slug = N'thiet-bi-dien-tu')
BEGIN
    INSERT INTO danh_muc (ten_danh_muc, duong_dan_slug, cap_do, dang_hoat_dong)
    VALUES (N'Thiết Bị Điện Tử & Âm Thanh', N'thiet-bi-dien-tu', 1, 1);
END

IF NOT EXISTS (SELECT 1 FROM danh_muc WHERE duong_dan_slug = N'thoi-trang-nam')
BEGIN
    INSERT INTO danh_muc (ten_danh_muc, duong_dan_slug, cap_do, dang_hoat_dong)
    VALUES (N'Thời Trang Nam', N'thoi-trang-nam', 1, 1);
END

DECLARE @MaDanhMucDienTu BIGINT = (SELECT TOP 1 ma_danh_muc FROM danh_muc WHERE duong_dan_slug = N'thiet-bi-dien-tu');
DECLARE @MaDanhMucThoiTrang BIGINT = (SELECT TOP 1 ma_danh_muc FROM danh_muc WHERE duong_dan_slug = N'thoi-trang-nam');
DECLARE @MaShop1 BIGINT = (SELECT TOP 1 ma_gian_hang FROM gian_hang WHERE duong_dan_slug = N'techzone-flagship-store');
DECLARE @MaShop2 BIGINT = (SELECT TOP 1 ma_gian_hang FROM gian_hang WHERE duong_dan_slug = N'flex-fashion-official');

-- Sản phẩm 1: Tai nghe chống ồn
IF @MaShop1 IS NOT NULL AND @MaDanhMucDienTu IS NOT NULL AND NOT EXISTS (SELECT 1 FROM san_pham WHERE duong_dan_slug = N'tai-nghe-bluetooth-pro-max')
BEGIN
    INSERT INTO san_pham (ma_gian_hang, ma_danh_muc, ten_san_pham, duong_dan_slug, gia_co_ban, trang_thai)
    VALUES (@MaShop1, @MaDanhMucDienTu, N'Tai nghe không dây Bluetooth chống ồn Pro Max', N'tai-nghe-bluetooth-pro-max', 1250000.00, N'HOAT_DONG');
END

DECLARE @MaSp1 BIGINT = (SELECT TOP 1 ma_san_pham FROM san_pham WHERE duong_dan_slug = N'tai-nghe-bluetooth-pro-max');
IF @MaSp1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM bien_the_san_pham WHERE ma_sku = N'SKU-EAR-BLK-01')
BEGIN
    INSERT INTO bien_the_san_pham (ma_san_pham, ma_sku, ten_bien_the, gia_ban)
    VALUES (@MaSp1, N'SKU-EAR-BLK-01', N'Màu Đen Nhám - Bluetooth 5.3', 1250000.00);
END

-- Sản phẩm 2: Áo sơ mi nam
IF @MaShop2 IS NOT NULL AND @MaDanhMucThoiTrang IS NOT NULL AND NOT EXISTS (SELECT 1 FROM san_pham WHERE duong_dan_slug = N'ao-so-mi-nam-oxford')
BEGIN
    INSERT INTO san_pham (ma_gian_hang, ma_danh_muc, ten_san_pham, duong_dan_slug, gia_co_ban, trang_thai)
    VALUES (@MaShop2, @MaDanhMucThoiTrang, N'Áo Sơ Mi Nam Oxford Form Slimfit Kháng Nhăn', N'ao-so-mi-nam-oxford', 280000.00, N'HOAT_DONG');
END

DECLARE @MaSp2 BIGINT = (SELECT TOP 1 ma_san_pham FROM san_pham WHERE duong_dan_slug = N'ao-so-mi-nam-oxford');
IF @MaSp2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM bien_the_san_pham WHERE ma_sku = N'SKU-SOMI-BLU-L')
BEGIN
    INSERT INTO bien_the_san_pham (ma_san_pham, ma_sku, ten_bien_the, gia_ban)
    VALUES (@MaSp2, N'SKU-SOMI-BLU-L', N'Màu Xanh Pastel - Size L', 280000.00);
END
GO

-- 6. ĐƠN HÀNG TỔNG & ĐƠN HÀNG SHOP (ĐỂ TEST KHIẾU NẠI)
DECLARE @MaKhachHang BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');
DECLARE @MaDiaChi BIGINT = (SELECT TOP 1 ma_dia_chi FROM dia_chi_nguoi_dung WHERE ma_nguoi_dung = @MaKhachHang);
DECLARE @MaShop1 BIGINT = (SELECT TOP 1 ma_gian_hang FROM gian_hang WHERE duong_dan_slug = N'techzone-flagship-store');
DECLARE @MaShop2 BIGINT = (SELECT TOP 1 ma_gian_hang FROM gian_hang WHERE duong_dan_slug = N'flex-fashion-official');
DECLARE @MaBt1 BIGINT = (SELECT TOP 1 ma_bien_the FROM bien_the_san_pham WHERE ma_sku = N'SKU-EAR-BLK-01');
DECLARE @MaBt2 BIGINT = (SELECT TOP 1 ma_bien_the FROM bien_the_san_pham WHERE ma_sku = N'SKU-SOMI-BLU-L');

-- Đơn hàng 1 (TechZone - 1.280.000đ - Đã giao)
IF @MaKhachHang IS NOT NULL AND @MaDiaChi IS NOT NULL AND NOT EXISTS (SELECT 1 FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-20260901-001')
BEGIN
    INSERT INTO don_hang_tong (ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, tong_tien_hang, tong_phi_van_chuyen, tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_don_hang, trang_thai_thanh_toan)
    VALUES (N'MASTER-20260901-001', @MaKhachHang, @MaDiaChi, 1250000.00, 30000.00, 1280000.00, N'COD', N'HOAN_TAT', N'DA_THANH_TOAN');
END

DECLARE @MaDonTong1 BIGINT = (SELECT TOP 1 ma_don_hang_tong FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-20260901-001');
IF @MaDonTong1 IS NOT NULL AND @MaShop1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-20260901-88')
BEGIN
    INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tong_tien_shop_nhan, trang_thai, ma_van_don)
    VALUES (N'SHOP-TECH-20260901-88', @MaDonTong1, @MaShop1, 1250000.00, 30000.00, 1280000.00, N'DA_GIAO', N'VNP98234123VN');

    DECLARE @MaDonShop1 BIGINT = (SELECT TOP 1 ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-20260901-88');
    IF @MaBt1 IS NOT NULL
    BEGIN
        INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
        VALUES (@MaDonShop1, @MaBt1, N'Tai nghe không dây Bluetooth chống ồn Pro Max', N'Màu Đen Nhám - Bluetooth 5.3', N'SKU-EAR-BLK-01', 1250000.00, 1, 1250000.00);
    END
END

-- Đơn hàng 2 (Flex Fashion - 585.000đ - Đã giao)
IF @MaKhachHang IS NOT NULL AND @MaDiaChi IS NOT NULL AND NOT EXISTS (SELECT 1 FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-20260903-002')
BEGIN
    INSERT INTO don_hang_tong (ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, tong_tien_hang, tong_phi_van_chuyen, tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_don_hang, trang_thai_thanh_toan)
    VALUES (N'MASTER-20260903-002', @MaKhachHang, @MaDiaChi, 560000.00, 25000.00, 585000.00, N'VNPAY_QR', N'HOAN_TAT', N'DA_THANH_TOAN');
END

DECLARE @MaDonTong2 BIGINT = (SELECT TOP 1 ma_don_hang_tong FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-20260903-002');
IF @MaDonTong2 IS NOT NULL AND @MaShop2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-FASHION-20260903-99')
BEGIN
    INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tong_tien_shop_nhan, trang_thai, ma_van_don)
    VALUES (N'SHOP-FASHION-20260903-99', @MaDonTong2, @MaShop2, 560000.00, 25000.00, 585000.00, N'DA_GIAO', N'GHN88776655VN');

    DECLARE @MaDonShop2 BIGINT = (SELECT TOP 1 ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-FASHION-20260903-99');
    IF @MaBt2 IS NOT NULL
    BEGIN
        INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
        VALUES (@MaDonShop2, @MaBt2, N'Áo Sơ Mi Nam Oxford Form Slimfit Kháng Nhăn', N'Màu Xanh Pastel - Size L', N'SKU-SOMI-BLU-L', 280000.00, 2, 560000.00);
    END
END

-- Đơn hàng 3 (Đang giao - Dùng để test validation không cho khiếu nại đơn chưa giao)
IF @MaKhachHang IS NOT NULL AND @MaDiaChi IS NOT NULL AND NOT EXISTS (SELECT 1 FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-20260904-003')
BEGIN
    INSERT INTO don_hang_tong (ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, tong_tien_hang, tong_phi_van_chuyen, tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_don_hang, trang_thai_thanh_toan)
    VALUES (N'MASTER-20260904-003', @MaKhachHang, @MaDiaChi, 1250000.00, 30000.00, 1280000.00, N'COD', N'DANG_GIAO', N'CHUA_THANH_TOAN');
END

DECLARE @MaDonTong3 BIGINT = (SELECT TOP 1 ma_don_hang_tong FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-20260904-003');
IF @MaDonTong3 IS NOT NULL AND @MaShop1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-20260904-77')
BEGIN
    INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tong_tien_shop_nhan, trang_thai, ma_van_don)
    VALUES (N'SHOP-TECH-20260904-77', @MaDonTong3, @MaShop1, 1250000.00, 30000.00, 1280000.00, N'DANG_GIAO', N'VNP99887766VN');
END
GO

-- 7. CÁC PHIẾU KHIẾU NẠI MẪU CHO US-45 & KẾT NỐI US-46, US-47
DECLARE @MaKhachHang BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');
DECLARE @MaCskh BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'cskh@flexshop.vn');
DECLARE @MaAdmin BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'admin@flexshop.vn');
DECLARE @MaShop1 BIGINT = (SELECT TOP 1 ma_gian_hang FROM gian_hang WHERE duong_dan_slug = N'techzone-flagship-store');
DECLARE @MaShop2 BIGINT = (SELECT TOP 1 ma_gian_hang FROM gian_hang WHERE duong_dan_slug = N'flex-fashion-official');
DECLARE @MaDonShop1 BIGINT = (SELECT TOP 1 ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-20260901-88');
DECLARE @MaDonShop2 BIGINT = (SELECT TOP 1 ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-FASHION-20260903-99');

-- Phiếu 1: MO_MOI (Khách gửi khiếu nại hỏng vỡ kèm ảnh/video lỗi - Đang chờ tiếp nhận)
IF NOT EXISTS (SELECT 1 FROM phieu_khieu_nai WHERE ma_code_phieu = N'KN-260901-1001')
BEGIN
    INSERT INTO phieu_khieu_nai (ma_code_phieu, ma_khach_hang, ma_don_hang_shop, ma_gian_hang, loai_khieu_nai, muc_do_uu_tien, trang_thai, noi_dung_mo_ta, giai_phap_yeu_cau, so_tien_hoan_tra, ngay_tao)
    VALUES (
        N'KN-260901-1001',
        @MaKhachHang,
        @MaDonShop1,
        @MaShop1,
        N'HONG_VO',
        N'CAO',
        N'MO_MOI',
        N'Kiện hàng giao đến bị móp méo hộp nặng, tai nghe bên trong bị nứt khớp nối gọng bên phải và không lên nguồn.',
        N'HOAN_TIEN_TRA_HANG',
        1280000.00,
        DATEADD(HOUR, -6, GETDATE())
    );

    DECLARE @MaPhieu1 BIGINT = SCOPE_IDENTITY();
    -- Đính kèm ảnh và video lỗi mẫu
    INSERT INTO bang_chung_khieu_nai (ma_phieu, link_tep_tin, loai_tep_tin, vai_tro_tai_len)
    VALUES 
        (@MaPhieu1, N'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800', N'HINH_ANH', N'KHACH_HANG'),
        (@MaPhieu1, N'https://www.w3schools.com/html/mov_bbb.mp4', N'VIDEO', N'KHACH_HANG');
END

-- Phiếu 2: DANG_XU_LY (CSKH đang xác minh đối chiếu 3 bên - US-46)
IF NOT EXISTS (SELECT 1 FROM phieu_khieu_nai WHERE ma_code_phieu = N'KN-260902-1002')
BEGIN
    INSERT INTO phieu_khieu_nai (ma_code_phieu, ma_khach_hang, ma_don_hang_shop, ma_gian_hang, loai_khieu_nai, muc_do_uu_tien, trang_thai, noi_dung_mo_ta, giai_phap_yeu_cau, ma_cskh_xu_ly, so_tien_hoan_tra, ngay_tao)
    VALUES (
        N'KN-260902-1002',
        @MaKhachHang,
        @MaDonShop2,
        @MaShop2,
        N'GIAO_SAI',
        N'TRUNG_BINH',
        N'DANG_XU_LY',
        N'Tôi đặt 2 áo sơ mi màu xanh pastel size L nhưng shop giao nhầm thành màu đen size M.',
        N'DOI_HANG',
        @MaCskh,
        585000.00,
        DATEADD(DAY, -1, GETDATE())
    );

    DECLARE @MaPhieu2 BIGINT = SCOPE_IDENTITY();
    INSERT INTO bang_chung_khieu_nai (ma_phieu, link_tep_tin, loai_tep_tin, vai_tro_tai_len)
    VALUES (@MaPhieu2, N'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800', N'HINH_ANH', N'KHACH_HANG');

    -- Ghi chú nội bộ CSKH (Chuẩn bị sẵn cho US-46)
    INSERT INTO ghi_chu_noi_bo_khieu_nai (ma_phieu, ma_nhan_vien, noi_dung)
    VALUES (@MaPhieu2, @MaCskh, N'Đã liên hệ chủ shop Flex Fashion, shop xác nhận kho đóng nhầm mã SKU. Đang tạo mã điều chuyển đổi hàng mới.');
END

-- Phiếu 3: CHAP_NHAN_HOAN_TIEN (Đã được Admin/CSKH phán quyết bồi hoàn tiền - US-47)
IF NOT EXISTS (SELECT 1 FROM phieu_khieu_nai WHERE ma_code_phieu = N'KN-260903-1003')
BEGIN
    INSERT INTO phieu_khieu_nai (ma_code_phieu, ma_khach_hang, ma_don_hang_shop, ma_gian_hang, loai_khieu_nai, muc_do_uu_tien, trang_thai, noi_dung_mo_ta, giai_phap_yeu_cau, ma_cskh_xu_ly, ma_nguoi_phan_quyet, ghi_chu_phan_quyet, so_tien_hoan_tra, ngay_tao)
    VALUES (
        N'KN-260903-1003',
        @MaKhachHang,
        @MaDonShop1,
        @MaShop1,
        N'HANG_GIA',
        N'KHAN_CAP',
        N'CHAP_NHAN_HOAN_TIEN',
        N'Sản phẩm không có tem kiểm định chính hãng như cam kết của gian hàng Mall.',
        N'HOAN_TIEN_KHONG_TRA',
        @MaCskh,
        @MaAdmin,
        N'Phán quyết: Bằng chứng xác thực. Duyệt hoàn 100% tiền thanh toán 1.280.000 VNĐ vào ví khách hàng. Phạt trừ điểm sao quả tạ shop vi phạm.',
        1280000.00,
        DATEADD(DAY, -2, GETDATE())
    );

    DECLARE @MaPhieu3 BIGINT = SCOPE_IDENTITY();
    INSERT INTO bang_chung_khieu_nai (ma_phieu, link_tep_tin, loai_tep_tin, vai_tro_tai_len)
    VALUES (@MaPhieu3, N'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=800', N'HINH_ANH', N'KHACH_HANG');

    -- Lệnh hoàn tiền bồi thường (Chuẩn bị sẵn cho US-47)
    INSERT INTO lenh_hoan_tien_boi_thuong (ma_phieu, nguoi_nhan_tien, so_tien, ben_chiu_phi, trang_thai)
    VALUES (@MaPhieu3, @MaKhachHang, 1280000.00, N'NGUOI_BAN', N'DA_CHUYEN_TIEN');
END

-- Phiếu 4: TU_CHOI_KHIEU_NAI (Khiếu nại bị từ chối)
IF NOT EXISTS (SELECT 1 FROM phieu_khieu_nai WHERE ma_code_phieu = N'KN-260904-1004')
BEGIN
    INSERT INTO phieu_khieu_nai (ma_code_phieu, ma_khach_hang, ma_don_hang_shop, ma_gian_hang, loai_khieu_nai, muc_do_uu_tien, trang_thai, noi_dung_mo_ta, giai_phap_yeu_cau, ma_cskh_xu_ly, ma_nguoi_phan_quyet, ghi_chu_phan_quyet, so_tien_hoan_tra, ngay_tao)
    VALUES (
        N'KN-260904-1004',
        @MaKhachHang,
        @MaDonShop2,
        @MaShop2,
        N'KHAC',
        N'THAP',
        N'TU_CHOI_KHIEU_NAI',
        N'Tôi muốn trả hàng vì đổi ý không thích màu này nữa sau khi đã giặt và cắt tag.',
        N'HOAN_TIEN_TRA_HANG',
        @MaCskh,
        @MaAdmin,
        N'Từ chối: Sản phẩm đã cắt tem mác và qua sử dụng/giặt tẩy, không thỏa mãn chính sách đổi trả của sàn.',
        585000.00,
        DATEADD(DAY, -3, GETDATE())
    );
END
GO

PRINT N'=== HOÀN TẤT ĐỒNG BỘ DỮ LIỆU KIỂM THỬ US-45 ===';

-- 8. TRUY VẤN KIỂM TRA KẾT QUẢ ĐỒNG BỘ
SELECT 
    p.ma_phieu AS [Mã Phiếu ID],
    p.ma_code_phieu AS [Mã Code Phiếu],
    p.loai_khieu_nai AS [Lý Do],
    p.giai_phap_yeu_cau AS [Giải Pháp],
    FORMAT(p.so_tien_hoan_tra, 'N0') + ' đ' AS [Tiền Hoàn],
    p.trang_thai AS [Trạng Thái],
    u.ho_va_ten AS [Khách Hàng],
    s.ten_gian_hang AS [Gian Hàng],
    d.ma_code_don_shop AS [Mã Đơn Shop],
    COUNT(b.ma_bang_chung) AS [Số Bằng Chứng]
FROM phieu_khieu_nai p
INNER JOIN nguoi_dung u ON p.ma_khach_hang = u.ma_nguoi_dung
INNER JOIN gian_hang s ON p.ma_gian_hang = s.ma_gian_hang
INNER JOIN don_hang_shop d ON p.ma_don_hang_shop = d.ma_don_hang_shop
LEFT JOIN bang_chung_khieu_nai b ON p.ma_phieu = b.ma_phieu
GROUP BY 
    p.ma_phieu, p.ma_code_phieu, p.loai_khieu_nai, p.giai_phap_yeu_cau, 
    p.so_tien_hoan_tra, p.trang_thai, u.ho_va_ten, s.ten_gian_hang, d.ma_code_don_shop
ORDER BY p.ma_phieu DESC;
GO
