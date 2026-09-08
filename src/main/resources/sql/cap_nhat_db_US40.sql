-- ============================================================================
-- CAP NHAT DB CHO US-40: TAO YEU CAU CHUYEN HOAN KHI THAT BAI 3 LAN
-- Bang yeu_cau_chuyen_hoan da co trong db_v2_setup.sql ban moi;
-- file nay de patch DB cu (idempotent).
-- Quy tac: so_lan_giao=3 + that bai -> tu dong tao yeu cau DANG_CHUYEN_HOAN,
--   nhiem vu -> CHUYEN_HOAN, don shop -> GIAO_THAT_BAI, sinh ma van don tra.
-- ============================================================================

USE FlexShop_V2_Full;
GO

IF OBJECT_ID(N'yeu_cau_chuyen_hoan', N'U') IS NULL
BEGIN
    CREATE TABLE yeu_cau_chuyen_hoan (
        ma_chuyen_hoan BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_don_hang_shop BIGINT NOT NULL UNIQUE,
        ly_do_chuyen_hoan NVARCHAR(255) NOT NULL,
        ma_van_don_tra_hang NVARCHAR(100) NULL,
        trang_thai NVARCHAR(30) DEFAULT N'DANG_CHUYEN_HOAN',
        ngay_tao DATETIME2 DEFAULT GETDATE(),
        FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop)
    );
    PRINT N'[US-40] Da tao bang yeu_cau_chuyen_hoan.';
END
ELSE
    PRINT N'[US-40] Bang yeu_cau_chuyen_hoan da ton tai.';
GO
