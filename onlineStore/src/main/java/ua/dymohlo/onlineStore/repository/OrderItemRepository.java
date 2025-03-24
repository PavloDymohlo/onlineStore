package ua.dymohlo.onlineStore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.onlineStore.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
