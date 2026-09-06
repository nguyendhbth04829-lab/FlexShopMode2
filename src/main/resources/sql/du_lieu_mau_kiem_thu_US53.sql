-- =========================================================================================
-- KỊCH BẢN NẠP DỮ LIỆU MẪU KIỂM THỬ US-53 (MODULE: PROMOTION)
-- Admin / Seller tạo chương trình Flash Sale theo Khung giờ (0h, 12h, 21h)
-- Thiết lập khung giờ, giá giảm sốc, giới hạn số lượng và đếm ngược thời gian
-- Đảm bảo chuẩn UTF-8, Unicode tiếng Việt với tiền tố N'...'
-- =========================================================================================

USE [FlexShop_V2_Full];
GO

-- 1. XÓA DỮ LIỆU KIỂM THỬ CŨ NẾU CÓ
DELETE FROM [san_pham_flash_sale];
DELETE FROM [khung_gio_flash_sale];
GO

-- 2. NẠP CÁC KHUNG GIỜ FLASH SALE CHUẨN TRONG NGÀY (0H, 12H, 21H VÀ KHUNG GIỜ ĐANG DIỄN RA)

-- Khung 1: Flash Sale 0h - Nửa Đêm Săn Deal Cháy Phố (00:00 - 02:00)
INSERT INTO [khung_gio_flash_sale] (
    [tieu_de], [link_banner], [thoi_gian_bat_dau], [thoi_gian_ket_thuc], [trang_thai]
) VALUES (
    N'⚡ Flash Sale 0h - Nửa Đêm Săn Deal Cháy Phố',
    N'/images/banner-flashsale-0h.jpg',
    DATEADD(hour, 0, CAST(CAST(GETDATE() AS date) AS datetime2)),
    DATEADD(hour, 2, CAST(CAST(GETDATE() AS date) AS datetime2)),
    N'DA_KET_THUC'
);

-- Khung 2: Flash Sale 12h - Nghỉ Trưa Deal Sốc Chớp Nhoáng (12:00 - 14:00)
INSERT INTO [khung_gio_flash_sale] (
    [tieu_de], [link_banner], [thoi_gian_bat_dau], [thoi_gian_ket_thuc], [trang_thai]
) VALUES (
    N'⚡ Flash Sale 12h - Nghỉ Trưa Deal Sốc Chớp Nhoáng',
    N'/images/banner-flashsale-12h.jpg',
    DATEADD(hour, 12, CAST(CAST(GETDATE() AS date) AS datetime2)),
    DATEADD(hour, 14, CAST(CAST(GETDATE() AS date) AS datetime2)),
    N'SAP_DIEN_RA'
);

-- Khung 3: Flash Sale 21h - Giờ Vàng Xả Kho Giá Rẻ Vô Địch (21:00 - 23:59)
INSERT INTO [khung_gio_flash_sale] (
    [tieu_de], [link_banner], [thoi_gian_bat_dau], [thoi_gian_ket_thuc], [trang_thai]
) VALUES (
    N'⚡ Flash Sale 21h - Giờ Vàng Xả Kho Giá Rẻ Vô Địch',
    N'/images/banner-flashsale-21h.jpg',
    DATEADD(hour, 21, CAST(CAST(GETDATE() AS date) AS datetime2)),
    DATEADD(second, 86399, CAST(CAST(GETDATE() AS date) AS datetime2)),
    N'SAP_DIEN_RA'
);

-- Khung 4: Khung Giờ Đang Diễn Ra Trực Tiếp (Bắt đầu cách đây 1 giờ, kết thúc sau 3 giờ)
-- Đảm bảo luôn có khung giờ ĐANG DIỄN RA để kiểm tra Countdown Timer chạy thời gian thực!
INSERT INTO [khung_gio_flash_sale] (
    [tieu_de], [link_banner], [thoi_gian_bat_dau], [thoi_gian_ket_thuc], [trang_thai]
) VALUES (
    N'🔥 Flash Sale Trực Tiếp - Đại Tiệc Deal Giờ Vàng Đang Diễn Ra',
    N'/images/banner-flashsale-live.jpg',
    DATEADD(hour, -1, SYSDATETIME()),
    DATEADD(hour, 3, SYSDATETIME()),
    N'DANG_DIEN_RA'
);
GO

-- 3. NẠP CÁC SẢN PHẨM GIẢM SỐC VÀO KHUNG GIỜ ĐANG DIỄN RA
DECLARE @MaKhungLive BIGINT = (SELECT TOP 1 [ma_flash_sale] FROM [khung_gio_flash_sale] WHERE [trang_thai] = N'DANG_DIEN_RA');

-- Sản phẩm 1: Tai nghe Sony WH-1000XM5 (Biến thể 1, Giá gốc 1.250.000đ -> Giảm sốc còn 799.000đ, Giảm -36%)
INSERT INTO [san_pham_flash_sale] (
    [ma_flash_sale], [ma_bien_the], [gia_flash_sale],
    [so_luong_gioi_han], [so_luong_da_ban], [gioi_han_mua_moi_khach], [phien_ban_lock]
) VALUES (
    @MaKhungLive, 1, 799000.00,
    50, 18, 1, 0
);

-- Sản phẩm 2: Áo Sơ Mi Nam Oxford (Biến thể 2, Giá gốc 280.000đ -> Giảm sốc còn 149.000đ, Giảm -47%)
INSERT INTO [san_pham_flash_sale] (
    [ma_flash_sale], [ma_bien_the], [gia_flash_sale],
    [so_luong_gioi_han], [so_luong_da_ban], [gioi_han_mua_moi_khach], [phien_ban_lock]
) VALUES (
    @MaKhungLive, 2, 149000.00,
    100, 65, 2, 0
);
GO

-- 4. KIỂM TRA LẠI DỮ LIỆU ĐÃ NẠP
SELECT [ma_flash_sale], [tieu_de], [thoi_gian_bat_dau], [thoi_gian_ket_thuc], [trang_thai]
FROM [khung_gio_flash_sale];

SELECT s.[ma_san_pham_fs], s.[ma_flash_sale], b.[ten_bien_the], b.[gia_ban] AS [gia_goc], s.[gia_flash_sale], s.[so_luong_da_ban], s.[so_luong_gioi_han]
FROM [san_pham_flash_sale] s
JOIN [bien_the_san_pham] b ON s.[ma_bien_the] = b.[ma_bien_the];
GO
