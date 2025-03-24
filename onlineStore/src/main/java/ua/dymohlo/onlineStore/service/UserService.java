package ua.dymohlo.onlineStore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ua.dymohlo.onlineStore.dto.response.OrderSummaryResponse;
import ua.dymohlo.onlineStore.dto.response.UserDetailedResponse;
import ua.dymohlo.onlineStore.entity.Order;
import ua.dymohlo.onlineStore.entity.User;
import ua.dymohlo.onlineStore.exception.AccessForbiddenException;
import ua.dymohlo.onlineStore.exception.ResourceNotFoundException;
import ua.dymohlo.onlineStore.repository.OrderRepository;
import ua.dymohlo.onlineStore.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public UserDetailedResponse getUserProfile(String email, Authentication authentication) {
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmailIgnoreCase(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        boolean isOwner = currentUserEmail.equalsIgnoreCase(email);

        if (!isAdmin && !isOwner) {
            throw new AccessForbiddenException("You don't have permission to access this user's information");
        }

        User requestedUser = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        List<Order> userOrders = orderRepository.findByUserId(requestedUser.getId());
        List<OrderSummaryResponse> orderSummaries = userOrders.stream()
                .map(this::convertToOrderSummary)
                .collect(Collectors.toList());

        return UserDetailedResponse.builder()
                .email(requestedUser.getEmail())
                .fullName(requestedUser.getFullname())
                .role(requestedUser.getRole())
                .orders(orderSummaries)
                .build();
    }

    private OrderSummaryResponse convertToOrderSummary(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public void deleteUser(String email, Authentication authentication) {
        String currentUserEmail = authentication.getName();
        User currentUser = userRepository.findByEmailIgnoreCase(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        boolean isOwner = currentUserEmail.equalsIgnoreCase(email);

        if (!isAdmin && !isOwner) {
            throw new AccessForbiddenException("You don't have permission to delete this user");
        }

        User userToDelete = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if ("ADMIN".equals(userToDelete.getRole()) && !isAdmin) {
            throw new AccessForbiddenException("Regular users cannot delete administrators");
        }

        userRepository.delete(userToDelete);
    }

    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole());
    }

    public void validateOrderAccess(User user, Order order) {
        if (!isAdmin(user) && !order.getUser().getId().equals(user.getId())) {
            throw new AccessForbiddenException("You don't have access to this order");
        }
    }
}
