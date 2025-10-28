package com.reactorx.service;

import com.reactorx.dto.CategoryDTO;
import com.reactorx.entity.Category;
import com.reactorx.repository.CategoryRepository;
import com.reactorx.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository; // Correctly Injected

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setImageUrl(category.getImageUrl());

        // This is the line that generates the count based on the slug.
        dto.setCount(productRepository.countByCategorySlug(category.getSlug()));

        return dto;
    }
}