 c-- ============================================================================
-- CAP NHAT DB CHO US-39: DASHBOARD LICH SU + THONG KE COD DOI SOAT
-- Khong doi schema (tong hop tu nhiem_vu_giao_hang + tai_xe_giao_hang);
-- file nay de patch DB cu (idempotent) va ghi nhan quy tac.
-- Quy tac: thanh cong/that bai theo nhiem vu; COD dang giu = so_du_cod_dang_giu.
-- ============================================================================

USE FlexShop_V2_Full;
GO

IF OBJECT_ID(N'nhiem_vu_giao_hang', N'U') IS NULL
    PRINT N'[US-39] THIEU bang nhiem_vu_giao_hang! Hay chay cap_nhat_db_US35.sql truoc.';
ELSE
    PRINT N'[US-39] Bang nhiem_vu_giao_hang da ton tai - khong can doi schema.';
GO
