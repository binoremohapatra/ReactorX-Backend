package com.reactorx.controller;

import com.reactorx.dto.AddCartRequestDTO;
import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.UpdateCartRequestDTO;
import com.reactorx.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CartItemDTO>> getCart(Principal principal) {
        logger.info("Fetching cart for user: {}", principal.getName());
        return ResponseEntity.ok(cartService.getCartForUser(principal.getName()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CartItemDTO>> addToCart(@Valid @RequestBody AddCartRequestDTO request,
                                                       Principal principal) {
        logger.info("Adding to cart. User: {}, Product: {}", principal.getName(), request.getProductId());
        List<CartItemDTO> updatedCart = cartService.addToCartForUser(principal.getName(), request);
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CartItemDTO>> updateCartItem(@PathVariable Long productId,
                                                            @Valid @RequestBody UpdateCartRequestDTO request,
                                                            Principal principal) {
        logger.info("Updating cart item. User: {}, ProductId: {}, Quantity: {}", principal.getName(), productId, request.getQuantity());
        List<CartItemDTO> updatedCart = cartService.updateCartItemForUser(principal.getName(), productId, request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CartItemDTO>> removeFromCart(@PathVariable Long productId,
                                                            Principal principal) {
        logger.info("Removing from cart. User: {}, ProductId: {}", principal.getName(), productId);
        List<CartItemDTO> updatedCart = cartService.removeFromCartForUser(principal.getName(), productId);
        return ResponseEntity.ok(updatedCart);
    }
}
