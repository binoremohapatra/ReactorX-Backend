package com.reactorx.controller;

import com.reactorx.dto.AddCartRequestDTO;
import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.UpdateCartRequestDTO;
import com.reactorx.repository.ProductRepository;
import com.reactorx.service.CartService;
import jakarta.servlet.http.HttpSession; // Import HttpSession
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
// Remove Model import if no longer needed for other things
// import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
// Remove SessionAttributes import
// import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Import Optional

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
// Remove @SessionAttributes("userCart")
public class CartController {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(CartController.class); // Add logger
    private static final String CART_SESSION_KEY = "userCart"; // Define session key constant

    // Helper method to get cart from session, initializing if necessary
    private List<CartItemDTO> getCartFromSession(HttpSession session) {
        List<CartItemDTO> userCart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (userCart == null) {
            logger.info("Initializing new cart in session.");
            userCart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, userCart); // Store the newly created list
        }
        // Ensure we always work with a mutable list downstream if needed, although service returns new lists
        return new ArrayList<>(userCart);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CartItemDTO>> getCart(HttpSession session) { // Inject HttpSession
        List<CartItemDTO> userCart = getCartFromSession(session);
        logger.debug("Getting cart from session, size: {}", userCart.size());
        return ResponseEntity.ok(userCart);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CartItemDTO>> addToCart(@Valid @RequestBody AddCartRequestDTO request,
                                                       HttpSession session) { // Inject HttpSession
        List<CartItemDTO> currentCart = getCartFromSession(session);
        logger.info("Adding to cart. Current size: {}, Request: {}", currentCart.size(), request);

        List<CartItemDTO> updatedCart = cartService.addToCart(currentCart, request, productRepository);

        session.setAttribute(CART_SESSION_KEY, updatedCart); // Directly update the session
        logger.info("Cart updated (add). New size: {}", updatedCart.size());
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CartItemDTO>> updateCartItem(@PathVariable Long productId,
                                                            @Valid @RequestBody UpdateCartRequestDTO request,
                                                            HttpSession session) { // Inject HttpSession
        List<CartItemDTO> currentCart = getCartFromSession(session);
        logger.info("Updating cart item. ProductId: {}, New Quantity: {}, Current cart size: {}", productId, request.getQuantity(), currentCart.size());

        List<CartItemDTO> updatedCart = cartService.updateCartItem(currentCart, productId, request.getQuantity());

        session.setAttribute(CART_SESSION_KEY, updatedCart); // Directly update the session
        logger.info("Cart updated (update). New size: {}", updatedCart.size());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CartItemDTO>> removeFromCart(@PathVariable Long productId,
                                                            HttpSession session) { // Inject HttpSession
        List<CartItemDTO> currentCart = getCartFromSession(session);
        logger.info("Removing from cart. ProductId: {}, Current cart size: {}", productId, currentCart.size());

        List<CartItemDTO> updatedCart = cartService.removeFromCart(currentCart, productId);

        session.setAttribute(CART_SESSION_KEY, updatedCart); // Directly update the session
        logger.info("Cart updated (remove). New size: {}", updatedCart.size());
        // Return OK with the potentially empty or reduced list
        return ResponseEntity.ok(updatedCart);
    }
}