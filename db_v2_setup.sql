-- ============================================================================
-- DỰ ÁN: SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG FLEXSHOP - ENTERPRISE V2
-- TỔNG CỘNG: 74 BẢNG (Bao gồm 2 bảng mới: nhan_vien_gian_hang, lich_su_gia_bien_the)
-- ============================================================================

CREATE DATABASE FlexShop_V2_Full;
GO
USE FlexShop_V2_Full;
GO

-- ============================================================================
-- 1. PHÂN HỆ TÀI KHOẢN, PHÂN QUYỀN & THIẾT BỊ (6 Bảng)
-- ============================================================================
CREATE TABLE vai_tro (
    ma_vai_tro INT IDENTITY(1,1) PRIMARY KEY,
    ten_vai_tro NVARCHAR(50) NOT NULL UNIQUE,
    mo_ta NVARCHAR(255) NULL
);

CREATE TABLE nguoi_dung (
    ma_nguoi_dung BIGINT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(150) NOT NULL UNIQUE,
    so_dien_thoai NVARCHAR(20) NULL UNIQUE,
    mat_khau_ma_hoa NVARCHAR(255) NOT NULL,
    ho_va_ten NVARCHAR(100) NOT NULL,
    anh_dai_dien NVARCHAR(500) NULL,
    trang_thai NVARCHAR(30) DEFAULT N'HOAT_DONG',
    da_xoa BIT DEFAULT 0,
    ngay_xoa DATETIME2 NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE nguoi_dung_vai_tro (
    ma_nguoi_dung BIGINT NOT NULL,
    ma_vai_tro INT NOT NULL,
    ngay_gan DATETIME2 DEFAULT GETDATE(),
    PRIMARY KEY (ma_nguoi_dung, ma_vai_tro),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
    FOREIGN KEY (ma_vai_tro) REFERENCES vai_tro(ma_vai_tro)
);

CREATE TABLE xac_thuc_otp (
    ma_otp BIGINT IDENTITY(1,1) PRIMARY KEY,
    nguoi_nhan NVARCHAR(150) NOT NULL,
    ma_xac_thuc NVARCHAR(10) NOT NULL,
    loai_otp NVARCHAR(30) NOT NULL,
    thoi_gian_het_han DATETIME2 NOT NULL,
    da_su_dung BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE dia_chi_nguoi_dung (
    ma_dia_chi BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    ten_nguoi_nhan NVARCHAR(100) NOT NULL,
    so_dien_thoai NVARCHAR(20) NOT NULL,
    tinh_thanh NVARCHAR(100) NOT NULL,
    quan_huyen NVARCHAR(100) NOT NULL,
    xa_phuong NVARCHAR(100) NOT NULL,
    dia_chi_chi_tiet NVARCHAR(255) NOT NULL,
    la_mac_dinh BIT DEFAULT 0,
    da_xoa BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE thiet_bi_nguoi_dung (
    ma_thiet_bi BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    token_push_fcm NVARCHAR(500) NOT NULL,
    loai_he_dieu_hanh NVARCHAR(30) NOT NULL,
    ngay_dang_nhap_cuoi DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung) ON DELETE CASCADE
);

-- ============================================================================
-- 2. PHÂN HỆ GIAN HÀNG & CHỨNG CHỈ (5 Bảng)
-- ============================================================================
CREATE TABLE gian_hang (
    ma_gian_hang BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_chu_so_huu BIGINT NOT NULL,
    ten_gian_hang NVARCHAR(100) NOT NULL UNIQUE,
    duong_dan_slug NVARCHAR(120) NOT NULL UNIQUE,
    mo_ta NVARCHAR(MAX) NULL,
    link_logo NVARCHAR(500) NULL,
    link_banner NVARCHAR(500) NULL,
    dia_chi_kho NVARCHAR(255) NOT NULL,
    sdt_kho NVARCHAR(20) NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'CHO_DUYET',
    ly_do_tu_choi NVARCHAR(255) NULL,
    hang_gian_hang NVARCHAR(30) DEFAULT N'CHUAN',
    diem_sao_qua_ta INT DEFAULT 0,
    diem_danh_gia_tb DECIMAL(3,2) DEFAULT 0.00,
    tong_danh_gia INT DEFAULT 0,
    tong_don_hang INT DEFAULT 0,
    ty_le_phan_hoi_chat DECIMAL(5,2) DEFAULT 100.00,
    da_xoa BIT DEFAULT 0,
    ngay_xoa DATETIME2 NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_chu_so_huu) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE nhan_vien_gian_hang (
    ma_nv_shop BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL,
    ma_nguoi_dung BIGINT NOT NULL,
    vai_tro_shop NVARCHAR(50) NOT NULL, 
    trang_thai NVARCHAR(30) DEFAULT N'HOAT_DONG',
    da_xoa BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE lich_su_sao_qua_ta (
    ma_phat BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL,
    so_diem_phat INT NOT NULL,
    ly_do NVARCHAR(255) NOT NULL,
    loai_vi_pham NVARCHAR(50) NOT NULL,
    nguoi_phat_admin BIGINT NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

CREATE TABLE goi_dich_vu_gian_hang (
    ma_goi INT IDENTITY(1,1) PRIMARY KEY,
    ten_goi NVARCHAR(100) NOT NULL,
    phi_dich_vu_phan_tram DECIMAL(5,2) NOT NULL,
    mo_ta NVARCHAR(255) NULL,
    dang_ap_dung BIT DEFAULT 1
);

CREATE TABLE chung_chi_gian_hang (
    ma_chung_chi BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL,
    loai_giay_to NVARCHAR(100) NOT NULL,
    so_giay_to NVARCHAR(100) NOT NULL,
    link_anh_giay_to NVARCHAR(500) NOT NULL,
    trang_thai_duyet NVARCHAR(30) DEFAULT N'CHO_DUYET',
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

-- ============================================================================
-- 3. PHÂN HỆ DANH MỤC, THƯƠNG HIỆU & SẢN PHẨM (8 Bảng)
-- ============================================================================
CREATE TABLE danh_muc (
    ma_danh_muc BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten_danh_muc NVARCHAR(100) NOT NULL,
    duong_dan_slug NVARCHAR(120) NOT NULL UNIQUE,
    link_icon NVARCHAR(500) NULL,
    ma_danh_muc_cha BIGINT NULL,
    cap_do INT DEFAULT 1,
    thu_tu_hien_thi INT DEFAULT 0,
    dang_hoat_dong BIT DEFAULT 1,
    da_xoa BIT DEFAULT 0,
    FOREIGN KEY (ma_danh_muc_cha) REFERENCES danh_muc(ma_danh_muc)
);

CREATE TABLE thuong_hieu (
    ma_thuong_hieu BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten_thuong_hieu NVARCHAR(100) NOT NULL UNIQUE,
    link_logo NVARCHAR(500) NULL,
    dang_hoat_dong BIT DEFAULT 1,
    da_xoa BIT DEFAULT 0
);

CREATE TABLE san_pham (
    ma_san_pham BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL,
    ma_danh_muc BIGINT NOT NULL,
    ma_thuong_hieu BIGINT NULL,
    ten_san_pham NVARCHAR(255) NOT NULL,
    duong_dan_slug NVARCHAR(280) NOT NULL UNIQUE,
    mo_ta_ngan NVARCHAR(500) NULL,
    mo_ta_chi_tiet NVARCHAR(MAX) NULL,
    gia_co_ban DECIMAL(18,2) NOT NULL,
    bi_khoa BIT DEFAULT 0,
    ly_do_khoa NVARCHAR(255) NULL,
    trang_thai NVARCHAR(30) DEFAULT N'HOAT_DONG',
    can_nang_gram INT NOT NULL DEFAULT 200,
    chieu_dai_cm INT NOT NULL DEFAULT 10,
    chieu_rong_cm INT NOT NULL DEFAULT 10,
    chieu_cao_cm INT NOT NULL DEFAULT 10,
    danh_gia_tb DECIMAL(3,2) DEFAULT 0.00,
    tong_da_ban INT DEFAULT 0,
    tong_luot_xem INT DEFAULT 0,
    da_xoa BIT DEFAULT 0,
    ngay_xoa DATETIME2 NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang),
    FOREIGN KEY (ma_danh_muc) REFERENCES danh_muc(ma_danh_muc),
    FOREIGN KEY (ma_thuong_hieu) REFERENCES thuong_hieu(ma_thuong_hieu)
);

CREATE TABLE hinh_anh_san_pham (
    ma_anh BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_san_pham BIGINT NOT NULL,
    link_anh NVARCHAR(500) NOT NULL,
    la_anh_chinh BIT DEFAULT 0,
    thu_tu_hien_thi INT DEFAULT 0,
    FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham) ON DELETE CASCADE
);

CREATE TABLE thuoc_tinh_san_pham (
    ma_thuoc_tinh BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_san_pham BIGINT NOT NULL,
    ten_thuoc_tinh NVARCHAR(100) NOT NULL,
    FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham)
);

CREATE TABLE gia_tri_thuoc_tinh (
    ma_gia_tri BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_thuoc_tinh BIGINT NOT NULL,
    gia_tri NVARCHAR(100) NOT NULL,
    FOREIGN KEY (ma_thuoc_tinh) REFERENCES thuoc_tinh_san_pham(ma_thuoc_tinh)
);

CREATE TABLE bien_the_san_pham (
    ma_bien_the BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_san_pham BIGINT NOT NULL,
    ma_sku NVARCHAR(100) NOT NULL UNIQUE,
    ten_bien_the NVARCHAR(200) NOT NULL,
    gia_ban DECIMAL(18,2) NOT NULL,
    gia_goc DECIMAL(18,2) NULL,
    link_anh NVARCHAR(500) NULL,
    can_nang_gram INT NULL,
    chieu_dai_cm INT NULL,
    chieu_rong_cm INT NULL,
    chieu_cao_cm INT NULL,
    da_xoa BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham)
);

CREATE TABLE lich_su_gia_bien_the (
    ma_lich_su BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_bien_the BIGINT NOT NULL,
    gia_cu DECIMAL(18,2) NOT NULL,
    gia_moi DECIMAL(18,2) NOT NULL,
    ngay_thay_doi DATETIME2 DEFAULT GETDATE(),
    nguoi_thay_doi BIGINT NULL,
    FOREIGN KEY (ma_bien_the) REFERENCES bien_the_san_pham(ma_bien_the)
);

-- ============================================================================
-- 4. PHÂN HỆ QUẢN LÝ ĐA KHO HÀNG WMS & LÔ HÀNG (6 Bảng)
-- ============================================================================
CREATE TABLE kho_hang (
    ma_kho BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL,
    ten_kho NVARCHAR(150) NOT NULL,
    dia_chi NVARCHAR(255) NOT NULL,
    tinh_thanh NVARCHAR(100) NOT NULL,
    sdt_lien_he NVARCHAR(20) NOT NULL,
    la_kho_chinh BIT DEFAULT 0,
    dang_hoat_dong BIT DEFAULT 1,
    da_xoa BIT DEFAULT 0,
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

CREATE TABLE vi_tri_ke_kho (
    ma_vi_tri BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_kho BIGINT NOT NULL,
    ma_khu_vuc NVARCHAR(50) NOT NULL,
    FOREIGN KEY (ma_kho) REFERENCES kho_hang(ma_kho) ON DELETE CASCADE
);

CREATE TABLE lo_hang_san_pham (
    ma_lo BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_bien_the BIGINT NOT NULL,
    ma_so_lo NVARCHAR(100) NOT NULL,
    ngay_san_xuat DATE NOT NULL,
    han_su_dung DATE NULL,
    FOREIGN KEY (ma_bien_the) REFERENCES bien_the_san_pham(ma_bien_the)
);

CREATE TABLE ton_kho_chi_tiet (
    ma_ton_kho BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_kho BIGINT NOT NULL,
    ma_vi_tri BIGINT NULL,
    ma_bien_the BIGINT NOT NULL,
    ma_lo BIGINT NULL,
    so_luong_ton INT NOT NULL DEFAULT 0,
    so_luong_tam_giu INT NOT NULL DEFAULT 0,
    phien_ban_lock INT DEFAULT 1, -- Optimistic Locking
    FOREIGN KEY (ma_kho) REFERENCES kho_hang(ma_kho),
    FOREIGN KEY (ma_vi_tri) REFERENCES vi_tri_ke_kho(ma_vi_tri),
    FOREIGN KEY (ma_bien_the) REFERENCES bien_the_san_pham(ma_bien_the),
    FOREIGN KEY (ma_lo) REFERENCES lo_hang_san_pham(ma_lo)
);

CREATE TABLE phieu_nhap_xuat_kho (
    ma_phieu_kho BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_kho BIGINT NOT NULL,
    loai_phieu NVARCHAR(30) NOT NULL,
    ma_chung_tu_lien_quan NVARCHAR(100) NULL,
    nguoi_lap_phieu BIGINT NOT NULL,
    ghi_chu NVARCHAR(255) NULL,
    ngay_lap DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_kho) REFERENCES kho_hang(ma_kho),
    FOREIGN KEY (nguoi_lap_phieu) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE chi_tiet_phieu_kho (
    ma_chi_tiet_phieu BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu_kho BIGINT NOT NULL,
    ma_bien_the BIGINT NOT NULL,
    so_luong INT NOT NULL,
    don_gia_von DECIMAL(18,2) DEFAULT 0.00,
    FOREIGN KEY (ma_phieu_kho) REFERENCES phieu_nhap_xuat_kho(ma_phieu_kho) ON DELETE CASCADE,
    FOREIGN KEY (ma_bien_the) REFERENCES bien_the_san_pham(ma_bien_the)
);

-- ============================================================================
-- 5. PHÂN HỆ GIỎ HÀNG & ĐƠN HÀNG ĐA SHOP (6 Bảng)
-- ============================================================================
CREATE TABLE gio_hang (
    ma_gio_hang BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL UNIQUE,
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE chi_tiet_gio_hang (
    ma_chi_tiet_gio BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gio_hang BIGINT NOT NULL,
    ma_bien_the BIGINT NOT NULL,
    so_luong INT NOT NULL DEFAULT 1,
    da_tich_chon BIT DEFAULT 1,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_gio_hang) REFERENCES gio_hang(ma_gio_hang) ON DELETE CASCADE,
    FOREIGN KEY (ma_bien_the) REFERENCES bien_the_san_pham(ma_bien_the)
);

CREATE TABLE don_hang_tong (
    ma_don_hang_tong BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_code_don_tong NVARCHAR(50) NOT NULL UNIQUE,
    ma_khach_hang BIGINT NOT NULL,
    ma_dia_chi_giao BIGINT NOT NULL,
    tong_tien_hang DECIMAL(18,2) NOT NULL,
    tong_phi_van_chuyen DECIMAL(18,2) NOT NULL,
    tong_tien_thue_vat DECIMAL(18,2) DEFAULT 0.00,
    tong_giam_gia_san DECIMAL(18,2) DEFAULT 0.00,
    tong_giam_gia_shop DECIMAL(18,2) DEFAULT 0.00,
    tong_thanh_toan_cuoi DECIMAL(18,2) NOT NULL,
    phuong_thuc_thanh_toan NVARCHAR(50) NOT NULL,
    trang_thai_thanh_toan NVARCHAR(30) DEFAULT N'CHUA_THANH_TOAN',
    trang_thai_don_hang NVARCHAR(30) DEFAULT N'CHO_XU_LY',
    ghi_chu NVARCHAR(500) NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_khach_hang) REFERENCES nguoi_dung(ma_nguoi_dung),
    FOREIGN KEY (ma_dia_chi_giao) REFERENCES dia_chi_nguoi_dung(ma_dia_chi)
);

CREATE TABLE don_hang_shop (
    ma_don_hang_shop BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_code_don_shop NVARCHAR(60) NOT NULL UNIQUE,
    ma_don_hang_tong BIGINT NOT NULL,
    ma_gian_hang BIGINT NOT NULL,
    tien_hang_shop DECIMAL(18,2) NOT NULL,
    phi_van_chuyen DECIMAL(18,2) NOT NULL,
    tien_thue_vat DECIMAL(18,2) DEFAULT 0.00,
    giam_gia_voucher_shop DECIMAL(18,2) DEFAULT 0.00,
    giam_gia_voucher_san DECIMAL(18,2) DEFAULT 0.00,
    tong_tien_shop_nhan DECIMAL(18,2) NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'CHO_XAC_NHAN',
    ly_do_huy NVARCHAR(255) NULL,
    nguoi_huy NVARCHAR(50) NULL,
    ma_van_don NVARCHAR(100) NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_don_hang_tong) REFERENCES don_hang_tong(ma_don_hang_tong),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

CREATE TABLE chi_tiet_don_hang (
    ma_chi_tiet_don BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_don_hang_shop BIGINT NOT NULL,
    ma_bien_the BIGINT NOT NULL,
    ten_san_pham NVARCHAR(255) NOT NULL,
    ten_bien_the NVARCHAR(200) NOT NULL,
    ma_sku NVARCHAR(100) NOT NULL,
    don_gia DECIMAL(18,2) NOT NULL,
    so_luong INT NOT NULL,
    tong_tien DECIMAL(18,2) NOT NULL,
    FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop) ON DELETE CASCADE,
    FOREIGN KEY (ma_bien_the) REFERENCES bien_the_san_pham(ma_bien_the)
);

CREATE TABLE lich_su_trang_thai_don (
    ma_lich_su BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_don_hang_shop BIGINT NOT NULL,
    trang_thai_cu NVARCHAR(30) NULL,
    trang_thai_moi NVARCHAR(30) NOT NULL,
    nguoi_thuc_hien NVARCHAR(100) NOT NULL,
    ghi_chu NVARCHAR(255) NULL,
    thoi_gian DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop) ON DELETE CASCADE
);

-- ============================================================================
-- 6. PHÂN HỆ LOGISTICS 3PL & SHIPPER POD (7 Bảng)
-- ============================================================================
CREATE TABLE doi_tac_van_chuyen (
    ma_doi_tac INT IDENTITY(1,1) PRIMARY KEY,
    ten_doi_tac NVARCHAR(100) NOT NULL,
    ma_ket_noi_api NVARCHAR(100) NULL,
    dang_hoat_dong BIT DEFAULT 1
);

CREATE TABLE tram_trung_chuyen_hub (
    ma_hub BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten_hub NVARCHAR(150) NOT NULL,
    dia_chi NVARCHAR(255) NOT NULL,
    tinh_thanh NVARCHAR(100) NOT NULL,
    suc_chua_kien_hang INT DEFAULT 50000
);

CREATE TABLE bang_gia_van_chuyen (
    ma_bang_gia BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_doi_tac INT NOT NULL,
    tuyen_van_chuyen NVARCHAR(50) NOT NULL,
    khoi_luong_chuan_gram INT NOT NULL,
    cuoc_phi_chuan DECIMAL(18,2) NOT NULL,
    cuoc_phi_vuot_moi_500g DECIMAL(18,2) NOT NULL,
    FOREIGN KEY (ma_doi_tac) REFERENCES doi_tac_van_chuyen(ma_doi_tac)
);

CREATE TABLE tai_xe_giao_hang (
    ma_tai_xe BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL UNIQUE,
    loai_phuong_tien NVARCHAR(50) DEFAULT N'XE_MAY',
    bien_so_xe NVARCHAR(30) NOT NULL,
    dang_truc_tuyen BIT DEFAULT 0,
    vi_do_hien_tai DECIMAL(10,8) NULL,
    kinh_do_hien_tai DECIMAL(11,8) NULL,
    so_du_cod_dang_giu DECIMAL(18,2) DEFAULT 0.00,
    diem_danh_gia_tb DECIMAL(3,2) DEFAULT 5.00,
    trang_thai NVARCHAR(30) DEFAULT N'DANG_HOAT_DONG',
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE nhiem_vu_giao_hang (
    ma_nhiem_vu BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_don_hang_shop BIGINT NOT NULL,
    ma_tai_xe BIGINT NOT NULL,
    loai_nhiem_vu NVARCHAR(30) DEFAULT N'GIAO_HANG',
    trang_thai NVARCHAR(30) DEFAULT N'DA_PHAN_CONG',
    tien_cod_can_thu DECIMAL(18,2) DEFAULT 0.00,
    da_thu_cod BIT DEFAULT 0,
    link_anh_bang_chung_pod NVARCHAR(500) NULL,
    vi_do_giao_hang DECIMAL(10,8) NULL,
    kinh_do_giao_hang DECIMAL(11,8) NULL,
    ma_otp_xac_nhan NVARCHAR(10) NULL,
    ly_do_that_bai NVARCHAR(255) NULL,
    so_lan_giao INT DEFAULT 1,
    thoi_gian_hen_giao_lai DATETIME2 NULL,
    thoi_gian_lay_hang DATETIME2 NULL,
    thoi_gian_giao_thanh_cong DATETIME2 NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop),
    FOREIGN KEY (ma_tai_xe) REFERENCES tai_xe_giao_hang(ma_tai_xe)
);

CREATE TABLE lich_su_hanh_trinh_don (
    ma_hanh_trinh BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_don_hang_shop BIGINT NOT NULL,
    ma_hub BIGINT NULL,
    tieu_de_moc NVARCHAR(150) NOT NULL,
    vi_tri_hien_tai NVARCHAR(255) NULL,
    thoi_gian DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop) ON DELETE CASCADE,
    FOREIGN KEY (ma_hub) REFERENCES tram_trung_chuyen_hub(ma_hub)
);

CREATE TABLE yeu_cau_chuyen_hoan (
    ma_chuyen_hoan BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_don_hang_shop BIGINT NOT NULL UNIQUE,
    ly_do_chuyen_hoan NVARCHAR(255) NOT NULL,
    ma_van_don_tra_hang NVARCHAR(100) NULL,
    trang_thai NVARCHAR(30) DEFAULT N'DANG_CHUYEN_HOAN',
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop)
);

-- ============================================================================
-- 7. PHÂN HỆ TÀI CHÍNH, VÍ, KÝ QUỸ & TRẢ SAU (7 Bảng)
-- ============================================================================
CREATE TABLE vi_nguoi_ban (
    ma_vi BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL UNIQUE,
    so_du_kha_dung DECIMAL(18,2) DEFAULT 0.00,
    so_du_tam_giu_escrow DECIMAL(18,2) DEFAULT 0.00,
    tong_tien_da_rut DECIMAL(18,2) DEFAULT 0.00,
    phien_ban_lock INT DEFAULT 1, -- Optimistic Locking cho dòng tiền
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

CREATE TABLE giao_dich_ky_quy (
    ma_ky_quy BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_don_hang_shop BIGINT NOT NULL UNIQUE,
    ma_gian_hang BIGINT NOT NULL,
    tong_tien_don_hang DECIMAL(18,2) NOT NULL,
    ty_le_phi_san_phan_tram DECIMAL(5,2) DEFAULT 3.00,
    tien_phi_san DECIMAL(18,2) NOT NULL,
    tien_phi_thanh_toan DECIMAL(18,2) DEFAULT 0.00,
    tien_phi_dich_vu DECIMAL(18,2) DEFAULT 0.00,
    tien_thuc_nhan_ve_vi DECIMAL(18,2) NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'DANG_TAM_GIU',
    ngay_du_kien_nha_tien DATETIME2 NOT NULL,
    ngay_thuc_te_nha_tien DATETIME2 NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

CREATE TABLE lich_su_giao_dich_vi (
    ma_giao_dich BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_vi BIGINT NOT NULL,
    loai_giao_dich NVARCHAR(50) NOT NULL,
    so_tien DECIMAL(18,2) NOT NULL,
    so_du_sau_giao_dich DECIMAL(18,2) NOT NULL,
    ma_tham_chieu NVARCHAR(100) NOT NULL,
    mo_ta NVARCHAR(255) NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_vi) REFERENCES vi_nguoi_ban(ma_vi)
);

CREATE TABLE yeu_cau_rut_tien (
    ma_yeu_cau BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL,
    so_tien_rut DECIMAL(18,2) NOT NULL,
    ten_ngan_hang NVARCHAR(100) NOT NULL,
    so_tai_khoan NVARCHAR(50) NOT NULL,
    ten_chu_tai_khoan NVARCHAR(100) NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'CHO_DUYET',
    ly_do_tu_choi NVARCHAR(255) NULL,
    ma_admin_duyet BIGINT NULL,
    ngay_duyet DATETIME2 NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang),
    FOREIGN KEY (ma_admin_duyet) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE tai_khoan_tra_sau (
    ma_tk_tra_sau BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL UNIQUE,
    han_muc_duoc_cap DECIMAL(18,2) NOT NULL DEFAULT 5000000.00,
    han_muc_con_lai DECIMAL(18,2) NOT NULL DEFAULT 5000000.00,
    diem_tin_dung INT DEFAULT 650,
    trang_thai NVARCHAR(30) DEFAULT N'HOAT_DONG',
    ngay_cap DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE hop_dong_tra_sau (
    ma_hop_dong BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_tk_tra_sau BIGINT NOT NULL,
    ma_don_hang_tong BIGINT NOT NULL UNIQUE,
    tong_so_tien_vay DECIMAL(18,2) NOT NULL,
    so_ky_tra_gop INT NOT NULL DEFAULT 3,
    lai_suat_thang_phan_tram DECIMAL(4,2) DEFAULT 0.00,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_tk_tra_sau) REFERENCES tai_khoan_tra_sau(ma_tk_tra_sau),
    FOREIGN KEY (ma_don_hang_tong) REFERENCES don_hang_tong(ma_don_hang_tong)
);

CREATE TABLE ky_thanh_toan_tra_sau (
    ma_ky BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_hop_dong BIGINT NOT NULL,
    ky_so INT NOT NULL,
    so_tien_can_tra DECIMAL(18,2) NOT NULL,
    so_tien_da_tra DECIMAL(18,2) DEFAULT 0.00,
    han_chot_thanh_toan DATE NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'CHUA_TRA',
    FOREIGN KEY (ma_hop_dong) REFERENCES hop_dong_tra_sau(ma_hop_dong) ON DELETE CASCADE
);

-- ============================================================================
-- 8. PHÂN HỆ CSKH, KHIẾU NẠI & FAQ (5 Bảng)
-- ============================================================================
CREATE TABLE phieu_khieu_nai (
    ma_phieu BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_code_phieu NVARCHAR(50) NOT NULL UNIQUE,
    ma_khach_hang BIGINT NOT NULL,
    ma_don_hang_shop BIGINT NOT NULL,
    ma_gian_hang BIGINT NOT NULL,
    loai_khieu_nai NVARCHAR(50) NOT NULL,
    muc_do_uu_tien NVARCHAR(20) DEFAULT N'TRUNG_BINH',
    trang_thai NVARCHAR(30) DEFAULT N'MO_MOI',
    noi_dung_mo_ta NVARCHAR(MAX) NOT NULL,
    giai_phap_yeu_cau NVARCHAR(50) NOT NULL,
    ma_cskh_xu_ly BIGINT NULL,
    ma_nguoi_phan_quyet BIGINT NULL,
    ghi_chu_phan_quyet NVARCHAR(MAX) NULL,
    so_tien_hoan_tra DECIMAL(18,2) DEFAULT 0.00,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_khach_hang) REFERENCES nguoi_dung(ma_nguoi_dung),
    FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang),
    FOREIGN KEY (ma_cskh_xu_ly) REFERENCES nguoi_dung(ma_nguoi_dung),
    FOREIGN KEY (ma_nguoi_phan_quyet) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE bang_chung_khieu_nai (
    ma_bang_chung BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu BIGINT NOT NULL,
    link_tep_tin NVARCHAR(500) NOT NULL,
    loai_tep_tin NVARCHAR(20) DEFAULT N'HINH_ANH',
    vai_tro_tai_len NVARCHAR(30) NOT NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_phieu) REFERENCES phieu_khieu_nai(ma_phieu) ON DELETE CASCADE
);

CREATE TABLE ghi_chu_noi_bo_khieu_nai (
    ma_ghi_chu BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu BIGINT NOT NULL,
    ma_nhan_vien BIGINT NOT NULL,
    noi_dung NVARCHAR(MAX) NOT NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_phieu) REFERENCES phieu_khieu_nai(ma_phieu) ON DELETE CASCADE,
    FOREIGN KEY (ma_nhan_vien) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE lenh_hoan_tien_boi_thuong (
    ma_lenh BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_phieu BIGINT NOT NULL UNIQUE,
    nguoi_nhan_tien BIGINT NOT NULL,
    so_tien DECIMAL(18,2) NOT NULL,
    ben_chiu_phi NVARCHAR(50) NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'DA_CHUYEN_TIEN',
    ngay_thuc_hien DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_phieu) REFERENCES phieu_khieu_nai(ma_phieu),
    FOREIGN KEY (nguoi_nhan_tien) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE cau_hoi_thuong_gap (
    ma_faq BIGINT IDENTITY(1,1) PRIMARY KEY,
    chuyen_muc NVARCHAR(100) NOT NULL,
    cau_hoi NVARCHAR(255) NOT NULL,
    cau_tra_loi NVARCHAR(MAX) NOT NULL,
    thu_tu_hien_thi INT DEFAULT 0,
    dang_xuat_ban BIT DEFAULT 1
);

-- ============================================================================
-- 9. PHÂN HỆ ĐÁNH GIÁ, Q&A, WISHLIST (5 Bảng)
-- ============================================================================
CREATE TABLE danh_gia_san_pham (
    ma_danh_gia BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_chi_tiet_don BIGINT NOT NULL UNIQUE,
    ma_san_pham BIGINT NOT NULL,
    ma_gian_hang BIGINT NOT NULL,
    ma_nguoi_dung BIGINT NOT NULL,
    so_sao INT NOT NULL CHECK (so_sao BETWEEN 1 AND 5),
    noi_dung NVARCHAR(MAX) NULL,
    an_danh BIT DEFAULT 0,
    bi_an BIT DEFAULT 0,
    phan_hoi_cua_shop NVARCHAR(MAX) NULL,
    ngay_shop_phan_hoi DATETIME2 NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_chi_tiet_don) REFERENCES chi_tiet_don_hang(ma_chi_tiet_don),
    FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE hinh_anh_danh_gia (
    ma_anh_danh_gia BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_danh_gia BIGINT NOT NULL,
    link_anh NVARCHAR(500) NOT NULL,
    FOREIGN KEY (ma_danh_gia) REFERENCES danh_gia_san_pham(ma_danh_gia) ON DELETE CASCADE
);

CREATE TABLE san_pham_yeu_thich (
    ma_nguoi_dung BIGINT NOT NULL,
    ma_san_pham BIGINT NOT NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    PRIMARY KEY (ma_nguoi_dung, ma_san_pham),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung) ON DELETE CASCADE,
    FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham) ON DELETE CASCADE
);

CREATE TABLE theo_doi_gian_hang (
    ma_nguoi_dung BIGINT NOT NULL,
    ma_gian_hang BIGINT NOT NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    PRIMARY KEY (ma_nguoi_dung, ma_gian_hang),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung) ON DELETE CASCADE,
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang) ON DELETE CASCADE
);

CREATE TABLE hoi_dap_san_pham (
    ma_hoi_dap BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_san_pham BIGINT NOT NULL,
    ma_nguoi_hoi BIGINT NOT NULL,
    cau_hoi NVARCHAR(MAX) NOT NULL,
    cau_tra_loi NVARCHAR(MAX) NULL,
    ma_nguoi_tra_loi BIGINT NULL,
    ngay_hoi DATETIME2 DEFAULT GETDATE(),
    ngay_tra_loi DATETIME2 NULL,
    FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham) ON DELETE CASCADE,
    FOREIGN KEY (ma_nguoi_hoi) REFERENCES nguoi_dung(ma_nguoi_dung),
    FOREIGN KEY (ma_nguoi_tra_loi) REFERENCES nguoi_dung(ma_nguoi_dung)
);

-- ============================================================================
-- 10. PHÂN HỆ KHUYẾN MÃI, FLASH SALE & TÍCH XU (8 Bảng)
-- ============================================================================
CREATE TABLE ma_giam_gia (
    ma_voucher BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NULL,
    ma_code_voucher NVARCHAR(50) NOT NULL UNIQUE,
    ten_voucher NVARCHAR(150) NOT NULL,
    loai_voucher NVARCHAR(30) NOT NULL,
    gia_tri_giam DECIMAL(18,2) NOT NULL,
    giam_toi_da DECIMAL(18,2) NULL,
    gia_tri_don_toi_thieu DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    tong_so_luong_phat_hanh INT NOT NULL DEFAULT 1000,
    gioi_han_moi_nguoi INT NOT NULL DEFAULT 1,
    so_luong_da_dung INT DEFAULT 0,
    ngay_bat_dau DATETIME2 NOT NULL,
    ngay_ket_thuc DATETIME2 NOT NULL,
    dang_hoat_dong BIT DEFAULT 1,
    da_xoa BIT DEFAULT 0,
    phien_ban_lock INT DEFAULT 1, -- Chống nhận/sử dụng lố số lượng voucher
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

CREATE TABLE lich_su_dung_ma_giam_gia (
    ma_lich_su_dung BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_voucher BIGINT NOT NULL,
    ma_nguoi_dung BIGINT NOT NULL,
    ma_don_hang_tong BIGINT NOT NULL,
    ma_don_hang_shop BIGINT NULL,
    so_tien_da_giam DECIMAL(18,2) NOT NULL,
    ngay_su_dung DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_voucher) REFERENCES ma_giam_gia(ma_voucher),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung),
    FOREIGN KEY (ma_don_hang_tong) REFERENCES don_hang_tong(ma_don_hang_tong),
    FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop)
);

CREATE TABLE khung_gio_flash_sale (
    ma_flash_sale BIGINT IDENTITY(1,1) PRIMARY KEY,
    tieu_de NVARCHAR(150) NOT NULL,
    link_banner NVARCHAR(500) NULL,
    thoi_gian_bat_dau DATETIME2 NOT NULL,
    thoi_gian_ket_thuc DATETIME2 NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'SAP_DIEN_RA'
);

CREATE TABLE san_pham_flash_sale (
    ma_san_pham_fs BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_flash_sale BIGINT NOT NULL,
    ma_bien_the BIGINT NOT NULL,
    gia_flash_sale DECIMAL(18,2) NOT NULL,
    so_luong_gioi_han INT NOT NULL,
    so_luong_da_ban INT DEFAULT 0,
    gioi_han_mua_moi_khach INT DEFAULT 2,
    phien_ban_lock INT DEFAULT 1, -- Optimistic Locking 
    FOREIGN KEY (ma_flash_sale) REFERENCES khung_gio_flash_sale(ma_flash_sale) ON DELETE CASCADE,
    FOREIGN KEY (ma_bien_the) REFERENCES bien_the_san_pham(ma_bien_the)
);

CREATE TABLE combo_khuyen_mai (
    ma_combo BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL,
    ten_combo NVARCHAR(150) NOT NULL,
    loai_combo NVARCHAR(50) NOT NULL,
    so_luong_toi_thieu INT NOT NULL DEFAULT 2,
    gia_tri_giam DECIMAL(18,2) NOT NULL,
    ngay_bat_dau DATETIME2 NOT NULL,
    ngay_ket_thuc DATETIME2 NOT NULL,
    dang_hoat_dong BIT DEFAULT 1,
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

CREATE TABLE vi_xu_nguoi_dung (
    ma_nguoi_dung BIGINT PRIMARY KEY,
    so_xu_hien_tai BIGINT DEFAULT 0,
    tong_xu_da_tich_luy BIGINT DEFAULT 0,
    ngay_cap_nhat DATETIME2 DEFAULT GETDATE(),
    phien_ban_lock INT DEFAULT 1,
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE lich_su_giao_dich_xu (
    ma_giao_dich_xu BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    so_xu_thay_doi BIGINT NOT NULL,
    loai_giao_dich NVARCHAR(50) NOT NULL,
    ma_tham_chieu NVARCHAR(100) NULL,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

CREATE TABLE vong_quay_may_man (
    ma_luot_quay BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nguoi_dung BIGINT NOT NULL,
    phan_thuong NVARCHAR(150) NOT NULL,
    ngay_quay DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_nguoi_dung) REFERENCES nguoi_dung(ma_nguoi_dung)
);

-- ============================================================================
-- 11. PHÂN HỆ LIVE COMMERCE, VIDEO NGẮN & REAL-TIME CHAT (5 Bảng)
-- ============================================================================
CREATE TABLE phong_livestream (
    ma_live BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL,
    tieu_de NVARCHAR(255) NOT NULL,
    link_stream_rtmp NVARCHAR(500) NOT NULL,
    link_anh_bia NVARCHAR(500) NULL,
    tong_luot_xem INT DEFAULT 0,
    trang_thai NVARCHAR(30) DEFAULT N'DANG_LIVE',
    thoi_gian_bat_dau DATETIME2 DEFAULT GETDATE(),
    thoi_gian_ket_thuc DATETIME2 NULL,
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

CREATE TABLE san_pham_livestream (
    ma_sp_live BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_live BIGINT NOT NULL,
    ma_san_pham BIGINT NOT NULL,
    la_san_pham_dang_ghim BIT DEFAULT 0,
    gia_doc_quyen_live DECIMAL(18,2) NULL,
    FOREIGN KEY (ma_live) REFERENCES phong_livestream(ma_live) ON DELETE CASCADE,
    FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham)
);

CREATE TABLE video_ngan_review (
    ma_video BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nguoi_dang BIGINT NOT NULL,
    ma_san_pham_gan_kem BIGINT NOT NULL,
    tieu_de NVARCHAR(255) NOT NULL,
    link_video NVARCHAR(500) NOT NULL,
    tong_luot_tim INT DEFAULT 0,
    tong_luot_xem INT DEFAULT 0,
    ngay_dang DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_nguoi_dang) REFERENCES nguoi_dung(ma_nguoi_dung),
    FOREIGN KEY (ma_san_pham_gan_kem) REFERENCES san_pham(ma_san_pham)
);

CREATE TABLE cuoc_tro_chuyen (
    ma_cuoc_tro_chuyen BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_khach_hang BIGINT NOT NULL,
    ma_gian_hang BIGINT NOT NULL,
    tin_nhan_cuoi_cung NVARCHAR(MAX) NULL,
    thoi_gian_tin_cuoi DATETIME2 DEFAULT GETDATE(),
    so_tin_chua_doc_khach INT DEFAULT 0,
    so_tin_chua_doc_shop INT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    UNIQUE (ma_khach_hang, ma_gian_hang),
    FOREIGN KEY (ma_khach_hang) REFERENCES nguoi_dung(ma_nguoi_dung),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang)
);

-- Khuyến nghị thực tế: Bảng tin_nhan nên được migrate sang NoSQL (MongoDB) khi scale lớn
CREATE TABLE tin_nhan (
    ma_tin_nhan BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_cuoc_tro_chuyen BIGINT NOT NULL,
    loai_nguoi_gui NVARCHAR(20) NOT NULL,
    ma_nguoi_gui BIGINT NOT NULL,
    loai_tin_nhan NVARCHAR(30) DEFAULT N'VAN_BAN',
    noi_dung NVARCHAR(MAX) NOT NULL,
    du_lieu_dinh_kem_json NVARCHAR(MAX) NULL,
    da_xem BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_cuoc_tro_chuyen) REFERENCES cuoc_tro_chuyen(ma_cuoc_tro_chuyen) ON DELETE CASCADE
);

-- ============================================================================
-- 12. PHÂN HỆ ADS, AFFILIATE & THÔNG BÁO, BẢO MẬT (6 Bảng)
-- ============================================================================
CREATE TABLE chien_dich_quang_cao (
    ma_chien_dich BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_gian_hang BIGINT NOT NULL,
    ma_san_pham BIGINT NOT NULL,
    ten_chien_dich NVARCHAR(150) NOT NULL,
    ngan_sach_ngay DECIMAL(18,2) NOT NULL,
    tong_chi_phi_da_dung DECIMAL(18,2) DEFAULT 0.00,
    trang_thai NVARCHAR(30) DEFAULT N'DANG_CHAY',
    ngay_bat_dau DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_gian_hang) REFERENCES gian_hang(ma_gian_hang),
    FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham)
);

CREATE TABLE tu_khoa_dau_thau_ads (
    ma_tu_khoa BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_chien_dich BIGINT NOT NULL,
    tu_khoa NVARCHAR(100) NOT NULL,
    gia_thau_moi_click_cpc DECIMAL(18,2) NOT NULL,
    tong_luot_click INT DEFAULT 0,
    dang_kich_hoat BIT DEFAULT 1,
    FOREIGN KEY (ma_chien_dich) REFERENCES chien_dich_quang_cao(ma_chien_dich) ON DELETE CASCADE
);

CREATE TABLE tiep_thi_lien_ket (
    ma_affiliate BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_koc BIGINT NOT NULL,
    ma_san_pham BIGINT NOT NULL,
    ma_link_affiliate NVARCHAR(100) NOT NULL UNIQUE,
    ty_le_hoa_hong_phan_tram DECIMAL(4,2) NOT NULL,
    tong_hoa_hong_kiem_duoc DECIMAL(18,2) DEFAULT 0.00,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_koc) REFERENCES nguoi_dung(ma_nguoi_dung),
    FOREIGN KEY (ma_san_pham) REFERENCES san_pham(ma_san_pham)
);

CREATE TABLE don_hang_tiep_thi (
    ma_don_affiliate BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_affiliate BIGINT NOT NULL,
    ma_don_hang_shop BIGINT NOT NULL,
    hoa_hong_duoc_nhan DECIMAL(18,2) NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'CHO_DOI_SOAT',
    ngay_ghi_nhan DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_affiliate) REFERENCES tiep_thi_lien_ket(ma_affiliate),
    FOREIGN KEY (ma_don_hang_shop) REFERENCES don_hang_shop(ma_don_hang_shop)
);

CREATE TABLE thong_bao_he_thong (
    ma_thong_bao BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_nguoi_nhan BIGINT NOT NULL,
    tieu_de NVARCHAR(200) NOT NULL,
    noi_dung NVARCHAR(MAX) NOT NULL,
    loai_thong_bao NVARCHAR(50) NOT NULL,
    link_dieu_huong NVARCHAR(500) NULL,
    da_doc BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (ma_nguoi_nhan) REFERENCES nguoi_dung(ma_nguoi_dung) ON DELETE CASCADE
);

CREATE TABLE canh_bao_gian_lan (
    ma_canh_bao BIGINT IDENTITY(1,1) PRIMARY KEY,
    loai_doi_tuong NVARCHAR(50) NOT NULL,
    ma_doi_tuong BIGINT NOT NULL,
    diem_rui_ro INT NOT NULL,
    ly_do_canh_bao NVARCHAR(255) NOT NULL,
    trang_thai NVARCHAR(30) DEFAULT N'CHO_DIEU_TRA',
    ngay_tao DATETIME2 DEFAULT GETDATE()
);
GO

-- ============================================================================
-- 13. INDEXES TỐI ƯU HÓA HIỆU NĂNG TRUY VẤN
-- ============================================================================
CREATE INDEX idx_san_pham_gian_hang ON san_pham(ma_gian_hang);
CREATE INDEX idx_san_pham_danh_muc ON san_pham(ma_danh_muc);
CREATE INDEX idx_san_pham_slug ON san_pham(duong_dan_slug);
CREATE INDEX idx_bien_the_sku ON bien_the_san_pham(ma_sku);
CREATE INDEX idx_ton_kho_bien_the ON ton_kho_chi_tiet(ma_bien_the);
CREATE INDEX idx_don_hang_shop_tong ON don_hang_shop(ma_don_hang_tong);
CREATE INDEX idx_don_hang_shop_gian_hang ON don_hang_shop(ma_gian_hang);
CREATE INDEX idx_nhiem_vu_tai_xe ON nhiem_vu_giao_hang(ma_tai_xe);
CREATE INDEX idx_ky_quy_gian_hang ON giao_dich_ky_quy(ma_gian_hang);
CREATE INDEX idx_thong_bao_nguoi_nhan ON thong_bao_he_thong(ma_nguoi_nhan);
-- [V2] Optimize query performance for soft delete
CREATE INDEX idx_san_pham_active ON san_pham(ma_san_pham, da_xoa, trang_thai);
GO