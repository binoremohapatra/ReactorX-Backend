package com.reactorx.repository;

import com.reactorx.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Used by CategoryPage and HomePage dynamic sections
    List<Product> findByCategorySlug(String categorySlug);

    // Used by CategoryService to get the product count per category
    long countByCategorySlug(String categorySlug);

    // Used by SearchModal
    List<Product> findByNameContainingIgnoreCase(String name);

    // NOTE: Ensure the incorrect method "Optional<Category> findBySlug(String slug);" is NOT present here.
}