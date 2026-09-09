-- Dữ liệu test cho chức năng WMS Đa Kho (US-15 & US-16)

-- 1. Tạo Kho Hàng
INSERT INTO kho_hang (ma_gian_hang, ten_kho, dia_chi, tinh_thanh, sdt_lien_he, la_kho_chinh, dang_hoat_dong, da_xoa)
VALUES (1, N'Kho Tổng Hà Nội', N'Số 1 Đại Cồ Việt', N'Hà Nội', '0987654321', 1, 1, 0);

-- 2. Tạo Vị Trí Kệ Kho
INSERT INTO vi_tri_ke_kho (ma_kho, ma_khu_vuc) VALUES (1, 'A1-01');
INSERT INTO vi_tri_ke_kho (ma_kho, ma_khu_vuc) VALUES (1, 'A1-02');

-- 3. Khởi tạo Tồn kho ban đầu cho Biến thể 1 (Áo thun Đen Size M)
INSERT INTO ton_kho_chi_tiet (ma_kho, ma_vi_tri, ma_bien_the, so_luong_ton, so_luong_tam_giu, phien_ban_lock)
VALUES (1, 1, 1, 50, 0, 1);

-- 4. Khởi tạo Tồn kho ban đầu cho Biến thể 2 (Áo thun Đen Size L) - Đang chạm mức cảnh báo an toàn
INSERT INTO ton_kho_chi_tiet (ma_kho, ma_vi_tri, ma_bien_the, so_luong_ton, so_luong_tam_giu, phien_ban_lock)
VALUES (1, 2, 2, 5, 0, 1);

-- 5. Tạo 1 Phiếu Nhập Kho (Lịch sử)
INSERT INTO phieu_nhap_xuat_kho (ma_kho, loai_phieu, ma_chung_tu_lien_quan, nguoi_lap_phieu, ghi_chu)
VALUES (1, 'NHAP_KHO', 'PO-2026-001', 1, N'Nhập hàng đầu tháng');

-- 6. Chi tiết Phiếu Nhập
INSERT INTO chi_tiet_phieu_kho (ma_phieu_kho, ma_bien_the, so_luong, don_gia_von)
VALUES (1, 1, 50, 200000);
INSERT INTO chi_tiet_phieu_kho (ma_phieu_kho, ma_bien_the, so_luong, don_gia_von)
VALUES (1, 2, 5, 200000);
