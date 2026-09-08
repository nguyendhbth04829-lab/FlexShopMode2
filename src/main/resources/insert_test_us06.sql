-- ==========================================================================================
-- SCRIPT TEST & ĐỒNG BỘ DỮ LIỆU US-06: CHỈ 1 VAI TRÒ DUY NHẤT CHO MỖI NGƯỜI DÙNG
-- CƠ SỞ DỮ LIỆU: FlexShop_V2_Full (SQL Server)
-- Mật khẩu mặc định của tất cả tài khoản tạo mẫu: 12345678
-- Chuỗi mã hóa BCrypt: $2a$10$b.bp6UGuVLioQIiICqTFNOS0whjXnb4ivL9bi3smPXbtO.rKt4cFS
-- ==========================================================================================

USE FlexShop_V2_Full;
GO

-- 1. Đảm bảo các vai trò nội bộ đã tồn tại trong bảng vai_tro
IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = 'ADMIN')
    INSERT INTO vai_tro (ten_vai_tro, moTa) VALUES ('ADMIN', N'Quản trị viên toàn hệ thống');

IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = 'NGUOI_BAN')
    INSERT INTO vai_tro (ten_vai_tro, moTa) VALUES ('NGUOI_BAN', N'Người bán hàng / Chủ gian hàng');

IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = 'KHACH_HANG')
    INSERT INTO vai_tro (ten_vai_tro, moTa) VALUES ('KHACH_HANG', N'Khách hàng mua sắm');

IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = 'TAI_XE')
    INSERT INTO vai_tro (ten_vai_tro, moTa) VALUES ('TAI_XE', N'Nhân viên tài xế giao hàng POD');

IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ten_vai_tro = 'CSKH')
    INSERT INTO vai_tro (ten_vai_tro, moTa) VALUES ('CSKH', N'Nhân viên hỗ trợ & chăm sóc khách hàng');
GO

-- 2. ĐỒNG BỘ & CHUẨN HÓA DỮ LIỆU HIỆN CÓ:
-- Nếu một người dùng đang có nhiều hơn 1 vai trò, chỉ giữ lại 1 vai trò duy nhất có độ ưu tiên cao nhất:
-- ADMIN (Ưu tiên 1) > CSKH (Ưu tiên 2) > TAI_XE (Ưu tiên 3) > NGUOI_BAN (Ưu tiên 4) > KHACH_HANG (Ưu tiên 5)
;WITH PhanHangVaiTro AS (
    SELECT 
        ndvt.ma_nguoi_dung,
        ndvt.ma_vai_tro,
        ROW_NUMBER() OVER (
            PARTITION BY ndvt.ma_nguoi_dung 
            ORDER BY 
                CASE vt.ten_vai_tro
                    WHEN 'ADMIN' THEN 1
                    WHEN 'CSKH' THEN 2
                    WHEN 'TAI_XE' THEN 3
                    WHEN 'SHIPPER' THEN 3
                    WHEN 'NGUOI_BAN' THEN 4
                    WHEN 'KHACH_HANG' THEN 5
                    ELSE 6
                END ASC
        ) AS ThuTuUuTien
    FROM nguoi_dung_vai_tro ndvt
    JOIN vai_tro vt ON ndvt.ma_vai_tro = vt.ma_vai_tro
)
DELETE FROM nguoi_dung_vai_tro
WHERE EXISTS (
    SELECT 1 FROM PhanHangVaiTro p
    WHERE p.ma_nguoi_dung = nguoi_dung_vai_tro.ma_nguoi_dung
      AND p.ma_vai_tro = nguoi_dung_vai_tro.ma_vai_tro
      AND p.ThuTuUuTien > 1
);
PRINT N'-> Đã chuẩn hóa database: Mỗi người dùng chỉ có 1 vai trò duy nhất!';
GO

-- 3. Thêm Nhân viên Tài xế giao hàng mẫu (Chỉ 1 vai trò TAI_XE)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = 'shipper_test@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (
        email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, 
        anh_dai_dien, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat
    ) VALUES (
        'shipper_test@flexshop.vn',
        '0933112233',
        '$2a$10$b.bp6UGuVLioQIiICqTFNOS0whjXnb4ivL9bi3smPXbtO.rKt4cFS',
        N'Nguyễn Văn Vận Chuyển',
        'https://ui-avatars.com/api/?name=Nguyen+Van+Van+Chuyen&background=0284c7&color=fff',
        'HOAT_DONG',
        0,
        DATEADD(DAY, -5, GETDATE()),
        GETDATE()
    );

    DECLARE @IdShipper BIGINT = SCOPE_IDENTITY();
    DECLARE @RoleTaiXeId INT = (SELECT TOP 1 ma_vai_tro FROM vai_tro WHERE ten_vai_tro = 'TAI_XE');

    IF (@RoleTaiXeId IS NOT NULL)
        INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@IdShipper, @RoleTaiXeId, GETDATE());

    PRINT N'-> Đã thêm nhân viên Tài xế: shipper_test@flexshop.vn (Vai trò duy nhất: TAI_XE)';
END
ELSE
BEGIN
    PRINT N'-> Tài khoản shipper_test@flexshop.vn đã tồn tại.';
END
GO

-- 4. Thêm Nhân viên Chăm sóc khách hàng mẫu (Chỉ 1 vai trò CSKH)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = 'cskh_test@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (
        email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, 
        anh_dai_dien, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat
    ) VALUES (
        'cskh_test@flexshop.vn',
        '0944112233',
        '$2a$10$b.bp6UGuVLioQIiICqTFNOS0whjXnb4ivL9bi3smPXbtO.rKt4cFS',
        N'Lê Thị Chăm Sóc',
        'https://ui-avatars.com/api/?name=Le+Thi+Cham+Soc&background=10b981&color=fff',
        'HOAT_DONG',
        0,
        DATEADD(DAY, -2, GETDATE()),
        GETDATE()
    );

    DECLARE @IdCskh BIGINT = SCOPE_IDENTITY();
    DECLARE @RoleCskhId INT = (SELECT TOP 1 ma_vai_tro FROM vai_tro WHERE ten_vai_tro = 'CSKH');

    IF (@RoleCskhId IS NOT NULL)
        INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@IdCskh, @RoleCskhId, GETDATE());

    PRINT N'-> Đã thêm nhân viên CSKH: cskh_test@flexshop.vn (Vai trò duy nhất: CSKH)';
END
ELSE
BEGIN
    PRINT N'-> Tài khoản cskh_test@flexshop.vn đã tồn tại.';
END
GO

-- 5. Thêm Người dùng có trạng thái Bị Khóa (Chỉ 1 vai trò KHACH_HANG)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = 'user_bikhoa@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (
        email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, 
        anh_dai_dien, trang_thai, da_xoa, ngay_tao, ngay_cap_nhat
    ) VALUES (
        'user_bikhoa@flexshop.vn',
        '0955112233',
        '$2a$10$b.bp6UGuVLioQIiICqTFNOS0whjXnb4ivL9bi3smPXbtO.rKt4cFS',
        N'Trần Văn Vi Phạm',
        'https://ui-avatars.com/api/?name=Tran+Van+Vi+Pham&background=ef4444&color=fff',
        'BI_KHOA',
        0,
        DATEADD(DAY, -10, GETDATE()),
        GETDATE()
    );

    DECLARE @IdBiKhoa BIGINT = SCOPE_IDENTITY();
    DECLARE @RoleKhachHangId INT = (SELECT TOP 1 ma_vai_tro FROM vai_tro WHERE ten_vai_tro = 'KHACH_HANG');

    IF (@RoleKhachHangId IS NOT NULL)
        INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro, ngay_gan) VALUES (@IdBiKhoa, @RoleKhachHangId, GETDATE());

    PRINT N'-> Đã thêm tài khoản Bị Khóa: user_bikhoa@flexshop.vn (Vai trò: KHACH_HANG, Trạng thái: BI_KHOA)';
END
ELSE
BEGIN
    PRINT N'-> Tài khoản user_bikhoa@flexshop.vn đã tồn tại.';
END
GO

-- 6. Kiểm tra lại danh sách người dùng - Mỗi người dùng chỉ sở hữu 1 vai trò duy nhất
SELECT 
    nd.ma_nguoi_dung AS ID,
    nd.ho_va_ten AS HoVaTen,
    nd.email AS Email,
    nd.so_dien_thoai AS SoDienThoai,
    nd.trang_thai AS TrangThai,
    vt.ten_vai_tro AS VaiTroDuyNhat,
    nd.ngay_tao AS NgayTao
FROM nguoi_dung nd
LEFT JOIN nguoi_dung_vai_tro ndvt ON nd.ma_nguoi_dung = ndvt.ma_nguoi_dung
LEFT JOIN vai_tro vt ON ndvt.ma_vai_tro = vt.ma_vai_tro
WHERE nd.da_xoa = 0
ORDER BY nd.ma_nguoi_dung DESC;
GO
