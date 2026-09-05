USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;

-- 1. Bổ sung chi_tiet_don_hang cho Order 1 (SHOP-TECH-20260901-88)
DECLARE @maDon1 BIGINT;
SELECT @maDon1 = ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-20260901-88';
IF @maDon1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon1)
BEGIN
    INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
    VALUES (@maDon1, 1, N'Tai nghe Bluetooth Sony WH-1000XM5 Chống Ồn Cao Cấp', N'Màu Đen Nhám - Bluetooth 5.3', N'SKU-SONY-WH5-BLK', 1250000.00, 1, 1250000.00);
    PRINT N'Đã bổ sung chi tiết sản phẩm cho Order 1.';
END

-- 2. Bổ sung chi_tiet_don_hang cho Order 62 (SHOP-FASHION-READY-US45)
DECLARE @maDon62 BIGINT;
SELECT @maDon62 = ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-FASHION-READY-US45';
IF @maDon62 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDon62)
BEGIN
    INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, ten_san_pham, ten_bien_the, ma_sku, don_gia, so_luong, tong_tien)
    VALUES (@maDon62, 2, N'Áo Polo Nam Thể Thao Co Giãn Thoáng Khí Cao Cấp', N'Màu Trắng Phối Sọc - Size L', N'SKU-POLO-WHT-L', 450000.00, 1, 450000.00);
    PRINT N'Đã bổ sung chi tiết sản phẩm cho Order 62.';
END

-- 3. Bổ sung nhiem_vu_giao_hang cho Order 62
DECLARE @maTaiXe BIGINT;
SELECT TOP 1 @maTaiXe = ma_tai_xe FROM tai_xe_giao_hang WHERE trang_thai = N'HOAT_DONG';

IF @maDon62 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nhiem_vu_giao_hang WHERE ma_don_hang_shop = @maDon62)
BEGIN
    INSERT INTO nhiem_vu_giao_hang (
        ma_don_hang_shop, ma_tai_xe, loai_nhiem_vu, trang_thai, tien_cod_can_thu, da_thu_cod,
        link_anh_bang_chung_pod, vi_do_giao_hang, kinh_do_giao_hang, ma_otp_xac_nhan, so_lan_giao,
        thoi_gian_lay_hang, thoi_gian_giao_thanh_cong, ngay_tao
    ) VALUES (
        @maDon62, @maTaiXe, N'GIAO_HANG', N'THANH_CONG', 0, 1,
        N'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=800',
        21.028511, 105.854444, N'6688', 1,
        DATEADD(HOUR, -5, GETDATE()), DATEADD(HOUR, -1, GETDATE()), DATEADD(DAY, -1, GETDATE())
    );
    PRINT N'Đã bổ sung nhiệm vụ giao hàng và ảnh POD cho Order 62.';
END

-- 4. Bổ sung lịch sử trạng thái đơn cho Order 62
IF @maDon62 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM lich_su_trang_thai_don WHERE ma_don_hang_shop = @maDon62)
BEGIN
    INSERT INTO lich_su_trang_thai_don (ma_don_hang_shop, trang_thai_cu, trang_thai_moi, nguoi_thuc_hien, ghi_chu, thoi_gian)
    VALUES 
    (@maDon62, NULL, N'CHO_XAC_NHAN', N'Khách hàng: Nguyễn Văn An', N'Khách đặt hàng thành công', DATEADD(DAY, -2, GETDATE())),
    (@maDon62, N'CHO_XAC_NHAN', N'DANG_CHUAN_BI', N'Shop: Thời Trang Nam Cao Cấp', N'Shop xác nhận và đóng gói kiện hàng', DATEADD(HOUR, -24, GETDATE())),
    (@maDon62, N'DANG_CHUAN_BI', N'DANG_GIAO', N'Shipper: Phạm Văn Giao Vận', N'Lấy hàng từ kho và tiến hành giao hàng', DATEADD(HOUR, -5, GETDATE())),
    (@maDon62, N'DANG_GIAO', N'DA_GIAO', N'Shipper: Phạm Văn Giao Vận', N'Giao hàng thành công tận tay người nhận, đã chụp ảnh POD', DATEADD(HOUR, -1, GETDATE()));
    PRINT N'Đã bổ sung lịch sử trạng thái cho Order 62.';
END

PRINT N'=== HOÀN TẤT BỔ SUNG DỮ LIỆU ĐỐI CHIẾU 3 BÊN CHO 100% ĐƠN HÀNG! ===';
GO
