-- ============================================================================
-- KICH BAN DU LIEU MAU KIEM THU US-34: TAI KHOAN SHIPPER TEST MOBILE APP
-- Database: FlexShop_V2_Full
-- Ngay tao: 2026-09-08
-- Muc dich: tao 2 shipper (1 DANG_HOAT_DONG offline, 1 DANG_HOAT_DONG online)
--   de test bat/tat trang thai + validate GPS tren giao dien mobile.
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BAT DAU DONG BO DU LIEU MAU CHO US-34 (SHIPPER) ===';

IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'shipper1@flexshop.vn')
    INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (N'shipper1@flexshop.vn', N'0909111222', N'$2a$10$7EqJtq98hPqEX7fNZaFWoO3jS8e1s89P1dJ9q6V8J3v8v8k9v7v9m', N'Nguyen Van Shipper 1', N'HOAT_DONG', 0);
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'shipper2@flexshop.vn')
    INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (N'shipper2@flexshop.vn', N'0909333444', N'$2a$10$7EqJtq98hPqEX7fNZaFWoO3jS8e1s89P1dJ9q6V8J3v8v8k9v7v9m', N'Tran Thi Shipper 2', N'HOAT_DONG', 0);

DECLARE @Nd1 BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'shipper1@flexshop.vn');
DECLARE @Nd2 BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = N'shipper2@flexshop.vn');

IF @Nd1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM tai_xe_giao_hang WHERE ma_nguoi_dung = @Nd1)
    INSERT INTO tai_xe_giao_hang (ma_nguoi_dung, loai_phuong_tien, bien_so_xe, dang_truc_tuyen, vi_do_hien_tai, kinh_do_hien_tai, so_du_cod_dang_giu, diem_danh_gia_tb, trang_thai)
    VALUES (@Nd1, N'XE_MAY', N'29X1-11223', 0, 21.027800, 105.834200, 0.00, 5.00, N'DANG_HOAT_DONG');

IF @Nd2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM tai_xe_giao_hang WHERE ma_nguoi_dung = @Nd2)
    INSERT INTO tai_xe_giao_hang (ma_nguoi_dung, loai_phuong_tien, bien_so_xe, dang_truc_tuyen, vi_do_hien_tai, kinh_do_hien_tai, so_du_cod_dang_giu, diem_danh_gia_tb, trang_thai)
    VALUES (@Nd2, N'XE_MAY', N'30Y2-44556', 1, 21.028500, 105.854200, 0.00, 4.80, N'DANG_HOAT_DONG');

PRINT N'[US-34] Dong bo xong 2 shipper test (1 offline, 1 online).';
GO
PRINT N'=== KET THUC US-34 ===';
