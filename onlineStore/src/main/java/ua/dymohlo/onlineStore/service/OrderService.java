package ua.dymohlo.onlineStore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.dymohlo.onlineStore.dto.request.OrderCreateRequest;
import ua.dymohlo.onlineStore.dto.request.OrderItemRequest;
import ua.dymohlo.onlineStore.dto.response.OrderItemResponse;
import ua.dymohlo.onlineStore.dto.response.OrderResponse;
import ua.dymohlo.onlineStore.entity.Order;
import ua.dymohlo.onlineStore.entity.OrderItem;
import ua.dymohlo.onlineStore.entity.Product;
import ua.dymohlo.onlineStore.entity.User;
import ua.dymohlo.onlineStore.exception.AccessForbiddenException;
import ua.dymohlo.onlineStore.exception.ProductQuantityException;
import ua.dymohlo.onlineStore.exception.ResourceNotFoundException;
import ua.dymohlo.onlineStore.repository.OrderItemRepository;
import ua.dymohlo.onlineStore.repository.OrderRepository;
import ua.dymohlo.onlineStore.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ua.dymohlo.onlineStore.util.GlobalConstants.STATUS_CANCELLED;
import static ua.dymohlo.onlineStore.util.GlobalConstants.STATUS_NEW;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        String orderNumber = generateUniqueOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(STATUS_NEW)
                .totalAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productService.getProductByName(itemRequest.getProductName());

            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new ProductQuantityException("Not enough quantity for product: " + product.getName());
            }

            BigDecimal itemTotalPrice = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotalPrice);

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderItemRepository.save(orderItem);

            itemResponses.add(createOrderItemResponse(orderItem));

            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder);

        return createOrderResponse(savedOrder, itemResponses);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber, Authentication authentication) {
        Order order = getOrderByNumber(orderNumber);
        User currentUser = userService.getCurrentUser(authentication);

        userService.validateOrderAccess(currentUser, order);

        List<OrderItemResponse> itemResponses = createOrderItemResponses(order);

        return createOrderResponse(order, itemResponses);
    }

    @Transactional
    public OrderResponse cancelOrder(String orderNumber, Authentication authentication) {
        Order order = getOrderByNumber(orderNumber);
        User currentUser = userService.getCurrentUser(authentication);

        userService.validateOrderAccess(currentUser, order);

        if (!STATUS_NEW.equals(order.getStatus())) {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(STATUS_CANCELLED);

        for (OrderItem orderItem : order.getOrderItems()) {
            returnProductToInventory(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        List<OrderItemResponse> itemResponses = createOrderItemResponses(savedOrder);

        return createOrderResponse(savedOrder, itemResponses);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String email, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        boolean isAdmin = userService.isAdmin(currentUser);

        if (!isAdmin && !currentUser.getEmail().equalsIgnoreCase(email)) {
            throw new AccessForbiddenException("You don't have access to these orders");
        }

        User user = userService.getUserByEmail(email);
        List<Order> userOrders = orderRepository.findByUser(user);

        return userOrders.stream()
                .map(order -> {
                    List<OrderItemResponse> itemResponses = createOrderItemResponses(order);
                    return createOrderResponse(order, itemResponses);
                })
                .collect(Collectors.toList());
    }

    private Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    private void returnProductToInventory(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        product.setQuantity(product.getQuantity() + orderItem.getQuantity());
        productRepository.save(product);
    }

    private OrderItemResponse createOrderItemResponse(OrderItem item) {
        BigDecimal totalPrice = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemResponse.builder()
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .pricePerUnit(item.getPrice())
                .totalPrice(totalPrice)
                .build();
    }

    private List<OrderItemResponse> createOrderItemResponses(Order order) {
        return order.getOrderItems().stream()
                .map(this::createOrderItemResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse createOrderResponse(Order order, List<OrderItemResponse> itemResponses) {
        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .customerName(order.getUser().getFullname())
                .customerEmail(order.getUser().getEmail())
                .status(order.getStatus())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private String generateUniqueOrderNumber() {
        String orderNumber;
        boolean isUnique = false;

        do {
            String uniquePart = UUID.randomUUID().toString().substring(0, 8);
            orderNumber = "ORD-" + uniquePart;
            isUnique = !orderRepository.existsByOrderNumber(orderNumber);
        } while (!isUnique);

        return orderNumber;
    }
}