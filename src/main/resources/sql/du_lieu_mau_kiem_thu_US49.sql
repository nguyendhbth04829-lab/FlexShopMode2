-- ==============================================================================
-- KỊCH BẢN NẠP DỮ LIỆU MẪU KIỂM THỬ US-49: ĐÁNH GIÁ SẢN PHẨM & TÍNH ĐIỂM RATING
-- Module: ENGAGE (Khách hàng đánh giá 1-5 sao + Nhận xét + Đính kèm ảnh)
-- Tự động tính lại điểm Rating trung bình của Sản phẩm & Gian Hàng
-- Hệ quản trị CSDL: Microsoft SQL Server (FlexShop_V2_Full) - Chuẩn Unicode Tiếng Việt
-- ==============================================================================

USE FlexShop_V2_Full;
GO

-- 1. LẤY HOẶC TẠO TÀI KHOẢN KHÁCH HÀNG KIỂM THỬ
DECLARE @maKhachHangAn BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');
DECLARE @maUserDan BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'nguyencoc288@gmail.com');
DECLARE @maUserLinh BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'thuylinh@gmail.com');

-- Tạo thêm tài khoản kiểm thử nếu chưa có
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'tranthib@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (ho_va_ten, email, mat_khau_ma_hoa, so_dien_thoai, trang_thai, da_xoa, ngay_tao)
    VALUES (N'Trần Thị Bích', N'tranthib@flexshop.vn', N'$2a$10$demoHashBich123456789', N'0912345678', N'HOAT_DONG', 0, GETDATE());
END
DECLARE @maUserBich BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'tranthib@flexshop.vn');

IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'levanc@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (ho_va_ten, email, mat_khau_ma_hoa, so_dien_thoai, trang_thai, da_xoa, ngay_tao)
    VALUES (N'Lê Văn Cường', N'levanc@flexshop.vn', N'$2a$10$demoHashCuong12345678', N'0923456789', N'HOAT_DONG', 0, GETDATE());
END
DECLARE @maUserCuong BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'levanc@flexshop.vn');

-- 2. ĐẢM BẢO CÓ ĐƠN TỔNG MẪU CHO CÁC KHÁCH HÀNG
DECLARE @maDonTongAn BIGINT = (SELECT TOP 1 ma_don_hang_tong FROM don_hang_tong WHERE ma_khach_hang = @maKhachHangAn);

DECLARE @maDonTongBich BIGINT = (SELECT TOP 1 ma_don_hang_tong FROM don_hang_tong WHERE ma_khach_hang = @maUserBich);
IF @maDonTongBich IS NULL
BEGIN
    INSERT INTO don_hang_tong (
        ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, 
        tong_tien_hang, tong_phi_van_chuyen, tong_tien_thue_vat, tong_giam_gia_san, tong_giam_gia_shop, 
        tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_thanh_toan, trang_thai_don_hang, ngay_tao
    ) VALUES (
        N'MASTER-US49-BICH-01', @maUserBich, 1, 
        1250000.00, 30000.00, 0, 0, 0, 
        1280000.00, N'COD', N'DA_THANH_TOAN', N'HOAN_TAT', DATEADD(DAY, -4, GETDATE())
    );
    SET @maDonTongBich = SCOPE_IDENTITY();
END

DECLARE @maDonTongCuong BIGINT = (SELECT TOP 1 ma_don_hang_tong FROM don_hang_tong WHERE ma_khach_hang = @maUserCuong);
IF @maDonTongCuong IS NULL
BEGIN
    INSERT INTO don_hang_tong (
        ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, 
        tong_tien_hang, tong_phi_van_chuyen, tong_tien_thue_vat, tong_giam_gia_san, tong_giam_gia_shop, 
        tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_thanh_toan, trang_thai_don_hang, ngay_tao
    ) VALUES (
        N'MASTER-US49-CUONG-01', @maUserCuong, 1, 
        1250000.00, 30000.00, 0, 0, 0, 
        1280000.00, N'VNPAY', N'DA_THANH_TOAN', N'HOAN_TAT', DATEADD(DAY, -3, GETDATE())
    );
    SET @maDonTongCuong = SCOPE_IDENTITY();
END

-- 3. TẠO CÁC ĐƠN HÀNG SHOP ĐÃ GIAO (DA_GIAO) ĐỂ TEST ĐÁNH GIÁ
-- Đơn hàng 101: Đã giao cho Trần Thị Bích (Shop 1 - TechZone)
IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-2026-US49-101')
BEGIN
    INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tien_thue_vat, giam_gia_voucher_shop, giam_gia_voucher_san, tong_tien_shop_nhan, trang_thai, ma_van_don, ngay_tao)
    VALUES (N'SHOP-TECH-2026-US49-101', @maDonTongBich, 1, 1250000.00, 30000.00, 0, 0, 0, 1250000.00, N'DA_GIAO', N'GHN-US49-01', DATEADD(DAY, -4, GETDATE()));
END
DECLARE @maDon101 BIGINT = (SELECT ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-2026-US49-101');

IF @maDon101 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon101)
BEGIN
    INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
    VALUES (@maDon101, 1, N'Tai nghe không dây Bluetooth chống ồn Sony WH-1000XM5', N'Màu Đen Nhám - Bluetooth 5.3', N'SONY-WH1000XM5-BLK', 1250000.00, 1, 1250000.00);
END

-- Đơn hàng 102: Đã giao cho Lê Văn Cường (Shop 1 - TechZone)
IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-2026-US49-102')
BEGIN
    INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tien_thue_vat, giam_gia_voucher_shop, giam_gia_voucher_san, tong_tien_shop_nhan, trang_thai, ma_van_don, ngay_tao)
    VALUES (N'SHOP-TECH-2026-US49-102', @maDonTongCuong, 1, 1250000.00, 30000.00, 0, 0, 0, 1250000.00, N'DA_GIAO', N'GHN-US49-02', DATEADD(DAY, -3, GETDATE()));
END
DECLARE @maDon102 BIGINT = (SELECT ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-2026-US49-102');

IF @maDon102 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon102)
BEGIN
    INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
    VALUES (@maDon102, 1, N'Tai nghe không dây Bluetooth chống ồn Sony WH-1000XM5', N'Màu Đen Nhám - Bluetooth 5.3', N'SONY-WH1000XM5-BLK', 1250000.00, 1, 1250000.00);
END

-- Đơn hàng 103: Đã giao cho Hoàng Thùy Linh (Shop 1 - TechZone)
DECLARE @maDonTongLinh BIGINT = (SELECT TOP 1 ma_don_hang_tong FROM don_hang_tong WHERE ma_khach_hang = @maUserLinh);
IF @maDonTongLinh IS NULL
BEGIN
    INSERT INTO don_hang_tong (
        ma_code_don_tong, ma_khach_hang, ma_dia_chi_giao, 
        tong_tien_hang, tong_phi_van_chuyen, tong_tien_thue_vat, tong_giam_gia_san, tong_giam_gia_shop, 
        tong_thanh_toan_cuoi, phuong_thuc_thanh_toan, trang_thai_thanh_toan, trang_thai_don_hang, ngay_tao
    ) VALUES (
        N'MASTER-US49-LINH-01', @maUserLinh, 1, 
        1250000.00, 30000.00, 0, 0, 0, 
        1280000.00, N'VNPAY', N'DA_THANH_TOAN', N'HOAN_TAT', DATEADD(DAY, -2, GETDATE())
    );
    SET @maDonTongLinh = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-2026-US49-103')
BEGIN
    INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tien_thue_vat, giam_gia_voucher_shop, giam_gia_voucher_san, tong_tien_shop_nhan, trang_thai, ma_van_don, ngay_tao)
    VALUES (N'SHOP-TECH-2026-US49-103', @maDonTongLinh, 1, 1250000.00, 30000.00, 0, 0, 0, 1250000.00, N'DA_GIAO', N'GHN-US49-03', DATEADD(DAY, -2, GETDATE()));
END
DECLARE @maDon103 BIGINT = (SELECT ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-2026-US49-103');

IF @maDon103 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon103)
BEGIN
    INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
    VALUES (@maDon103, 1, N'Tai nghe không dây Bluetooth chống ồn Sony WH-1000XM5', N'Màu Đen Nhám - Bluetooth 5.3', N'SONY-WH1000XM5-BLK', 1250000.00, 1, 1250000.00);
END

-- ĐƠN HÀNG 104 DÀNH RIÊNG ĐỂ KHÁCH HÀNG NGUYỄN VĂN AN TỰ VIẾT ĐÁNH GIÁ TRÊN GIAO DIỆN WEB!
IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-CHO-DANH-GIA-US49')
BEGIN
    INSERT INTO don_hang_shop (ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen, tien_thue_vat, giam_gia_voucher_shop, giam_gia_voucher_san, tong_tien_shop_nhan, trang_thai, ma_van_don, ngay_tao)
    VALUES (N'SHOP-TECH-CHO-DANH-GIA-US49', @maDonTongAn, 1, 1250000.00, 30000.00, 0, 0, 0, 1250000.00, N'DA_GIAO', N'GHN-READY-US49', DATEADD(DAY, -1, GETDATE()));
END
DECLARE @maDonChoDanhGia BIGINT = (SELECT ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-CHO-DANH-GIA-US49');

IF @maDonChoDanhGia IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDonChoDanhGia)
BEGIN
    INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
    VALUES (@maDonChoDanhGia, 1, N'Tai nghe không dây Bluetooth chống ồn Sony WH-1000XM5', N'Màu Đen Nhám - Bluetooth 5.3', N'SONY-WH1000XM5-BLK', 1250000.00, 1, 1250000.00);
END

-- 4. XÓA DỮ LIỆU ĐÁNH GIÁ CŨ ĐỂ TEST MỚI HOÀN TOÀN ĐỒNG BỘ
DELETE FROM hinh_anh_danh_gia;
DELETE FROM danh_gia_san_pham;

-- 5. CHÈN CÁC ĐÁNH GIÁ MẪU CHO SẢN PHẨM 1 (Sony WH-1000XM5)
DECLARE @ct101 BIGINT = (SELECT TOP 1 ma_chi_tiet_don FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon101);
DECLARE @ct102 BIGINT = (SELECT TOP 1 ma_chi_tiet_don FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon102);
DECLARE @ct103 BIGINT = (SELECT TOP 1 ma_chi_tiet_don FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon103);

-- Đánh giá 1: 5 Sao + 2 Hình ảnh unboxing + Không ẩn danh (Trần Thị Bích)
INSERT INTO danh_gia_san_pham (ma_chi_tiet_don, ma_san_pham, ma_gian_hang, ma_nguoi_dung, so_sao, noi_dung, an_danh, bi_an, phan_hoi_cua_shop, ngay_shop_phan_hoi, ngay_tao)
VALUES (@ct101, 1, 1, @maUserBich, 5, 
        N'Tai nghe Sony WH-1000XM5 đỉnh thực sự! Chống ồn chủ động ANC cực tốt, khử gần như 95% tiếng ồn xung quanh. Đeo cả ngày làm việc không hề bị đau tai hay bí bách. Chất âm ấm, bass đầm, pin dùng trâu tầm 30 tiếng liên tục. Đóng gói 3 lớp chống sốc rất kỹ lưỡng, giao siêu nhanh trong 24h. Cho shop 10/10 sao!',
        0, 0, 
        N'Dạ TechZone Official Store cảm ơn chị Bích đã dành lời khen và tin tưởng ủng hộ sản phẩm ạ! Chúc chị có những phút giây thưởng thức âm nhạc tuyệt vời cùng Sony WH-1000XM5 nha!',
        DATEADD(HOUR, 2, DATEADD(DAY, -4, GETDATE())),
        DATEADD(DAY, -4, GETDATE()));
DECLARE @maDg1 BIGINT = SCOPE_IDENTITY();

-- Đính kèm 2 ảnh thực tế unboxing cho Đánh giá 1
INSERT INTO hinh_anh_danh_gia (ma_danh_gia, link_anh)
VALUES 
(@maDg1, N'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600'),
(@maDg1, N'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600');

-- Đánh giá 2: 4 Sao + 1 Hình ảnh + Ẩn danh (Lê Văn Cường)
INSERT INTO danh_gia_san_pham (ma_chi_tiet_don, ma_san_pham, ma_gian_hang, ma_nguoi_dung, so_sao, noi_dung, an_danh, bi_an, phan_hoi_cua_shop, ngay_shop_phan_hoi, ngay_tao)
VALUES (@ct102, 1, 1, @maUserCuong, 4, 
        N'Chất âm tốt, kết nối Bluetooth 5.3 rất mượt mà không bị giật lag khi chơi game. Tuy nhiên hộp đựng hơi to một chút khi bỏ vào balo nhỏ. Nhìn chung rất hài lòng với chất lượng so với giá tiền.',
        1, 0, 
        N'Shop cảm ơn quý khách đã đóng góp ý kiến ạ! Do thiết kế gập phẳng thế hệ mới giúp bảo vệ khớp tai nghe tối ưu nên hộp đựng có kích thước nhỉnh hơn thế hệ trước một chút. Shop chúc bạn trải nghiệm vui vẻ!',
        DATEADD(HOUR, 4, DATEADD(DAY, -3, GETDATE())),
        DATEADD(DAY, -3, GETDATE()));
DECLARE @maDg2 BIGINT = SCOPE_IDENTITY();

INSERT INTO hinh_anh_danh_gia (ma_danh_gia, link_anh)
VALUES (@maDg2, N'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=600');

-- Đánh giá 3: 5 Sao + Đánh giá chi tiết (Hoàng Thùy Linh)
INSERT INTO danh_gia_san_pham (ma_chi_tiet_don, ma_san_pham, ma_gian_hang, ma_nguoi_dung, so_sao, noi_dung, an_danh, bi_an, phan_hoi_cua_shop, ngay_shop_phan_hoi, ngay_tao)
VALUES (@ct103, 1, 1, @maUserLinh, 5, 
        N'Hàng chuẩn chính hãng tem bảo hành đầy đủ, âm bass sâu lắng, mic thu âm trong trẻo khi họp online. Rất đáng đồng tiền bát gạo!',
        0, 0, 
        N'TechZone Official xin chân thành cảm ơn bạn Linh ạ!',
        DATEADD(HOUR, 1, DATEADD(DAY, -2, GETDATE())),
        DATEADD(DAY, -2, GETDATE()));

-- 6. TÍNH TOÁN LẠI ĐIỂM RATING TRUNG BÌNH CỦA SẢN PHẨM & SHOP
-- Điểm trung bình sản phẩm 1: (5 + 4 + 5) / 3 = 4.67 -> làm tròn 4.7
UPDATE san_pham 
SET danh_gia_tb = (
    SELECT CAST(ROUND(AVG(CAST(so_sao AS DECIMAL(5,2))), 1) AS DECIMAL(3,2))
    FROM danh_gia_san_pham 
    WHERE ma_san_pham = 1 AND bi_an = 0
), ngay_cap_nhat = GETDATE()
WHERE ma_san_pham = 1;

-- Điểm trung bình và tổng đánh giá của Shop 1
UPDATE gian_hang
SET diem_danh_gia_tb = (
    SELECT CAST(ROUND(AVG(CAST(so_sao AS DECIMAL(5,2))), 1) AS DECIMAL(3,2))
    FROM danh_gia_san_pham 
    WHERE ma_gian_hang = 1 AND bi_an = 0
),
tong_danh_gia = (
    SELECT COUNT(*)
    FROM danh_gia_san_pham 
    WHERE ma_gian_hang = 1 AND bi_an = 0
)
WHERE ma_gian_hang = 1;

-- 7. HIỂN THỊ KẾT QUẢ KIỂM TRA ĐỒNG BỘ CSDL
SELECT N'=== BẢNG ĐÁNH GIÁ SẢN PHẨM (danh_gia_san_pham) ===' AS ThongBao;
SELECT ma_danh_gia, ma_chi_tiet_don, ma_san_pham, so_sao, an_danh, LEFT(noi_dung, 60) + N'...' AS noi_dung_ngan, ngay_tao 
FROM danh_gia_san_pham;

SELECT N'=== BẢNG HÌNH ẢNH ĐÍNH KÈM (hinh_anh_danh_gia) ===' AS ThongBao;
SELECT ma_anh_danh_gia, ma_danh_gia, link_anh 
FROM hinh_anh_danh_gia;

SELECT N'=== ĐIỂM RATING SẢN PHẨM ĐƯỢC CẬP NHẬT TỰ ĐỘNG (san_pham) ===' AS ThongBao;
SELECT ma_san_pham, ten_san_pham, danh_gia_tb 
FROM san_pham WHERE ma_san_pham = 1;

SELECT N'=== ĐIỂM RATING VÀ TỔNG ĐÁNH GIÁ GIAN HÀNG (gian_hang) ===' AS ThongBao;
SELECT ma_gian_hang, ten_gian_hang, diem_danh_gia_tb, tong_danh_gia 
FROM gian_hang WHERE ma_gian_hang = 1;

SELECT N'=== ĐƠN HÀNG SẴN SÀNG ĐỂ KHÁCH HÀNG NGUYỄN VĂN AN TEST ĐÁNH GIÁ TRỰC TIẾP ===' AS ThongBao;
SELECT dhs.ma_don_hang_shop, dhs.ma_code_don_shop, dhs.trang_thai, ct.ma_chi_tiet_don, ct.ten_san_pham
FROM don_hang_shop dhs
JOIN chi_tiet_don_hang ct ON dhs.ma_don_hang_shop = ct.ma_don_hang_shop
WHERE dhs.ma_code_don_shop = N'SHOP-TECH-CHO-DANH-GIA-US49';
GO
