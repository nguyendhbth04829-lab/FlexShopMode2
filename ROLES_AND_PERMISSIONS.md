# TỔNG HỢP DANH SÁCH CHỨC NĂNG THEO VAI TRÒ HỆ THỐNG FLEXSHOP
*(Mô hình: Web E-commerce Multi-Vendor Marketplace)*

---

## 1. VAI TRÒ: KHÁCH HÀNG (CUSTOMER)
*Mục tiêu: Tìm kiếm, mua sắm tiện lợi từ nhiều shop, theo dõi đơn và đánh giá.*

### Tài khoản & Hồ sơ:
- Đăng ký, đăng nhập, đăng xuất, đổi mật khẩu.
- Quản lý hồ sơ cá nhân (Họ tên, SĐT, Avatar).
- Quản lý sổ địa chỉ nhận hàng (Thêm / Sửa / Xóa / Đặt làm mặc định).

### Duyệt & Tìm kiếm sản phẩm:
- Xem trang chủ (Banner, Sản phẩm mới, Bán chạy, Danh mục nổi bật).
- Tìm kiếm sản phẩm theo từ khóa.
- Lọc nâng cao (theo Danh mục, Khoảng giá, Đánh giá sao) và Sắp xếp (Giá tăng/giảm, Mới nhất, Bán chạy).
- Xem trang chi tiết sản phẩm: Hình ảnh, mô tả, chọn biến thể (Size/Màu), xem tồn kho, xem đánh giá của người mua trước.
- Xem trang hồ sơ gian hàng (Shop Profile) và toàn bộ sản phẩm của Shop đó.

### Giỏ hàng (Multi-vendor Cart):
- Thêm sản phẩm kèm biến thể vào giỏ hàng.
- Tự động gom nhóm sản phẩm theo từng Shop trong giỏ.
- Tăng/giảm số lượng, xóa sản phẩm khỏi giỏ.
- Tích chọn một phần hoặc toàn bộ sản phẩm để tiến hành Checkout.

### Đặt hàng & Thanh toán (Checkout):
- Chọn địa chỉ giao hàng và phương thức thanh toán (COD hoặc Mock Online Payment).
- Áp dụng mã giảm giá (Shop Voucher).
- Xem trước phí vận chuyển và tổng tiền phân tách theo từng gian hàng.
- Đặt hàng (Hệ thống tự động tách thành các `ShopOrder` tương ứng cho từng Shop).

### Quản lý đơn hàng & Vận chuyển:
- Xem danh sách đơn hàng theo trạng thái: *Chờ xử lý, Đang giao, Đã giao, Đã hủy*.
- Xem chi tiết từng đơn hàng con (ShopOrder) và thông tin đơn vị vận chuyển/Shipper.
- Hủy đơn hàng (khi đơn còn ở trạng thái chờ xác nhận).
- Xác nhận "Đã nhận được hàng".

### Đánh giá & Chăm sóc:
- Đánh giá sản phẩm (Chấm 1-5 sao + Bình luận + Đính kèm ảnh) sau khi nhận hàng thành công.
- Gửi yêu cầu hỗ trợ / khiếu nại (Hàng lỗi, giao thiếu, hàng giả...).
- Quản lý danh sách sản phẩm yêu thích (Wishlist).

---

## 2. VAI TRÒ: NGƯỜI BÁN (SELLER / SHOP OWNER)
*Mục tiêu: Đăng bán sản phẩm, quản lý tồn kho, xử lý đơn hàng và theo dõi doanh thu.*

### Quản lý Gian hàng (Shop):
- Đăng ký mở gian hàng (chờ Admin xét duyệt).
- Thiết lập thông tin Shop: Tên shop, mô tả, logo, banner, địa chỉ kho lấy hàng.

### Quản lý Sản phẩm (Product Catalog):
- Thêm mới sản phẩm, chọn danh mục sàn.
- Quản lý biến thể sản phẩm (Tạo phân loại theo Size, Màu sắc với mức giá và tồn kho riêng).
- Tải lên nhiều hình ảnh cho sản phẩm.
- Cập nhật thông tin, giá bán, tạm ẩn hoặc xóa sản phẩm (Soft delete).

### Quản lý Tồn kho (Inventory):
- Quản lý và cập nhật số lượng tồn kho theo từng biến thể sản phẩm.
- Cảnh báo sản phẩm sắp hết hàng.

### Xử lý Đơn hàng (Order Fulfillment):
- Tiếp nhận danh sách `ShopOrder` mới từ khách hàng.
- Xác nhận đơn hàng (Confirm) → Chuyển sang đóng gói chuẩn bị hàng.
- Bàn giao đơn cho Shipper (Gán Shipper hoặc xác nhận Shipper đã lấy hàng).
- Từ chối / Hủy đơn hàng trong trường hợp bất khả kháng (Kèm lý do).

### Khuyến mãi (Promotion):
- Tạo và quản lý mã giảm giá riêng của Shop (Shop Voucher: Giảm theo %, Giảm số tiền cố định, Giá trị đơn tối thiểu).

### Chăm sóc khách hàng & Đánh giá:
- Xem và phản hồi các đánh giá của khách hàng về sản phẩm của shop.
- Tiếp nhận và phối hợp xử lý khiếu nại từ bộ phận CSKH.

### Báo cáo & Thống kê (Dashboard):
- Thống kê tổng doanh thu theo mốc thời gian (Hôm nay, Tuần này, Tháng này).
- Thống kê số lượng đơn hàng (Thành công, Đang giao, Đã hủy).
- Thống kê top sản phẩm bán chạy nhất.

---

## 3. VAI TRÒ: QUẢN TRỊ VIÊN (ADMIN - NỀN TẢNG)
*Mục tiêu: Quản lý vận hành toàn sàn, kiểm duyệt, phân quyền và đảm bảo chất lượng hệ thống.*

### Quản lý Người dùng (User Management):
- Xem danh sách toàn bộ tài khoản (Customer, Seller, Shipper, CSKH).
- Tìm kiếm, xem chi tiết thông tin và lịch sử hoạt động.
- Khóa (Lock/Ban) hoặc Mở khóa tài khoản vi phạm chính sách.

### Quản lý & Duyệt Gian hàng (Shop Management):
- Duyệt (Approve) hoặc Từ chối (Reject) yêu cầu đăng ký mở Shop mới.
- Tạm ngưng (Suspend) hoặc Khóa các gian hàng có dấu hiệu gian lận/vi phạm.

### Quản lý Danh mục hệ thống (Category Management):
- Thêm, sửa, xóa các danh mục sản phẩm toàn sàn (Cấu trúc phân cấp Cha - Con).

### Quản lý Sản phẩm toàn sàn (Product Moderation):
- Giám sát danh mục sản phẩm đăng bán trên toàn hệ thống.
- Ẩn hoặc Gỡ bỏ các sản phẩm vi phạm bản quyền, hàng cấm.

### Giám sát Đơn hàng & Vận hành:
- Theo dõi tổng quan luồng đơn hàng trên toàn hệ sinh thái.
- Can thiệp cưỡng chế hủy đơn hoặc hoàn tiền khi xảy ra tranh chấp nghiêm trọng.

### Phân quyền & Quản lý Nhân sự nội bộ:
- Tạo tài khoản và cấp quyền cho nhân viên nội bộ: Nhân viên CSKH, Nhân viên Giao hàng.

### Báo cáo & Dashboard tổng thể:
- Thống kê số liệu toàn sàn: Tổng người dùng, tổng số shop, tổng sản phẩm, tổng đơn hàng, tổng GMV (Doanh thu sàn).
- Biểu đồ tăng trưởng người dùng và đơn hàng.

---

## 4. VAI TRÒ: NHÂN VIÊN GIAO HÀNG (SHIPPER)
*Mục tiêu: Nhận đơn từ Shop, giao hàng cho khách đúng hạn, thu tiền COD và cập nhật trạng thái.*

### Tài khoản & Trạng thái làm việc:
- Đăng nhập tài khoản Shipper.
- Bật/Tắt trạng thái làm việc (Đang sẵn sàng nhận đơn / Tạm nghỉ).

### Nhận & Quản lý chuyến giao (Task Management):
- Xem danh sách các đơn hàng (`ShopOrder`) cần lấy từ các Shop trong khu vực phụ trách.
- Nhận đơn vận chuyển (Accept Order).
- Xem lộ trình lấy hàng tại kho của Seller và địa chỉ giao tới Customer.

### Quy trình Giao nhận (Delivery Lifecycle):
- **Lấy hàng:** Đến Shop lấy hàng → Xác nhận "Đã lấy hàng" (Chuyển trạng thái đơn: *Đang giao / In Transit*).
- **Giao hàng:**
  - Xem thông tin người nhận (Họ tên, SĐT, Địa chỉ, Tiền thu hộ COD).
  - Xác nhận **"Giao hàng thành công"** (Thu tiền COD nếu có, cập nhật trạng thái đơn).
  - Báo cáo **"Giao hàng thất bại"** (Chọn lý do: Khách không nghe máy, Sai địa chỉ, Khách đổi ý không nhận → Lên lịch hẹn giao lại lần 2/3).

### Lịch sử & Đối soát:
- Xem lịch sử các đơn hàng đã giao thành công/thất bại.
- Thống kê tổng số tiền COD đã thu hộ trong ngày để đối soát nộp lại sàn/shop.

---

## 5. VAI TRÒ: NHÂN VIÊN CHĂM SÓC KHÁCH HÀNG (CUSTOMER SERVICE / SUPPORT)
*Mục tiêu: Tiếp nhận và xử lý khiếu nại, giải quyết tranh chấp giữa Khách - Shop - Shipper.*

### Quản lý Khiếu nại & Hỗ trợ (Complaint / Ticket System):
- Tiếp nhận các yêu cầu khiếu nại từ Khách hàng hoặc Người bán gửi lên.
- Phân loại khiếu nại theo danh mục: *Hàng hỏng vỡ, Giao sai mẫu, Không nhận được hàng, Shipper thái độ kém, Hàng giả...*
- Đặt mức độ ưu tiên xử lý (Khẩn cấp / Trung bình / Thấp).

### Tra cứu & Xác minh thông tin:
- Tra cứu nhanh thông tin lịch sử đơn hàng, lịch sử vận chuyển của Shipper và thông tin Shop liên quan.
- Ghi chú nhật ký xử lý vụ việc (Internal notes) trong từng ticket.

### Xử lý & Giải quyết tranh chấp:
- Đóng vai trò trung gian trao đổi với Shop và Shipper để làm rõ sự cố.
- Đưa ra quyết định xử lý:
  - *Đóng khiếu nại / Bác bỏ yêu cầu* (nếu khách khiếu nại sai).
  - *Chấp thuận đổi trả / Yêu cầu shop gửi bù hàng*.
  - *Duyệt hoàn tiền cho khách hàng* (khi hàng lỗi/hư hỏng).

### Quản lý Câu hỏi thường gặp (FAQ / Help Center):
- Quản lý danh sách các bài viết hướng dẫn mua hàng, chính sách đổi trả, quy định mở shop cho người dùng tham khảo.

### Báo cáo chất lượng CSKH:
- Báo cáo số lượng khiếu nại đã tiếp nhận, tỷ lệ xử lý thành công, thời gian xử lý trung bình.
- Thống kê các Shop/Sản phẩm có tỷ lệ bị khiếu nại cao để đề xuất Admin xử lý.

---

## 💡 TÓM TẮT MA TRẬN PHÂN QUYỀN (QUICK VIEW)

| Module / Chức năng | Khách hàng | Seller | Shipper | CSKH | Admin |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Đăng ký / Đăng nhập** | ✅ | ✅ | ✅ (Cấp TK) | ✅ (Cấp TK) | ✅ (Cấp TK) |
| **Duyệt / Tìm kiếm / Mua hàng** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Quản lý Shop & Đăng sản phẩm** | ❌ | ✅ | ❌ | ❌ | 👁️ (Kiểm duyệt) |
| **Xác nhận / Đóng gói đơn (ShopOrder)** | ❌ | ✅ | ❌ | ❌ | 👁️ (Giám sát) |
| **Nhận đơn & Cập nhật giao hàng** | ❌ | ❌ | ✅ | ❌ | 👁️ (Giám sát) |
| **Xử lý Khiếu nại / Tranh chấp** | Gửi đơn | Phối hợp | Phối hợp | ✅ (Xử lý chính) | ✅ (Duyệt tối cao) |
| **Duyệt Shop / Quản lý Danh mục sàn** | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Báo cáo & Dashboard toàn hệ thống** | ❌ | ❌ (Chỉ xem shop mình) | ❌ (Chỉ xem cá nhân) | 👁️ (Xem CSKH) | ✅ (Toàn sàn) |
