package com.reactorx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactorx.dto.AddCartRequestDTO;
import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.MediaDTO; // Import MediaDTO
import com.reactorx.entity.Product;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.ProductRepository;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    // Add ObjectMapper and Logger
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    // --- REVISED METHOD ---
    public List<CartItemDTO> addToCart(List<CartItemDTO> currentCart, AddCartRequestDTO request, ProductRepository productRepository) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        // Find if item exists in the original list first
        Optional<CartItemDTO> existingItemOptional = currentCart.stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        List<CartItemDTO> updatedCart; // Declare the list to be returned

        if (existingItemOptional.isPresent()) {
            // Item exists: Create a NEW list by mapping, updating the quantity of the matched item.
            updatedCart = currentCart.stream()
                    .map(item -> {
                        if (item.getProductId().equals(request.getProductId())) {
                            // Create a NEW DTO instance with the incremented quantity
                            CartItemDTO updatedItem = new CartItemDTO();
                            updatedItem.setProductId(item.getProductId());
                            updatedItem.setProductName(item.getProductName());
                            updatedItem.setProductPrice(item.getProductPrice());
                            updatedItem.setProductImage(item.getProductImage());
                            updatedItem.setQuantity(item.getQuantity() + request.getQuantity()); // Increment quantity
                            return updatedItem;
                        }
                        return item; // Keep other items as they are
                    })
                    .collect(Collectors.toList());
        } else {
            // Item doesn't exist: Create a NEW list copy and add the new item to it.
            updatedCart = new ArrayList<>(currentCart); // Make a mutable copy

            CartItemDTO newItem = new CartItemDTO();
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());

            BigDecimal price = product.getPrice();
            newItem.setProductPrice(price != null ? price.toString() : "0.00");

            // --- Get Primary Image URL from mediaJson ---
            String imageUrl = "placeholder.jpg"; // Default placeholder
            String mediaJsonString = product.getMediaJson();

            if (mediaJsonString != null && !mediaJsonString.isEmpty()) {
                try {
                    List<MediaDTO> mediaList = objectMapper.readValue(mediaJsonString, new TypeReference<List<MediaDTO>>() {});
                    imageUrl = mediaList.stream()
                            .filter(m -> m != null && "image".equalsIgnoreCase(m.getType()))
                            .map(MediaDTO::getSrc)
                            .findFirst()
                            .orElse(imageUrl);
                } catch (JsonProcessingException e) {
                    logger.error("Error parsing mediaJson for product ID {}: {}", product.getId(), e.getMessage());
                }
            }
            newItem.setProductImage(imageUrl);
            // --- End Image URL Logic ---

            newItem.setQuantity(request.getQuantity());
            updatedCart.add(newItem); // Add the new item to the copy
        }
        // Return the newly created/modified list
        return updatedCart;
    }

    // --- updateCartItem method ---
    // (Remains the same - already returns a new list)
    public List<CartItemDTO> updateCartItem(List<CartItemDTO> currentCart, Long productId, int quantity) {
        if (quantity <= 0) {
            return removeFromCart(currentCart, productId);
        }
        return currentCart.stream()
                .map(item -> {
                    if (item.getProductId().equals(productId)) {
                        CartItemDTO updatedItem = new CartItemDTO();
                        updatedItem.setProductId(item.getProductId());
                        updatedItem.setProductName(item.getProductName());
                        updatedItem.setProductPrice(item.getProductPrice());
                        updatedItem.setProductImage(item.getProductImage());
                        updatedItem.setQuantity(quantity);
                        return updatedItem;
                    }
                    return item;
                })
                .collect(Collectors.toList());
    }

    // --- removeFromCart method ---
    // (Remains the same - already returns a new list)
    public List<CartItemDTO> removeFromCart(List<CartItemDTO> currentCart, Long productId) {
        return currentCart.stream()
                .filter(item -> !item.getProductId().equals(productId))
                .collect(Collectors.toList());
    }
}

