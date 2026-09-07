-- ============================================================================
-- CAP NHAT DB CHO US-35: NHAN CUOC GIAO (NHIEM VU GIAO HANG)
-- Bang nhiem_vu_giao_hang da co trong db_v2_setup.sql ban moi;
-- file nay de patch DB cu (idempotent).
-- Quy tac: don DA_XAC_NHAN + chua ai nhan + shipper Online/DANG_HOAT_DONG.
-- ============================================================================

USE FlexShop_V2_Full;
GO

IF OBJECT_ID(N'nhiem_vu_giao_hang', N'U') IS NULL
BEGIN
    CREATE TABLE nhiem_vu_giao_hang (
        ma_nhiem_vu BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_don_hang_shop BIGINT NOT NULL,
        ma_tai_xe BIGINT NOT NULL,
        loai_nhiem_vu NVARCHAR(30) DEFAULT N'GIAO_HANG',
        trang_thai NVARCHAR(30) DEFAULT N'DA_PHAN_CONG',
        tien_cod_can_thu DECIMAL(18,2) DEFAULT 0.00,
        da_thu_cod BIT DEFAULT 0,
        link_anh_bang_chung_pod NVARCHAR(500) NULL,
        vi_do_giao_hang DECIMAL(10,8) NULL,
        kinh_do_giao_hang DECIMAL(11,8) NULL,
        ma_otp_xac_nhan NVARCHAR(10) NULL,
        ly_do_that_bai NVARCHAR(255) NULL,
        so_lan_giao INT DEFAULT 1,
        thoi_gian_hen_giao_lai DATETIME2 NULL,
        thoi_gian_lay_hang DATETIME2 NULL,
        thoi_gian_giao_thanh_cong DATETIME2 NULL,
        ngay_tao DATETIME2 DEFAULT GETDATE(),
        FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop),
        FOREIGN KEY (ma_tai_xe) REFERENCES tai_xe_giao_hang(ma_tai_xe)
    );
    PRINT N'[US-35] Da tao bang nhiem_vu_giao_hang.';
END
ELSE
    PRINT N'[US-35] Bang nhiem_vu_giao_hang da ton tai.';
GO
