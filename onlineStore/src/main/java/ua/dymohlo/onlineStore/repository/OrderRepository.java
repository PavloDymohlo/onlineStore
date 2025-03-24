package ua.dymohlo.onlineStore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.onlineStore.entity.Order;
import ua.dymohlo.onlineStore.entity.User;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long id);
    boolean existsByOrderNumber(String orderNumber);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUser(User user);
}
