package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads/khieu-nai/}")
    private String uploadDir;

    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    public static final int MAX_FILE_COUNT = 5;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".gif",
            ".mp4", ".mov", ".avi", ".webm", ".mkv"
    );

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "video/mp4", "video/quicktime", "video/x-msvideo", "video/webm", "video/x-matroska", "video/3gpp"
    );

    public static class FileUploadResult {
        private final String fileUrl;
        private final String fileType; // HINH_ANH or VIDEO

        public FileUploadResult(String fileUrl, String fileType) {
            this.fileUrl = fileUrl;
            this.fileType = fileType;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public String getFileType() {
            return fileType;
        }
    }

    /**
     * Kiểm tra tính hợp lệ nghiêm ngặt của tệp tin đính kèm
     */
    public void kiemTraTepTinHopLe(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        // 1. Kiểm tra dung lượng
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Tệp tin '" + file.getOriginalFilename() + "' vượt quá dung lượng cho phép (Tối đa 50MB)!");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Tên tệp tin không hợp lệ hoặc bị rỗng!");
        }

        // 2. Chống Directory Traversal attack
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new SecurityException("Phát hiện đường dẫn tệp tin không an toàn: " + originalFilename);
        }

        // 3. Kiểm tra phần mở rộng tệp tin
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("Tệp tin '" + originalFilename + "' thiếu phần mở rộng (.jpg, .png, .mp4...)!");
        }

        String extension = originalFilename.substring(lastDotIndex).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Tệp tin '" + originalFilename + "' không thuộc định dạng được hỗ trợ (Chỉ chấp nhận: JPG, PNG, WEBP, GIF, MP4, MOV, AVI, WEBM, MKV)!");
        }

        // 4. Kiểm tra MIME Content-Type
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String lowerContentType = contentType.toLowerCase();
            boolean isAllowedMime = ALLOWED_MIME_TYPES.stream().anyMatch(lowerContentType::startsWith);
            if (!isAllowedMime && !lowerContentType.equals("application/octet-stream")) {
                throw new IllegalArgumentException("Kiểu nội dung MIME '" + contentType + "' của tệp '" + originalFilename + "' không hợp lệ cho ảnh hoặc video bằng chứng!");
            }
        }
    }

    /**
     * Lưu tệp tin an toàn vào thư mục lưu trữ
     */
    public FileUploadResult luuTepTin(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        kiemTraTepTinHopLe(file);

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        String uniqueFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
        Path targetLocation = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String fileType = "HINH_ANH";
        if (extension.matches(".*(mp4|mov|avi|webm|mkv|3gp)$")) {
            fileType = "VIDEO";
        }

        String fileUrl = "/uploads/khieu-nai/" + uniqueFileName;
        return new FileUploadResult(fileUrl, fileType);
    }

    /**
     * US-37: Luu anh bang chung POD cua shipper (chi nhan anh, toi da 10MB).
     * Thu muc: uploads/pod/ - URL: /uploads/pod/
     */
    public String luuAnhPod(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Bat buoc chup/upload anh bang chung giao hang (POD)!");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Anh POD vuot qua 10MB!");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()
                || originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new IllegalArgumentException("Ten file anh POD khong hop le!");
        }
        int dot = originalFilename.lastIndexOf(".");
        if (dot == -1) {
            throw new IllegalArgumentException("Anh POD thieu phan mo rong (.jpg, .png...)!");
        }
        String extension = originalFilename.substring(dot).toLowerCase();
        if (!Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(extension)) {
            throw new IllegalArgumentException("Anh POD chi chap nhan JPG, PNG, WEBP, GIF!");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()
                && !contentType.toLowerCase().startsWith("image/")
                && !contentType.equalsIgnoreCase("application/octet-stream")) {
            throw new IllegalArgumentException("File POD phai la anh!");
        }
        Path podPath = Paths.get("uploads", "pod");
        if (!Files.exists(podPath)) {
            Files.createDirectories(podPath);
        }
        String uniqueFileName = "pod_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;
        Files.copy(file.getInputStream(), podPath.resolve(uniqueFileName), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/pod/" + uniqueFileName;
    }

    /**
     * Lưu tệp tin ảnh banner cho chương trình Flash Sale từ máy tính
     */
    public String luuAnhBannerFlashSale(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 1. Kiểm tra dung lượng tối đa 15MB cho banner
        if (file.getSize() > 15 * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh banner vượt quá dung lượng cho phép (Tối đa 15MB)!");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Tên tệp tin ảnh banner không hợp lệ!");
        }

        // 2. Chống Directory Traversal
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new SecurityException("Phát hiện tên tệp tin không an toàn: " + originalFilename);
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("Tệp tin ảnh thiếu phần mở rộng (.jpg, .png, .webp...)!");
        }

        String extension = originalFilename.substring(lastDotIndex).toLowerCase();
        List<String> validExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");
        if (!validExtensions.contains(extension)) {
            throw new IllegalArgumentException("Định dạng ảnh không hợp lệ (Chỉ chấp nhận: JPG, PNG, WEBP, GIF)!");
        }

        // 3. Thư mục lưu trữ: uploads/flash-sale/
        Path flashSaleUploadPath = Paths.get("uploads", "flash-sale");
        if (!Files.exists(flashSaleUploadPath)) {
            Files.createDirectories(flashSaleUploadPath);
        }

        String uniqueFileName = "banner_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;
        Path targetLocation = flashSaleUploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/flash-sale/" + uniqueFileName;
    }
}
