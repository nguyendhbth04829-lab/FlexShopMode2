-- ============================================================================
-- CAP NHAT DB CHO US-33: TRAM TRUNG CHUYEN HUB + LICH SU HANH TRINH DON
-- Chay 1 lan tren DB cu de bo sung 2 bang neu thieu.
-- (Bang da co trong db_v2_setup.sql ban moi; file nay de patch DB cu.)
-- ============================================================================

USE FlexShop_V2_Full;
GO

IF OBJECT_ID(N'tram_trung_chuyen_hub', N'U') IS NULL
BEGIN
    CREATE TABLE tram_trung_chuyen_hub (
        ma_hub BIGINT IDENTITY(1,1) PRIMARY KEY,
        ten_hub NVARCHAR(150) NOT NULL,
        dia_chi NVARCHAR(255) NOT NULL,
        tinh_thanh NVARCHAR(100) NOT NULL,
        suc_chua_kien_hang INT DEFAULT 50000
    );
    PRINT N'[US-33] Da tao bang tram_trung_chuyen_hub.';
END
ELSE
    PRINT N'[US-33] Bang tram_trung_chuyen_hub da ton tai.';
GO

IF OBJECT_ID(N'lich_su_hanh_trinh_don', N'U') IS NULL
BEGIN
    CREATE TABLE lich_su_hanh_trinh_don (
        ma_hanh_trinh BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_don_hang_shop BIGINT NOT NULL,
        ma_hub BIGINT NULL,
        tieu_de_moc NVARCHAR(150) NOT NULL,
        vi_tri_hien_tai NVARCHAR(255) NULL,
        thoi_gian DATETIME2 DEFAULT GETDATE(),
        FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop) ON DELETE CASCADE,
        FOREIGN KEY (ma_hub) REFERENCES tram_trung_chuyen_hub(ma_hub)
    );
    PRINT N'[US-33] Da tao bang lich_su_hanh_trinh_don.';
END
ELSE
    PRINT N'[US-33] Bang lich_su_hanh_trinh_don da ton tai.';
GO
