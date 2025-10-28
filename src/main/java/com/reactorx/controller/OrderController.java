package com.reactorx.controller;

import com.reactorx.dto.AddressDTO;
import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.OrderSummaryDTO;
import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.UserRepository;
import com.reactorx.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList; // Import ArrayList
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    /**
     * GET /api/orders
     * Fetches the order history for the currently logged-in user.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderSummaryDTO>> getOrders(Authentication authentication) {
        logger.info("Received request to get orders for user: {}", authentication.getName());
        try {
            List<OrderSummaryDTO> orders = orderService.getOrdersForCurrentUser();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error fetching orders for user: {}", authentication.getName(), e);
            // Return 500 but don't leak the exception message to the client
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/orders
     * Creates a new order from the items in the cart and the address in the session.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(Authentication authentication) {

        try {
            // 1. Get the authenticated user
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

            // 2. Get Cart and Address from session
            // ✅ CORRECT: Retrieve as a List, matching CartController
            List<CartItemDTO> userCart = (List<CartItemDTO>) httpSession.getAttribute("userCart");
            AddressDTO shippingAddress = (AddressDTO) httpSession.getAttribute("shippingAddress");

            // 3. Validate session data
            if (userCart == null || userCart.isEmpty()) {
                logger.warn("Order creation failed for {}: Cart is empty or null.", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cart is empty.");
            }
            if (shippingAddress == null) {
                logger.warn("Order creation failed for {}: Shipping address is missing from session.", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Shipping address is missing.");
            }

            logger.info("Creating order for {} with {} items and address: {}", email, userCart.size(), shippingAddress.getShippingCity());

            // 4. Call the service to create the order
            // We can pass userCart directly since it's already a List
            OrderSummaryDTO newOrder = orderService.createOrderFromCart(userCart, shippingAddress, currentUser);

            // 5. Clear the cart and address from the session
            httpSession.removeAttribute("userCart");
            httpSession.removeAttribute("shippingAddress");
            logger.info("Successfully created order {} and cleared session cart/address.", newOrder.getId());

            // 6. Return the new order summary
            return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);

        } catch (ResourceNotFoundException e) {
            logger.warn("Order creation failed due to missing resource: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error during order creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }
}