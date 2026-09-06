-- =================================================================================================
-- KỊCH BẢN DỮ LIỆU MẪU KIỂM THỬ US-54 (MODULE: PROMOTION - PHÂN HỆ SELLER)
-- ĐỀ TÀI: TẠO COMBO KHUYẾN MÃI & MUA KÈM DEAL SỐC (ADD-ON DEALS)
-- CHỨC NĂNG CỐT LÕI: Cấu hình chương trình "Mua sản phẩm chính A giảm giá 50% cho phụ kiện B"
-- CSDL: FlexShop_V2_Full (Hỗ trợ chuẩn Tiếng Việt Unicode N'')
-- =================================================================================================

USE [FlexShop_V2_Full];
GO

-- 1. TẠO BẢNG san_pham_combo_khuyen_mai (NẾU CHƯA CÓ)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'san_pham_combo_khuyen_mai')
BEGIN
    CREATE TABLE [dbo].[san_pham_combo_khuyen_mai](
        [ma_san_pham_combo] [bigint] IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [ma_combo] [bigint] NOT NULL,
        [ma_bien_the] [bigint] NOT NULL,
        [vai_tro] [nvarchar](30) NOT NULL, -- 'SAN_PHAM_CHINH' (A) hoặc 'MUA_KEM_DEAL_SOC' (B)
        [phan_tram_giam] [decimal](5, 2) NULL, -- Ví dụ: 50.00%
        [gia_uu_dai] [decimal](18, 2) NULL, -- Giá sau giảm
        [gioi_han_mua_kem_moi_don] [int] NULL DEFAULT 1, -- Mua tối đa X phụ kiện kèm 1 sản phẩm chính
        [so_luong_toi_da] [int] NULL DEFAULT 100, -- Tổng số suất mua kèm mở bán
        [so_luong_da_ban] [int] NULL DEFAULT 0,
        CONSTRAINT [FK_sp_combo_khuyen_mai] FOREIGN KEY([ma_combo]) REFERENCES [combo_khuyen_mai] ([ma_combo]) ON DELETE CASCADE,
        CONSTRAINT [FK_sp_combo_bien_the] FOREIGN KEY([ma_bien_the]) REFERENCES [bien_the_san_pham] ([ma_bien_the])
    );
    PRINT N'Đã tạo bảng san_pham_combo_khuyen_mai thành công!';
END
GO

-- 2. CẬP NHẬT CHUẨN TIẾNG VIỆT CÓ DẤU CHO SẢN PHẨM & PHỤ KIỆN
UPDATE san_pham 
SET ten_san_pham = N'Điện Thoại Thông Minh FlexPhone Ultra 5G 256GB',
    mo_ta_ngan = N'Điện thoại flagship cao cấp đỉnh cao công nghệ 2026',
    mo_ta_chi_tiet = N'Màn hình 6.8 inch Dynamic AMOLED 2X, Chip Snapdragon 8 Gen 4, Camera 200MP zoom 100x.'
WHERE ma_san_pham = 251 OR duong_dan_slug = 'dien-thoai-flexphone-ultra-5g-256gb';

UPDATE san_pham 
SET ten_san_pham = N'Củ Sạc Nhanh 65W GaN 3 Cổng Type-C An Toàn Chống Nổ',
    mo_ta_ngan = N'Công nghệ GaN III siêu nhỏ gọn, sạc siêu nhanh 65W đa thiết bị',
    mo_ta_chi_tiet = N'Hỗ trợ Power Delivery 3.0, Quick Charge 4.0, bảo vệ quá nhiệt quá dòng tuyệt đối.'
WHERE ma_san_pham = 252 OR duong_dan_slug = 'cu-sac-nhanh-65w-gan-3-cong-type-c';

UPDATE san_pham 
SET ten_san_pham = N'Cáp Sạc Nhanh Bọc Dù Type-C to Type-C 100W Dài 1.2M',
    mo_ta_ngan = N'Cáp sạc siêu bền bọc sợi Kevlar chống đứt gãy gập 30.000 lần',
    mo_ta_chi_tiet = N'Tích hợp chip E-Marker thông minh, công suất tải 100W 5A truyền dữ liệu siêu tốc 480Mbps.'
WHERE ma_san_pham = 253 OR duong_dan_slug = 'cap-sac-nhanh-boc-du-type-c-100w';

UPDATE bien_the_san_pham
SET ten_bien_the = N'Bản Titan Tự Nhiên - 256GB'
WHERE ma_sku = 'SKU-PHONE-TITAN-256' OR ma_bien_the = 6;

UPDATE bien_the_san_pham
SET ten_bien_the = N'Màu Trắng Băng Tuyết - 65W 3 Cổng'
WHERE ma_sku = 'SKU-SAC-GAN-65W' OR ma_bien_the = 7;

UPDATE bien_the_san_pham
SET ten_bien_the = N'Màu Đen Nhám - Dài 1.2M'
WHERE ma_sku = 'SKU-CAP-DU-100W' OR ma_bien_the = 8;
GO

-- 3. KHỞI TẠO HOẶC CẬP NHẬT CHƯƠNG TRÌNH DEAL SỐC & COMBO MẪU (US-54)
IF NOT EXISTS (SELECT 1 FROM combo_khuyen_mai WHERE ten_combo LIKE N'%FlexPhone%Giảm Giá 50% Phụ Kiện%')
BEGIN
    INSERT INTO combo_khuyen_mai (
        ma_gian_hang, ten_combo, loai_combo, so_luong_toi_thieu,
        gia_tri_giam, ngay_bat_dau, ngay_ket_thuc, dang_hoat_dong
    )
    VALUES (
        1, N'Deal Sốc Phụ Kiện: Mua Điện Thoại FlexPhone Giảm Giá 50% Phụ Kiện Cao Cấp',
        N'DEAL_SOC_MUA_KEM', 1, 50.00,
        DATEADD(day, -1, GETDATE()), DATEADD(day, 30, GETDATE()), 1
    );
END
ELSE
BEGIN
    UPDATE combo_khuyen_mai
    SET ten_combo = N'Deal Sốc Phụ Kiện: Mua Điện Thoại FlexPhone Giảm Giá 50% Phụ Kiện Cao Cấp'
    WHERE ten_combo LIKE N'%FlexPhone%Giảm Giá 50% Phụ Kiện%';
END
GO

-- 4. GÁN SẢN PHẨM CHÍNH A VÀ PHỤ KIỆN B GIẢM 50% VÀO COMBO
DECLARE @MaCombo BIGINT = (SELECT TOP 1 ma_combo FROM combo_khuyen_mai WHERE ten_combo LIKE N'%FlexPhone%Giảm Giá 50% Phụ Kiện%' ORDER BY ma_combo DESC);
DECLARE @MaBienTheA BIGINT = (SELECT TOP 1 ma_bien_the FROM bien_the_san_pham WHERE ma_sku IN ('SKU-PHONE-TITAN-256', 'SKU-FLX-ULTRA-TITAN') OR ten_bien_the LIKE N'%Titan%');
DECLARE @MaBienTheB1 BIGINT = (SELECT TOP 1 ma_bien_the FROM bien_the_san_pham WHERE ma_sku IN ('SKU-SAC-GAN-65W') OR ten_bien_the LIKE N'%65W%');
DECLARE @MaBienTheB2 BIGINT = (SELECT TOP 1 ma_bien_the FROM bien_the_san_pham WHERE ma_sku IN ('SKU-CAP-DU-100W', 'SKU-CAP-100W-DU') OR ten_bien_the LIKE N'%100W%');
DECLARE @MaBienTheB3 BIGINT = (SELECT TOP 1 ma_bien_the FROM bien_the_san_pham WHERE ma_sku = 'SKU-EAR-BLK-01' OR ma_bien_the = 1);

-- Làm sạch dữ liệu mapping cũ để nạp mới chuẩn xác
DELETE FROM san_pham_combo_khuyen_mai WHERE ma_combo = @MaCombo;

-- Gán Sản phẩm chính A: Điện thoại FlexPhone Ultra (Bán đúng giá niêm yết 15.000.000 đ)
IF @MaBienTheA IS NOT NULL
BEGIN
    INSERT INTO san_pham_combo_khuyen_mai (ma_combo, ma_bien_the, vai_tro, phan_tram_giam, gia_uu_dai, gioi_han_mua_kem_moi_don, so_luong_toi_da, so_luong_da_ban)
    VALUES (@MaCombo, @MaBienTheA, N'SAN_PHAM_CHINH', 0.00, 15000000.00, 1, 500, 12);
END

-- Gán Phụ kiện B1: Củ Sạc 65W GaN (Giá gốc 400.000đ -> Giảm 50% còn 200.000đ)
IF @MaBienTheB1 IS NOT NULL
BEGIN
    INSERT INTO san_pham_combo_khuyen_mai (ma_combo, ma_bien_the, vai_tro, phan_tram_giam, gia_uu_dai, gioi_han_mua_kem_moi_don, so_luong_toi_da, so_luong_da_ban)
    VALUES (@MaCombo, @MaBienTheB1, N'MUA_KEM_DEAL_SOC', 50.00, 200000.00, 1, 200, 15);
END

-- Gán Phụ kiện B2: Cáp Sạc 100W (Giá gốc 150.000đ -> Giảm 50% còn 75.000đ)
IF @MaBienTheB2 IS NOT NULL
BEGIN
    INSERT INTO san_pham_combo_khuyen_mai (ma_combo, ma_bien_the, vai_tro, phan_tram_giam, gia_uu_dai, gioi_han_mua_kem_moi_don, so_luong_toi_da, so_luong_da_ban)
    VALUES (@MaCombo, @MaBienTheB2, N'MUA_KEM_DEAL_SOC', 50.00, 75000.00, 2, 300, 22);
END

-- Gán Phụ kiện B3: Tai nghe chống ồn Sony (Giá gốc 1.250.000đ -> Giảm 50% còn 625.000đ)
IF @MaBienTheB3 IS NOT NULL
BEGIN
    INSERT INTO san_pham_combo_khuyen_mai (ma_combo, ma_bien_the, vai_tro, phan_tram_giam, gia_uu_dai, gioi_han_mua_kem_moi_don, so_luong_toi_da, so_luong_da_ban)
    VALUES (@MaCombo, @MaBienTheB3, N'MUA_KEM_DEAL_SOC', 50.00, 625000.00, 1, 100, 8);
END

-- 5. HIỂN THỊ KẾT QUẢ ĐÃ THIẾT LẬP
SELECT 
    spk.ma_san_pham_combo AS [Mã SP Combo],
    spk.vai_tro AS [Vai Trò],
    bt.ten_bien_the AS [Tên Mặt Hàng],
    bt.gia_ban AS [Giá Gốc Niêm Yết],
    spk.phan_tram_giam AS [Mức Giảm %],
    spk.gia_uu_dai AS [Giá Ưu Đãi Mua Kèm],
    (bt.gia_ban - spk.gia_uu_dai) AS [Số Tiền Tiết Kiệm]
FROM san_pham_combo_khuyen_mai spk
JOIN bien_the_san_pham bt ON spk.ma_bien_the = bt.ma_bien_the
WHERE spk.ma_combo = @MaCombo;
GO