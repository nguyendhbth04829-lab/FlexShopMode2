-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-37: CUOC DANG GIAO DE TEST POD + COD
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: tao san 1 nhiem vu DANG_GIAO cho shipper1 tren SHOP-US35-06
--   (don COD) de test chup POD + thu COD ngay.
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-37 (POD) ===';

DECLARE @MaTaiXe BIGINT = (SELECT TOP 1 ma_tai_xe FROM tai_xe_giao_hang WHERE ma_nguoi_dung = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'shipper1@flexshop.vn'));
DECLARE @MaDon BIGINT = (SELECT TOP 1 ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US35-06');

IF @MaTaiXe IS NULL OR @MaDon IS NULL
    PRINT N'[US-37] Thieu shipper1 hoac SHOP-US35-06. Hay chay SQL US34 + US35 truoc.';
ELSE IF EXISTS (SELECT 1 FROM nhiem_vu_giao_hang WHERE ma_don_hang_shop = @MaDon)
    PRINT N'[US-37] SHOP-US35-06 da co nhiem vu - giu nguyen de test.';
ELSE
BEGIN
    UPDATE don_hang_shop SET trang_thai = N'DANG_GIAO' WHERE ma_don_hang_shop = @MaDon;
    DECLARE @Cod DECIMAL(18,2) = (SELECT tong_tien_shop_nhan FROM don_hang_shop WHERE ma_don_hang_shop = @MaDon);
    INSERT INTO nhiem_vu_giao_hang (ma_don_hang_shop, ma_tai_xe, loai_nhiem_vu, trang_thai, tien_cod_can_thu, da_thu_cod, so_lan_giao, thoi_gian_lay_hang, ngay_tao)
    VALUES (@MaDon, @MaTaiXe, N'GIAO_HANG', N'DANG_GIAO', @Cod, 0, 1, GETDATE(), GETDATE());
    PRINT N'[US-37] Da tao nhiem vu DANG_GIAO cho shipper1 - SHOP-US35-06.';
END
GO
PRINT N'=== KET THUC US-37 ===';
