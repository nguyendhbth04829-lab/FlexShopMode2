-- ============================================================================
-- CAP NHAT DB CHO US-34: SHIPPER BAT/TAT TRANG THAI LAM VIEC
-- Bang tai_xe_giao_hang da co trong db_v2_setup.sql ban moi;
-- file nay de patch DB cu (idempotent).
-- Quy tac: shipper chi nhan don khi dang_truc_tuyen=1
--   VA trang_thai=DANG_HOAT_DONG; GPS vi do [-90,90], kinh do [-180,180].
-- ============================================================================

USE FlexShop_V2_Full;
GO

IF OBJECT_ID(N'tai_xe_giao_hang', N'U') IS NULL
BEGIN
    CREATE TABLE tai_xe_giao_hang (
        ma_tai_xe BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_nguoi_dung BIGINT NOT NULL UNIQUE,
        loai_phuong_tien NVARCHAR(50) DEFAULT N'XE_MAY',
        bien_so_xe NVARCHAR(30) NOT NULL,
        dang_truc_tuyen BIT DEFAULT 0,
        vi_do_hien_tai DECIMAL(10,8) NULL,
        kinh_do_hien_tai DECIMAL(11,8) NULL,
        so_du_cod_dang_giu DECIMAL(18,2) DEFAULT 0.00,
        diem_danh_gia_tb DECIMAL(3,2) DEFAULT 5.00,
        trang_thai NVARCHAR(30) DEFAULT N'DANG_HOAT_DONG',
        FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
    );
    PRINT N'[US-34] Da tao bang tai_xe_giao_hang.';
END
ELSE
    PRINT N'[US-34] Bang tai_xe_giao_hang da ton tai.';
GO
