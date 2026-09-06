-- =================================================================================================
-- KỊCH BẢN DỮ LIỆU MẪU KIỂM THỬ US-55 (MODULE: COIN / REWARD - PHÂN HỆ KHÁCH HÀNG)
-- ĐỀ TÀI: TÍCH LŨY & SỬ DỤNG ĐIỂM THƯỞNG / XU (COIN REWARD) ĐỂ TRỪ TIỀN ĐƠN HÀNG
-- TỶ LỆ QUY ĐỔI CHUẨN: 1 XU = 1 VNĐ
-- CSDL: FlexShop_V2_Full (Hỗ trợ chuẩn Tiếng Việt Unicode N'')
-- =================================================================================================

USE [FlexShop_V2_Full];
GO

-- 1. BỔ SUNG CỘT CHO BẢNG don_hang_tong (NẾU CHƯA CÓ)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'don_hang_tong' AND COLUMN_NAME = 'so_xu_da_dung')
BEGIN
    ALTER TABLE don_hang_tong ADD so_xu_da_dung BIGINT DEFAULT 0;
    PRINT N'Đã thêm cột so_xu_da_dung vào bảng don_hang_tong.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'don_hang_tong' AND COLUMN_NAME = 'so_tien_giam_tu_xu')
BEGIN
    ALTER TABLE don_hang_tong ADD so_tien_giam_tu_xu DECIMAL(18,2) DEFAULT 0;
    PRINT N'Đã thêm cột so_tien_giam_tu_xu vào bảng don_hang_tong.';
END
GO

-- 2. BỔ SUNG CỘT CHO BẢNG lich_su_giao_dich_xu (NẾU CHƯA CÓ)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'lich_su_giao_dich_xu' AND COLUMN_NAME = 'mo_ta')
BEGIN
    ALTER TABLE lich_su_giao_dich_xu ADD mo_ta NVARCHAR(255) NULL;
    PRINT N'Đã thêm cột mo_ta vào bảng lich_su_giao_dich_xu.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'lich_su_giao_dich_xu' AND COLUMN_NAME = 'so_xu_sau_giao_dich')
BEGIN
    ALTER TABLE lich_su_giao_dich_xu ADD so_xu_sau_giao_dich BIGINT NULL;
    PRINT N'Đã thêm cột so_xu_sau_giao_dich vào bảng lich_su_giao_dich_xu.';
END
GO

-- 3. KHỞI TẠO VÍ XU CHO KHÁCH HÀNG DEMO: Nguyễn Văn An (khachhang@flexshop.vn - ID: 4)
DECLARE @MaKhachHang BIGINT = (
    SELECT TOP 1 ma_nguoi_dung 
    FROM nguoi_dung 
    WHERE email = 'khachhang@flexshop.vn' OR ma_nguoi_dung = 4
);

IF @MaKhachHang IS NOT NULL
BEGIN
    -- Cập nhật hoặc thêm mới Ví Xu
    IF EXISTS (SELECT 1 FROM vi_xu_nguoi_dung WHERE ma_nguoi_dung = @MaKhachHang)
    BEGIN
        UPDATE vi_xu_nguoi_dung
        SET so_xu_hien_tai = 50000,
            tong_xu_da_tich_luy = 85000,
            ngay_cap_nhat = SYSDATETIME(),
            phien_ban_lock = ISNULL(phien_ban_lock, 0) + 1
        WHERE ma_nguoi_dung = @MaKhachHang;
    END
    ELSE
    BEGIN
        INSERT INTO vi_xu_nguoi_dung (ma_nguoi_dung, so_xu_hien_tai, tong_xu_da_tich_luy, ngay_cap_nhat, phien_ban_lock)
        VALUES (@MaKhachHang, 50000, 85000, SYSDATETIME(), 1);
    END

    -- Làm sạch lịch sử giao dịch mẫu cũ của khách hàng này để nạp mới chuẩn xác
    DELETE FROM lich_su_giao_dich_xu WHERE ma_nguoi_dung = @MaKhachHang;

    -- Giao dịch 1: Tích xu mua sắm đơn hàng #DH-1001 (+50,000 Xu)
    INSERT INTO lich_su_giao_dich_xu (ma_nguoi_dung, so_xu_thay_doi, loai_giao_dich, ma_tham_chieu, mo_ta, so_xu_sau_giao_dich, ngay_tao)
    VALUES (@MaKhachHang, 50000, 'TICH_XU_DON_HANG', 'DH-1001', N'Tích 1% xu từ đơn hàng hoàn thành #DH-1001', 50000, DATEADD(day, -4, SYSDATETIME()));

    -- Giao dịch 2: Dùng xu thanh toán đơn hàng #DH-1002 (-35,000 Xu = Giảm 35.000 VNĐ)
    INSERT INTO lich_su_giao_dich_xu (ma_nguoi_dung, so_xu_thay_doi, loai_giao_dich, ma_tham_chieu, mo_ta, so_xu_sau_giao_dich, ngay_tao)
    VALUES (@MaKhachHang, -35000, 'TRU_XU_DON_HANG', 'DH-1002', N'Dùng 35.000 xu giảm trực tiếp vào đơn hàng #DH-1002', 15000, DATEADD(day, -3, SYSDATETIME()));

    -- Giao dịch 3: Tích xu đơn hàng #DH-1003 (+34,000 Xu)
    INSERT INTO lich_su_giao_dich_xu (ma_nguoi_dung, so_xu_thay_doi, loai_giao_dich, ma_tham_chieu, mo_ta, so_xu_sau_giao_dich, ngay_tao)
    VALUES (@MaKhachHang, 34000, 'TICH_XU_DON_HANG', 'DH-1003', N'Tích 1% xu từ đơn hàng hoàn thành #DH-1003', 49000, DATEADD(day, -2, SYSDATETIME()));

    -- Giao dịch 4: Điểm danh nhận xu hôm qua (+1,000 Xu)
    INSERT INTO lich_su_giao_dich_xu (ma_nguoi_dung, so_xu_thay_doi, loai_giao_dich, ma_tham_chieu, mo_ta, so_xu_sau_giao_dich, ngay_tao)
    VALUES (@MaKhachHang, 1000, 'DIEM_DANH_HANG_NGAY', 'CHECKIN-' + CONVERT(VARCHAR(10), DATEADD(day, -1, GETDATE()), 120), N'Điểm danh hàng ngày nhận thưởng FlexShop Xu', 50000, DATEADD(day, -1, SYSDATETIME()));

    PRINT N'Đã khởi tạo số dư và lịch sử giao dịch Ví Xu thành công cho khách hàng ID #' + CAST(@MaKhachHang AS NVARCHAR(20));
END
ELSE
BEGIN
    PRINT N'Không tìm thấy khách hàng demo khachhang@flexshop.vn!';
END
GO

-- 4. HIỂN THỊ KẾT QUẢ VÍ XU VÀ LỊCH SỬ GIAO DỊCH
SELECT 
    vx.ma_nguoi_dung AS [Mã Khách Hàng],
    nd.ho_va_ten AS [Họ Tên],
    nd.email AS [Email],
    vx.so_xu_hien_tai AS [Số Xu Hiện Tại],
    vx.tong_xu_da_tich_luy AS [Tổng Xu Đã Tích],
    (vx.so_xu_hien_tai * 1) AS [Giá Trị Quy Đổi VNĐ],
    vx.ngay_cap_nhat AS [Cập Nhật Lúc]
FROM vi_xu_nguoi_dung vx
JOIN nguoi_dung nd ON vx.ma_nguoi_dung = nd.ma_nguoi_dung;

SELECT TOP 10
    ls.ma_giao_dich_xu AS [Mã GD],
    ls.ngay_tao AS [Thời Gian],
    ls.loai_giao_dich AS [Loại Giao Dịch],
    CASE 
        WHEN ls.so_xu_thay_doi > 0 THEN '+' + CAST(ls.so_xu_thay_doi AS NVARCHAR(20))
        ELSE CAST(ls.so_xu_thay_doi AS NVARCHAR(20))
    END AS [Biến Động Xu],
    ls.so_xu_sau_giao_dich AS [Số Dư Sau GD],
    ls.mo_ta AS [Nội Dung Chi Tiết]
FROM lich_su_giao_dich_xu ls
ORDER BY ls.ma_giao_dich_xu DESC;
GO
