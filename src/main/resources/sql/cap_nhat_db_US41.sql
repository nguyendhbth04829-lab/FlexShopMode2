-- ============================================================================
-- CAP NHAT DB CHO US-41: KHACH XAC NHAN DA NHAN -> HOAN_THANH
-- Khong doi schema (dung trang_thai don_hang_shop san co);
-- file nay de patch DB cu (idempotent) va ghi nhan quy tac.
-- Quy tac: don DA_GIAO (shipper POD) + dung chu don -> HOAN_THANH,
--   auto-ghi hanh trinh, mo danh gia (danh gia mo tu DA_GIAO).
-- ============================================================================

USE FlexShop_V2_Full;
GO

IF OBJECT_ID(N'don_hang_shop', N'U') IS NULL
    PRINT N'[US-41] THIEU bang don_hang_shop!';
ELSE
    PRINT N'[US-41] Bang don_hang_shop da ton tai - khong can doi schema.';
GO
