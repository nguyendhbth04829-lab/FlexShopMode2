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

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp", ".gif",
            ".mp4", ".mov", ".avi", ".webm", ".mkv"
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

    public void kiemTraTepTinHopLe(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Tệp tin '" + file.getOriginalFilename() + "' vượt quá giới hạn 50MB!");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Tệp tin '" + originalFilename + "' không đúng định dạng cho phép (JPG, PNG, WEBP, MP4, MOV, WEBM)!");
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
}
