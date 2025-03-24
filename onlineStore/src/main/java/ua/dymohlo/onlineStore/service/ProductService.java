package ua.dymohlo.onlineStore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.dymohlo.onlineStore.dto.request.ProductCreateRequest;
import ua.dymohlo.onlineStore.dto.request.ProductUpdateRequest;
import ua.dymohlo.onlineStore.entity.Product;
import ua.dymohlo.onlineStore.entity.User;
import ua.dymohlo.onlineStore.exception.AccessForbiddenException;
import ua.dymohlo.onlineStore.exception.ResourceNotFoundException;
import ua.dymohlo.onlineStore.repository.ProductRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserService userService;
    @CacheEvict(value = "products-list", allEntries = true)
    public Product createProduct(ProductCreateRequest request, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (!userService.isAdmin(currentUser)) {
            throw new AccessForbiddenException("Only administrators can add products");
        }

        Product product = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .deleted(false)
                .build();

        return productRepository.save(product);
    }
    @Cacheable(value = "products-list")
    public List<Product> getAllProducts() {
        return productRepository.findByDeletedFalse();
    }
    @CacheEvict(value = "products-list", allEntries = true)
    public void deleteProduct(String productName, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (!userService.isAdmin(currentUser)) {
            throw new AccessForbiddenException("Only administrators can delete products");
        }

        Product product = productRepository.findByName(productName)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with name: " + productName));

        product.setDeleted(true);
        productRepository.save(product);

        log.info("Product soft deleted: {}", productName);
    }

    public Product getProductByName(String productName) {
        return productRepository.findByNameAndDeletedFalse(productName)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with name: " + productName));
    }

//    @Transactional
//    @CacheEvict(value = "products-list", allEntries = true)
//    public Product updateProduct(String productName, ProductUpdateRequest updateRequest, Authentication authentication) {
//        User currentUser = userService.getCurrentUser(authentication);
//        if (!userService.isAdmin(currentUser)) {
//            throw new AccessForbiddenException("Only administrators can update products");
//        }
//
//        Product product = productRepository.findByName(productName)
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found with name: " + productName));
//
//        if (updateRequest.getName() != null && !updateRequest.getName().equals(product.getName())) {
//            productRepository.findByName(updateRequest.getName())
//                    .ifPresent(existingProduct -> {
//                        if (!existingProduct.getId().equals(product.getId())) {
//                            throw new IllegalStateException("Product with name " + updateRequest.getName() + " already exists");
//                        }
//                    });
//            product.setName(updateRequest.getName());
//        }
//
//        if (updateRequest.getCategory() != null) {
//            product.setCategory(updateRequest.getCategory());
//        }
//
//        if (updateRequest.getPrice() != null) {
//            product.setPrice(updateRequest.getPrice());
//        }
//
//        if (updateRequest.getQuantity() != null) {
//            product.setQuantity(updateRequest.getQuantity());
//        }
//
//        return productRepository.save(product);
//    }

    @Transactional
    @CacheEvict(value = "products-list", allEntries = true)
    public Product updateProduct(String productName, ProductUpdateRequest updateRequest, Authentication authentication) {
        User currentUser = userService.getCurrentUser(authentication);
        if (!userService.isAdmin(currentUser)) {
            throw new AccessForbiddenException("Only administrators can update products");
        }

        Product product = productRepository.findByName(productName)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with name: " + productName));

        if (updateRequest.getName() != null && !updateRequest.getName().equals(product.getName())) {
            updateProductName(product, updateRequest.getName());
        }

        if (updateRequest.getCategory() != null) {
            product.setCategory(updateRequest.getCategory());
        }

        if (updateRequest.getPrice() != null) {
            product.setPrice(updateRequest.getPrice());
        }

        if (updateRequest.getQuantity() != null) {
            product.setQuantity(updateRequest.getQuantity());
        }

        return productRepository.save(product);
    }

    private void updateProductName(Product product, String newName) {
        productRepository.findByName(newName)
                .ifPresent(existingProduct -> {
                    validateProductNameUniqueness(existingProduct, product.getId(), newName);
                });
        product.setName(newName);
    }

    private void validateProductNameUniqueness(Product existingProduct, Long currentProductId, String newName) {
        if (!existingProduct.getId().equals(currentProductId)) {
            throw new IllegalStateException("Product with name " + newName + " already exists");
        }
    }
}