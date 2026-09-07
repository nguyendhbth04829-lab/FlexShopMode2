-- ==============================================================================
-- FILE CÀI ĐẶT TOÀN BỘ DỮ LIỆU TEST CHO CÁC CHỨC NĂNG CỦA HƯNG
-- (Bao gồm: Tạo User/Shop mồi, Danh mục, Sản phẩm, Biến thể, Kho hàng, Tồn kho)
-- ==============================================================================

-- 1. TẠO DATA MỒI (USER & GIAN HÀNG) ĐỂ THỎA MÃN KHÓA NGOẠI
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all";
GO

IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE ma_nguoi_dung = 1)
BEGIN
    SET IDENTITY_INSERT nguoi_dung ON;
    INSERT INTO nguoi_dung (ma_nguoi_dung, email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (1, 'shoptest@gmail.com', '0987654321', '123456', N'Chủ Shop Test', N'HOAT_DONG', 0);
    SET IDENTITY_INSERT nguoi_dung OFF;
END
GO

IF NOT EXISTS (SELECT 1 FROM gian_hang WHERE ma_gian_hang = 1)
BEGIN
    SET IDENTITY_INSERT gian_hang ON;
    INSERT INTO gian_hang (ma_gian_hang, ma_chu_so_huu, ten_gian_hang, duong_dan_slug, dia_chi_kho, sdt_kho, trang_thai, hang_gian_hang, diem_sao_qua_ta)
    VALUES (1, 1, N'Gian Hàng FlexShop Test', 'gian-hang-flexshop-test', N'Hà Nội', '0987654321', N'DA_DUYET', N'CHUAN', 0);
    SET IDENTITY_INSERT gian_hang OFF;
END
GO

EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all";
GO

-- ==============================================================================
-- 2. TẠO DỮ LIỆU DANH MỤC (US-07)
-- ==============================================================================
IF NOT EXISTS (SELECT 1 FROM danh_muc WHERE ma_danh_muc = 1)
BEGIN
    SET IDENTITY_INSERT danh_muc ON;
    INSERT INTO danh_muc (ma_danh_muc, ten_danh_muc, duong_dan_slug, ma_danh_muc_cha, cap_do, dang_hoat_dong, da_xoa)
    VALUES (1, N'Thời trang Nam', 'thoi-trang-nam', NULL, 1, 1, 0);

    INSERT INTO danh_muc (ma_danh_muc, ten_danh_muc, duong_dan_slug, ma_danh_muc_cha, cap_do, dang_hoat_dong, da_xoa)
    VALUES (5, N'Áo thun Nam', 'ao-thun-nam', 1, 2, 1, 0);
    SET IDENTITY_INSERT danh_muc OFF;
END
GO

-- ==============================================================================
-- 3. TẠO DỮ LIỆU SẢN PHẨM & BIẾN THỂ SKU (US-12, US-13, US-14)
-- ==============================================================================
IF NOT EXISTS (SELECT 1 FROM thuong_hieu WHERE ma_thuong_hieu = 1)
BEGIN
    SET IDENTITY_INSERT thuong_hieu ON;
    INSERT INTO thuong_hieu (ma_thuong_hieu, ten_thuong_hieu, link_logo, dang_hoat_dong, da_xoa) 
    VALUES (1, N'Adidas', 'logo-adidas.png', 1, 0);
    SET IDENTITY_INSERT thuong_hieu OFF;
END
GO

IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 1)
BEGIN
    SET IDENTITY_INSERT san_pham ON;
    INSERT INTO san_pham (ma_san_pham, ma_gian_hang, ma_danh_muc, ma_thuong_hieu, ten_san_pham, duong_dan_slug, gia_co_ban, can_nang_gram, chieu_dai_cm, chieu_rong_cm, chieu_cao_cm, da_xoa)
    VALUES (1, 1, 5, 1, N'Áo thun thể thao Adidas', 'ao-thun-the-thao-adidas', 250000, 250, 15, 10, 5, 0);
    SET IDENTITY_INSERT san_pham OFF;

    INSERT INTO hinh_anh_san_pham (ma_san_pham, link_anh, la_anh_chinh, thu_tu_hien_thi)
    VALUES (1, 'ao-adidas-chinh.jpg', 1, 1),
           (1, 'ao-adidas-phu1.jpg', 0, 2);

    SET IDENTITY_INSERT bien_the_san_pham ON;
    INSERT INTO bien_the_san_pham (ma_bien_the, ma_san_pham, ma_sku, ten_bien_the, gia_ban, gia_goc, da_xoa)
    VALUES (1, 1, 'ADI-AOTHUN-DEN-M', N'Màu Đen - Size M', 250000, 300000, 0),
           (2, 1, 'ADI-AOTHUN-DEN-L', N'Màu Đen - Size L', 250000, 300000, 0);
    SET IDENTITY_INSERT bien_the_san_pham OFF;
END
GO

-- ==============================================================================
-- 4. TẠO DỮ LIỆU KHO HÀNG & TỒN KHO AN TOÀN (US-15, US-16)
-- ==============================================================================
IF NOT EXISTS (SELECT 1 FROM kho_hang WHERE ma_kho = 1)
BEGIN
    SET IDENTITY_INSERT kho_hang ON;
    INSERT INTO kho_hang (ma_kho, ma_gian_hang, ten_kho, dia_chi, tinh_thanh, sdt_lien_he, la_kho_chinh, dang_hoat_dong, da_xoa)
    VALUES (1, 1, N'Kho Tổng Hà Nội', N'Số 1 Đại Cồ Việt', N'Hà Nội', '0987654321', 1, 1, 0);
    SET IDENTITY_INSERT kho_hang OFF;
    
    SET IDENTITY_INSERT vi_tri_ke_kho ON;
    INSERT INTO vi_tri_ke_kho (ma_vi_tri, ma_kho, ma_khu_vuc) VALUES (1, 1, 'A1-01');
    INSERT INTO vi_tri_ke_kho (ma_vi_tri, ma_kho, ma_khu_vuc) VALUES (2, 1, 'A1-02');
    SET IDENTITY_INSERT vi_tri_ke_kho OFF;

    INSERT INTO ton_kho_chi_tiet (ma_kho, ma_vi_tri, ma_bien_the, so_luong_ton, so_luong_tam_giu, phien_ban_lock)
    VALUES (1, 1, 1, 50, 0, 1),
           (1, 2, 2, 5, 0, 1);
END
GO

PRINT N'CÀI ĐẶT THÀNH CÔNG TẤT CẢ DỮ LIỆU TEST CHO HƯNG!';
