-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-31: IN PHIEU DONG GOI / VAN DON A6
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: tao 4 don shop cho gian hang 1 (TechZone) de test in hang loat,
--   gom ca don thieu ma van don (test tu sinh FS-yyyyMMdd-XXXXXX)
--   va don da co san ma van don.
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-31 (VAN DON A6) ===';

DECLARE @MaKhach BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');
DECLARE @MaDiaChi BIGINT = (SELECT TOP 1 ma_dia_chi FROM dia_chi_nguoi_dung WHERE ma_nguoi_dung = @MaKhach);
DECLARE @MaGianHang BIGINT = (SELECT TOP 1 ma_gian_hang FROM gian_hang WHERE ten_gian_hang = N'TechZone Flagship Store');
DECLARE @MaBienThe BIGINT = (SELECT TOP 1 ma_bien_the FROM bien_the_san_pham WHERE ma_sku = N'SKU-EAR-BLK-01');

IF @MaKhach IS NULL OR @MaDiaChi IS NULL OR @MaGianHang IS NULL OR @MaBienThe IS NULL
BEGIN
    PRINT N'[US-31] Thieu du lieu nen (khach/dia chi/shop/bien the). Hay chay US-45 truoc.';
END
ELSE
BEGIN
    -- Don 1: DA_XAC_NHAN, CHUA co van don -> test tu sinh ma
    IF NOT EXISTS (SELECT 1 FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-US31-001')
    BEGIN
        INSERT INTO don_hang_tong (ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, tong_tien_hang, tong_phi_van_chuyen, tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_don_hang, trang_thai_thanh_toan)
        VALUES (N'MASTER-US31-001', @MaKhach, @MaDiaChi, 1250000.00, 30000.00, 1280000.00, N'COD', N'CHO_XU_LY', N'CHUA_THANH_TOAN');
    END
    DECLARE @Tong1 BIGINT = (SELECT ma_don_hang_tong FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-US31-001');
    IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US31-001')
    BEGIN
        INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tong_tien_shop_nhan, trang_thai, ma_van_don)
        VALUES (N'SHOP-US31-001', @Tong1, @MaGianHang, 1250000.00, 30000.00, 1280000.00, N'DA_XAC_NHAN', NULL);
        DECLARE @Shop1 BIGINT = SCOPE_IDENTITY();
        INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
        VALUES (@Shop1, @MaBienThe, N'Tai nghe khong day Bluetooth chong on Pro Max', N'Mau Den Nham - Bluetooth 5.3', N'SKU-EAR-BLK-01', 1250000.00, 1, 1250000.00);
    END

    -- Don 2: DANG_GIAO, CHUA co van don -> test tu sinh ma
    IF NOT EXISTS (SELECT 1 FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-US31-002')
    BEGIN
        INSERT INTO don_hang_tong (ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, tong_tien_hang, tong_phi_van_chuyen, tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_don_hang, trang_thai_thanh_toan)
        VALUES (N'MASTER-US31-002', @MaKhach, @MaDiaChi, 2500000.00, 30000.00, 2530000.00, N'COD', N'CHO_XU_LY', N'CHUA_THANH_TOAN');
    END
    DECLARE @Tong2 BIGINT = (SELECT ma_don_hang_tong FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-US31-002');
    IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US31-002')
    BEGIN
        INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tong_tien_shop_nhan, trang_thai, ma_van_don)
        VALUES (N'SHOP-US31-002', @Tong2, @MaGianHang, 2500000.00, 30000.00, 2530000.00, N'DANG_GIAO', NULL);
        DECLARE @Shop2 BIGINT = SCOPE_IDENTITY();
        INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
        VALUES (@Shop2, @MaBienThe, N'Tai nghe khong day Bluetooth chong on Pro Max', N'Mau Den Nham - Bluetooth 5.3', N'SKU-EAR-BLK-01', 1250000.00, 2, 2500000.00);
    END

    -- Don 3: DA_XAC_NHAN, DA co van don
    IF NOT EXISTS (SELECT 1 FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-US31-003')
    BEGIN
        INSERT INTO don_hang_tong (ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, tong_tien_hang, tong_phi_van_chuyen, tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_don_hang, trang_thai_thanh_toan)
        VALUES (N'MASTER-US31-003', @MaKhach, @MaDiaChi, 1250000.00, 30000.00, 1280000.00, N'VNPAY_QR', N'CHO_XU_LY', N'DA_THANH_TOAN');
    END
    DECLARE @Tong3 BIGINT = (SELECT ma_don_hang_tong FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-US31-003');
    IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US31-003')
    BEGIN
        INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tong_tien_shop_nhan, trang_thai, ma_van_don)
        VALUES (N'SHOP-US31-003', @Tong3, @MaGianHang, 1250000.00, 30000.00, 1280000.00, N'DA_XAC_NHAN', N'GHN-US31-003-VN');
        DECLARE @Shop3 BIGINT = SCOPE_IDENTITY();
        INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
        VALUES (@Shop3, @MaBienThe, N'Tai nghe khong day Bluetooth chong on Pro Max', N'Mau Den Nham - Bluetooth 5.3', N'SKU-EAR-BLK-01', 1250000.00, 1, 1250000.00);
    END

    -- Don 4: CHO_XAC_NHAN, DA co van don
    IF NOT EXISTS (SELECT 1 FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-US31-004')
    BEGIN
        INSERT INTO don_hang_tong (ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, tong_tien_hang, tong_phi_van_chuyen, tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_don_hang, trang_thai_thanh_toan)
        VALUES (N'MASTER-US31-004', @MaKhach, @MaDiaChi, 1250000.00, 30000.00, 1280000.00, N'COD', N'CHO_XU_LY', N'CHUA_THANH_TOAN');
    END
    DECLARE @Tong4 BIGINT = (SELECT ma_don_hang_tong FROM don_hang_tong WHERE ma_code_don_tong = N'MASTER-US31-004');
    IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US31-004')
    BEGIN
        INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tong_tien_shop_nhan, trang_thai, ma_van_don)
        VALUES (N'SHOP-US31-004', @Tong4, @MaGianHang, 1250000.00, 30000.00, 1280000.00, N'CHO_XAC_NHAN', N'GHTK-US31-004-VN');
        DECLARE @Shop4 BIGINT = SCOPE_IDENTITY();
        INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
        VALUES (@Shop4, @MaBienThe, N'Tai nghe khong day Bluetooth chong on Pro Max', N'Mau Den Nham - Bluetooth 5.3', N'SKU-EAR-BLK-01', 1250000.00, 1, 1250000.00);
    END

    PRINT N'[US-31] Dong bo xong 4 don test in A6 (2 thieu van don, 2 co san).';
END
GO
PRINT N'=== KET THUC US-31 ===';
