-- =================================================================================================
-- KỊCH BẢN DỮ LIỆU MẪU KIỂM THỬ US-62 (MODULE: VIDEO - PHÂN HỆ KOC & KHÁCH HÀNG)
-- ĐỀ TÀI: ĐĂNG VIDEO NGẮN REVIEW SẢN PHẨM (SHOPEE VIDEO / TIKTOK REELS) GẮN LINK GIỎ HÀNG
-- CSDL: FlexShop_V2_Full (Hỗ trợ chuẩn Tiếng Việt Unicode N'')
-- =================================================================================================

USE [FlexShop_V2_Full];
GO

-- 1. BỔ SUNG CÁC CỘT TIỆN ÍCH CHO BẢNG video_ngan_review (NẾU CHƯA CÓ)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('video_ngan_review') AND name = 'mo_ta')
BEGIN
    ALTER TABLE video_ngan_review ADD mo_ta NVARCHAR(1000) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('video_ngan_review') AND name = 'link_anh_bia')
BEGIN
    ALTER TABLE video_ngan_review ADD link_anh_bia NVARCHAR(500) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('video_ngan_review') AND name = 'hashtag')
BEGIN
    ALTER TABLE video_ngan_review ADD hashtag NVARCHAR(255) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('video_ngan_review') AND name = 'thoi_luong_giay')
BEGIN
    ALTER TABLE video_ngan_review ADD thoi_luong_giay INT NULL DEFAULT 30;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('video_ngan_review') AND name = 'tong_luot_binh_luan')
BEGIN
    ALTER TABLE video_ngan_review ADD tong_luot_binh_luan INT NULL DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('video_ngan_review') AND name = 'tong_luot_chia_se')
BEGIN
    ALTER TABLE video_ngan_review ADD tong_luot_chia_se INT NULL DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('video_ngan_review') AND name = 'trang_thai')
BEGIN
    ALTER TABLE video_ngan_review ADD trang_thai NVARCHAR(50) NULL DEFAULT N'HOAT_DONG';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('video_ngan_review') AND name = 'da_xoa')
BEGIN
    ALTER TABLE video_ngan_review ADD da_xoa BIT NULL DEFAULT 0;
END
GO

-- 2. TẠO BẢNG binh_luan_video (NẾU CHƯA CÓ)
IF OBJECT_ID('binh_luan_video', 'U') IS NULL
BEGIN
    CREATE TABLE binh_luan_video (
        ma_binh_luan BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_video BIGINT NOT NULL,
        ma_nguoi_dung BIGINT NOT NULL,
        ho_ten_nguoi_dung NVARCHAR(100) NULL,
        noi_dung NVARCHAR(1000) NOT NULL,
        thoi_gian_gui DATETIME2 NULL DEFAULT SYSDATETIME(),
        da_xoa BIT NULL DEFAULT 0,
        CONSTRAINT FK_BinhLuanVideo_Video FOREIGN KEY (ma_video) REFERENCES video_ngan_review(ma_video) ON DELETE CASCADE,
        CONSTRAINT FK_BinhLuanVideo_NguoiDung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
    );
END
GO

-- 3. TẠO BẢNG luot_thich_video (NẾU CHƯA CÓ - QUẢN LÝ LƯỢT THẢ TIM CHỐNG SPAM)
IF OBJECT_ID('luot_thich_video', 'U') IS NULL
BEGIN
    CREATE TABLE luot_thich_video (
        ma_thich BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_video BIGINT NOT NULL,
        ma_nguoi_dung BIGINT NOT NULL,
        thoi_gian_thich DATETIME2 NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_LuotThichVideo_Video FOREIGN KEY (ma_video) REFERENCES video_ngan_review(ma_video) ON DELETE CASCADE,
        CONSTRAINT FK_LuotThichVideo_NguoiDung FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
        CONSTRAINT UQ_Video_NguoiDung UNIQUE (ma_video, ma_nguoi_dung)
    );
END
GO

-- 4. INSERT DỮ LIỆU MẪU REVIEW VIDEO HẤP DẪN (SHOPEE VIDEO FEED)
-- Đảm bảo sản phẩm gắn kèm tồn tại trong CSDL
DECLARE @maNguoiDang1 BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE email LIKE '%khach%' OR email LIKE '%nguyen%');
DECLARE @maNguoiDang2 BIGINT = (SELECT TOP 1 ma_nguoi_dung FROM nguoi_dung WHERE ma_nguoi_dung = 5); -- Hoàng Thùy Linh KOC
IF @maNguoiDang2 IS NULL SET @maNguoiDang2 = @maNguoiDang1;

DECLARE @maSp1 BIGINT = (SELECT TOP 1 ma_san_pham FROM san_pham WHERE ma_san_pham = 1);
DECLARE @maSp2 BIGINT = (SELECT TOP 1 ma_san_pham FROM san_pham WHERE ma_san_pham = 2);
DECLARE @maSp3 BIGINT = (SELECT TOP 1 ma_san_pham FROM san_pham WHERE ma_san_pham = 251);
DECLARE @maSp4 BIGINT = (SELECT TOP 1 ma_san_pham FROM san_pham WHERE ma_san_pham = 252);

-- Làm sạch video cũ nếu đã tồn tại để tránh trùng lặp khi chạy lại
DELETE FROM binh_luan_video WHERE ma_video IN (SELECT ma_video FROM video_ngan_review WHERE tieu_de LIKE N'%[US-62]%');
DELETE FROM luot_thich_video WHERE ma_video IN (SELECT ma_video FROM video_ngan_review WHERE tieu_de LIKE N'%[US-62]%');
DELETE FROM video_ngan_review WHERE tieu_de LIKE N'%[US-62]%';

-- Video 1: Review Tai nghe chống ồn Sony WH-1000XM5
INSERT INTO video_ngan_review (
    ma_nguoi_dang, ma_san_pham_gan_kem, tieu_de, link_video, link_anh_bia, mo_ta, hashtag,
    thoi_luong_giay, tong_luot_tim, tong_luot_xem, tong_luot_binh_luan, tong_luot_chia_se, trang_thai, da_xoa, ngay_dang
) VALUES (
    @maNguoiDang2, @maSp1,
    N'[US-62] Review tai nghe chống ồn Sony đỉnh chóp WH-1000XM5 sau 1 tháng sử dụng!',
    'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
    'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80',
    N'Đeo êm tai cả ngày, chống ồn siêu đỉnh khi đi máy bay hay làm việc ở quán cafe. Âm bass cực sâu và ấm áp! Anh em bấm vào giỏ hàng góc trái săn deal sốc nhé!',
    N'#review #tainghesony #congnghe #dealhot #flexshop',
    45, 1250, 8920, 3, 142, N'HOAT_DONG', 0, DATEADD(hour, -5, SYSDATETIME())
);
DECLARE @vid1 BIGINT = SCOPE_IDENTITY();

-- Video 2: Review Áo Sơ Mi Nam Oxford Form Slimfit
INSERT INTO video_ngan_review (
    ma_nguoi_dang, ma_san_pham_gan_kem, tieu_de, link_video, link_anh_bia, mo_ta, hashtag,
    thoi_luong_giay, tong_luot_tim, tong_luot_xem, tong_luot_binh_luan, tong_luot_chia_se, trang_thai, da_xoa, ngay_dang
) VALUES (
    @maNguoiDang1, @maSp2,
    N'[US-62] Phối đồ chuẩn soái ca công sở với Áo Sơ Mi Oxford Slimfit chống nhăn',
    'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
    'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600&auto=format&fit=crop&q=80',
    N'Chất vải cotton oxford mềm mịn, giặt máy thoải mái không bị nhăn nhàu. Tôn dáng cực kỳ luôn nha cả nhà ơi. Link giỏ hàng bên dưới!',
    N'#thoitrangnam #aosomi #ootd #shopeereview #koc',
    35, 840, 5410, 2, 78, N'HOAT_DONG', 0, DATEADD(hour, -2, SYSDATETIME())
);
DECLARE @vid2 BIGINT = SCOPE_IDENTITY();

-- Video 3: Đập hộp FlexPhone Ultra 5G 256GB
IF @maSp3 IS NOT NULL
BEGIN
    INSERT INTO video_ngan_review (
        ma_nguoi_dang, ma_san_pham_gan_kem, tieu_de, link_video, link_anh_bia, mo_ta, hashtag,
        thoi_luong_giay, tong_luot_tim, tong_luot_xem, tong_luot_binh_luan, tong_luot_chia_se, trang_thai, da_xoa, ngay_dang
    ) VALUES (
        @maNguoiDang2, @maSp3,
        N'[US-62] Mở hộp siêu phẩm FlexPhone Ultra 5G - Camera 200MP chụp đêm đỉnh cao!',
        'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4',
        'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&auto=format&fit=crop&q=80',
        N'Màn hình 120Hz mượt mà không góc chết, sạc nhanh 100W đầy pin trong 20 phút. Quá xứng đáng với tầm giá!',
        N'#unbox #dienthoai #flexphone #smartphone #techreview',
        50, 3100, 15400, 2, 320, N'HOAT_DONG', 0, DATEADD(hour, -1, SYSDATETIME())
    );
END

-- Thêm bình luận mẫu cho Video 1
IF @vid1 IS NOT NULL
BEGIN
    INSERT INTO binh_luan_video (ma_video, ma_nguoi_dung, ho_ten_nguoi_dung, noi_dung, thoi_gian_gui)
    VALUES 
    (@vid1, @maNguoiDang1, N'Nguyễn Văn An', N'Video review chi tiết và có tâm quá bạn ơi! Mình vừa chốt 1 chiếc qua link giỏ hàng rồi.', DATEADD(minute, -120, SYSDATETIME())),
    (@vid1, @maNguoiDang2, N'Hoàng Thùy Linh', N'Cảm ơn bạn nhiều nhé, tai nghe bảo hành 12 tháng chính hãng an tâm tuyệt đối luôn nha!', DATEADD(minute, -100, SYSDATETIME())),
    (@vid1, @maNguoiDang1, N'Trần Minh Đức', N'Màu bạc nhìn sang chảnh thật sự, chống ồn tốt không bạn?', DATEADD(minute, -60, SYSDATETIME()));

    INSERT INTO luot_thich_video (ma_video, ma_nguoi_dung)
    VALUES (@vid1, @maNguoiDang1);
END

PRINT N'✅ [THÀNH CÔNG] Đã khởi tạo cấu trúc và dữ liệu mẫu US-62 (Shopee Video Review)!';
