-- Dữ liệu test cho chức năng Sản Phẩm & Biến thể (US-12, 13, 14)
-- Yêu cầu: validate tên, ảnh, SKU duy nhất, lưu lịch sử giá

-- 1. Tạo Thương hiệu
INSERT INTO thuong_hieu (ten_thuong_hieu, link_logo, dang_hoat_dong, da_xoa) 
VALUES (N'Adidas', 'logo-adidas.png', 1, 0);

-- 2. Tạo Sản phẩm (dựa vào danh mục Áo thun Nam (id=5) đã tạo ở US-07)
INSERT INTO san_pham (ma_gian_hang, ma_danh_muc, ma_thuong_hieu, ten_san_pham, duong_dan_slug, gia_co_ban, can_nang_gram, chieu_dai_cm, chieu_rong_cm, chieu_cao_cm, da_xoa)
VALUES (1, 5, 1, N'Áo thun thể thao Adidas', 'ao-thun-the-thao-adidas', 250000, 250, 15, 10, 5, 0);

-- 3. Tạo Ảnh cho sản phẩm (Id sản phẩm vừa tạo là 2 do ở script trước insert id 1)
-- Theo luật: Tối đa 9 ảnh (1 chính, 8 phụ)
INSERT INTO hinh_anh_san_pham (ma_san_pham, link_anh, la_anh_chinh, thu_tu_hien_thi)
VALUES (2, 'ao-adidas-chinh.jpg', 1, 1);
INSERT INTO hinh_anh_san_pham (ma_san_pham, link_anh, la_anh_chinh, thu_tu_hien_thi)
VALUES (2, 'ao-adidas-phu1.jpg', 0, 2);

-- 4. Tạo Biến thể (Ma trận SKU) - Validate Giá gốc >= Giá bán
INSERT INTO bien_the_san_pham (ma_san_pham, ma_sku, ten_bien_the, gia_ban, gia_goc, da_xoa)
VALUES (2, 'ADI-AOTHUN-DEN-M', N'Màu Đen - Size M', 250000, 300000, 0);

INSERT INTO bien_the_san_pham (ma_san_pham, ma_sku, ten_bien_the, gia_ban, gia_goc, da_xoa)
VALUES (2, 'ADI-AOTHUN-DEN-L', N'Màu Đen - Size L', 250000, 300000, 0);

-- 5. Lịch sử giá (Demo chức năng tự động lưu khi giá cập nhật)
-- Giá cũ 250k, giá mới 220k (Giảm giá)
INSERT INTO lich_su_gia_bien_the (ma_bien_the, gia_cu, gia_moi, nguoi_thay_doi)
VALUES (1, 250000, 220000, 1);
