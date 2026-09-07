-- =================================================================================================
-- KỊCH BẢN DỮ LIỆU MẪU KIỂM THỬ US-61 (MODULE: LIVE - PHÂN HỆ SELLER & KHÁCH HÀNG)
-- ĐỀ TÀI: PHÁT LIVESTREAM BÁN HÀNG (FLEXSHOP LIVE) VÀ GHIM SẢN PHẨM GIẢM GIÁ SỐC
-- CSDL: FlexShop_V2_Full (Hỗ trợ chuẩn Tiếng Việt Unicode N'', định dạng UTF-8 BOM)
-- =================================================================================================

USE [FlexShop_V2_Full];
GO

-- 1. BỔ SUNG CỘT TIỆN ÍCH CHO BẢNG phong_livestream (NẾU CHƯA CÓ)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phong_livestream') AND name = 'so_nguoi_xem_hien_tai')
BEGIN
    ALTER TABLE phong_livestream ADD so_nguoi_xem_hien_tai INT NULL DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phong_livestream') AND name = 'so_luot_thich')
BEGIN
    ALTER TABLE phong_livestream ADD so_luot_thich INT NULL DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phong_livestream') AND name = 'mo_ta')
BEGIN
    ALTER TABLE phong_livestream ADD mo_ta NVARCHAR(MAX) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('phong_livestream') AND name = 'ngay_tao')
BEGIN
    ALTER TABLE phong_livestream ADD ngay_tao DATETIME2 NULL DEFAULT SYSDATETIME();
END
GO

-- 2. BỔ SUNG CỘT TIỆN ÍCH CHO BẢNG san_pham_livestream (NẾU CHƯA CÓ)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('san_pham_livestream') AND name = 'thu_tu_hien_thi')
BEGIN
    ALTER TABLE san_pham_livestream ADD thu_tu_hien_thi INT NULL DEFAULT 1;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('san_pham_livestream') AND name = 'so_luong_gioi_han')
BEGIN
    ALTER TABLE san_pham_livestream ADD so_luong_gioi_han INT NULL DEFAULT 50;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('san_pham_livestream') AND name = 'so_luong_da_ban')
BEGIN
    ALTER TABLE san_pham_livestream ADD so_luong_da_ban INT NULL DEFAULT 0;
END
GO

-- 3. TẠO BẢNG binh_luan_livestream (NẾU CHƯA CÓ)
IF OBJECT_ID('binh_luan_livestream', 'U') IS NULL
BEGIN
    CREATE TABLE binh_luan_livestream (
        ma_binh_luan BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_live BIGINT NOT NULL,
        ma_nguoi_dung BIGINT NOT NULL,
        ho_ten_nguoi_dung NVARCHAR(100) NOT NULL,
        noi_dung NVARCHAR(500) NOT NULL,
        thoi_gian_gui DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        la_tin_he_thong BIT NOT NULL DEFAULT 0,
        la_nguoi_ban BIT NOT NULL DEFAULT 0,
        CONSTRAINT FK_BinhLuan_PhongLive FOREIGN KEY (ma_live) REFERENCES phong_livestream(ma_live) ON DELETE CASCADE,
        CONSTRAINT FK_BinhLuan_NguoiDung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
    );
    PRINT N'Đã tạo bảng binh_luan_livestream thành công.';
END
GO

-- 4. KHỞI TẠO DỮ LIỆU PHIÊN LIVESTREAM MẪU CHO SHOP TECHZONE (ID: 1)
DECLARE @MaGianHang BIGINT = 1; -- TechZone Flagship Store
DECLARE @MaChuShop BIGINT = (SELECT TOP 1 ma_chu_so_huu FROM gian_hang WHERE ma_gian_hang = @MaGianHang);
DECLARE @MaKhachAn BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = 'khachhang@flexshop.vn' OR ma_nguoi_dung = 4);
DECLARE @MaKhachLinh BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email = 'hoanglinh@flexshop.vn' OR ma_nguoi_dung = 5);

-- Xóa dữ liệu mẫu cũ liên quan đến livestream để đảm bảo tính toàn vẹn
DELETE FROM binh_luan_livestream WHERE ma_live IN (SELECT ma_live FROM phong_livestream WHERE ma_gian_hang = @MaGianHang);
DELETE FROM san_pham_livestream WHERE ma_live IN (SELECT ma_live FROM phong_livestream WHERE ma_gian_hang = @MaGianHang);
DELETE FROM phong_livestream WHERE ma_gian_hang = @MaGianHang;

IF NOT EXISTS (SELECT 1 FROM phong_livestream)
BEGIN
    DBCC CHECKIDENT ('phong_livestream', RESEED, 0);
    DBCC CHECKIDENT ('san_pham_livestream', RESEED, 0);
    DBCC CHECKIDENT ('binh_luan_livestream', RESEED, 0);
END

-- =================================================================================================
-- PHIÊN 1: ĐANG PHÁT TRỰC TIẾP (DANG_LIVE)
-- =================================================================================================
INSERT INTO phong_livestream (
    ma_gian_hang, tieu_de, link_stream_rtmp, link_anh_bia, 
    tong_luot_xem, trang_thai, thoi_gian_bat_dau, thoi_gian_ket_thuc, 
    so_nguoi_xem_hien_tai, so_luot_thich, mo_ta, ngay_tao
)
VALUES (
    @MaGianHang,
    N'🔥 ĐẠI TIỆC FLEXSHOP LIVE - SALE SẬP SÀN PHỤ KIỆN & TAI NGHE CHÍNH HÃNG 50%',
    N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
    N'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800',
    1280,
    N'DANG_LIVE',
    DATEADD(minute, -35, SYSDATETIME()),
    NULL,
    348,
    1560,
    N'Chào mừng các bạn đến với phiên Live đặc biệt của TechZone! Hôm nay săn deal tai nghe Sony và củ sạc GaN 65W giảm giá kịch sàn độc quyền chỉ có trên Live!',
    DATEADD(hour, -2, SYSDATETIME())
);

DECLARE @MaLive1 BIGINT = SCOPE_IDENTITY();

-- Thêm các sản phẩm vào Phiên 1
-- Sản phẩm 1: Tai nghe Sony WH-1000XM5 (ĐANG ĐƯỢC GHIM - la_san_pham_dang_ghim = 1)
INSERT INTO san_pham_livestream (
    ma_live, ma_san_pham, la_san_pham_dang_ghim, gia_doc_quyen_live, 
    thu_tu_hien_thi, so_luong_gioi_han, so_luong_da_ban
)
VALUES (
    @MaLive1, 1, 1, 950000.00, 1, 30, 14
);

-- Sản phẩm 2: Củ Sạc Nhanh 65W GaN 3 Cổng Type-C
INSERT INTO san_pham_livestream (
    ma_live, ma_san_pham, la_san_pham_dang_ghim, gia_doc_quyen_live, 
    thu_tu_hien_thi, so_luong_gioi_han, so_luong_da_ban
)
VALUES (
    @MaLive1, 252, 0, 269000.00, 2, 50, 28
);

-- Sản phẩm 3: Cáp Sạc Nhanh Bọc Dù 100W Dài 1.2M
INSERT INTO san_pham_livestream (
    ma_live, ma_san_pham, la_san_pham_dang_ghim, gia_doc_quyen_live, 
    thu_tu_hien_thi, so_luong_gioi_han, so_luong_da_ban
)
VALUES (
    @MaLive1, 253, 0, 89000.00, 3, 100, 45
);

-- Sản phẩm 4: Điện Thoại FlexPhone Ultra 5G
INSERT INTO san_pham_livestream (
    ma_live, ma_san_pham, la_san_pham_dang_ghim, gia_doc_quyen_live, 
    thu_tu_hien_thi, so_luong_gioi_han, so_luong_da_ban
)
VALUES (
    @MaLive1, 251, 0, 17990000.00, 4, 10, 3
);

-- Bình luận mẫu sôi nổi trong Phiên 1
IF @MaKhachAn IS NOT NULL
BEGIN
    INSERT INTO binh_luan_livestream (ma_live, ma_nguoi_dung, ho_ten_nguoi_dung, noi_dung, thoi_gian_gui, la_tin_he_thong, la_nguoi_ban)
    VALUES (@MaLive1, @MaKhachAn, N'Nguyễn Văn An', N'Tai nghe Sony này có kèm hộp chống sốc chính hãng không Shop?', DATEADD(minute, -15, SYSDATETIME()), 0, 0);

    INSERT INTO binh_luan_livestream (ma_live, ma_nguoi_dung, ho_ten_nguoi_dung, noi_dung, thoi_gian_gui, la_tin_he_thong, la_nguoi_ban)
    VALUES (@MaLive1, @MaChuShop, N'TechZone Store (Chủ Shop)', N'Dạ fullbox đầy đủ cáp sạc và túi đựng da cao cấp nhé bạn An ơi!', DATEADD(minute, -13, SYSDATETIME()), 0, 1);

    INSERT INTO binh_luan_livestream (ma_live, ma_nguoi_dung, ho_ten_nguoi_dung, noi_dung, thoi_gian_gui, la_tin_he_thong, la_nguoi_ban)
    VALUES (@MaLive1, @MaKhachAn, N'Hệ Thống FlexShop', N'🎉 Khách hàng Nguyễn Văn An vừa chốt đơn [Tai nghe Sony WH-1000XM5] với giá deal sốc 950.000đ!', DATEADD(minute, -10, SYSDATETIME()), 1, 0);
END

IF @MaKhachLinh IS NOT NULL
BEGIN
    INSERT INTO binh_luan_livestream (ma_live, ma_nguoi_dung, ho_ten_nguoi_dung, noi_dung, thoi_gian_gui, la_tin_he_thong, la_nguoi_ban)
    VALUES (@MaLive1, @MaKhachLinh, N'Hoàng Thùy Linh', N'Đã thả tim cho Shop! Ghim lại củ sạc GaN 65W cho mình xem với ạ!', DATEADD(minute, -5, SYSDATETIME()), 0, 0);

    INSERT INTO binh_luan_livestream (ma_live, ma_nguoi_dung, ho_ten_nguoi_dung, noi_dung, thoi_gian_gui, la_tin_he_thong, la_nguoi_ban)
    VALUES (@MaLive1, @MaKhachLinh, N'Hoàng Thùy Linh', N'Vừa áp thêm mã Freeship giảm thêm 30k sướng quá!', DATEADD(minute, -2, SYSDATETIME()), 0, 0);
END

-- =================================================================================================
-- PHIÊN 2: SẮP DIỄN RA (SAP_DIEN_RA)
-- =================================================================================================
INSERT INTO phong_livestream (
    ma_gian_hang, tieu_de, link_stream_rtmp, link_anh_bia, 
    tong_luot_xem, trang_thai, thoi_gian_bat_dau, thoi_gian_ket_thuc, 
    so_nguoi_xem_hien_tai, so_luot_thich, mo_ta, ngay_tao
)
VALUES (
    @MaGianHang,
    N'⏰ [20H TỐI NAY] Livestream Ra Mắt Siêu Phẩm FlexPhone Ultra 5G & Tặng Quà Cực Khủng',
    N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4',
    N'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800',
    0,
    N'SAP_DIEN_RA',
    DATEADD(hour, 8, SYSDATETIME()),
    NULL,
    0,
    215,
    N'Đặt lịch ngay để không bỏ lỡ cơ hội nhận voucher 500k và quà tặng củ sạc GaN 65W trị giá 400k cho 50 đơn hàng đầu tiên!',
    DATEADD(day, -1, SYSDATETIME())
);

-- =================================================================================================
-- PHIÊN 3: ĐÃ KẾT THÚC (DA_KET_THUC)
-- =================================================================================================
INSERT INTO phong_livestream (
    ma_gian_hang, tieu_de, link_stream_rtmp, link_anh_bia, 
    tong_luot_xem, trang_thai, thoi_gian_bat_dau, thoi_gian_ket_thuc, 
    so_nguoi_xem_hien_tai, so_luot_thich, mo_ta, ngay_tao
)
VALUES (
    @MaGianHang,
    N'Livestream Xả Kho Công Nghệ Cuối Tuần - Cảm Ơn 5.000 Khán Giả Đồng Hành',
    N'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
    N'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800',
    5240,
    N'DA_KET_THUC',
    DATEADD(day, -2, SYSDATETIME()),
    DATEADD(hour, -46, SYSDATETIME()),
    0,
    8920,
    N'Phiên live thành công rực rỡ với hơn 350 đơn hàng hoàn tất. Hẹn gặp lại quý khách vào phiên live kế tiếp!',
    DATEADD(day, -3, SYSDATETIME())
);

PRINT N'==> Nạp dữ liệu mẫu US-61 Livestream FlexShop Live thành công 100%!';
GO
