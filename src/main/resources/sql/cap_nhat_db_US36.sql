-- ============================================================================
-- CAP NHAT DB CHO US-36: XAC NHAN DA LAY HANG -> DANG_GIAO
-- Khong doi schema (dung bang nhiem_vu_giao_hang + don_hang_shop san co);
-- file nay de patch DB cu (idempotent) va ghi nhan quy tac.
-- Quy tac: nhiem vu DA_PHAN_CONG + don DA_XAC_NHAN -> DANG_GIAO,
--   set thoi_gian_lay_hang + auto-ghi hanh trinh.
-- ============================================================================

USE FlexShop_V2_Full;
GO

IF OBJECT_ID(N'nhiem_vu_giao_hang', N'U') IS NULL
    PRINT N'[US-36] THIEU bang nhiem_vu_giao_hang! Hay chay cap_nhat_db_US35.sql truoc.';
ELSE
    PRINT N'[US-36] Bang nhiem_vu_giao_hang da ton tai - khong can doi schema.';
GO
