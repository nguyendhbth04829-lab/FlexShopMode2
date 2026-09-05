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
}
