package ua.dymohlo.onlineStore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.onlineStore.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
    List<Product> findByDeletedFalse();
    Optional<Product> findByNameAndDeletedFalse(String name);
}

