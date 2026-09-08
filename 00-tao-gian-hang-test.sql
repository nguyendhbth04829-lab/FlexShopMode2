-- Script tạo dữ liệu gốc (Người dùng & Gian hàng) để thỏa mãn Khóa ngoại (Foreign Key)

-- 1. Tắt tạm kiểm tra khóa ngoại (đề phòng thứ tự)
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all"
GO

-- 2. Đảm bảo có người dùng ID = 1 (Tài khoản chủ shop)
SET IDENTITY_INSERT nguoi_dung ON;
INSERT INTO nguoi_dung (ma_nguoi_dung, email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
VALUES (1, 'shoptest@gmail.com', '0987654321', '123456', N'Chủ Shop Test', N'HOAT_DONG', 0);
SET IDENTITY_INSERT nguoi_dung OFF;
GO

-- 3. Đảm bảo có gian hàng ID = 1
SET IDENTITY_INSERT gian_hang ON;
INSERT INTO gian_hang (ma_gian_hang, ma_chu_so_huu, ten_gian_hang, duong_dan_slug, dia_chi_kho, sdt_kho, trang_thai, hang_gian_hang, diem_sao_qua_ta)
VALUES (1, 1, N'Gian Hàng FlexShop Test', 'gian-hang-flexshop-test', N'Hà Nội', '0987654321', N'DA_DUYET', N'CHUAN', 0);
SET IDENTITY_INSERT gian_hang OFF;
GO

-- 4. Bật lại kiểm tra khóa ngoại
EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all"
GO