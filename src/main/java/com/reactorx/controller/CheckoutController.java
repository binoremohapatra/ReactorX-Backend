package com.reactorx.controller;

import com.reactorx.dto.CartItemDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CheckoutController {

    private final HttpSession session;

    public CheckoutController(HttpSession session) {
        this.session = session;
    }

    @PostMapping
    public ResponseEntity<?> checkout() {
        List<CartItemDTO> cart = (List<CartItemDTO>) session.getAttribute("userCart");

        if (cart == null || cart.isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty. Add items before checkout.");
        }

        // 💳 (You can add order creation / payment logic here later)
        // For now, just clear the cart to simulate a successful purchase.
        session.removeAttribute("userCart");

        return ResponseEntity.ok("✅ Order placed successfully!");
    }
}
