-- ============================================================================
-- CAP NHAT DB CHO US-38: BAO GIAO THAT BAI + HEN GIAO LAI (TOI DA 3 LAN)
-- Khong doi schema (dung cot ly_do_that_bai, so_lan_giao,
--   thoi_gian_hen_giao_lai san co); file nay de patch DB cu (idempotent).
-- Quy tac: so_lan_giao <= 3; lan 3 that bai -> cho chuyen hoan (US-40).
-- ============================================================================

USE FlexShop_V2_Full;
GO

IF OBJECT_ID(N'nhiem_vu_giao_hang', N'U') IS NULL
    PRINT N'[US-38] THIEU bang nhiem_vu_giao_hang! Hay chay cap_nhat_db_US35.sql truoc.';
ELSE
    PRINT N'[US-38] Bang nhiem_vu_giao_hang da ton tai - khong can doi schema.';
GO
