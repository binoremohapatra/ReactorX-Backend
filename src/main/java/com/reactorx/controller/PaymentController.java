package com.reactorx.controller;

import com.reactorx.dto.AddressDTO;
import com.reactorx.dto.CartItemDTO;
import com.reactorx.dto.OrderSummaryDTO;
import com.reactorx.dto.PaymentVerificationRequest;
import com.reactorx.entity.User;
import com.reactorx.exception.ResourceNotFoundException;
import com.reactorx.repository.UserRepository;
import com.reactorx.service.OrderService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // CRITICAL
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;
    private final UserRepository userRepository; // To get the User object
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private static final String CART_SESSION_KEY = "userCart";
    private static final String ADDRESS_SESSION_KEY = "shippingAddress";

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    // Helper to get cart from session
    private List<CartItemDTO> getCartFromSession(HttpSession session) {
        List<CartItemDTO> userCart = (List<CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        return (userCart != null) ? new ArrayList<>(userCart) : new ArrayList<>();
    }

    /**
     * Step 1: Create a Razorpay Order
     */
    @PostMapping("/create-order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> createOrder(HttpSession session) {
        try {
            List<CartItemDTO> userCart = getCartFromSession(session);
            if (userCart.isEmpty()) {
                logger.warn("User {} tried to create order with empty cart", session.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cart is empty");
            }

            if (session.getAttribute(ADDRESS_SESSION_KEY) == null) {
                logger.warn("User {} tried to pay without shipping address", session.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Shipping address is missing. Please complete the address form.");
            }

            BigDecimal totalAmount = orderService.getCartTotal(userCart);
            int amountInPaise = totalAmount.multiply(new BigDecimal(100)).intValue();

            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "receipt_order_" + session.getId());

            Order order = razorpayClient.orders.create(orderRequest);

            // --- START OF FIX for Ambiguous method call ---
            // Check for a null order or null ID before putting it in JSONObject
            if (order == null || order.get("id") == null) {
                logger.error("Failed to create Razorpay order, or order ID was null.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating payment order: Razorpay returned null.");
            }
            Object orderId = order.get("id"); // Get it as an Object
            // --- END OF FIX ---

            JSONObject response = new JSONObject();
            response.put("order_id", orderId); // Use the non-null Object
            response.put("razorpay_key_id", razorpayKeyId);
            response.put("amount", amountInPaise);

            logger.info("Created Razorpay order {} for session {}", orderId, session.getId());
            return ResponseEntity.ok(response.toString());

        } catch (RazorpayException e) {
            logger.error("Razorpay exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            logger.error("General error in createOrder: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    /**
     * Step 2: Verify the Payment
     */
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request, HttpSession session) {
        try {
            String razorpayOrderId = request.getRazorpay_order_id();
            String razorpayPaymentId = request.getRazorpay_payment_id();
            String razorpaySignature = request.getRazorpay_signature();

            // --- START OF FIX for Ambiguous method call ---
            // Add null checks for all request parameters
            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
                logger.warn("Payment verification failed: Received null values in request.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Missing required payment details."));
            }
            // --- END OF FIX ---

            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);

            boolean isSignatureValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            if (isSignatureValid) {
                logger.info("Payment verification successful for order: {}", razorpayOrderId);

                List<CartItemDTO> userCart = getCartFromSession(session);
                AddressDTO shippingAddress = (AddressDTO) session.getAttribute(ADDRESS_SESSION_KEY);
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));


                if (userCart.isEmpty()) {
                    logger.warn("Payment verified but cart is empty for session: {}. Possible double-click.", session.getId());
                    return ResponseEntity.ok(Map.of("status", "success", "message", "Payment already processed"));
                }

                if (shippingAddress == null) {
                    logger.error("Payment verified but shipping address is MISSING from session: {}", session.getId());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", "Shipping address was lost. Please try again."));
                }

                // Create the order in your database
                OrderSummaryDTO newOrder = orderService.createOrderFromCart(userCart, shippingAddress, currentUser);

                // Clear the cart AND address from the session
                session.setAttribute(CART_SESSION_KEY, new ArrayList<CartItemDTO>());
                session.removeAttribute(ADDRESS_SESSION_KEY);
                logger.info("Cart and address cleared for session: {}", session.getId());

                // Return success
                return ResponseEntity.ok(Map.of("status", "success", "order", newOrder));
            } else {
                logger.warn("Payment verification FAILED for order: {}", razorpayOrderId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Payment verification failed"));
            }
        } catch (Exception e) {
            logger.error("Error during payment verification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}