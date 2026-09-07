-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-32: DOI TAC + BANG GIA VAN CHUYEN
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: seed 2 doi tac 3PL va 6 bang gia (3 tuyen x 2 doi tac)
--   de test cong thuc: QuyDoi(g)=D*R*C/5, TinhCuoc=MAX(thuc,quyDoi).
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-32 (BANG GIA 3PL) ===';

-- 1. Doi tac van chuyen
IF NOT EXISTS (SELECT 1 FROM doi_tac_van_chuyen WHERE ten_doi_tac = N'Giao Hang Nhanh')
    INSERT INTO doi_tac_van_chuyen (ten_doi_tac, ma_ket_noi_api, dang_hoat_dong)
    VALUES (N'Giao Hang Nhanh', N'GHN_API_V1', 1);
IF NOT EXISTS (SELECT 1 FROM doi_tac_van_chuyen WHERE ten_doi_tac = N'Giao Hang Tiet Kiem')
    INSERT INTO doi_tac_van_chuyen (ten_doi_tac, ma_ket_noi_api, dang_hoat_dong)
    VALUES (N'Giao Hang Tiet Kiem', N'GHTK_API_V1', 1);

DECLARE @Ghn INT = (SELECT TOP 1 ma_doi_tac FROM doi_tac_van_chuyen WHERE ten_doi_tac = N'Giao Hang Nhanh');
DECLARE @Ghtk INT = (SELECT TOP 1 ma_doi_tac FROM doi_tac_van_chuyen WHERE ten_doi_tac = N'Giao Hang Tiet Kiem');

-- 2. Bang gia GHN: chuan 1000g
IF @Ghn IS NOT NULL AND NOT EXISTS (SELECT 1 FROM bang_gia_van_chuyen WHERE ma_doi_tac = @Ghn AND tuyen_van_chuyen = N'NOI_THANH')
    INSERT INTO bang_gia_van_chuyen (ma_doi_tac, tuyen_van_chuyen, khoi_luong_chuan_gram, cuoc_phi_chuan, cuoc_phi_vuot_moi_500g)
    VALUES (@Ghn, N'NOI_THANH', 1000, 22000.00, 5000.00);
IF @Ghn IS NOT NULL AND NOT EXISTS (SELECT 1 FROM bang_gia_van_chuyen WHERE ma_doi_tac = @Ghn AND tuyen_van_chuyen = N'TINH')
    INSERT INTO bang_gia_van_chuyen (ma_doi_tac, tuyen_van_chuyen, khoi_luong_chuan_gram, cuoc_phi_chuan, cuoc_phi_vuot_moi_500g)
    VALUES (@Ghn, N'TINH', 1000, 32000.00, 7000.00);
IF @Ghn IS NOT NULL AND NOT EXISTS (SELECT 1 FROM bang_gia_van_chuyen WHERE ma_doi_tac = @Ghn AND tuyen_van_chuyen = N'BAN_SO_DIA')
    INSERT INTO bang_gia_van_chuyen (ma_doi_tac, tuyen_van_chuyen, khoi_luong_chuan_gram, cuoc_phi_chuan, cuoc_phi_vuot_moi_500g)
    VALUES (@Ghn, N'BAN_SO_DIA', 1000, 45000.00, 10000.00);

-- 3. Bang gia GHTK: chuan 500g, re hon noi thanh
IF @Ghtk IS NOT NULL AND NOT EXISTS (SELECT 1 FROM bang_gia_van_chuyen WHERE ma_doi_tac = @Ghtk AND tuyen_van_chuyen = N'NOI_THANH')
    INSERT INTO bang_gia_van_chuyen (ma_doi_tac, tuyen_van_chuyen, khoi_luong_chuan_gram, cuoc_phi_chuan, cuoc_phi_vuot_moi_500g)
    VALUES (@Ghtk, N'NOI_THANH', 500, 18000.00, 4500.00);
IF @Ghtk IS NOT NULL AND NOT EXISTS (SELECT 1 FROM bang_gia_van_chuyen WHERE ma_doi_tac = @Ghtk AND tuyen_van_chuyen = N'TINH')
    INSERT INTO bang_gia_van_chuyen (ma_doi_tac, tuyen_van_chuyen, khoi_luong_chuan_gram, cuoc_phi_chuan, cuoc_phi_vuot_moi_500g)
    VALUES (@Ghtk, N'TINH', 500, 30000.00, 6500.00);
IF @Ghtk IS NOT NULL AND NOT EXISTS (SELECT 1 FROM bang_gia_van_chuyen WHERE ma_doi_tac = @Ghtk AND tuyen_van_chuyen = N'BAN_SO_DIA')
    INSERT INTO bang_gia_van_chuyen (ma_doi_tac, tuyen_van_chuyen, khoi_luong_chuan_gram, cuoc_phi_chuan, cuoc_phi_vuot_moi_500g)
    VALUES (@Ghtk, N'BAN_SO_DIA', 500, 42000.00, 9500.00);

PRINT N'[US-32] Dong bo xong 2 doi tac + 6 bang gia.';
GO
PRINT N'=== KET THUC US-32 ===';
