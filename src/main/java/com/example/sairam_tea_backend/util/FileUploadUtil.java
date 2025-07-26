// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/util/FileUploadUtil.java
package com.example.sairam_tea_backend.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUploadUtil {

    // Method to save a MultipartFile to a specified upload directory
    // Returns the filename if successful, null otherwise
    public static String saveFile(String uploadDir, String fileName, MultipartFile multipartFile) throws IOException {
        // Create the directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            // Copy the file, replacing existing if it has the same name
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName; // Return the saved file name
        } catch (IOException ioe) {
            throw new IOException("Could not save image file: " + fileName, ioe);
        }
    }

    // Method to clean up (delete) a directory
    public static void cleanDir(String dir) {
        Path dirPath = Paths.get(dir);
        try {
            Files.list(dirPath).forEach(file -> {
                if (!Files.isDirectory(file)) {
                    try {
                        Files.delete(file);
                    } catch (IOException ex) {
                        System.err.println("Could not delete file: " + file);
                    }
                }
            });
        } catch (IOException ex) {
            System.err.println("Could not list directory: " + dirPath);
        }
    }
}
