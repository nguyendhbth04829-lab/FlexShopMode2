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

    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
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
        private final String fileType;

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

    public void kiemTraTepTinHopLe(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Tệp tin '" + file.getOriginalFilename() + "' vượt quá dung lượng cho phép (Tối đa 50MB)!");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Tên tệp tin không hợp lệ hoặc bị rỗng!");
        }

        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new SecurityException("Phát hiện đường dẫn tệp tin không an toàn: " + originalFilename);
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("Tệp tin '" + originalFilename + "' thiếu phần mở rộng (.jpg, .png, .mp4...)!");
        }

        String extension = originalFilename.substring(lastDotIndex).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Tệp tin '" + originalFilename + "' không thuộc định dạng được hỗ trợ!");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String lowerContentType = contentType.toLowerCase();
            boolean isAllowedMime = ALLOWED_MIME_TYPES.stream().anyMatch(lowerContentType::startsWith);
            if (!isAllowedMime && !lowerContentType.equals("application/octet-stream")) {
                throw new IllegalArgumentException("Kiểu nội dung MIME '" + contentType + "' không hợp lệ!");
            }
        }
    }

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

    public String luuFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + fileExtension;

            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path targetLocation = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file. Vui lòng thử lại!", ex);
        }
    }

    public String luuAnhPod(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Bắt buộc chụp/upload ảnh bằng chứng giao hàng (POD)!");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh POD vượt quá 10MB!");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()
                || originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new IllegalArgumentException("Tên file ảnh POD không hợp lệ!");
        }
        int dot = originalFilename.lastIndexOf(".");
        if (dot == -1) {
            throw new IllegalArgumentException("Ảnh POD thiếu phần mở rộng!");
        }
        String extension = originalFilename.substring(dot).toLowerCase();
        if (!Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(extension)) {
            throw new IllegalArgumentException("Ảnh POD chỉ chấp nhận JPG, PNG, WEBP, GIF!");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()
                && !contentType.toLowerCase().startsWith("image/")
                && !contentType.equalsIgnoreCase("application/octet-stream")) {
            throw new IllegalArgumentException("File POD phải là ảnh!");
        }
        Path podPath = Paths.get("uploads", "pod");
        if (!Files.exists(podPath)) {
            Files.createDirectories(podPath);
        }
        String uniqueFileName = "pod_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;
        Files.copy(file.getInputStream(), podPath.resolve(uniqueFileName), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/pod/" + uniqueFileName;
    }

    public String luuAnhBannerFlashSale(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > 15 * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh banner vượt quá dung lượng cho phép (Tối đa 15MB)!");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Tên tệp tin ảnh banner không hợp lệ!");
        }

        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new SecurityException("Phát hiện tên tệp tin không an toàn: " + originalFilename);
        }

        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("Tệp tin ảnh thiếu phần mở rộng!");
        }

        String extension = originalFilename.substring(lastDotIndex).toLowerCase();
        List<String> validExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");
        if (!validExtensions.contains(extension)) {
            throw new IllegalArgumentException("Định dạng ảnh không hợp lệ!");
        }

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
