# FlexShop Multi-Vendor E-commerce

Chào mừng đến với dự án FlexShop! Repository này chứa Shared Foundation để cả team 7 người bắt đầu code song song.

## 🚀 1. Hướng Dẫn Cài Đặt Ban Đầu

### Yêu cầu hệ thống:
- Java 17
- Maven
- SQL Server

### Bước 1: Clone Project
\\\ash
git clone https://github.com/nguyendhbth04829-lab/FlexShop.git
cd FlexShop
\\\

### Bước 2: Khởi tạo Database
1. Mở SQL Server Management Studio (SSMS).
2. Chạy toàn bộ file script database/db_v2_setup.sql.
3. Script sẽ tự động tạo database tên là **FlexShop_V2_Full** với 74 bảng chuẩn xác.

### Bước 3: Cấu hình Môi trường
1. Copy file .env.example thành .env (nếu dùng công cụ đọc .env) hoặc cấu hình trực tiếp biến môi trường.
2. Hoặc cách nhanh nhất cho Local Dev: Sửa trực tiếp thông tin trong src/main/resources/application.properties:
   - spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=FlexShop_V2_Full;encrypt=true;trustServerCertificate=true;
   - spring.datasource.username=sa
   - spring.datasource.password=mat_khau_cua_ban

### Bước 4: Chạy Project
Mở project bằng IntelliJ IDEA, tải Maven dependencies và bấm chạy file HelloWolrdFlexShopApplication.java.
Hoặc chạy bằng command:
\\\ash
./mvnw spring-boot:run
\\\

## 📚 2. Tài Liệu Quan Trọng (Vui lòng đọc trước khi code)

- [Shared Files & Phân chia Package](docs/SHARED-FILES.md)
- [Database Mapping Entities](docs/DATABASE-MAPPING.md)
- [Hướng dẫn Code (Controller -> Service -> Repo)](docs/DEVELOPMENT-GUIDE.md)
- [Hướng dẫn Git Workflow](docs/GIT-WORKFLOW.md)

## 🎯 3. Nguyên Tắc Cốt Lõi:
1. **KHÔNG** sửa các file dùng chung (Base) nếu chưa báo cho team.
2. Code tính năng của mình ở trong package/module của mình.
3. Luôn lấy file database/db_v2_setup.sql làm nguồn chuẩn duy nhất cho DB, không tham chiếu tài liệu cũ nào khác.
