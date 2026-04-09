package com.reactorx.service;

import com.reactorx.dto.CartItemDTO;
import com.reactorx.entity.CartItem;
import com.reactorx.entity.Product;
import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.CartRepository;
import com.reactorx.repository.ProductRepository;
import com.reactorx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // --- Private DTO Mapper Method (Required for image and clean price) ---
    private CartItemDTO mapToDto(CartItem item) {
        if (item == null || item.getProduct() == null) {
            return null;
        }

        Product product = item.getProduct();

        // Ensure Product entity has an accessible getImageUrl() method/field
        return CartItemDTO.builder()
                .productId(product.getId())
                .productName(product.getName())
                // FIX: Convert Price (BigDecimal/Double) to String to match DTO
                .productPrice(product.getPrice().toString())
                // 🌟 FIX: Map the Image URL for the frontend 🌟
                .productImage(product.getImageUrl())
                .quantity(item.getQuantity())
                .build();
    }


    // 1. GET CART (FIXED: Returns DTO List)
    @Transactional(readOnly = true)
    // 💡 RETURN TYPE CHANGED: Now returns List<CartItemDTO>
    public List<CartItemDTO> getCartItems(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<CartItem> cartItems = cartRepository.findByUser(user);

        // FIX: Convert raw entities to DTOs using the mapper
        return cartItems.stream()
                .map(this::mapToDto)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    // 2. ADD TO CART (Logic remains the same, but now references the correct imports)
    @Transactional
    public String addToCart(String userEmail, Long productId, int quantity) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem existing = cartRepository.findByUserAndProduct(user, product).orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartRepository.save(existing);
            return "Quantity updated in cart!";
        }

        // Create new item
        CartItem item = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .build();

        cartRepository.save(item);
        return "Product added to cart!";
    }

    // 3. REMOVE FROM CART (No change needed here, logic is clean)
    @Transactional
    public String removeFromCart(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        cartRepository.deleteByUserAndProduct(user, product);

        return "Removed from cart!";
    }

    // 4. CLEAR CART (No change needed here; usage is in CheckoutController)
    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }
}
