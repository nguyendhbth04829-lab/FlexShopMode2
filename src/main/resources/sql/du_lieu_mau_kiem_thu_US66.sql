-- =================================================================================================
-- KỊCH BẢN DỮ LIỆU MẪU KIỂM THỬ US-66 (MODULE: GAME / MINI-GAME VÒNG QUAY MAY MẮN)
-- ĐỀ TÀI: CHƠI VÒNG QUAY MAY MẮN MỖI NGÀY ĐỂ NHẬN XU VÀ VOUCHER (SHOPEE LUCKY WHEEL)
-- TRẢ THƯỞNG TỨC THÌ VÀO VÍ XU HOẶC KHO VOUCHER CÁ NHÂN
-- CSDL: FlexShop_V2_Full (Hỗ trợ chuẩn Tiếng Việt Unicode N'')
-- =================================================================================================

USE [FlexShop_V2_Full];
GO

-- 1. BỔ SUNG CỘT CHO BẢNG vong_quay_may_man (NẾU CHƯA CÓ)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'vong_quay_may_man' AND COLUMN_NAME = 'loai_phan_thuong')
BEGIN
    ALTER TABLE vong_quay_may_man ADD loai_phan_thuong VARCHAR(30) NOT NULL DEFAULT 'XU';
    PRINT N'Đã thêm cột loai_phan_thuong vào bảng vong_quay_may_man.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'vong_quay_may_man' AND COLUMN_NAME = 'so_xu_nhan')
BEGIN
    ALTER TABLE vong_quay_may_man ADD so_xu_nhan BIGINT NOT NULL DEFAULT 0;
    PRINT N'Đã thêm cột so_xu_nhan vào bảng vong_quay_may_man.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'vong_quay_may_man' AND COLUMN_NAME = 'ma_voucher')
BEGIN
    ALTER TABLE vong_quay_may_man ADD ma_voucher BIGINT NULL;
    PRINT N'Đã thêm cột ma_voucher vào bảng vong_quay_may_man.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'vong_quay_may_man' AND COLUMN_NAME = 'ma_code_voucher')
BEGIN
    ALTER TABLE vong_quay_may_man ADD ma_code_voucher VARCHAR(50) NULL;
    PRINT N'Đã thêm cột ma_code_voucher vào bảng vong_quay_may_man.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'vong_quay_may_man' AND COLUMN_NAME = 'trang_thai_tra_thuong')
BEGIN
    ALTER TABLE vong_quay_may_man ADD trang_thai_tra_thuong NVARCHAR(50) NOT NULL DEFAULT N'Đã cộng thưởng';
    PRINT N'Đã thêm cột trang_thai_tra_thuong vào bảng vong_quay_may_man.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'vong_quay_may_man' AND COLUMN_NAME = 'goc_quay_do')
BEGIN
    ALTER TABLE vong_quay_may_man ADD goc_quay_do INT NULL DEFAULT 0;
    PRINT N'Đã thêm cột goc_quay_do vào bảng vong_quay_may_man.';
END

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'vong_quay_may_man' AND COLUMN_NAME = 'mo_ta_ket_qua')
BEGIN
    ALTER TABLE vong_quay_may_man ADD mo_ta_ket_qua NVARCHAR(255) NULL;
    PRINT N'Đã thêm cột mo_ta_ket_qua vào bảng vong_quay_may_man.';
END
GO

-- 2. TẠO BẢNG CẤU HÌNH 8 Ô PHẦN THƯỞNG VÒNG QUAY (cau_hinh_phan_thuong_vong_quay)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'cau_hinh_phan_thuong_vong_quay')
BEGIN
    CREATE TABLE cau_hinh_phan_thuong_vong_quay (
        ma_phan_thuong BIGINT IDENTITY(1,1) PRIMARY KEY,
        ten_phan_thuong NVARCHAR(150) NOT NULL,
        loai_phan_thuong VARCHAR(30) NOT NULL, -- XU, VOUCHER, MAY_MAN_LAN_SAU
        gia_tri_xu BIGINT NOT NULL DEFAULT 0,
        ma_voucher BIGINT NULL,
        ty_le_trung DECIMAL(5,2) NOT NULL DEFAULT 12.50,
        so_luong_gioi_han INT NOT NULL DEFAULT 9999,
        so_luong_da_trung INT NOT NULL DEFAULT 0,
        mau_sac_o VARCHAR(20) NOT NULL DEFAULT '#FF6B6B',
        mau_chu VARCHAR(20) NOT NULL DEFAULT '#FFFFFF',
        icon VARCHAR(50) NOT NULL DEFAULT 'bi-coin',
        thu_tu_o INT NOT NULL, -- 1 đến 8
        dang_hoat_dong BIT NOT NULL DEFAULT 1,
        ngay_tao DATETIME2 DEFAULT SYSDATETIME()
    );
    PRINT N'Đã tạo mới bảng cau_hinh_phan_thuong_vong_quay.';
END
GO

-- 3. NẠP 8 Ô PHẦN THƯỞNG MẪU VÀO cau_hinh_phan_thuong_vong_quay (TỔNG XÁC SUẤT = 100%)
DECLARE @VoucherFreeship BIGINT = (SELECT TOP 1 ma_voucher FROM ma_giam_gia WHERE ma_code_voucher = 'FREESHIP30K');
DECLARE @VoucherTechZone BIGINT = (SELECT TOP 1 ma_voucher FROM ma_giam_gia WHERE ma_code_voucher = 'TECHZONE50K');
DECLARE @VoucherFashion BIGINT = (SELECT TOP 1 ma_voucher FROM ma_giam_gia WHERE ma_code_voucher = 'FASHION20K');

-- Làm sạch cấu hình cũ để nạp chuẩn 8 ô vòng quay
DELETE FROM cau_hinh_phan_thuong_vong_quay;

INSERT INTO cau_hinh_phan_thuong_vong_quay 
(ten_phan_thuong, loai_phan_thuong, gia_tri_xu, ma_voucher, ty_le_trung, so_luong_gioi_han, so_luong_da_trung, mau_sac_o, mau_chu, icon, thu_tu_o, dang_hoat_dong)
VALUES
(N'1.000 Xu FlexShop', 'XU', 1000, NULL, 25.00, 9999, 15, '#FF5722', '#FFFFFF', 'bi-coin', 1, 1),
(N'Voucher Freeship 30K', 'VOUCHER', 0, @VoucherFreeship, 15.00, 500, 8, '#00b894', '#FFFFFF', 'bi-truck', 2, 1),
(N'5.000 Xu May Mắn', 'XU', 5000, NULL, 10.00, 1000, 5, '#fdcb6e', '#2d3436', 'bi-stars', 3, 1),
(N'Voucher Giảm 50K', 'VOUCHER', 0, @VoucherTechZone, 10.00, 300, 3, '#0984e3', '#FFFFFF', 'bi-ticket-perforated-fill', 4, 1),
(N'Chúc Bạn May Mắn', 'MAY_MAN_LAN_SAU', 0, NULL, 15.00, 9999, 12, '#636e72', '#FFFFFF', 'bi-emoji-smile', 5, 1),
(N'2.000 Xu FlexShop', 'XU', 2000, NULL, 15.00, 2000, 9, '#e17055', '#FFFFFF', 'bi-coin', 6, 1),
(N'Voucher Thời Trang 20K', 'VOUCHER', 0, @VoucherFashion, 8.00, 500, 4, '#6c5ce7', '#FFFFFF', 'bi-bag-heart-fill', 7, 1),
(N'10.000 Xu Độc Đắc', 'XU', 10000, NULL, 2.00, 100, 1, '#d63031', '#FFFFFF', 'bi-trophy-fill', 8, 1);

PRINT N'Đã nạp thành công 8 ô phần thưởng chuẩn xác suất 100% cho Vòng quay may mắn.';
GO

-- 4. NẠP DỮ LIỆU KIỂM THỬ LỊCH SỬ QUAY CHO KHÁCH HÀNG DEMO (khachhang@flexshop.vn - ID: 4)
DECLARE @MaKhachHang BIGINT = (
    SELECT TOP 1 ma_nguoi_dung 
    FROM nguoi_dung 
    WHERE email = 'khachhang@flexshop.vn' OR ma_nguoi_dung = 4
);

IF @MaKhachHang IS NOT NULL
BEGIN
    -- Đảm bảo ví xu của khách hàng demo đã được khởi tạo
    IF NOT EXISTS (SELECT 1 FROM vi_xu_nguoi_dung WHERE ma_nguoi_dung = @MaKhachHang)
    BEGIN
        INSERT INTO vi_xu_nguoi_dung (ma_nguoi_dung, so_xu_hien_tai, tong_xu_da_tich_luy, ngay_cap_nhat, phien_ban_lock)
        VALUES (@MaKhachHang, 50000, 85000, SYSDATETIME(), 1);
    END

    -- Làm sạch lịch sử quay mẫu cũ của khách hàng này để nạp mới chuẩn xác
    DELETE FROM vong_quay_may_man WHERE ma_nguoi_dung = @MaKhachHang;

    -- Lượt quay 1: Trúng 1.000 Xu (3 ngày trước)
    INSERT INTO vong_quay_may_man 
    (ma_nguoi_dung, phan_thuong, loai_phan_thuong, so_xu_nhan, ma_voucher, ma_code_voucher, trang_thai_tra_thuong, goc_quay_do, mo_ta_ket_qua, ngay_quay)
    VALUES 
    (@MaKhachHang, N'1.000 Xu FlexShop', 'XU', 1000, NULL, NULL, N'Đã cộng vào Ví Xu', 22, N'Cộng trực tiếp +1.000 Xu vào ví', DATEADD(day, -3, SYSDATETIME()));

    -- Lượt quay 2: Trúng Voucher Freeship 30K (2 ngày trước)
    INSERT INTO vong_quay_may_man 
    (ma_nguoi_dung, phan_thuong, loai_phan_thuong, so_xu_nhan, ma_voucher, ma_code_voucher, trang_thai_tra_thuong, goc_quay_do, mo_ta_ket_qua, ngay_quay)
    VALUES 
    (@MaKhachHang, N'Voucher Freeship 30K', 'VOUCHER', 0, 10, 'FREESHIP30K', N'Đã lưu vào Kho Voucher', 67, N'Lưu mã FREESHIP30K vào Kho Voucher', DATEADD(day, -2, SYSDATETIME()));

    -- Lượt quay 3: Trúng 5.000 Xu May Mắn (Hôm qua)
    INSERT INTO vong_quay_may_man 
    (ma_nguoi_dung, phan_thuong, loai_phan_thuong, so_xu_nhan, ma_voucher, ma_code_voucher, trang_thai_tra_thuong, goc_quay_do, mo_ta_ket_qua, ngay_quay)
    VALUES 
    (@MaKhachHang, N'5.000 Xu May Mắn', 'XU', 5000, NULL, NULL, N'Đã cộng vào Ví Xu', 112, N'Cộng trực tiếp +5.000 Xu vào ví', DATEADD(day, -1, SYSDATETIME()));

    PRINT N'Đã khởi tạo lịch sử quay thưởng mẫu thành công cho khách hàng ID #' + CAST(@MaKhachHang AS NVARCHAR(20));
END
GO

-- 5. HIỂN THỊ KẾT QUẢ KIỂM THỬ ĐỒNG BỘ
PRINT N'--- DANH SÁCH 8 Ô PHẦN THƯỞNG VÒNG QUAY ---';
SELECT 
    thu_tu_o AS [Ô Số],
    ten_phan_thuong AS [Tên Phần Thưởng],
    loai_phan_thuong AS [Loại Quà],
    gia_tri_xu AS [Số Xu],
    ma_voucher AS [Mã Voucher],
    ty_le_trung AS [Tỷ Lệ %],
    mau_sac_o AS [Màu Sắc],
    icon AS [Icon],
    dang_hoat_dong AS [Hoạt Động]
FROM cau_hinh_phan_thuong_vong_quay
ORDER BY thu_tu_o ASC;

PRINT N'--- LỊCH SỬ QUAY MẪU CỦA KHÁCH HÀNG DEMO ---';
SELECT 
    vq.ma_luot_quay AS [Mã Lượt],
    nd.ho_va_ten AS [Khách Hàng],
    vq.phan_thuong AS [Phần Thưởng],
    vq.loai_phan_thuong AS [Loại],
    vq.so_xu_nhan AS [Xu Nhận],
    vq.ma_code_voucher AS [Code Voucher],
    vq.trang_thai_tra_thuong AS [Trạng Thái],
    vq.ngay_quay AS [Thời Gian Quay]
FROM vong_quay_may_man vq
JOIN nguoi_dung nd ON vq.ma_nguoi_dung = nd.ma_nguoi_dung
ORDER BY vq.ngay_quay DESC;
GO
