-- =========================================================================================
-- KỊCH BẢN NẠP DỮ LIỆU MẪU KIỂM THỬ US-52 (MODULE: VOUCHER)
-- Khách hàng áp dụng đồng thời: Mã Freeship Sàn + Voucher Sàn + Voucher Shop
-- Thuật toán Voucher lồng nhau (Stackable Vouchers) phân bổ chính xác nguồn tiền tài trợ
-- Đảm bảo chuẩn UTF-8, Unicode tiếng Việt với tiền tố N'...'
-- =========================================================================================

USE [FlexShop_V2_Full];
GO

-- 1. XÓA DỮ LIỆU KIỂM THỬ CŨ CỦA US-52 NẾU CÓ ĐỂ ĐẢM BẢO TÍNH ĐỘC LẬP
DELETE FROM [lich_su_dung_ma_giam_gia] 
WHERE [ma_voucher] IN (
    SELECT [ma_voucher] FROM [ma_giam_gia] 
    WHERE [ma_code_voucher] IN ('FREESHIP30K', 'FREESHIP50K', 'FLEXSAN100K', 'FLEXSAN10PCT', 'TECHZONE50K', 'FASHION20K', 'HETHAN100K', 'HETLUOT50K', 'DONCAO2TR')
);

DELETE FROM [ma_giam_gia] 
WHERE [ma_code_voucher] IN ('FREESHIP30K', 'FREESHIP50K', 'FLEXSAN100K', 'FLEXSAN10PCT', 'TECHZONE50K', 'FASHION20K', 'HETHAN100K', 'HETLUOT50K', 'DONCAO2TR');
GO

-- 2. NẠP CÁC VOUCHER MẪU CHO THUẬT TOÁN 3 TẦNG (STACKABLE VOUCHERS)

-- =========================================================================================
-- TẦNG 3: MÃ FREESHIP SÀN (Platform Shipping Voucher - ma_gian_hang IS NULL, loai_voucher = 'FREESHIP')
-- =========================================================================================
INSERT INTO [ma_giam_gia] (
    [ma_gian_hang], [ma_code_voucher], [ten_voucher], [loai_voucher],
    [gia_tri_giam], [giam_toi_da], [gia_tri_don_toi_thieu],
    [tong_so_luong_phat_hanh], [gioi_han_moi_nguoi], [so_luong_da_dung],
    [ngay_bat_dau], [ngay_ket_thuc], [dang_hoat_dong], [da_xoa], [phien_ban_lock]
) VALUES 
(
    NULL, 'FREESHIP30K', N'Freeship Sàn Toàn Quốc Giảm 30K', 'FREESHIP',
    30000.00, 30000.00, 200000.00,
    500, 5, 0,
    DATEADD(day, -30, SYSDATETIME()), DATEADD(day, 60, SYSDATETIME()), 1, 0, 0
),
(
    NULL, 'FREESHIP50K', N'Freeship Sàn Đơn Lớn Giảm 50K', 'FREESHIP',
    50000.00, 50000.00, 500000.00,
    200, 3, 0,
    DATEADD(day, -30, SYSDATETIME()), DATEADD(day, 60, SYSDATETIME()), 1, 0, 0
);

-- =========================================================================================
-- TẦNG 2: VOUCHER SÀN FLEXSHOP (Platform Voucher - ma_gian_hang IS NULL, loai_voucher != 'FREESHIP')
-- =========================================================================================
INSERT INTO [ma_giam_gia] (
    [ma_gian_hang], [ma_code_voucher], [ten_voucher], [loai_voucher],
    [gia_tri_giam], [giam_toi_da], [gia_tri_don_toi_thieu],
    [tong_so_luong_phat_hanh], [gioi_han_moi_nguoi], [so_luong_da_dung],
    [ngay_bat_dau], [ngay_ket_thuc], [dang_hoat_dong], [da_xoa], [phien_ban_lock]
) VALUES 
(
    NULL, 'FLEXSAN100K', N'Đại Tiệc Sale FlexShop Giảm Trực Tiếp 100K', 'GIAM_GIA',
    100000.00, 100000.00, 800000.00,
    100, 2, 0,
    DATEADD(day, -30, SYSDATETIME()), DATEADD(day, 60, SYSDATETIME()), 1, 0, 0
),
(
    NULL, 'FLEXSAN10PCT', N'Voucher Sàn FlexShop Giảm 10% Tối Đa 70K', 'PHAN_TRAM',
    10.00, 70000.00, 400000.00,
    300, 3, 0,
    DATEADD(day, -30, SYSDATETIME()), DATEADD(day, 60, SYSDATETIME()), 1, 0, 0
);

-- =========================================================================================
-- TẦNG 1: VOUCHER GIAN HÀNG (Shop Voucher - ma_gian_hang IS NOT NULL)
-- =========================================================================================
INSERT INTO [ma_giam_gia] (
    [ma_gian_hang], [ma_code_voucher], [ten_voucher], [loai_voucher],
    [gia_tri_giam], [giam_toi_da], [gia_tri_don_toi_thieu],
    [tong_so_luong_phat_hanh], [gioi_han_moi_nguoi], [so_luong_da_dung],
    [ngay_bat_dau], [ngay_ket_thuc], [dang_hoat_dong], [da_xoa], [phien_ban_lock]
) VALUES 
(
    1, 'TECHZONE50K', N'Voucher TechZone Flagship Tri Ân Giảm 50K', 'GIAM_GIA',
    50000.00, 50000.00, 300000.00,
    50, 2, 0,
    DATEADD(day, -30, SYSDATETIME()), DATEADD(day, 60, SYSDATETIME()), 1, 0, 0
),
(
    2, 'FASHION20K', N'Voucher Flex Fashion Đón Hè Giảm 20K', 'GIAM_GIA',
    20000.00, 20000.00, 150000.00,
    100, 3, 0,
    DATEADD(day, -30, SYSDATETIME()), DATEADD(day, 60, SYSDATETIME()), 1, 0, 0
);

-- =========================================================================================
-- CÁC MÃ DÙNG ĐỂ KIỂM THỬ VALIDATION KHẮT KHE
-- =========================================================================================
-- Mã 1: Đã hết hạn sử dụng (Hạn kết thúc cách đây 5 ngày)
INSERT INTO [ma_giam_gia] (
    [ma_gian_hang], [ma_code_voucher], [ten_voucher], [loai_voucher],
    [gia_tri_giam], [giam_toi_da], [gia_tri_don_toi_thieu],
    [tong_so_luong_phat_hanh], [gioi_han_moi_nguoi], [so_luong_da_dung],
    [ngay_bat_dau], [ngay_ket_thuc], [dang_hoat_dong], [da_xoa], [phien_ban_lock]
) VALUES 
(
    NULL, 'HETHAN100K', N'Mã Siêu Sale Đã Hết Hạn Giảm 100K', 'GIAM_GIA',
    100000.00, 100000.00, 200000.00,
    100, 2, 0,
    DATEADD(day, -60, SYSDATETIME()), DATEADD(day, -5, SYSDATETIME()), 1, 0, 0
);

-- Mã 2: Đã hết số lượng phát hành trên hệ thống (so_luong_da_dung = tong_so_luong_phat_hanh)
INSERT INTO [ma_giam_gia] (
    [ma_gian_hang], [ma_code_voucher], [ten_voucher], [loai_voucher],
    [gia_tri_giam], [giam_toi_da], [gia_tri_don_toi_thieu],
    [tong_so_luong_phat_hanh], [gioi_han_moi_nguoi], [so_luong_da_dung],
    [ngay_bat_dau], [ngay_ket_thuc], [dang_hoat_dong], [da_xoa], [phien_ban_lock]
) VALUES 
(
    NULL, 'HETLUOT50K', N'Mã Sàn Giới Hạn Đã Hết Lượt Giảm 50K', 'GIAM_GIA',
    50000.00, 50000.00, 200000.00,
    10, 1, 10,
    DATEADD(day, -10, SYSDATETIME()), DATEADD(day, 30, SYSDATETIME()), 1, 0, 0
);

-- Mã 3: Đơn tối thiểu cao (Yêu cầu đơn từ 2.000.000đ)
INSERT INTO [ma_giam_gia] (
    [ma_gian_hang], [ma_code_voucher], [ten_voucher], [loai_voucher],
    [gia_tri_giam], [giam_toi_da], [gia_tri_don_toi_thieu],
    [tong_so_luong_phat_hanh], [gioi_han_moi_nguoi], [so_luong_da_dung],
    [ngay_bat_dau], [ngay_ket_thuc], [dang_hoat_dong], [da_xoa], [phien_ban_lock]
) VALUES 
(
    NULL, 'DONCAO2TR', N'Mã Giảm 200K Cho Đơn Tối Thiểu 2 Triệu', 'GIAM_GIA',
    200000.00, 200000.00, 2000000.00,
    50, 1, 0,
    DATEADD(day, -10, SYSDATETIME()), DATEADD(day, 30, SYSDATETIME()), 1, 0, 0
);
GO

-- =========================================================================================
-- 3. TẠO ĐƠN HÀNG MẪU ĐA GIAN HÀNG ĐỂ KIỂM THỬ THUẬT TOÁN 3 TẦNG (NẾU CHƯA CÓ)
-- =========================================================================================
IF NOT EXISTS (SELECT 1 FROM [don_hang_tong] WHERE [ma_code_don_tong] = 'MASTER-US52-STACKABLE')
BEGIN
    INSERT INTO [don_hang_tong] (
        [ma_code_don_tong], [ma_khach_hang], [ma_dia_chi_giao],
        [tong_tien_hang], [tong_phi_van_chuyen], [tong_tien_thue_vat],
        [tong_giam_gia_san], [tong_giam_gia_shop], [tong_thanh_toan_cuoi],
        [phuong_thuc_thanh_toan], [trang_thai_thanh_toan], [trang_thai_don_hang],
        [ghi_chu], [ngay_tao]
    ) VALUES (
        'MASTER-US52-STACKABLE', 4, 1,
        1410000.00, 55000.00, 0.00,
        0.00, 0.00, 1465000.00,
        N'COD', N'CHUA_THANH_TOAN', N'CHO_XU_LY',
        N'Đơn hàng mẫu đa gian hàng kiểm thử US-52 Stackable Vouchers', SYSDATETIME()
    );

    DECLARE @NewDonTongId bigint = SCOPE_IDENTITY();

    -- Đơn hàng con Shop 1 (TechZone: 850.000đ hàng + 30.000đ ship)
    INSERT INTO [don_hang_shop] (
        [ma_code_don_shop], [ma_don_hang_tong], [ma_gian_hang],
        [tien_hang_shop], [phi_van_chuyen], [tien_thue_vat],
        [giam_gia_voucher_shop], [giam_gia_voucher_san], [tong_tien_shop_nhan],
        [trang_thai], [ngay_tao]
    ) VALUES (
        'SHOP-US52-TECHZONE-01', @NewDonTongId, 1,
        850000.00, 30000.00, 0.00,
        0.00, 0.00, 850000.00,
        N'CHO_XAC_NHAN', SYSDATETIME()
    );

    -- Đơn hàng con Shop 2 (Flex Fashion: 560.000đ hàng + 25.000đ ship)
    INSERT INTO [don_hang_shop] (
        [ma_code_don_shop], [ma_don_hang_tong], [ma_gian_hang],
        [tien_hang_shop], [phi_van_chuyen], [tien_thue_vat],
        [giam_gia_voucher_shop], [giam_gia_voucher_san], [tong_tien_shop_nhan],
        [trang_thai], [ngay_tao]
    ) VALUES (
        'SHOP-US52-FASHION-02', @NewDonTongId, 2,
        560000.00, 25000.00, 0.00,
        0.00, 0.00, 560000.00,
        N'CHO_XAC_NHAN', SYSDATETIME()
    );
END
GO

-- 4. KIỂM TRA LẠI DỮ LIỆU ĐÃ NẠP
SELECT [ma_voucher], [ma_code_voucher], [ten_voucher], [loai_voucher], [gia_tri_giam], [gia_tri_don_toi_thieu], [ma_gian_hang] 
FROM [ma_giam_gia];

SELECT [ma_don_hang_tong], [ma_code_don_tong], [tong_tien_hang], [tong_phi_van_chuyen], [tong_thanh_toan_cuoi] 
FROM [don_hang_tong] 
WHERE [ma_code_don_tong] = 'MASTER-US52-STACKABLE';
GO
