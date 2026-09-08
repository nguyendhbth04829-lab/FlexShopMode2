-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-41: DON DA_GIAO DE TEST XAC NHAN
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: dam bao SHOP-US35-07 o DA_GIAO de test nut "Da nhan duoc hang".
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-41 (DA NHAN) ===';

IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US35-07' AND trang_thai = N'DA_GIAO')
    PRINT N'[US-41] SHOP-US35-07 khong o DA_GIAO (co the da HOAN_THANH khi test). Reset ve DA_GIAO de test lai: UPDATE don_hang_shop SET trang_thai=N''DA_GIAO'' WHERE ma_code_don_shop=N''SHOP-US35-07''.';
ELSE
    PRINT N'[US-41] SHOP-US35-07 san sang (DA_GIAO) de test nut xac nhan.';
GO
PRINT N'=== KET THUC US-41 ===';
