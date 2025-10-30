package com.reactorx.service;

import com.reactorx.entity.CartItem;
import com.reactorx.entity.Product;
import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.CartRepository;
import com.reactorx.repository.ProductRepository;
import com.reactorx.repository.UserRepository;
import jakarta.transaction.Transactional; // Import needed for @Transactional
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // 1. GET CART
    public List<CartItem> getCartItems(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        return cartRepository.findByUser(user);
    }

    // 2. ADD TO CART
    // This is called by the new CartController endpoint
    public String addToCart(String userEmail, Long productId, int quantity) {
        // Validation: User and Product exist
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

    // 3. REMOVE FROM CART
    // @Transactional is often necessary for DELETE operations to run fully
    @Transactional
    public String removeFromCart(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Use the repository method to delete by User and Product, which is cleaner
        cartRepository.deleteByUserAndProduct(user, product);

        // Since deletion by User and Product is executed, we just return success.
        return "Removed from cart!";
    }

    // 4. CLEAR CART
    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }
}