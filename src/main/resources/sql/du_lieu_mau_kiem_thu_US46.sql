-- ============================================================================
-- KỊCH BẢN DỮ LIỆU MẪU KIỂM THỬ US-46: CSKH DASHBOARD & ĐỐI CHIẾU 3 BÊN
-- Database: FlexShop_V2_Full
-- Ngày tạo: 2026-09-04
-- ============================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;
PRINT N'=== BẮT ĐẦU ĐỒNG BỘ DỮ LIỆU MẪU CHO US-46 (CSKH ĐỐI CHIẾU 3 BÊN & POD) ===';

-- 1. NẠP TÀI XẾ GIAO HÀNG (SHIPPER)
DECLARE @maShipperUser BIGINT;

IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = N'shipper.nhanh@flexshop.vn')
BEGIN
    INSERT INTO nguoi_dung (email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, trang_thai, da_xoa)
    VALUES (N'shipper.nhanh@flexshop.vn', N'0987654321', N'$2a$10$7EqJtq98hPqEX7fNZaFWoO3jS8e1s89P1dJ9q6V8J3v8v8k9v7v9m', N'Phạm Văn Giao Vận (Shipper Nhanh)', N'HOAT_DONG', 0);
END
SELECT @maShipperUser = ma_nguoi_dung FROM nguoi_dung WHERE email = N'shipper.nhanh@flexshop.vn';

-- Gán vai trò SHIPPER
DECLARE @maVaiTroShipper INT;
SELECT @maVaiTroShipper = ma_vai_tro FROM vai_tro WHERE ten_vai_tro = N'SHIPPER';
IF @maVaiTroShipper IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nguoi_dung_vai_tro WHERE ma_nguoi_dung = @maShipperUser AND ma_vai_tro = @maVaiTroShipper)
BEGIN
    INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro) VALUES (@maShipperUser, @maVaiTroShipper);
END

-- 2. HỒ SƠ TÀI XẾ GIAO HÀNG (tai_xe_giao_hang)
DECLARE @maTaiXe BIGINT;
IF NOT EXISTS (SELECT 1 FROM tai_xe_giao_hang WHERE ma_nguoi_dung = @maShipperUser)
BEGIN
    INSERT INTO tai_xe_giao_hang (ma_nguoi_dung, loai_phuong_tien, bien_so_xe, dang_truc_tuyen, vi_do_hien_tai, kinh_do_hien_tai, so_du_cod_dang_giu, diem_danh_gia_tb, trang_thai)
    VALUES (@maShipperUser, N'Xe máy Honda Wave Alpha', N'29-G1 888.99', 1, 21.028511, 105.854444, 585000, 4.95, N'HOAT_DONG');
END
SELECT @maTaiXe = ma_tai_xe FROM tai_xe_giao_hang WHERE ma_nguoi_dung = @maShipperUser;

-- 3. CHI TIẾT SẢN PHẨM SHOP ĐÓNG GÓI (chi_tiet_don_hang)
DECLARE @maDonTech BIGINT;
SELECT @maDonTech = ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-20260901-88';
IF @maDonTech IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDonTech)
BEGIN
    INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
    VALUES (@maDonTech, 1, N'Tai nghe Bluetooth Sony WH-1000XM5 Chống Ồn Cao Cấp', N'Màu Đen Nhám - Bluetooth 5.3', N'SKU-SONY-WH5-BLK', 1250000.00, 1, 1250000.00);
END

DECLARE @maDonFashion62 BIGINT;
SELECT @maDonFashion62 = ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-FASHION-READY-US45';
IF @maDonFashion62 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDonFashion62)
BEGIN
    INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
    VALUES (@maDonFashion62, 2, N'Áo Polo Nam Thể Thao Co Giãn Thoáng Khí Cao Cấp', N'Màu Trắng Phối Sọc - Size L', N'SKU-POLO-WHT-L', 450000.00, 1, 450000.00);
END

-- 4. NHIỆM VỤ GIAO HÀNG & ẢNH BẰNG CHỨNG POD (nhiem_vu_giao_hang)
-- Đơn hàng 1: SHOP-TECH-20260901-88
IF @maDonTech IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nhiem_vu_giao_hang WHERE ma_don_hang_shop = @maDonTech)
BEGIN
    INSERT INTO nhiem_vu_giao_hang (
        ma_don_hang_shop, ma_tai_xe, loai_nhiem_vu, trang_thai, tien_cod_can_thu, da_thu_cod,
        link_anh_bang_chung_pod, vi_do_giao_hang, kinh_do_giao_hang, ma_otp_xac_nhan, so_lan_giao,
        thoi_gian_lay_hang, thoi_gian_giao_thanh_cong, ngay_tao
    ) VALUES (
        @maDonTech, @maTaiXe, N'GIAO_HANG', N'THANH_CONG', 0, 1,
        N'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=800',
        21.028511, 105.854444, N'8899', 1,
        DATEADD(HOUR, -6, GETDATE()), DATEADD(HOUR, -2, GETDATE()), DATEADD(DAY, -1, GETDATE())
    );
END

-- Đơn hàng Order 62 (SHOP-FASHION-READY-US45)
IF @maDonFashion62 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nhiem_vu_giao_hang WHERE ma_don_hang_shop = @maDonFashion62)
BEGIN
    INSERT INTO nhiem_vu_giao_hang (
        ma_don_hang_shop, ma_tai_xe, loai_nhiem_vu, trang_thai, tien_cod_can_thu, da_thu_cod,
        link_anh_bang_chung_pod, vi_do_giao_hang, kinh_do_giao_hang, ma_otp_xac_nhan, so_lan_giao,
        thoi_gian_lay_hang, thoi_gian_giao_thanh_cong, ngay_tao
    ) VALUES (
        @maDonFashion62, @maTaiXe, N'GIAO_HANG', N'THANH_CONG', 0, 1,
        N'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=800',
        21.028511, 105.854444, N'6688', 1,
        DATEADD(HOUR, -5, GETDATE()), DATEADD(HOUR, -1, GETDATE()), DATEADD(DAY, -1, GETDATE())
    );
END

-- Đơn hàng 2: SHOP-FASHION-20260903-99
DECLARE @maDonFashion BIGINT;
SELECT @maDonFashion = ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-FASHION-20260903-99';

IF @maDonFashion IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nhiem_vu_giao_hang WHERE ma_don_hang_shop = @maDonFashion)
BEGIN
    INSERT INTO nhiem_vu_giao_hang (
        ma_don_hang_shop, ma_tai_xe, loai_nhiem_vu, trang_thai, tien_cod_can_thu, da_thu_cod,
        link_anh_bang_chung_pod, vi_do_giao_hang, kinh_do_giao_hang, ma_otp_xac_nhan, so_lan_giao,
        thoi_gian_lay_hang, thoi_gian_giao_thanh_cong, ngay_tao
    ) VALUES (
        @maDonFashion, @maTaiXe, N'GIAO_HANG', N'THANH_CONG', 585000, 1,
        N'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800',
        21.033333, 105.833333, N'7766', 1,
        DATEADD(HOUR, -12, GETDATE()), DATEADD(HOUR, -8, GETDATE()), DATEADD(DAY, -1, GETDATE())
    );
END

-- 4. DÒNG THỜI GIAN LỊCH SỬ TRẠNG THÁI ĐƠN HÀNG (lich_su_trang_thai_don)
IF @maDonTech IS NOT NULL AND NOT EXISTS (SELECT 1 FROM lich_su_trang_thai_don WHERE ma_don_hang_shop = @maDonTech)
BEGIN
    INSERT INTO lich_su_trang_thai_don (ma_don_hang_shop, trang_thai_cu, trang_thai_moi, nguoi_thuc_hien, ghi_chu, thoi_gian)
    VALUES 
    (@maDonTech, NULL, N'CHO_XAC_NHAN', N'Hệ thống tự động', N'Khách hàng đặt hàng thành công qua cổng thanh toán VNPay', DATEADD(DAY, -2, GETDATE())),
    (@maDonTech, N'CHO_XAC_NHAN', N'DA_XAC_NHAN', N'Chủ Shop (TechZone)', N'Gian hàng đã xác nhận đơn và in phiếu đóng gói', DATEADD(HOUR, -36, GETDATE())),
    (@maDonTech, N'DA_XAC_NHAN', N'DANG_GIAO', N'Shipper: Phạm Văn Giao Vận', N'Đã lấy hàng tại Kho Tổng Từ Liêm, đang vận chuyển đến khách', DATEADD(HOUR, -6, GETDATE())),
    (@maDonTech, N'DANG_GIAO', N'DA_GIAO', N'Shipper: Phạm Văn Giao Vận', N'Giao hàng thành công tận tay người nhận, đã chụp ảnh POD xác thực', DATEADD(HOUR, -2, GETDATE())),
    (@maDonTech, N'DA_GIAO', N'KHACH_KHIEU_NAI', N'Khách: Nguyễn Văn An', N'Khách mở phiếu khiếu nại KN-260901-1001: Tai nghe bị hỏng vỡ gọng khi nhận', DATEADD(MINUTE, -90, GETDATE())),
    (@maDonTech, N'KHACH_KHIEU_NAI', N'CSKH_TIEP_NHAN', N'CSKH: Ngô Thị CSKH Hỗ Trợ', N'Bắt đầu quy trình đối chiếu 3 bên (Khách - Shop - Shipper POD)', DATEADD(MINUTE, -30, GETDATE()));
END

IF @maDonFashion IS NOT NULL AND NOT EXISTS (SELECT 1 FROM lich_su_trang_thai_don WHERE ma_don_hang_shop = @maDonFashion)
BEGIN
    INSERT INTO lich_su_trang_thai_don (ma_don_hang_shop, trang_thai_cu, trang_thai_moi, nguoi_thuc_hien, ghi_chu, thoi_gian)
    VALUES 
    (@maDonFashion, NULL, N'CHO_XAC_NHAN', N'Khách: Hoàng Thùy Linh', N'Đặt hàng phương thức COD', DATEADD(DAY, -3, GETDATE())),
    (@maDonFashion, N'CHO_XAC_NHAN', N'DA_XAC_NHAN', N'Chủ Shop: Lê Thu Hà', N'Xác nhận đơn và chuẩn bị 2 áo sơ mi xanh pastel size L', DATEADD(DAY, -2, GETDATE())),
    (@maDonFashion, N'DA_XAC_NHAN', N'DANG_GIAO', N'Shipper: Phạm Văn Giao Vận', N'Lấy hàng và đi giao', DATEADD(HOUR, -12, GETDATE())),
    (@maDonFashion, N'DANG_GIAO', N'DA_GIAO', N'Shipper: Phạm Văn Giao Vận', N'Giao thành công, thu COD 585.000 VNĐ', DATEADD(HOUR, -8, GETDATE())),
    (@maDonFashion, N'DA_GIAO', N'KHACH_KHIEU_NAI', N'Khách: Hoàng Thùy Linh', N'Mở phiếu KN-260902-1002 phản ánh giao nhầm màu đen size M', DATEADD(HOUR, -4, GETDATE()));
END

-- 5. GHI CHÚ ĐIỀU TRA NỘI BỘ CSKH (ghi_chu_noi_bo_khieu_nai)
DECLARE @maPhieu1 BIGINT, @maPhieu2 BIGINT;
SELECT @maPhieu1 = ma_phieu FROM phieu_khieu_nai WHERE ma_code_phieu = N'KN-260901-1001';
SELECT @maPhieu2 = ma_phieu FROM phieu_khieu_nai WHERE ma_code_phieu = N'KN-260902-1002';

DECLARE @maCskhUser BIGINT;
SELECT @maCskhUser = ma_nguoi_dung FROM nguoi_dung WHERE email = N'cskh@flexshop.vn';

IF @maPhieu1 IS NOT NULL AND @maCskhUser IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM ghi_chu_noi_bo_khieu_nai WHERE ma_phieu = @maPhieu1 AND noi_dung LIKE N'%[Đối chiếu Shipper]%')
    BEGIN
        INSERT INTO ghi_chu_noi_bo_khieu_nai (ma_phieu, ma_nhan_vien, noi_dung, ngay_tao)
        VALUES (@maPhieu1, @maCskhUser, N'[Đối chiếu Shipper]: Đã liên hệ trực tiếp với tài xế Phạm Văn Giao Vận. Tài xế cho biết lúc nhận hàng từ kho vỏ hộp đã có dấu hiệu bị chèn ép móp góc, tuy nhiên vẫn nhận đi giao vì tưởng đóng gói chống sốc bên trong còn tốt.', DATEADD(MINUTE, -20, GETDATE()));
    END

    IF NOT EXISTS (SELECT 1 FROM ghi_chu_noi_bo_khieu_nai WHERE ma_phieu = @maPhieu1 AND noi_dung LIKE N'%[Đối chiếu Shop TechZone]%')
    BEGIN
        INSERT INTO ghi_chu_noi_bo_khieu_nai (ma_phieu, ma_nhan_vien, noi_dung, ngay_tao)
        VALUES (@maPhieu1, @maCskhUser, N'[Đối chiếu Shop TechZone]: Yêu cầu Shop gửi video đóng gói tại bàn kiểm tra. Shop đã phản hồi qua email xác nhận hộp xuất kho còn nguyên vẹn, nghi vấn xảy ra va đập mạnh trong quá trình phân loại ở Hub trung chuyển.', DATEADD(MINUTE, -10, GETDATE()));
    END
END

PRINT N'=== HOÀN TẤT ĐỒNG BỘ DỮ LIỆU MẪU CHO US-46 THÀNH CÔNG! ===';
GO
