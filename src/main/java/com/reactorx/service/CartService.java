package com.reactorx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactorx.dto.AddCartRequestDTO;
import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.MediaDTO;
import com.reactorx.entity.CartItem;
import com.reactorx.entity.Product;
import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.CartRepository;
import com.reactorx.repository.ProductRepository;
import com.reactorx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    /**
     * Get all cart items for the given user.
     */
    public List<CartItemDTO> getCartForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return cartRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Add a product to the user’s cart or increase its quantity.
     */
    public List<CartItemDTO> addToCartForUser(String email, AddCartRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        CartItem existingItem = cartRepository.findByUserAndProductId(user, product.getId()).orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cartRepository.save(newItem);
        }

        return getCartForUser(email);
    }

    /**
     * Update the quantity of a cart item.
     */
    public List<CartItemDTO> updateCartItemForUser(String email, Long productId, int quantity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        CartItem existingItem = cartRepository.findByUserAndProductId(user, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found for product ID: " + productId));

        if (quantity <= 0) {
            cartRepository.delete(existingItem);
        } else {
            existingItem.setQuantity(quantity);
            cartRepository.save(existingItem);
        }

        return getCartForUser(email);
    }

    /**
     * Remove a product from the user’s cart.
     */
    public List<CartItemDTO> removeFromCartForUser(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        cartRepository.findByUserAndProductId(user, productId)
                .ifPresent(cartRepository::delete);

        return getCartForUser(email);
    }

    /**
     * Convert CartItem entity to DTO.
     */
    private CartItemDTO convertToDTO(CartItem cartItem) {
        Product product = cartItem.getProduct();
        CartItemDTO dto = new CartItemDTO();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setProductPrice(product.getPrice() != null ? product.getPrice().toString() : "0.00");
        dto.setQuantity(cartItem.getQuantity());

        String imageUrl = "placeholder.jpg";
        try {
            if (product.getMediaJson() != null && !product.getMediaJson().isEmpty()) {
                List<MediaDTO> mediaList = objectMapper.readValue(product.getMediaJson(), new TypeReference<>() {});
                imageUrl = mediaList.stream()
                        .filter(m -> m != null && "image".equalsIgnoreCase(m.getType()))
                        .map(MediaDTO::getSrc)
                        .findFirst()
                        .orElse(imageUrl);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse mediaJson for product {}: {}", product.getId(), e.getMessage());
        }
        dto.setProductImage(imageUrl);
        return dto;
    }
}
