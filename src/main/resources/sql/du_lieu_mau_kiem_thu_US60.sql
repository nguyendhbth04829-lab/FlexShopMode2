-- =================================================================================================
-- KỊCH BẢN DỮ LIỆU MẪU KIỂM THỬ US-60 (MODULE: CHAT - PHÂN HỆ SELLER)
-- ĐỀ TÀI: THIẾT LẬP TIN NHẮN TỰ ĐỘNG TRẢ LỜI (AUTO-RESPONDER) KHI SHOP VẮNG MẶT
-- CHỨC NĂNG CỐT LÕI: Tự động phản hồi tin nhắn chào mừng hoặc gửi mã voucher cho khách mới nhắn tin
-- CSDL: FlexShop_V2_Full (Hỗ trợ chuẩn Tiếng Việt Unicode N'', định dạng UTF-8 BOM)
-- =================================================================================================

USE [FlexShop_V2_Full];
GO

-- 1. TẠO BẢNG cau_hinh_tin_nhan_tu_dong (NẾU CHƯA CÓ)
IF OBJECT_ID('cau_hinh_tin_nhan_tu_dong', 'U') IS NULL
BEGIN
    CREATE TABLE cau_hinh_tin_nhan_tu_dong (
        ma_cau_hinh BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_gian_hang BIGINT NOT NULL,
        tieu_de NVARCHAR(150) NOT NULL,
        loai_tin_nhan_tu_dong NVARCHAR(50) NOT NULL, -- 'CHAO_MUNG', 'NGOAI_GIO_LAM_VIEC', 'VANG_MAT_TAM_THOI', 'TU_KHOA'
        noi_dung_tin_nhan NVARCHAR(MAX) NOT NULL,
        ma_voucher BIGINT NULL,
        gio_bat_dau TIME NULL,
        gio_ket_thuc TIME NULL,
        tu_khoa_kich_hoat NVARCHAR(255) NULL,
        kich_hoat BIT NOT NULL DEFAULT 1,
        do_tre_giay INT NOT NULL DEFAULT 1,
        gioi_han_gui_moi_khach_ngay INT NOT NULL DEFAULT 1,
        so_lan_da_gui INT NOT NULL DEFAULT 0,
        ngay_tao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        ngay_cap_nhat DATETIME2 NULL,
        CONSTRAINT FK_CauHinh_GianHang FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang) ON DELETE CASCADE,
        CONSTRAINT FK_CauHinh_Voucher FOREIGN KEY (ma_voucher) REFERENCES ma_giam_gia(ma_voucher) ON DELETE SET NULL
    );
    PRINT N'Đã tạo bảng cau_hinh_tin_nhan_tu_dong thành công.';
END
GO

-- 2. TẠO BẢNG lich_su_tin_nhan_tu_dong (NẾU CHƯA CÓ)
IF OBJECT_ID('lich_su_tin_nhan_tu_dong', 'U') IS NULL
BEGIN
    CREATE TABLE lich_su_tin_nhan_tu_dong (
        ma_lich_su BIGINT IDENTITY(1,1) PRIMARY KEY,
        ma_cau_hinh BIGINT NOT NULL,
        ma_cuoc_tro_chuyen BIGINT NOT NULL,
        ma_khach_hang BIGINT NOT NULL,
        ma_gian_hang BIGINT NOT NULL,
        noi_dung_da_gui NVARCHAR(MAX) NOT NULL,
        ma_voucher_da_tang BIGINT NULL,
        thoi_gian_gui DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_LichSu_CauHinh FOREIGN KEY (ma_cau_hinh) REFERENCES cau_hinh_tin_nhan_tu_dong(ma_cau_hinh) ON DELETE CASCADE,
        CONSTRAINT FK_LichSu_CuocTroChuyen FOREIGN KEY (ma_cuoc_tro_chuyen) REFERENCES cuoc_tro_chuyen(ma_cuoc_tro_chuyen),
        CONSTRAINT FK_LichSu_KhachHang FOREIGN KEY (ma_khach_hang) REFERENCES nguoi_dung(ma_nguoi_dung),
        CONSTRAINT FK_LichSu_GianHang FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang),
        CONSTRAINT FK_LichSu_Voucher FOREIGN KEY (ma_voucher_da_tang) REFERENCES ma_giam_gia(ma_voucher)
    );
    PRINT N'Đã tạo bảng lich_su_tin_nhan_tu_dong thành công.';
END
GO

-- 3. TẠO MÃ VOUCHER CHÀO MỪNG KHÁCH MỚI CỦA SHOP TECHZONE (NẾU CHƯA CÓ)
DECLARE @MaGianHang BIGINT = 1; -- TechZone Flagship Store
DECLARE @MaVoucherChaoMung BIGINT;

SELECT @MaVoucherChaoMung = ma_voucher FROM ma_giam_gia WHERE ma_code_voucher = 'TECHZONE20K' AND ma_gian_hang = @MaGianHang;

IF @MaVoucherChaoMung IS NULL
BEGIN
    INSERT INTO ma_giam_gia (
        ma_gian_hang, ma_code_voucher, ten_voucher, loai_voucher, 
        gia_tri_giam, giam_toi_da, gia_tri_don_toi_thieu, tong_so_luong_phat_hanh, 
        gioi_han_moi_nguoi, so_luong_da_dung, ngay_bat_dau, ngay_ket_thuc, 
        dang_hoat_dong, da_xoa, phien_ban_lock
    )
    VALUES (
        @MaGianHang, 'TECHZONE20K', N'Voucher Chào Mừng Khách Mới Giảm 20K Toàn Shop', 'GIAM_GIA',
        20000.00, 20000.00, 150000.00, 500,
        1, 0, DATEADD(day, -5, SYSDATETIME()), DATEADD(day, 60, SYSDATETIME()),
        1, 0, 0
    );
    SET @MaVoucherChaoMung = SCOPE_IDENTITY();
    PRINT N'Đã tạo mới mã voucher chào mừng TECHZONE20K ID: ' + CAST(@MaVoucherChaoMung AS NVARCHAR(20));
END
ELSE
BEGIN
    PRINT N'Đã tìm thấy mã voucher chào mừng TECHZONE20K ID: ' + CAST(@MaVoucherChaoMung AS NVARCHAR(20));
END
GO

-- 4. KHỞI TẠO CÁC KỊCH BẢN TIN NHẮN TỰ ĐỘNG MẪU CHO SHOP TECHZONE (ID: 1)
DECLARE @MaGianHang BIGINT = 1;
DECLARE @MaVoucher20k BIGINT = (SELECT TOP 1 ma_voucher FROM ma_giam_gia WHERE ma_code_voucher = 'TECHZONE20K' AND ma_gian_hang = @MaGianHang);

-- Xóa dữ liệu cũ nếu đã tồn tại trước đó để đảm bảo đồng bộ
DELETE FROM lich_su_tin_nhan_tu_dong WHERE ma_gian_hang = @MaGianHang;
DELETE FROM cau_hinh_tin_nhan_tu_dong WHERE ma_gian_hang = @MaGianHang;

-- Kịch bản 1: Lời chào mừng khách hàng mới & Tặng mã giảm giá 20K
INSERT INTO cau_hinh_tin_nhan_tu_dong (
    ma_gian_hang, tieu_de, loai_tin_nhan_tu_dong, noi_dung_tin_nhan, 
    ma_voucher, gio_bat_dau, gio_ket_thuc, tu_khoa_kich_hoat, 
    kich_hoat, do_tre_giay, gioi_han_gui_moi_khach_ngay, so_lan_da_gui, ngay_tao
)
VALUES (
    @MaGianHang,
    N'Chào mừng khách hàng mới & Tặng Voucher 20K',
    'CHAO_MUNG',
    N'Dạ TechZone Flagship Store xin chào quý khách! Cảm ơn bạn đã quan tâm đến gian hàng của chúng tôi. Shop xin gửi tặng bạn Voucher giảm ngay 20.000đ cho đơn hàng đầu tiên. Chúc bạn có trải nghiệm mua sắm tuyệt vời!',
    @MaVoucher20k,
    NULL, NULL, NULL,
    1, 1, 1, 12, DATEADD(day, -3, SYSDATETIME())
);

-- Kịch bản 2: Phản hồi tự động ngoài giờ làm việc (18:00 - 08:00 hôm sau)
INSERT INTO cau_hinh_tin_nhan_tu_dong (
    ma_gian_hang, tieu_de, loai_tin_nhan_tu_dong, noi_dung_tin_nhan, 
    ma_voucher, gio_bat_dau, gio_ket_thuc, tu_khoa_kich_hoat, 
    kich_hoat, do_tre_giay, gioi_han_gui_moi_khach_ngay, so_lan_da_gui, ngay_tao
)
VALUES (
    @MaGianHang,
    N'Thông báo ngoài giờ làm việc (18h00 - 08h00)',
    'NGOAI_GIO_LAM_VIEC',
    N'Chào bạn! Hiện tại TechZone đang ngoài khung giờ trực chat (Giờ làm việc: 08:00 - 18:00 hàng ngày). Shop đã ghi nhận tin nhắn của bạn và chuyên viên chăm sóc sẽ phản hồi ngay khi mở ca sáng mai nhé. Trân trọng!',
    NULL,
    '18:00:00', '08:00:00', NULL,
    1, 1, 1, 8, DATEADD(day, -3, SYSDATETIME())
);

-- Kịch bản 3: Tự động giải đáp thắc mắc về Bảo hành và Hàng chính hãng
INSERT INTO cau_hinh_tin_nhan_tu_dong (
    ma_gian_hang, tieu_de, loai_tin_nhan_tu_dong, noi_dung_tin_nhan, 
    ma_voucher, gio_bat_dau, gio_ket_thuc, tu_khoa_kich_hoat, 
    kich_hoat, do_tre_giay, gioi_han_gui_moi_khach_ngay, so_lan_da_gui, ngay_tao
)
VALUES (
    @MaGianHang,
    N'Tư vấn tự động: Chính sách bảo hành & Cam kết chính hãng',
    'TU_KHOA',
    N'Tất cả sản phẩm tại TechZone Flagship Store đều là hàng phân phối CHÍNH HÃNG 100%, bảo hành 12 tháng tại các trung tâm ủy quyền toàn quốc, 1 đổi 1 trong 30 ngày nếu phát hiện lỗi từ nhà sản xuất. Bạn hoàn toàn yên tâm đặt hàng nhé!',
    NULL,
    NULL, NULL, N'bảo hành,chính hãng,đổi trả,auth,hàng thật',
    1, 1, 2, 25, DATEADD(day, -2, SYSDATETIME())
);

-- Kịch bản 4: Thông báo Shop tạm vắng mặt / Xử lý đơn hàng cao điểm
INSERT INTO cau_hinh_tin_nhan_tu_dong (
    ma_gian_hang, tieu_de, loai_tin_nhan_tu_dong, noi_dung_tin_nhan, 
    ma_voucher, gio_bat_dau, gio_ket_thuc, tu_khoa_kich_hoat, 
    kich_hoat, do_tre_giay, gioi_han_gui_moi_khach_ngay, so_lan_da_gui, ngay_tao
)
VALUES (
    @MaGianHang,
    N'Thông báo Shop tạm vắng mặt / Đóng gói đơn giờ cao điểm',
    'VANG_MAT_TAM_THOI',
    N'Chào bạn! Đội ngũ tư vấn viên đang tập trung đóng gói đơn hàng hỏa tốc trong kho nên phản hồi có thể chậm trễ từ 5-10 phút. Bạn vui lòng để lại câu hỏi hoặc yêu cầu, bên mình sẽ hỗ trợ bạn ngay lập tức ạ!',
    NULL,
    NULL, NULL, NULL,
    0, 2, 1, 5, DATEADD(day, -1, SYSDATETIME())
);

PRINT N'==> Khởi tạo thành công 4 kịch bản tin nhắn tự động mẫu cho Shop TechZone!';
GO
