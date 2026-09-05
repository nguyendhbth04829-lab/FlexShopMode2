-- ==============================================================================
-- KỊCH BẢN NẠP DỮ LIỆU MẪU KIỂM THỬ US-51: WISHLIST, FOLLOW SHOP & Q&A
-- Module: ENGAGE (Kênh khách hàng tương tác: Yêu thích, Theo dõi & Hỏi đáp)
-- Hệ quản trị CSDL: Microsoft SQL Server (FlexShop_V2_Full) - Chuẩn Unicode Tiếng Việt
-- ==============================================================================

USE FlexShop_V2_Full;
GO

-- 1. LẤY THÔNG TIN CÁC TÀI KHOẢN VÀ SẢN PHẨM / GIAN HÀNG
DECLARE @maKhachHangAn BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');
DECLARE @maKhachHangLinh BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'thuylinh@gmail.com');
DECLARE @maSellerTechZone BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'techzone@flexshop.vn');
DECLARE @maSellerFashion BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'flexfashion@flexshop.vn');

DECLARE @maGianHangTechZone BIGINT = 1; -- TechZone Flagship Store
DECLARE @maGianHangFashion BIGINT = 2;  -- Flex Fashion Official

DECLARE @maSpSony BIGINT = 1;   -- Tai nghe không dây Sony WH-1000XM5
DECLARE @maSpAoSoMi BIGINT = 2; -- Áo Sơ Mi Nam Oxford

-- 2. NẠP DỮ LIỆU SẢN PHẨM YÊU THÍCH (WISHLIST)
IF @maKhachHangAn IS NOT NULL AND @maSpSony IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM san_pham_yeu_thich WHERE ma_nguoi_dung = @maKhachHangAn AND ma_san_pham = @maSpSony)
    BEGIN
        INSERT INTO san_pham_yeu_thich (ma_nguoi_dung, ma_san_pham, ngay_tao)
        VALUES (@maKhachHangAn, @maSpSony, DATEADD(DAY, -3, GETDATE()));
    END
END

IF @maKhachHangAn IS NOT NULL AND @maSpAoSoMi IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM san_pham_yeu_thich WHERE ma_nguoi_dung = @maKhachHangAn AND ma_san_pham = @maSpAoSoMi)
    BEGIN
        INSERT INTO san_pham_yeu_thich (ma_nguoi_dung, ma_san_pham, ngay_tao)
        VALUES (@maKhachHangAn, @maSpAoSoMi, DATEADD(DAY, -1, GETDATE()));
    END
END

-- 3. NẠP DỮ LIỆU THEO DÕI GIAN HÀNG (FOLLOW SHOP)
IF @maKhachHangAn IS NOT NULL AND @maGianHangTechZone IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM theo_doi_gian_hang WHERE ma_nguoi_dung = @maKhachHangAn AND ma_gian_hang = @maGianHangTechZone)
    BEGIN
        INSERT INTO theo_doi_gian_hang (ma_nguoi_dung, ma_gian_hang, ngay_tao)
        VALUES (@maKhachHangAn, @maGianHangTechZone, DATEADD(DAY, -10, GETDATE()));
    END
END

IF @maKhachHangLinh IS NOT NULL AND @maGianHangTechZone IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM theo_doi_gian_hang WHERE ma_nguoi_dung = @maKhachHangLinh AND ma_gian_hang = @maGianHangTechZone)
    BEGIN
        INSERT INTO theo_doi_gian_hang (ma_nguoi_dung, ma_gian_hang, ngay_tao)
        VALUES (@maKhachHangLinh, @maGianHangTechZone, DATEADD(DAY, -5, GETDATE()));
    END
END

-- 4. NẠP DỮ LIỆU HỎI - ĐÁP CỘNG ĐỒNG (Q&A) TRÊN TRANG SẢN PHẨM
-- Câu 1: Đã được Người bán (TechZone) phản hồi chính thức
IF @maSpSony IS NOT NULL AND @maKhachHangAn IS NOT NULL AND @maSellerTechZone IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM hoi_dap_san_pham WHERE ma_san_pham = @maSpSony AND cau_hoi LIKE N'%Multipoint Connection%')
    BEGIN
        INSERT INTO hoi_dap_san_pham (ma_san_pham, ma_nguoi_hoi, cau_hoi, cau_tra_loi, ma_nguoi_tra_loi, ngay_hoi, ngay_tra_loi)
        VALUES (
            @maSpSony, 
            @maKhachHangAn, 
            N'Tai nghe này có hỗ trợ kết nối đồng thời cả điện thoại và máy tính laptop cùng lúc được không shop?',
            N'Dạ chào bạn An, tai nghe Sony WH-1000XM5 hỗ trợ tính năng Multipoint Connection cho phép kết nối đồng thời 2 thiết bị và tự động chuyển đổi âm thanh cực kỳ mượt mà bạn nhé!',
            @maSellerTechZone,
            DATEADD(DAY, -2, GETDATE()),
            DATEADD(DAY, -1, GETDATE())
        );
    END
END

-- Câu 2: Đã được thành viên cộng đồng phản hồi
IF @maSpSony IS NOT NULL AND @maKhachHangLinh IS NOT NULL AND @maKhachHangAn IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM hoi_dap_san_pham WHERE ma_san_pham = @maSpSony AND cau_hoi LIKE N'%bí tai%')
    BEGIN
        INSERT INTO hoi_dap_san_pham (ma_san_pham, ma_nguoi_hoi, cau_hoi, cau_tra_loi, ma_nguoi_tra_loi, ngay_hoi, ngay_tra_loi)
        VALUES (
            @maSpSony, 
            @maKhachHangLinh, 
            N'Đệm tai của dòng XM5 này đeo lâu có bị đau hay bí tai khi dùng phòng không máy lạnh không mọi người?',
            N'Mình dùng bản màu đen này được 3 tháng rồi, chất da cực kỳ mềm và êm tai, đeo liên tục 4-5 tiếng làm việc không hề đau đầu hay cấn tai nha bạn ơi.',
            @maKhachHangAn,
            DATEADD(HOUR, -12, GETDATE()),
            DATEADD(HOUR, -6, GETDATE())
        );
    END
END

-- Câu 3: Câu hỏi mới ĐANG CHỜ SHOP GIẢI ĐÁP
IF @maSpSony IS NOT NULL AND @maKhachHangAn IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM hoi_dap_san_pham WHERE ma_san_pham = @maSpSony AND cau_hoi LIKE N'%bảo hành chính hãng Sony%')
    BEGIN
        INSERT INTO hoi_dap_san_pham (ma_san_pham, ma_nguoi_hoi, cau_hoi, cau_tra_loi, ma_nguoi_tra_loi, ngay_hoi, ngay_tra_loi)
        VALUES (
            @maSpSony, 
            @maKhachHangAn, 
            N'Shop cho mình hỏi sản phẩm này là bảo hành chính hãng Sony Việt Nam 12 tháng đúng không? Có kích hoạt bảo hành điện tử theo số seri được không ạ?',
            NULL,
            NULL,
            DATEADD(HOUR, -2, GETDATE()),
            NULL
        );
    END
END

-- 5. TRUY VẤN XÁC MINH DỮ LIỆU ĐÃ ĐỒNG BỘ THÀNH CÔNG
SELECT N'=== DANH SÁCH SẢN PHẨM YÊU THÍCH (WISHLIST) ===' AS BangDuLieu;
SELECT y.ma_nguoi_dung, nd.ho_va_ten, sp.ten_san_pham, y.ngay_tao
FROM san_pham_yeu_thich y
JOIN nguoi_dung nd ON y.ma_nguoi_dung = nd.ma_nguoi_dung
JOIN san_pham sp ON y.ma_san_pham = sp.ma_san_pham;

SELECT N'=== DANH SÁCH THEO DÕI GIAN HÀNG (FOLLOW SHOP) ===' AS BangDuLieu;
SELECT t.ma_nguoi_dung, nd.ho_va_ten AS khach_hang, gh.ten_gian_hang, t.ngay_tao
FROM theo_doi_gian_hang t
JOIN nguoi_dung nd ON t.ma_nguoi_dung = nd.ma_nguoi_dung
JOIN gian_hang gh ON t.ma_gian_hang = gh.ma_gian_hang;

SELECT N'=== DANH SÁCH HỎI - ĐÁP CỘNG ĐỒNG (Q&A) ===' AS BangDuLieu;
SELECT h.ma_hoi_dap, sp.ten_san_pham, nd_hoi.ho_va_ten AS nguoi_hoi, h.cau_hoi,
       CASE WHEN h.cau_tra_loi IS NOT NULL THEN N'ĐÃ TRẢ LỜI' ELSE N'CHỜ SHOP GIẢI ĐÁP' END AS trang_thai,
       h.cau_tra_loi, nd_tl.ho_va_ten AS nguoi_tra_loi
FROM hoi_dap_san_pham h
JOIN san_pham sp ON h.ma_san_pham = sp.ma_san_pham
JOIN nguoi_dung nd_hoi ON h.ma_nguoi_hoi = nd_hoi.ma_nguoi_dung
LEFT JOIN nguoi_dung nd_tl ON h.ma_nguoi_tra_loi = nd_tl.ma_nguoi_dung;
GO
