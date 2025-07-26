// sairam-tea-backend/src/main/java/com/example/sairam_tea_backend/repository/ProductRepository.java
package com.example.sairam_tea_backend.repository;

import com.example.sairam_tea_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Import Optional

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Custom query to search products by name or description and filter by category
    @Query("SELECT p FROM Product p WHERE " +
            "(:searchQuery IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchQuery, '%'))) AND " +
            "(:category IS NULL OR LOWER(p.category) = LOWER(:category))")
    List<Product> findBySearchQueryAndCategory(
            @Param("searchQuery") String searchQuery,
            @Param("category") String category);

    // Optional: Find all distinct categories for filtering purposes (for frontend dropdown)
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND p.category != ''")
    List<String> findDistinctCategories();

    // NEW: Find a product by its exact name
    Optional<Product> findByName(String name);
}
