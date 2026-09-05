-- ==============================================================================
-- KỊCH BẢN NẠP DỮ LIỆU MẪU KIỂM THỬ US-50: SELLER XEM VÀ PHẢN HỒI ĐÁNH GIÁ
-- Module: ENGAGE (Kênh người bán - Quản lý phản hồi công khai đánh giá của khách)
-- Hệ quản trị CSDL: Microsoft SQL Server (FlexShop_V2_Full) - Chuẩn Unicode Tiếng Việt
-- ==============================================================================

USE FlexShop_V2_Full;
GO

-- 1. LẤY HOẶC TẠO THÔNG TIN NGƯỜI BÁN & KHÁCH HÀNG
DECLARE @maSellerTechZone BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'techzone@flexshop.vn');
DECLARE @maGianHang1 BIGINT = 1; -- TechZone Flagship Store

DECLARE @maUserBich BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'tranthib@flexshop.vn');
DECLARE @maUserCuong BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'levanc@flexshop.vn');
DECLARE @maUserLinh BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'thuylinh@gmail.com');
DECLARE @maUserAn BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');

-- 2. TẠO ĐƠN TỔNG & ĐƠN HÀNG SHOP BỔ SUNG NẾU CẦN
DECLARE @maDonTongAn BIGINT = (SELECT TOP 1 ma_don_hang_tong FROM don_hang_tong WHERE ma_khach_hang = @maUserAn);

-- Đơn hàng 201 cho Nguyễn Văn An (Trạng thái DA_GIAO)
IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-2026-US50-201')
BEGIN
    INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tien_thue_vat, giam_gia_voucher_shop, giam_gia_voucher_san, tong_tien_shop_nhan, trang_thai, ma_van_don, ngay_tao)
    VALUES (N'SHOP-TECH-2026-US50-201', @maDonTongAn, @maGianHang1, 1250000.00, 30000.00, 0, 0, 0, 1250000.00, N'DA_GIAO', N'GHN-US50-201', DATEADD(DAY, -1, GETDATE()));
END
DECLARE @maDon201 BIGINT = (SELECT ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-2026-US50-201');

IF @maDon201 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon201)
BEGIN
    INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
    VALUES (@maDon201, 1, N'Tai nghe không dây Bluetooth chống ồn Sony WH-1000XM5', N'Màu Đen Nhám - Bluetooth 5.3', N'SONY-WH1000XM5-BLK', 1250000.00, 1, 1250000.00);
END
DECLARE @ct201 BIGINT = (SELECT TOP 1 ma_chi_tiet_don FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon201);

-- 3. CHUẨN HÓA DỮ LIỆU ĐÁNH GIÁ CỦA GIAN HÀNG TECHZONE ĐỂ TEST TOÀN DIỆN CÁC TRẠNG THÁI
-- Đánh giá A: 5 sao - ĐÃ CÓ PHẢN HỒI (Trần Thị Bích) -> Phục vụ test chức năng "Chỉnh sửa phản hồi"
DECLARE @ct101 BIGINT = (SELECT TOP 1 ma_chi_tiet_don FROM chi_tiet_don_hang WHERE ten_san_pham LIKE N'%Sony WH-1000XM5%' AND ma_chi_tiet_don <> @ct201);
IF @ct101 IS NOT NULL
BEGIN
    UPDATE danh_gia_san_pham
    SET phan_hoi_cua_shop = N'TechZone cảm ơn bạn Bích đã tin tưởng và đánh giá 5 sao cho sản phẩm tai nghe chống ồn Sony!',
        ngay_shop_phan_hoi = DATEADD(HOUR, -5, GETDATE())
    WHERE ma_chi_tiet_don = @ct101;
END

-- Đánh giá B: 4 sao - CHƯA CÓ PHẢN HỒI (Lê Văn Cường) -> Phục vụ test chức năng "Viết phản hồi mới"
DECLARE @ct102 BIGINT = (SELECT TOP 1 ma_chi_tiet_don FROM chi_tiet_don_hang WHERE ten_san_pham LIKE N'%Sony WH-1000XM5%' AND ma_chi_tiet_don NOT IN (@ct201, ISNULL(@ct101, 0)));
IF @ct102 IS NOT NULL
BEGIN
    UPDATE danh_gia_san_pham
    SET phan_hoi_cua_shop = NULL,
        ngay_shop_phan_hoi = NULL
    WHERE ma_chi_tiet_don = @ct102;
END

-- Đánh giá C: Đánh giá mới 5 sao - CHƯA PHẢN HỒI của khách hàng Nguyễn Văn An
IF @ct201 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM danh_gia_san_pham WHERE ma_chi_tiet_don = @ct201)
BEGIN
    INSERT INTO danh_gia_san_pham (ma_chi_tiet_don, ma_san_pham, ma_gian_hang, ma_nguoi_dung, so_sao, noi_dung, an_danh, bi_an, phan_hoi_cua_shop, ngay_shop_phan_hoi, ngay_tao)
    VALUES (@ct201, 1, @maGianHang1, @maUserAn, 5, 
            N'Sản phẩm chính hãng, âm thanh rất hay, đệm tai êm ái! Giao hàng cực nhanh, shipper lịch sự. Sẽ tiếp tục ủng hộ shop các lần sau.',
            0, 0, NULL, NULL, DATEADD(HOUR, -2, GETDATE()));
    
    DECLARE @maDgMoi BIGINT = SCOPE_IDENTITY();
    INSERT INTO hinh_anh_danh_gia (ma_danh_gia, link_anh)
    VALUES (@maDgMoi, N'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600');
END
ELSE IF @ct201 IS NOT NULL
BEGIN
    UPDATE danh_gia_san_pham 
    SET phan_hoi_cua_shop = NULL, ngay_shop_phan_hoi = NULL 
    WHERE ma_chi_tiet_don = @ct201;
END

-- 4. HIỂN THỊ KẾT QUẢ ĐỒNG BỘ CSDL PHỤC VỤ TEST US-50
SELECT N'=== DANH SÁCH ĐÁNH GIÁ GIAN HÀNG TECHZONE (SẴN SÀNG CHO SELLER QUẢN LÝ & PHẢN HỒI) ===' AS ThongBao;
SELECT 
    d.ma_danh_gia,
    nd.ho_va_ten AS khach_hang,
    d.so_sao,
    LEFT(d.noi_dung, 45) + N'...' AS nhan_xet_khach,
    CASE 
        WHEN d.phan_hoi_cua_shop IS NOT NULL AND TRIM(d.phan_hoi_cua_shop) <> '' THEN N'ĐÃ PHẢN HỒI'
        ELSE N'CHƯA PHẢN HỒI (CẦN TRẢ LỜI)'
    END AS trang_thai_phan_hoi,
    d.phan_hoi_cua_shop
FROM danh_gia_san_pham d
JOIN nguoi_dung nd ON d.ma_nguoi_dung = nd.ma_nguoi_dung
WHERE d.ma_gian_hang = 1;
GO
