-- ==============================================================================
-- SCRIPT TEST US-10: THIẾT LẬP HỒ SƠ SHOP (BANNER, LOGO, GIỜ HOẠT ĐỘNG, ĐỊA CHỈ KHO)
-- Hệ thống: FlexShop Mode 2
-- Database: FlexShop_V2_Full
-- Ngày tạo: 2026-09-07
-- ==============================================================================

USE FlexShop_V2_Full;
GO

-- 1. Đảm bảo cấu trúc cột của US-10 trong gian_hang
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('gian_hang') AND name = 'gio_mo_cua')
BEGIN
    ALTER TABLE gian_hang ADD gio_mo_cua NVARCHAR(10) DEFAULT '08:00';
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('gian_hang') AND name = 'gio_dong_cua')
BEGIN
    ALTER TABLE gian_hang ADD gio_dong_cua NVARCHAR(10) DEFAULT '22:00';
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('gian_hang') AND name = 'dang_mo_cua')
BEGIN
    ALTER TABLE gian_hang ADD dang_mo_cua BIT DEFAULT 1;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('gian_hang') AND name = 'ghi_chu_kho')
BEGIN
    ALTER TABLE gian_hang ADD ghi_chu_kho NVARCHAR(255) NULL;
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('gian_hang') AND name = 'nguoi_lien_he_kho')
BEGIN
    ALTER TABLE gian_hang ADD nguoi_lien_he_kho NVARCHAR(100) NULL;
END;
GO

-- 2. Đảm bảo bảng nhật ký lịch sử thiết lập shop (phục vụ tìm lọc & phân trang)
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'lich_su_thiet_lap_shop')
BEGIN
    CREATE TABLE lich_su_thiet_lap_shop (
        ma_lich_su BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_gian_hang BIGINT NOT NULL,
        loai_thay_doi NVARCHAR(50) NOT NULL,
        noi_dung_thay_doi NVARCHAR(500) NOT NULL,
        nguoi_thuc_hien NVARCHAR(100) NOT NULL,
        thoi_gian DATETIME2 DEFAULT CURRENT_TIMESTAMP
    );
END;
GO

-- 3. Cập nhật dữ liệu gian hàng của tài khoản seller@flexshop.vn (ma_chu_so_huu = 2 hoặc 4)
DECLARE @maChuSoHuu BIGINT;
SELECT TOP 1 @maChuSoHuu = ma_nguoi_dung FROM nguoi_dung WHERE email = 'seller@flexshop.vn';

IF @maChuSoHuu IS NOT NULL
BEGIN
    -- Cập nhật gian hàng ở trạng thái HOAT_DONG với đầy đủ dữ liệu mẫu US-10
    UPDATE gian_hang
    SET 
        ten_gian_hang = N'Flex Fashion Official Store',
        duong_dan_slug = 'flex-fashion-official-store',
        mo_ta = N'Gian hàng thời trang cao cấp Flex Fashion, phân phối quần áo và phụ kiện chính hãng, bảo hành đổi trả 7 ngày miễn phí toàn quốc.',
        link_logo = 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&auto=format&fit=crop&q=80',
        link_banner = 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200&auto=format&fit=crop&q=80',
        dia_chi_kho = N'Số 88 Phố Huế, Phường Hàng Bài, Quận Hoàn Kiếm, Hà Nội',
        sdt_kho = '0987654321',
        gio_mo_cua = '08:00',
        gio_dong_cua = '22:00',
        dang_mo_cua = 1,
        ghi_chu_kho = N'Kho lấy hàng ở tầng 1, bấm chuông cửa cuốn màu xanh, gọi trước 15 phút.',
        nguoi_lien_he_kho = N'Nguyễn Văn Quản Kho',
        trang_thai = 'HOAT_DONG'
    WHERE ma_chu_so_huu = @maChuSoHuu;

    -- Lấy mã gian hàng vừa cập nhật
    DECLARE @maGianHang BIGINT;
    SELECT TOP 1 @maGianHang = ma_gian_hang FROM gian_hang WHERE ma_chu_so_huu = @maChuSoHuu;

    -- 4. Thêm các bản ghi mẫu vào bảng lịch sử thiết lập để test tìm lọc & phân trang
    IF @maGianHang IS NOT NULL
    BEGIN
        -- Xóa log cũ nếu có
        DELETE FROM lich_su_thiet_lap_shop WHERE ma_gian_hang = @maGianHang;

        INSERT INTO lich_su_thiet_lap_shop (ma_gian_hang, loai_thay_doi, noi_dung_thay_doi, nguoi_thuc_hien, thoi_gian)
        VALUES 
        (@maGianHang, 'THIET_LAP_TONG_THE', N'Khởi tạo thiết lập hồ sơ shop sau khi được Admin phê duyệt', N'Nguyễn Văn Bán', DATEADD(DAY, -5, CURRENT_TIMESTAMP)),
        (@maGianHang, 'DOI_LOGO', N'Cập nhật Logo nhận diện thương hiệu chuẩn 1:1', N'Nguyễn Văn Bán', DATEADD(DAY, -4, CURRENT_TIMESTAMP)),
        (@maGianHang, 'DOI_BANNER', N'Cập nhật ảnh Banner mùa khuyến mãi hè 2026', N'Nguyễn Văn Bán', DATEADD(DAY, -3, CURRENT_TIMESTAMP)),
        (@maGianHang, 'DOI_GIO_HOAT_DONG', N'Điều chỉnh giờ đóng cửa từ 21:00 thành 22:00 phục vụ khách tối', N'Nguyễn Văn Bán', DATEADD(DAY, -2, CURRENT_TIMESTAMP)),
        (@maGianHang, 'DOI_DIA_CHI_KHO', N'Cập nhật địa chỉ kho chính: 88 Phố Huế, Hoàn Kiếm, Hà Nội', N'Nguyễn Văn Bán', DATEADD(DAY, -1, CURRENT_TIMESTAMP)),
        (@maGianHang, 'BAT_TAT_NHAN_DON', N'Mở cửa gian hàng, sẵn sàng tiếp nhận đơn đặt mới', N'Nguyễn Văn Bán', CURRENT_TIMESTAMP);
    END;
END;
GO

-- Kiểm tra kết quả sau khi nạp dữ liệu test
SELECT ma_gian_hang, ten_gian_hang, duong_dan_slug, gio_mo_cua, gio_dong_cua, dang_mo_cua, dia_chi_kho, sdt_kho, trang_thai
FROM gian_hang
WHERE trang_thai = 'HOAT_DONG';

SELECT TOP 10 * FROM lich_su_thiet_lap_shop ORDER BY thoi_gian DESC;
GO
