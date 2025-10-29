package com.reactorx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactorx.dto.AddCartRequestDTO;
import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.MediaDTO;
import com.reactorx.entity.Product;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    // 🧠 In-memory map of username -> cart items
    private final Map<String, List<CartItemDTO>> userCarts = new ConcurrentHashMap<>();

    public List<CartItemDTO> getCartForUser(String username) {
        return new ArrayList<>(userCarts.getOrDefault(username, new ArrayList<>()));
    }

    public List<CartItemDTO> addToCartForUser(String username, AddCartRequestDTO request) {
        List<CartItemDTO> currentCart = getCartForUser(username);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        Optional<CartItemDTO> existingItem = currentCart.stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
        } else {
            CartItemDTO newItem = new CartItemDTO();
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            newItem.setProductPrice(product.getPrice() != null ? product.getPrice().toString() : "0.00");
            newItem.setQuantity(request.getQuantity());

            String imageUrl = "placeholder.jpg";
            String mediaJson = product.getMediaJson();
            if (mediaJson != null && !mediaJson.isEmpty()) {
                try {
                    List<MediaDTO> mediaList = objectMapper.readValue(mediaJson, new TypeReference<List<MediaDTO>>() {});
                    imageUrl = mediaList.stream()
                            .filter(m -> m != null && "image".equalsIgnoreCase(m.getType()))
                            .map(MediaDTO::getSrc)
                            .findFirst()
                            .orElse(imageUrl);
                } catch (Exception e) {
                    logger.warn("Failed to parse mediaJson for product {}: {}", product.getId(), e.getMessage());
                }
            }
            newItem.setProductImage(imageUrl);
            currentCart.add(newItem);
        }

        userCarts.put(username, currentCart);
        return currentCart;
    }

    public List<CartItemDTO> updateCartItemForUser(String username, Long productId, int quantity) {
        List<CartItemDTO> currentCart = getCartForUser(username);
        if (quantity <= 0) return removeFromCartForUser(username, productId);

        List<CartItemDTO> updatedCart = currentCart.stream()
                .map(item -> {
                    if (item.getProductId().equals(productId)) {
                        item.setQuantity(quantity);
                    }
                    return item;
                })
                .collect(Collectors.toList());

        userCarts.put(username, updatedCart);
        return updatedCart;
    }

    public List<CartItemDTO> removeFromCartForUser(String username, Long productId) {
        List<CartItemDTO> currentCart = getCartForUser(username);
        List<CartItemDTO> updatedCart = currentCart.stream()
                .filter(item -> !item.getProductId().equals(productId))
                .collect(Collectors.toList());

        userCarts.put(username, updatedCart);
        return updatedCart;
    }
}
