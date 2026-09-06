-- =================================================================================================
-- KỊCH BẢN DỮ LIỆU MẪU KIỂM THỬ US-59 (MODULE: CHAT - PHÂN HỆ KHÁCH HÀNG & SELLER)
-- ĐỀ TÀI: LIVE CHAT TRỰC TIẾP VỚI SHOP VÀ TRẢ GIÁ SẢN PHẨM (MAKE AN OFFER)
-- CSDL: FlexShop_V2_Full (Hỗ trợ chuẩn Tiếng Việt Unicode N'')
-- =================================================================================================

USE [FlexShop_V2_Full];
GO

-- 1. TẠO BẢNG de_xuat_tra_gia (NẾU CHƯA CÓ)
IF OBJECT_ID('de_xuat_tra_gia', 'U') IS NULL
BEGIN
    CREATE TABLE de_xuat_tra_gia (
        ma_de_xuat BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_cuoc_tro_chuyen BIGINT NOT NULL,
        ma_khach_hang BIGINT NOT NULL,
        ma_gian_hang BIGINT NOT NULL,
        ma_san_pham BIGINT NOT NULL,
        ma_bien_the BIGINT NULL,
        so_luong INT NOT NULL DEFAULT 1,
        gia_goc DECIMAL(18,2) NOT NULL,
        gia_de_xuat DECIMAL(18,2) NOT NULL,
        gia_shop_phan_hoi DECIMAL(18,2) NULL,
        trang_thai NVARCHAR(30) NOT NULL DEFAULT 'CHO_DUYET', -- 'CHO_DUYET', 'DONG_Y', 'TU_CHOI', 'PHAN_HOI_LAI', 'HET_HAN'
        ghi_chu_khach NVARCHAR(500) NULL,
        phan_hoi_shop NVARCHAR(500) NULL,
        ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        ngay_cap_nhat DATETIME2 NULL,
        ngay_het_han DATETIME2 NULL,
        CONSTRAINT FK_TraGia_CuocTroChuyen FOREIGN KEY (ma_cuoc_tro_chuyen) REFERENCES cuoc_tro_chuyen(ma_cuoc_tro_chuyen) ON DELETE CASCADE,
        CONSTRAINT FK_TraGia_KhachHang FOREIGN KEY (ma_khach_hang) REFERENCES nguoi_dung(ma_nguoi_dung),
        CONSTRAINT FK_TraGia_GianHang FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang),
        CONSTRAINT FK_TraGia_SanPham FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham)
    );
    PRINT N'Đã tạo bảng de_xuat_tra_gia thành công.';
END
GO

-- 2. KHỞI TẠO CUỘC TRÒ CHUYỆN MẪU GIỮA KHÁCH HÀNG VÀ SHOP
-- Khách hàng: Nguyễn Văn An (ID: 4, email: khachhang@flexshop.vn)
-- Shop: TechZone Flagship Store (ID: 1, chủ sở hữu ID: 2 - techzone@flexshop.vn)
DECLARE @MaKhachHang BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = 'khachhang@flexshop.vn' OR ma_nguoi_dung = 4);
DECLARE @MaGianHang BIGINT = 1; -- TechZone Flagship Store
DECLARE @MaSanPhamTaiNghe BIGINT = 1; -- Tai nghe Sony WH-1000XM5
DECLARE @MaBienTheTaiNghe BIGINT = 1; -- Biến thể 1
DECLARE @MaCuocTroChuyen BIGINT;

IF @MaKhachHang IS NOT NULL AND EXISTS (SELECT 1 FROM gian_hang WHERE ma_gian_hang = @MaGianHang)
BEGIN
    -- Kiểm tra xem đã có cuộc trò chuyện giữa Khách và Shop chưa
    SELECT @MaCuocTroChuyen = ma_cuoc_tro_chuyen 
    FROM cuoc_tro_chuyen 
    WHERE ma_khach_hang = @MaKhachHang AND ma_gian_hang = @MaGianHang;

    IF @MaCuocTroChuyen IS NULL
    BEGIN
        INSERT INTO cuoc_tro_chuyen (ma_khach_hang, ma_gian_hang, tin_nhan_cuoi_cung, thoi_gian_tin_cuoi, so_tin_chua_doc_khach, so_tin_chua_doc_shop, ngay_tao)
        VALUES (@MaKhachHang, @MaGianHang, N'Shop ơi mình gửi đề xuất trả giá tai nghe Sony nhé!', SYSDATETIME(), 0, 1, DATEADD(minute, -30, SYSDATETIME()));
        
        SET @MaCuocTroChuyen = SCOPE_IDENTITY();
        PRINT N'Đã tạo mới cuộc trò chuyện ID: ' + CAST(@MaCuocTroChuyen AS NVARCHAR(20));
    END
    ELSE
    BEGIN
        UPDATE cuoc_tro_chuyen
        SET tin_nhan_cuoi_cung = N'Shop ơi mình gửi đề xuất trả giá tai nghe Sony nhé!',
            thoi_gian_tin_cuoi = SYSDATETIME(),
            so_tin_chua_doc_khach = 0,
            so_tin_chua_doc_shop = 1
        WHERE ma_cuoc_tro_chuyen = @MaCuocTroChuyen;
    END

    -- Làm sạch tin nhắn và đề xuất cũ của cuộc trò chuyện này để nạp dữ liệu mẫu chuẩn nhất
    DELETE FROM de_xuat_tra_gia WHERE ma_cuoc_tro_chuyen = @MaCuocTroChuyen;
    DELETE FROM tin_nhan WHERE ma_cuoc_tro_chuyen = @MaCuocTroChuyen;

    -- Tin nhắn 1: Khách hàng chào hỏi
    INSERT INTO tin_nhan (ma_cuoc_tro_chuyen, loai_nguoi_gui, ma_nguoi_gui, loai_tin_nhan, noi_dung, du_lieu_dinh_kem_json, da_xem, ngay_tao)
    VALUES (@MaCuocTroChuyen, 'KHACH_HANG', @MaKhachHang, 'VAN_BAN', 
            N'Chào Shop, mẫu tai nghe Sony WH-1000XM5 này bên mình còn hàng sẵn ở kho Hà Nội không ạ?', 
            NULL, 1, DATEADD(minute, -25, SYSDATETIME()));

    -- Tin nhắn 2: Shop TechZone phản hồi
    INSERT INTO tin_nhan (ma_cuoc_tro_chuyen, loai_nguoi_gui, ma_nguoi_gui, loai_tin_nhan, noi_dung, du_lieu_dinh_kem_json, da_xem, ngay_tao)
    VALUES (@MaCuocTroChuyen, 'SHOP', 2, 'VAN_BAN', 
            N'Dạ TechZone chào bạn An ạ! Tai nghe Sony WH-1000XM5 màu đen nhám bản chính hãng hiện sẵn hàng tại kho, giao hỏa tốc nội thành 2h nhé bạn.', 
            NULL, 1, DATEADD(minute, -20, SYSDATETIME()));

    -- Tin nhắn 3: Khách hàng gửi Thẻ Sản Phẩm đang quan tâm
    INSERT INTO tin_nhan (ma_cuoc_tro_chuyen, loai_nguoi_gui, ma_nguoi_gui, loai_tin_nhan, noi_dung, du_lieu_dinh_kem_json, da_xem, ngay_tao)
    VALUES (@MaCuocTroChuyen, 'KHACH_HANG', @MaKhachHang, 'THE_SAN_PHAM', 
            N'Khách hàng đã gửi thẻ sản phẩm: Tai nghe không dây Bluetooth chống ồn Sony WH-1000XM5', 
            N'{"maSanPham":1,"tenSanPham":"Tai nghe không dây Bluetooth chống ồn Sony WH-1000XM5","giaBan":1250000,"maBienThe":1,"tenBienThe":"Màu Đen Nhám - Bluetooth 5.3","linkAnh":"https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500"}', 
            1, DATEADD(minute, -15, SYSDATETIME()));

    -- Tạo 1 Đề xuất Trả Giá mẫu (Offer)
    INSERT INTO de_xuat_tra_gia (ma_cuoc_tro_chuyen, ma_khach_hang, ma_gian_hang, ma_san_pham, ma_bien_the, so_luong, gia_goc, gia_de_xuat, trang_thai, ghi_chu_khach, ngay_tao, ngay_het_han)
    VALUES (@MaCuocTroChuyen, @MaKhachHang, @MaGianHang, @MaSanPhamTaiNghe, @MaBienTheTaiNghe, 1, 1250000, 1050000, 'CHO_DUYET', N'Shop bớt xíu 1.050.000đ mình chốt thanh toán ngay và luôn nhé!', DATEADD(minute, -10, SYSDATETIME()), DATEADD(day, 1, SYSDATETIME()));

    DECLARE @MaDeXuatMoi BIGINT = SCOPE_IDENTITY();

    -- Tin nhắn 4: Tin nhắn Đề xuất Trả Giá đính kèm
    INSERT INTO tin_nhan (ma_cuoc_tro_chuyen, loai_nguoi_gui, ma_nguoi_gui, loai_tin_nhan, noi_dung, du_lieu_dinh_kem_json, da_xem, ngay_tao, ma_de_xuat)
    VALUES (@MaCuocTroChuyen, 'KHACH_HANG', @MaKhachHang, 'DE_XUAT_TRA_GIA', 
            N'Đề xuất trả giá sản phẩm: Tai nghe Sony WH-1000XM5 từ 1.250.000đ còn 1.050.000đ', 
            N'{"maDeXuat":' + CAST(@MaDeXuatMoi AS NVARCHAR(20)) + N',"maSanPham":1,"tenSanPham":"Tai nghe không dây Bluetooth chống ồn Sony WH-1000XM5","giaGoc":1250000,"giaDeXuat":1050000,"soLuong":1,"trangThai":"CHO_DUYET","ghiChuKhach":"Shop bớt xíu 1.050.000đ mình chốt thanh toán ngay và luôn nhé!"}', 
            0, DATEADD(minute, -10, SYSDATETIME()), @MaDeXuatMoi);

    PRINT N'==> Nạp dữ liệu mẫu Cuộc trò chuyện #' + CAST(@MaCuocTroChuyen AS NVARCHAR(20)) + N' và Đề xuất trả giá #' + CAST(@MaDeXuatMoi AS NVARCHAR(20)) + N' thành công!';
END
ELSE
BEGIN
    PRINT N'Không tìm thấy thông tin Khách hàng hoặc Gian hàng mẫu!';
END
GO
