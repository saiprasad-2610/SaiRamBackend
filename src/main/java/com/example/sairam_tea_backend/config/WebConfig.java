package com.example.sairam_tea_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Inject the upload directory path from application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure uploadDir ends with a slash
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        System.out.println("Registering resource handler for: file:" + location);
        registry.addResourceHandler("/uploads/images/products/**")
                .addResourceLocations("file:" + location);
    }
}
