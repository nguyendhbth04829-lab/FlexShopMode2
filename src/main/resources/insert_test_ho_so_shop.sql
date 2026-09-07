-- ====================================================================================
-- SCRIPT TEST: US-08 / SELLER: HỒ SƠ GIAN HÀNG & QUẢN LÝ CHỨNG CHỈ, GIẤY PHÉP KINH DOANH
-- Cơ sở dữ liệu: FlexShop_V2_Full (SQL Server)
-- Mục đích: Khởi tạo dữ liệu mẫu chứng chỉ pháp lý (Đã duyệt, Chờ duyệt, Bị từ chối)
--          để kiểm thử toàn diện giao diện và API Hồ Sơ Shop (/seller/ho-so-shop)
-- ====================================================================================

USE FlexShop_V2_Full;
GO

SET NOCOUNT ON;

PRINT N'>>> BẮT ĐẦU NẠP DỮ LIỆU TEST HỒ SƠ GIAN HÀNG & CHỨNG CHỈ PHÁP LÝ...';

-- 1. Bổ sung các chứng chỉ mẫu cho gian hàng hiện tại của seller@flexshop.vn (Mã gian hàng: 4)
IF EXISTS (SELECT 1 FROM gian_hang WHERE ma_gian_hang = 4)
BEGIN
    PRINT N'1. Đang bổ sung chứng chỉ mẫu cho gian hàng ID 4 (seller@flexshop.vn)...';

    -- Chứng chỉ ATTP (Chờ duyệt)
    IF NOT EXISTS (SELECT 1 FROM chung_chi_gian_hang WHERE ma_gian_hang = 4 AND so_giay_to = 'VSATTP-HN-2026/088')
    BEGIN
        INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
        VALUES (4, 'AN_TOAN_THUC_PHAM', 'VSATTP-HN-2026/088', '/uploads/giay-phep-kd/sample_vsattp.pdf', 'CHO_DUYET', DATEADD(hour, -5, GETDATE()));
        PRINT N'   + Thêm chứng chỉ: AN_TOAN_THUC_PHAM (CHO_DUYET)';
    END

    -- Chứng nhận chất lượng ISO (Đã duyệt)
    IF NOT EXISTS (SELECT 1 FROM chung_chi_gian_hang WHERE ma_gian_hang = 4 AND so_giay_to = 'ISO-9001-2026-FLEX')
    BEGIN
        INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
        VALUES (4, 'CHUNG_NHAN_CHAT_LUONG', 'ISO-9001-2026-FLEX', '/uploads/giay-phep-kd/sample_iso9001.pdf', 'DA_DUYET', DATEADD(day, -2, GETDATE()));
        PRINT N'   + Thêm chứng nhận: CHUNG_NHAN_CHAT_LUONG (DA_DUYET)';
    END

    -- Giấy ủy quyền phân phối (Bị từ chối)
    IF NOT EXISTS (SELECT 1 FROM chung_chi_gian_hang WHERE ma_gian_hang = 4 AND so_giay_to = 'AUTH-DIST-2025/EXPIRED')
    BEGIN
        INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
        VALUES (4, 'UY_QUYEN_PHAN_PHOI', 'AUTH-DIST-2025/EXPIRED', '/uploads/giay-phep-kd/sample_auth.png', 'TU_CHOI', DATEADD(day, -5, GETDATE()));
        PRINT N'   + Thêm giấy tờ: UY_QUYEN_PHAN_PHOI (TU_CHOI)';
    END

    -- Cập nhật thêm thông tin chi tiết cho gian hàng 4
    UPDATE gian_hang
    SET hang_gian_hang = 'UY_TIN',
        mo_ta = N'Flex Fashion 1 - Chuỗi thời trang cao cấp hàng đầu Việt Nam, phân phối chính hãng các sản phẩm thời trang chất lượng chuẩn quốc tế.',
        sdt_kho = '0912345678',
        dia_chi_kho = N'Số 88 Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy, Hà Nội',
        diem_danh_gia_tb = 4.9,
        tong_danh_gia = 128,
        tong_don_hang = 356,
        ty_le_phan_hoi_chat = 99.5
    WHERE ma_gian_hang = 4;
    PRINT N'   + Cập nhật thông số vận hành cho Shop 4 thành công.';
END
ELSE
BEGIN
    PRINT N'Gian hàng ID 4 không tồn tại, bỏ qua bổ sung chứng chỉ cho Shop 4.';
END;

-- 2. Tạo tài khoản test chuyên dụng: seller_test_hoso@flexshop.vn (Mật khẩu: 12345678)
IF NOT EXISTS (SELECT 1 FROM nguoi_dung WHERE email = 'seller_test_hoso@flexshop.vn')
BEGIN
    PRINT N'2. Đang tạo tài khoản test: seller_test_hoso@flexshop.vn...';

    INSERT INTO nguoi_dung (
        email, so_dien_thoai, mat_khau_ma_hoa, ho_va_ten, 
        trang_thai, da_xoa, ngay_tao
    )
    VALUES (
        'seller_test_hoso@flexshop.vn', '0977889911', 
        '$2a$10$b.bp6UGuVLioQIiICqTFNOS0whjXnb4ivL9bi3smPXbtO.rKt4cFS', -- Mật khẩu: 12345678
        N'Chủ Shop Test Hồ Sơ', 'HOAT_DONG', 0, GETDATE()
    );

    DECLARE @NewUserId BIGINT = SCOPE_IDENTITY();

    -- Gán vai trò duy nhất: NGUOI_BAN (Mã vai trò 2)
    INSERT INTO nguoi_dung_vai_tro (ma_nguoi_dung, ma_vai_tro)
    VALUES (@NewUserId, 2);

    -- Tạo gian hàng cho tài khoản test
    INSERT INTO gian_hang (
        ma_chu_so_huu, ten_gian_hang, duong_dan_slug, mo_ta,
        dia_chi_kho, sdt_kho, trang_thai, hang_gian_hang,
        diem_sao_qua_ta, diem_danh_gia_tb, tong_danh_gia, tong_don_hang, ty_le_phan_hoi_chat,
        da_xoa, ngay_tao
    )
    VALUES (
        @NewUserId, N'Flex Test Shop Hồ Sơ', 'flex-test-shop-ho-so',
        N'Gian hàng test chuyên dụng để kiểm thử chức năng Quản lý Hồ Sơ & Chứng chỉ Pháp Lý (US-08).',
        N'Số 123 Đường Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh',
        '0977889911', 'HOAT_DONG', 'TIEM_NANG',
        0, 5.0, 10, 25, 100.0,
        0, GETDATE()
    );

    DECLARE @NewShopId BIGINT = SCOPE_IDENTITY();

    -- Thêm các chứng chỉ mẫu
    INSERT INTO chung_chi_gian_hang (ma_gian_hang, loai_giay_to, so_giay_to, link_anh_giay_to, trang_thai_duyet, ngay_tao)
    VALUES 
    (@NewShopId, 'GIAY_PHEP_KINH_DOANH', '0319998888', '/uploads/giay-phep-kd/sample_gpkd.pdf', 'DA_DUYET', DATEADD(day, -1, GETDATE())),
    (@NewShopId, 'CHUNG_NHAN_CHAT_LUONG', 'ISO-14001-2026', '/uploads/giay-phep-kd/sample_iso.png', 'CHO_DUYET', GETDATE());

    PRINT N'   + Tạo thành công tài khoản seller_test_hoso@flexshop.vn với Shop ID: ' + CAST(@NewShopId AS NVARCHAR(10));
END
ELSE
BEGIN
    PRINT N'Tài khoản seller_test_hoso@flexshop.vn đã tồn tại.';
END;

PRINT N'>>> HOÀN TẤT NẠP DỮ LIỆU TEST HỒ SƠ GIAN HÀNG & CHỨNG CHỈ PHÁP LÝ!';
GO

-- Kiểm tra lại danh sách chứng chỉ
SELECT 
    c.ma_chung_chi,
    g.ma_gian_hang,
    g.ten_gian_hang,
    u.email AS chu_shop,
    c.loai_giay_to,
    c.so_giay_to,
    c.trang_thai_duyet,
    c.link_anh_giay_to
FROM chung_chi_gian_hang c
JOIN gian_hang g ON c.ma_gian_hang = g.ma_gian_hang
JOIN nguoi_dung u ON g.ma_chu_so_huu = u.ma_nguoi_dung
ORDER BY c.ma_chung_chi DESC;
GO
