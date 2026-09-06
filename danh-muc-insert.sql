-- Dữ liệu test cho chức năng Danh Mục (US-07)
-- Cấp 1
INSERT INTO danh_muc (ten_danh_muc, duong_dan_slug, cap_do, thu_tu_hien_thi, dang_hoat_dong, da_xoa) 
VALUES (N'Thời trang Nam', 'thoi-trang-nam', 1, 1, 1, 0);

INSERT INTO danh_muc (ten_danh_muc, duong_dan_slug, cap_do, thu_tu_hien_thi, dang_hoat_dong, da_xoa) 
VALUES (N'Thời trang Nữ', 'thoi-trang-nu', 1, 2, 1, 0);

-- Cấp 2
INSERT INTO danh_muc (ten_danh_muc, duong_dan_slug, ma_danh_muc_cha, cap_do, thu_tu_hien_thi, dang_hoat_dong, da_xoa) 
VALUES (N'Áo Nam', 'ao-nam', 1, 2, 1, 1, 0);

INSERT INTO danh_muc (ten_danh_muc, duong_dan_slug, ma_danh_muc_cha, cap_do, thu_tu_hien_thi, dang_hoat_dong, da_xoa) 
VALUES (N'Quần Nam', 'quan-nam', 1, 2, 2, 1, 0);

-- Cấp 3
INSERT INTO danh_muc (ten_danh_muc, duong_dan_slug, ma_danh_muc_cha, cap_do, thu_tu_hien_thi, dang_hoat_dong, da_xoa) 
VALUES (N'Áo thun Nam', 'ao-thun-nam', 3, 3, 1, 1, 0);

INSERT INTO danh_muc (ten_danh_muc, duong_dan_slug, ma_danh_muc_cha, cap_do, thu_tu_hien_thi, dang_hoat_dong, da_xoa) 
VALUES (N'Áo sơ mi Nam', 'ao-so-mi-nam', 3, 3, 2, 1, 0);

-- Tạo 1 gian hàng để test Sản phẩm
-- INSERT INTO nguoi_dung ...
-- INSERT INTO gian_hang ...
-- (Bỏ qua vì chưa có schema đầy đủ, chỉ insert SanPham)

-- Thêm 1 sản phẩm vào 'Áo thun Nam' (id=5) để test tính năng KHÔNG CHO XÓA danh mục đang có sản phẩm
INSERT INTO san_pham (ma_gian_hang, ma_danh_muc, ten_san_pham, duong_dan_slug, gia_co_ban, da_xoa)
VALUES (1, 5, N'Áo thun cotton', 'ao-thun-cotton', 100000, 0);
