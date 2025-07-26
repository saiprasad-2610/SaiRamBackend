// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/service/ProductService.java
package com.example.sairam_tea_backend.service;

import com.example.sairam_tea_backend.model.Product;
import com.example.sairam_tea_backend.repository.ProductRepository;
import com.example.sairam_tea_backend.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Inject the upload directory from application.properties
    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product addProduct(Product product, MultipartFile imageFile) throws IOException {
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            // Use the injected uploadDir for saving
            FileUploadUtil.saveFile(uploadDir, uniqueFileName, imageFile);

            // The URL path should match what's configured in WebConfig
            product.setImageUrl("/uploads/images/products/" + uniqueFileName);
        } else {
            product.setImageUrl(null);
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails, MultipartFile imageFile) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id " + id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCategory(productDetails.getCategory());
        product.setStockQuantity(productDetails.getStockQuantity());

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            // Use the injected uploadDir for saving
            FileUploadUtil.saveFile(uploadDir, uniqueFileName, imageFile);
            product.setImageUrl("/uploads/images/products/" + uniqueFileName);
        } else if (productDetails.getImageUrl() == null || productDetails.getImageUrl().isEmpty()) {
            // If no new image uploaded and the existing image URL is explicitly cleared by frontend
            product.setImageUrl(null);
        }
        // If imageFile is null and productDetails.getImageUrl() is not null/empty,
        // it means no new image was uploaded and existing image URL should be retained.

        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id " + id));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            // Reconstruct the local file system path for deletion
            // Remove the URL prefix and prepend the actual upload directory
            String imageFileName = product.getImageUrl().replace("/uploads/images/products/", "");
            Path fileToDeletePath = Paths.get(uploadDir, imageFileName);
            try {
                Files.deleteIfExists(fileToDeletePath);
                System.out.println("Deleted image file: " + fileToDeletePath);
            } catch (IOException e) {
                System.err.println("Could not delete image file: " + fileToDeletePath + " - " + e.getMessage());
            }
        }
        productRepository.deleteById(id);
    }

    public List<Product> searchAndFilterProducts(String searchQuery, String category) {
        return productRepository.findBySearchQueryAndCategory(searchQuery, category);
    }

    public List<String> getAllDistinctCategories() {
        return productRepository.findDistinctCategories();
    }
}
