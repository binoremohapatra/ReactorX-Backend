package com.reactorx.controller;

import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.ProductSummaryDTO;
import com.reactorx.entity.Product;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CartController {

    private final HttpSession session;

    public CartController(HttpSession session) {
        this.session = session;
    }

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getCartItems() {
        try {
            List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("userCart");
            if (cart == null) cart = new ArrayList<>();
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("Failed to fetch cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody CartItemDTO item) {
        try {
            List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("userCart");
            if (cart == null) cart = new ArrayList<>();

            boolean exists = false;
            for (CartItemDTO cartItem : cart) {
                if (cartItem.getProductId().equals(item.getProductId())) {
                    cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                cart.add(item);
            }

            session.setAttribute("userCart", cart);
            log.info("Added product {} to cart", item.getProductId());
            return ResponseEntity.ok(cart);

        } catch (Exception e) {
            log.error("Error adding item to cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        session.removeAttribute("userCart");
        return ResponseEntity.ok("Cart cleared");
    }
}
