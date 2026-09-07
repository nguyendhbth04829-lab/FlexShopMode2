package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class CauHinhWebMvc implements WebMvcConfigurer {

    @Value("${flexshop.upload.dir:uploads/avatars}")
    private String thuMucUpload;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
        String uploadUri = uploadPath.toUri().toString();

        // Ánh xạ URL /uploads/** tới thư mục vật lý uploads trên máy chủ
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadUri.endsWith("/") ? uploadUri : uploadUri + "/");
    }
}
