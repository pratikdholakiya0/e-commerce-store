package org.akashbag.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.request.OrderStatusChangeRequest;
import org.akashbag.ecommerce.dto.response.OrderResponse;
import org.akashbag.ecommerce.enums.OrderStatus;
import org.akashbag.ecommerce.model.OrderStatusLog;
import org.akashbag.ecommerce.payload.ApiResponse;
import org.akashbag.ecommerce.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // 1. Create Order
//    @PostMapping("/create")
//    public ResponseEntity<ApiResponse> createOrder(@RequestBody OrderRequest orderRequest) {
//        orderService.createOrder(orderRequest);
//
//        ApiResponse response = ApiResponse.builder()
//                .message("Order created successfully")
//                .status(HttpStatus.OK)
//                .timestamp(LocalDateTime.now())
//                .build();
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    // 2. Cancel Order (User/Admin)8
    @PutMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable String id) {
        orderService.cancelOrder(id);

        ApiResponse response = ApiResponse.builder()
                .message("Order cancelled successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ✅ NEW: Get Specific Order (For "Order Confirmation" or "Details" page)
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        OrderResponse orderResponse = orderService.getOrderById(id);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    // ✅ NEW: Get My Order History (For "My Orders" page)
    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        List<OrderResponse> orderResponseList = orderService.getMyOrders();
        return new ResponseEntity<>(orderResponseList, HttpStatus.OK);
    }

    // ✅ NEW: Admin - Get ALL Orders (For Admin Dashboard)
    @GetMapping("/admin/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orderResponseList = orderService.getAllOrders();
        return new ResponseEntity<>(orderResponseList, HttpStatus.OK);
    }

    @GetMapping("/history/{orderId}")
    public ResponseEntity<List<OrderStatusLog>> getOrderHistory(@PathVariable String orderId) {
        List<OrderStatusLog> history = orderService.getHistory(orderId);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    // ✅ NEW: Admin - Update Status (e.g., Mark as SHIPPED or DELIVERED)
    @PutMapping("/admin/status/{id}")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable String id,
            @RequestBody OrderStatusChangeRequest status
    ) {
        // Logic: Find Order -> Set Status -> Save
        orderService.updateOrderStatus(id, status);

        ApiResponse response = ApiResponse.builder()
                .message("Order status updated successfully")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 4. Return Product (Advanced Feature)
    @PostMapping("/return/{id}")
    public ResponseEntity<ApiResponse> returnProduct(@PathVariable String id) {
        orderService.returnProduct(id);

        ApiResponse response = ApiResponse.builder()
                .message("Order return request sent")
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}