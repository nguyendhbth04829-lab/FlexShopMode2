-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-33: HUB + HANH TRINH KIEN HANG
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: seed 3 Hub (HN, DN, HCM) va timeline mau cho don SHOP-US31-002.
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-33 (HUB + HANH TRINH) ===';

-- 1. Hub trung chuyen
IF NOT EXISTS (SELECT 1 FROM tram_trung_chuyen_hub WHERE ten_hub = N'Hub Ha Noi - Long Bien')
    INSERT INTO tram_trung_chuyen_hub (ten_hub, dia_chi, tinh_thanh, suc_chua_kien_hang)
    VALUES (N'Hub Ha Noi - Long Bien', N'KCN Dai Tu, Long Bien, Ha Noi', N'Ha Noi', 50000);
IF NOT EXISTS (SELECT 1 FROM tram_trung_chuyen_hub WHERE ten_hub = N'Hub Da Nang - Hoa Vang')
    INSERT INTO tram_trung_chuyen_hub (ten_hub, dia_chi, tinh_thanh, suc_chua_kien_hang)
    VALUES (N'Hub Da Nang - Hoa Vang', N'KCN Hoa Khanh, Lien Chieu, Da Nang', N'Da Nang', 30000);
IF NOT EXISTS (SELECT 1 FROM tram_trung_chuyen_hub WHERE ten_hub = N'Hub TP.HCM - Tan Binh')
    INSERT INTO tram_trung_chuyen_hub (ten_hub, dia_chi, tinh_thanh, suc_chua_kien_hang)
    VALUES (N'Hub TP.HCM - Tan Binh', N'KCN Tan Binh, Quan Tan Phu, TP.HCM', N'TP.Ho Chi Minh', 60000);

DECLARE @HubHN BIGINT = (SELECT TOP 1 ma_hub FROM tram_trung_chuyen_hub WHERE ten_hub = N'Hub Ha Noi - Long Bien');
DECLARE @MaDon BIGINT = (SELECT TOP 1 ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-US31-002');

-- 2. Timeline mau cho don DANG_GIAO (neu chua co)
IF @MaDon IS NOT NULL AND NOT EXISTS (SELECT 1 FROM lich_su_hanh_trinh_don WHERE ma_don_hang_shop = @MaDon)
BEGIN
    INSERT INTO lich_su_hanh_trinh_don (ma_don_hang_shop, ma_hub, tieu_de_moc, vi_tri_hien_tai, thoi_gian)
    VALUES (@MaDon, NULL, N'Cho lay hang tu kho Shop', N'Kho TechZone, Long Bien, Ha Noi', DATEADD(HOUR, -6, GETDATE()));
    INSERT INTO lich_su_hanh_trinh_don (ma_don_hang_shop, ma_hub, tieu_de_moc, vi_tri_hien_tai, thoi_gian)
    VALUES (@MaDon, @HubHN, N'Dang nhap Hub', N'Hub Ha Noi - Long Bien', DATEADD(HOUR, -3, GETDATE()));
    INSERT INTO lich_su_hanh_trinh_don (ma_don_hang_shop, ma_hub, tieu_de_moc, vi_tri_hien_tai, thoi_gian)
    VALUES (@MaDon, @HubHN, N'Da roi Hub', N'Hub Ha Noi - Long Bien', DATEADD(HOUR, -1, GETDATE()));
    PRINT N'[US-33] Da seed 3 moc mau cho SHOP-US31-002.';
END
ELSE
    PRINT N'[US-33] Don SHOP-US31-002 da co timeline hoac chua ton tai (hay chay SQL US31 truoc).';
GO
PRINT N'=== KET THUC US-33 ===';
