package ua.dymohlo.onlineStore.controler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.onlineStore.dto.request.ProductCreateRequest;
import ua.dymohlo.onlineStore.dto.request.ProductUpdateRequest;
import ua.dymohlo.onlineStore.entity.Product;
import ua.dymohlo.onlineStore.service.ProductService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Product> addProduct(@RequestBody ProductCreateRequest request,
                                              Authentication authentication) {
        Product createdProduct = productService.createProduct(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/{productName}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productName, Authentication authentication) {
        productService.deleteProduct(productName, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productName}")
    public ResponseEntity<Product> updateProduct(@PathVariable String productName,
                                                 @RequestBody ProductUpdateRequest request,
                                                 Authentication authentication) {
        Product updatedProduct = productService.updateProduct(productName, request, authentication);
        return ResponseEntity.ok(updatedProduct);
    }
}