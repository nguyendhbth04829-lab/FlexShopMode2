-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-35: DON SAN SANG DE SHIPPER NHAN CUOC
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: dam bao co ~10 don DA_XAC_NHAN de test man don-cho-nhan
--   tren mobile app (xem list dai co bi chong lan khong).
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-35 (DON CHO NHAN) ===';

UPDATE don_hang_shop SET trang_thai = N'DA_XAC_NHAN'
WHERE ma_code_don_shop IN (N'SHOP-US31-001', N'SHOP-US31-003')
  AND trang_thai <> N'DA_XAC_NHAN';

-- Xoa nhiem vu cu cua don test (neu co) de test nhan lai tu dau
DELETE FROM nhiem_vu_giao_hang
WHERE ma_don_hang_shop IN (
    SELECT ma_don_hang_shop FROM don_hang_shop
    WHERE ma_code_don_shop LIKE N'SHOP-US3%'
);

DECLARE @MaKhach BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');
DECLARE @MaDiaChi BIGINT = (SELECT TOP 1 ma_dia_chi FROM dia_chi_nguoi_dung WHERE ma_nguoi_dung = @MaKhach);
DECLARE @MaGianHang BIGINT = (SELECT TOP 1 ma_gian_hang FROM gian_hang WHERE ten_gian_hang = N'TechZone Flagship Store');
DECLARE @MaBienThe BIGINT = (SELECT TOP 1 ma_bien_the FROM bien_the_san_pham WHERE ma_sku = N'SKU-EAR-BLK-01');
DECLARE @i INT = 5;
WHILE @i <= 14
BEGIN
    DECLARE @MaTong NVARCHAR(50) = N'MASTER-US35-0' + CAST(@i AS NVARCHAR(10));
    DECLARE @MaShop NVARCHAR(60) = N'SHOP-US35-0' + CAST(@i AS NVARCHAR(10));
    DECLARE @Sl INT = (@i % 3) + 1;
    DECLARE @Tien DECIMAL(18,2) = 1250000.00 * @Sl;
    DECLARE @Pttt NVARCHAR(50) = CASE WHEN @i % 2 = 0 THEN N'COD' ELSE N'VNPAY_QR' END;
    IF NOT EXISTS (SELECT 1 FROM don_hang_tong WHERE ma_code_don_tong = @MaTong)
    BEGIN
        INSERT INTO don_hang_tong (ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, tong_tien_hang, tong_phi_van_chuyen, tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_don_hang, trang_thai_thanh_toan)
        VALUES (@MaTong, @MaKhach, @MaDiaChi, @Tien, 30000.00, @Tien + 30000.00, @Pttt, N'CHO_XU_LY', N'CHUA_THANH_TOAN');
    END
    DECLARE @TongId BIGINT = (SELECT ma_don_hang_tong FROM don_hang_tong WHERE ma_code_don_tong = @MaTong);
    IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = @MaShop)
    BEGIN
        INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tong_tien_shop_nhan, trang_thai, ma_van_don)
        VALUES (@MaShop, @TongId, @MaGianHang, @Tien, 30000.00, @Tien + 30000.00, N'DA_XAC_NHAN',
                CASE WHEN @i % 2 = 0 THEN NULL ELSE N'GHN-US35-00' + CAST(@i AS NVARCHAR(10)) + N'-VN' END);
        DECLARE @ShopId BIGINT = SCOPE_IDENTITY();
        INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
        VALUES (@ShopId, @MaBienThe, N'Tai nghe khong day Bluetooth chong on Pro Max', N'Mau Den Nham - Bluetooth 5.3', N'SKU-EAR-BLK-01', 1250000.00, @Sl, @Tien);
    END
    SET @i = @i + 1;
END

PRINT N'[US-35] Da chuan bi xong ~12 don cho nhan (SHOP-US31-001/003 + SHOP-US35-005..014).';
GO
PRINT N'=== KET THUC US-35 ===';
