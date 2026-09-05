-- ==============================================================================
-- KỊCH BẢN NẠP DỮ LIỆU MẪU KIỂM THỬ US-47: CSKH RA PHÁN QUYẾT TRANH CHẤP
-- TỰ ĐỘNG TẠO LỆNH HOÀN TIỀN / BỒI THƯỜNG & ĐỒNG BỘ VÍ NGƯỜI BÁN
-- Hệ quản trị CSDL: Microsoft SQL Server (FlexShop_V2_Full)
-- ==============================================================================

USE FlexShop_V2_Full;
GO

-- 1. KHỞI TẠO VÍ NGƯỜI BÁN (vi_nguoi_ban) CHO CÁC GIAN HÀNG NẾU CHƯA CÓ
IF NOT EXISTS (SELECT 1 FROM vi_nguoi_ban WHERE ma_gian_hang = 1)
BEGIN
    INSERT INTO vi_nguoi_ban (ma_gian_hang, so_du_kha_dung, so_du_tam_giu_escrow, tong_tien_da_rut, phien_ban_lock, ngay_cap_nhat)
    VALUES (1, 15000000.00, 5000000.00, 2000000.00, 0, GETDATE());
END
ELSE
BEGIN
    UPDATE vi_nguoi_ban 
    SET so_du_kha_dung = 15000000.00, so_du_tam_giu_escrow = 5000000.00, ngay_cap_nhat = GETDATE()
    WHERE ma_gian_hang = 1;
END

IF NOT EXISTS (SELECT 1 FROM vi_nguoi_ban WHERE ma_gian_hang = 2)
BEGIN
    INSERT INTO vi_nguoi_ban (ma_gian_hang, so_du_kha_dung, so_du_tam_giu_escrow, tong_tien_da_rut, phien_ban_lock, ngay_cap_nhat)
    VALUES (2, 20000000.00, 8000000.00, 5000000.00, 0, GETDATE());
END
ELSE
BEGIN
    UPDATE vi_nguoi_ban 
    SET so_du_kha_dung = 20000000.00, so_du_tam_giu_escrow = 8000000.00, ngay_cap_nhat = GETDATE()
    WHERE ma_gian_hang = 2;
END
GO

-- 2. TẠO THÊM ĐƠN HÀNG VÀ TICKET MẪU PHỤC VỤ CÁC KỊCH BẢN PHÁN QUYẾT US-47
DECLARE @maKhachHang BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');
DECLARE @maCskhUser BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'cskh@flexshop.vn');
DECLARE @maAdminUser BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'admin@flexshop.vn');

-- Kịch bản A: Đơn hàng và Ticket KN-260905-1005 (Tranh chấp Vận chuyển làm hỏng hàng -> Bồi thường Shop)
DECLARE @maDonTong1 BIGINT = (SELECT TOP 1 ma_don_hang_tong FROM don_hang_tong WHERE ma_khach_hang = @maKhachHang);

IF NOT EXISTS (SELECT 1 FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-20260905-99')
BEGIN
    INSERT INTO don_hang_shop (
        ma_code_don_shop, ma_don_hang_tong, ma_gian_hang, tien_hang_shop, phi_van_chuyen,
        tien_thue_vat, giam_gia_voucher_shop, giam_gia_voucher_san, tong_tien_shop_nhan,
        trang_thai, ma_van_don, ngay_tao
    ) VALUES (
        N'SHOP-TECH-20260905-99', @maDonTong1, 1, 850000.00, 30000.00,
        0, 0, 0, 850000.00,
        N'DA_GIAO', N'GHN-EXPRESS-9988', DATEADD(DAY, -3, GETDATE())
    );
END

DECLARE @maDonTech99 BIGINT = (SELECT ma_don_hang_shop FROM don_hang_shop WHERE ma_code_don_shop = N'SHOP-TECH-20260905-99');

-- Chi tiết đơn hàng
IF @maDonTech99 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE ma_don_hang_shop = @maDonTech99)
BEGIN
    DECLARE @maBienTheSP1 BIGINT = (SELECT TOP 1 ma_bien_the FROM bien_the_san_pham);
    IF @maBienTheSP1 IS NOT NULL
    BEGIN
        INSERT INTO chi_tiet_don_hang (ma_don_hang_shop, ma_bien_the, so_luong, don_gia, tong_tien, ten_san_pham, ten_bien_the, ma_sku)
        VALUES (@maDonTech99, @maBienTheSP1, 1, 850000.00, 850000.00, N'Bàn Phím Cơ Không Dây RGB Cao Cấp', N'Bản LED RGB / Red Switch', N'KEY-RGB-RED');
    END
END

-- Nhiệm vụ giao hàng có POD bị rách hộp ngoài
DECLARE @maTaiXe BIGINT = (SELECT TOP 1 ma_tai_xe FROM tai_xe_giao_hang);
IF @maDonTech99 IS NOT NULL AND @maTaiXe IS NOT NULL AND NOT EXISTS (SELECT 1 FROM nhiem_vu_giao_hang WHERE ma_don_hang_shop = @maDonTech99)
BEGIN
    INSERT INTO nhiem_vu_giao_hang (
        ma_don_hang_shop, ma_tai_xe, loai_nhiem_vu, trang_thai, tien_cod_can_thu, da_thu_cod,
        link_anh_bang_chung_pod, vi_do_giao_hang, kinh_do_giao_hang, ma_otp_xac_nhan, so_lan_giao,
        thoi_gian_lay_hang, thoi_gian_giao_thanh_cong, ngay_tao
    ) VALUES (
        @maDonTech99, @maTaiXe, N'GIAO_HANG', N'THANH_CONG', 0, 1,
        N'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=800',
        21.028511, 105.854444, N'9988', 1,
        DATEADD(HOUR, -24, GETDATE()), DATEADD(HOUR, -20, GETDATE()), DATEADD(DAY, -2, GETDATE())
    );
END

-- Lịch sử đơn
IF @maDonTech99 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM lich_su_trang_thai_don WHERE ma_don_hang_shop = @maDonTech99)
BEGIN
    INSERT INTO lich_su_trang_thai_don (ma_don_hang_shop, trang_thai_cu, trang_thai_moi, nguoi_thuc_hien, ghi_chu, thoi_gian)
    VALUES 
    (@maDonTech99, NULL, N'CHO_XAC_NHAN', N'Hệ thống tự động', N'Khách đặt đơn thanh toán online thành công', DATEADD(DAY, -3, GETDATE())),
    (@maDonTech99, N'CHO_XAC_NHAN', N'DA_GIAO', N'Shipper: Phạm Văn Giao Vận', N'Đã giao hàng thành công tận nơi', DATEADD(HOUR, -20, GETDATE())),
    (@maDonTech99, N'DA_GIAO', N'KHACH_KHIEU_NAI', N'Khách: Nguyễn Văn An', N'Khách mở khiếu nại KN-260905-1005: Bàn phím bị cong mạch do va đập vận chuyển', DATEADD(HOUR, -10, GETDATE()));
END

-- Tạo Ticket KN-260905-1005 (Đang xử lý, sẵn sàng để CSKH ra phán quyết)
IF @maDonTech99 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM phieu_khieu_nai WHERE ma_code_phieu = N'KN-260905-1005')
BEGIN
    INSERT INTO phieu_khieu_nai (
        ma_code_phieu, ma_khach_hang, ma_don_hang_shop, ma_gian_hang,
        loai_khieu_nai, muc_do_uu_tien, trang_thai, noi_dung_mo_ta,
        giai_phap_yeu_cau, ma_cskh_xu_ly, so_tien_hoan_tra, ngay_tao
    ) VALUES (
        N'KN-260905-1005', @maKhachHang, @maDonTech99, 1,
        N'HONG_VO', N'CAO', N'DANG_XU_LY',
        N'Bàn phím cơ bị móp gãy khung nhôm bên trong, hộp ngoài có vết chèn ép rách thủng. Yêu cầu bồi thường thiệt hại.',
        N'HOAN_TIEN_TRA_HANG', @maCskhUser, 850000.00, DATEADD(HOUR, -10, GETDATE())
    );
END
GO

-- 3. NẠP MẪU CHỨNG TỪ LỆNH HOÀN TIỀN / BỒI THƯỜNG ĐÃ THỰC HIỆN ĐỂ TEST THỐNG KÊ (KPI)
DECLARE @maPhieu1001 BIGINT = (SELECT ma_phieu FROM phieu_khieu_nai WHERE ma_code_phieu = N'KN-260901-1001');
DECLARE @maKhach BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'khachhang@flexshop.vn');
DECLARE @maChuShopTech BIGINT = (SELECT ma_chu_so_huu FROM gian_hang WHERE ma_gian_hang = 1);
DECLARE @maViShop1 BIGINT = (SELECT ma_vi FROM vi_nguoi_ban WHERE ma_gian_hang = 1);

-- Đảm bảo ticket KN-260901-1001 có lệnh hoàn tiền mẫu
IF @maPhieu1001 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM lenh_hoan_tien_boi_thuong WHERE ma_phieu = @maPhieu1001)
BEGIN
    INSERT INTO lenh_hoan_tien_boi_thuong (ma_phieu, nguoi_nhan_tien, so_tien, ben_chiu_phi, trang_thai, ngay_thuc_hien)
    VALUES (@maPhieu1001, @maKhach, 1280000.00, N'NGUOI_BAN', N'DA_CHUYEN_TIEN', DATEADD(HOUR, -2, GETDATE()));
    
    -- Ghi log ví shop
    IF @maViShop1 IS NOT NULL
    BEGIN
        INSERT INTO lich_su_giao_dich_vi (ma_vi, loai_giao_dich, so_tien, so_du_sau_giao_dich, ma_tham_chieu, mo_ta, ngay_tao)
        VALUES (@maViShop1, N'TRU_TIEN_HOAN_TRA', -1280000.00, 18720000.00, N'KN-260901-1001', N'Khấu trừ hoàn tiền khiếu nại KN-260901-1001 cho khách', DATEADD(HOUR, -2, GETDATE()));
    END
END

-- Cập nhật thông tin phán quyết cho ticket KN-260901-1001
DECLARE @maCskh BIGINT = (SELECT ma_nguoi_dung FROM nguoi_dung WHERE email = N'cskh@flexshop.vn');
IF @maPhieu1001 IS NOT NULL
BEGIN
    UPDATE phieu_khieu_nai
    SET trang_thai = N'CHAP_NHAN_HOAN_TIEN',
        ma_nguoi_phan_quyet = @maCskh,
        ghi_chu_phan_quyet = N'Đối chiếu ảnh mở hộp của khách và ảnh POD của tài xế xác nhận sản phẩm bị vỡ gọng tai nghe. Lỗi thuộc về khâu đóng gói của người bán thiếu lớp đệm bóng khí chống sốc. Phán quyết: Duyệt hoàn tiền 1.280.000 đ cho khách, Shop chịu phí.'
    WHERE ma_phieu = @maPhieu1001;
END
GO

PRINT N'>>> ĐÃ NẠP DỮ LIỆU MẪU KIỂM THỬ US-47 THÀNH CÔNG VÀO CƠ SỞ DỮ LIỆU FlexShop_V2_Full!';
GO
