-- ============================================================================
-- CAP NHAT DB CHO US-37: XAC NHAN GIAO THANH CONG (POD + COD)
-- Khong doi schema (dung cot link_anh_bang_chung_pod, da_thu_cod,
--   vi_do/kinh_do_giao_hang, so_du_cod_dang_giu san co);
-- file nay de patch DB cu (idempotent) va ghi nhan quy tac.
-- Quy tac: anh POD bat buoc; don COD -> da_thu_cod=1
--   + cong don so_du_cod_dang_giu; don -> DA_GIAO.
-- ============================================================================

USE FlexShop_V2_Full;
GO

IF OBJECT_ID(N'nhiem_vu_giao_hang', N'U') IS NULL
    PRINT N'[US-37] THIEU bang nhiem_vu_giao_hang! Hay chay cap_nhat_db_US35.sql truoc.';
ELSE
    PRINT N'[US-37] Bang nhiem_vu_giao_hang da ton tai - khong can doi schema.';
GO
