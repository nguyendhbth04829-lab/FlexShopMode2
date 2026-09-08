-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-39: DU LIEU DEMO DASHBOARD COD
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: dam bao shipper1 co 1 cuoc THANH_CONG da thu COD (SHOP-US35-08)
--   de dashboard co so lieu tong hop demo.
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-39 (DASHBOARD COD) ===';

DECLARE @MaTaiXe BIGINT = (SELECT TOP 1 ma_tai_xe FROM tai_xe_giao_hang WHERE ma_nguoi_dung = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'shipper1@flexshop.vn'));
DECLARE @MaDon BIGINT = (SELECT TOP 1 ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US35-08');

IF @MaTaiXe IS NULL OR @MaDon IS NULL
    PRINT N'[US-39] Thieu shipper1 hoac SHOP-US35-08. Hay chay SQL US34 + US35 truoc.';
ELSE IF EXISTS (SELECT 1 FROM nhiem_vu_giao_hang WHERE ma_don_hang_shop = @MaDon)
    PRINT N'[US-39] SHOP-US35-08 da co nhiem vu - giu nguyen de test.';
ELSE
BEGIN
    DECLARE @Cod DECIMAL(18,2) = (SELECT tong_tien_shop_nhan FROM don_hang_shop WHERE ma_don_hang_shop = @MaDon);
    INSERT INTO nhiem_vu_giao_hang (ma_don_hang_shop, ma_tai_xe, loai_nhiem_vu, trang_thai, tien_cod_can_thu, da_thu_cod, so_lan_giao, thoi_gian_lay_hang, thoi_gian_giao_thanh_cong, ngay_tao)
    VALUES (@MaDon, @MaTaiXe, N'GIAO_HANG', N'THANH_CONG', @Cod, 1, 1, DATEADD(HOUR, -5, GETDATE()), DATEADD(HOUR, -2, GETDATE()), DATEADD(HOUR, -6, GETDATE()));
    UPDATE don_hang_shop SET trang_thai = N'DA_GIAO' WHERE ma_don_hang_shop = @MaDon;
    UPDATE tai_xe_giao_hang SET so_du_cod_dang_giu = ISNULL(so_du_cod_dang_giu, 0) + @Cod WHERE ma_tai_xe = @MaTaiXe;
    PRINT N'[US-39] Da tao cuoc THANH_CONG da thu COD cho shipper1 - SHOP-US35-08.';
END
GO
PRINT N'=== KET THUC US-39 ===';
