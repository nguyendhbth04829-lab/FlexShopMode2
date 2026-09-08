-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-36: CUOC SAN SANG DE XAC NHAN LAY HANG
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: tao san 1 nhiem vu DA_PHAN_CONG cho shipper1 tren SHOP-US35-005
--   de test nut "Da lay hang" ngay ma khong can nhan lai tu dau.
--   (Ma don 2 chu so: SHOP-US35-05 vi seed US35 dung format 05..14.)
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-36 (LAY HANG) ===';

DECLARE @MaTaiXe BIGINT = (SELECT TOP 1 ma_tai_xe FROM tai_xe_giao_hang WHERE ma_nguoi_dung = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'shipper1@flexshop.vn'));
DECLARE @MaDon BIGINT = (SELECT TOP 1 ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US35-05');

IF @MaTaiXe IS NULL OR @MaDon IS NULL
    PRINT N'[US-36] Thieu shipper1 hoac SHOP-US35-005. Hay chay SQL US34 + US35 truoc.';
ELSE IF EXISTS (SELECT 1 FROM nhiem_vu_giao_hang WHERE ma_don_hang_shop = @MaDon)
    PRINT N'[US-36] SHOP-US35-05 da co nhiem vu - giu nguyen de test.';
ELSE
BEGIN
    DECLARE @Cod DECIMAL(18,2) = (SELECT tong_tien_shop_nhan FROM don_hang_shop WHERE ma_don_hang_shop = @MaDon);
    INSERT INTO nhiem_vu_giao_hang (ma_don_hang_shop, ma_tai_xe, loai_nhiem_vu, trang_thai, tien_cod_can_thu, da_thu_cod, so_lan_giao, ngay_tao)
    VALUES (@MaDon, @MaTaiXe, N'GIAO_HANG', N'DA_PHAN_CONG', @Cod, 0, 1, GETDATE());
    PRINT N'[US-36] Da tao nhiem vu DA_PHAN_CONG cho shipper1 - SHOP-US35-05.';
END
GO
PRINT N'=== KET THUC US-36 ===';
