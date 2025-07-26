// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/controller/ProductController.java
package com.example.sairam_tea_backend.controller;

import com.example.sairam_tea_backend.model.Product;
import com.example.sairam_tea_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Import MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String category) {
        List<Product> products;
        if (searchQuery != null || category != null) {
            products = productService.searchAndFilterProducts(searchQuery, category);
        } else {
            products = productService.getAllProducts();
        }

        if (products.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // MODIFIED: addProduct now accepts MultipartFile
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // Specify content type
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addProduct(
            @RequestPart("product") Product product, // Product data as JSON part
            @RequestPart(value = "image", required = false) MultipartFile imageFile) { // Image file as part
        try {
            Product newProduct = productService.addProduct(product, imageFile);
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // MODIFIED: updateProduct now accepts MultipartFile
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") Product productDetails, // Product data as JSON part
            @RequestPart(value = "image", required = false) MultipartFile imageFile) { // Image file as part
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails, imageFile);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            return new ResponseEntity<>("Failed to update image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getDistinctCategories() {
        List<String> categories = productService.getAllDistinctCategories();
        if (categories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}
