-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-40: CUOC SAP CHAM 3 LAN THAT BAI
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: dua cuoc cua shipper1 tren SHOP-US35-09 ve lan 3/3 (DANG_GIAO)
--   de test bao that bai lan 3 -> tu dong tao chuyen hoan.
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-40 (CHUYEN HOAN) ===';

DECLARE @MaTaiXe BIGINT = (SELECT TOP 1 ma_tai_xe FROM tai_xe_giao_hang WHERE ma_nguoi_dung = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'shipper1@flexshop.vn'));
DECLARE @MaDon BIGINT = (SELECT TOP 1 ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US35-09');

IF @MaTaiXe IS NULL OR @MaDon IS NULL
    PRINT N'[US-40] Thieu shipper1 hoac SHOP-US35-09. Hay chay SQL US34 + US35 truoc.';
ELSE IF EXISTS (SELECT 1 FROM yeu_cau_chuyen_hoan WHERE ma_don_hang_shop = @MaDon)
    PRINT N'[US-40] SHOP-US35-09 da co yeu cau chuyen hoan - giu nguyen de test.';
ELSE
BEGIN
    UPDATE don_hang_shop SET trang_thai = N'DANG_GIAO' WHERE ma_don_hang_shop = @MaDon;
    IF NOT EXISTS (SELECT 1 FROM nhiem_vu_giao_hang WHERE ma_don_hang_shop = @MaDon)
    BEGIN
        DECLARE @Cod DECIMAL(18,2) = (SELECT tong_tien_shop_nhan FROM don_hang_shop WHERE ma_don_hang_shop = @MaDon);
        INSERT INTO nhiem_vu_giao_hang (ma_don_hang_shop, ma_tai_xe, loai_nhiem_vu, trang_thai, tien_cod_can_thu, da_thu_cod, so_lan_giao, ly_do_that_bai, thoi_gian_lay_hang, ngay_tao)
        VALUES (@MaDon, @MaTaiXe, N'GIAO_HANG', N'DANG_GIAO', @Cod, 0, 3, N'KHACH_KHONG_NGHE_MAY', GETDATE(), GETDATE());
    END
    ELSE
        UPDATE nhiem_vu_giao_hang SET trang_thai = N'DANG_GIAO', so_lan_giao = 3, ly_do_that_bai = N'KHACH_KHONG_NGHE_MAY' WHERE ma_don_hang_shop = @MaDon;
    PRINT N'[US-40] Da chuan bi cuoc lan 3/3 cho shipper1 - SHOP-US35-09.';
END
GO
PRINT N'=== KET THUC US-40 ===';
