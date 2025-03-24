package ua.dymohlo.onlineStore.controler;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.onlineStore.dto.request.OrderCreateRequest;
import ua.dymohlo.onlineStore.dto.response.OrderResponse;
import ua.dymohlo.onlineStore.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderCreateRequest request,
                                                     Authentication authentication) {
        OrderResponse createdOrder = orderService.createOrder(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber, Authentication authentication) {
        OrderResponse orderResponse = orderService.getOrderByOrderNumber(orderNumber, authentication);
        return ResponseEntity.ok(orderResponse);
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderNumber, Authentication authentication) {
        OrderResponse cancelledOrder = orderService.cancelOrder(orderNumber, authentication);
        return ResponseEntity.ok(cancelledOrder);
    }
    @GetMapping("/user/{email}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable String email, Authentication authentication) {
        List<OrderResponse> userOrders = orderService.getUserOrders(email, authentication);
        return ResponseEntity.ok(userOrders);
    }
}